package io.github.jacidprogrammer.verifactu.spring;

import io.github.jacidprogrammer.verifactu.chain.RecordChain;
import io.github.jacidprogrammer.verifactu.client.AeatClient;
import io.github.jacidprogrammer.verifactu.spring.VerifactuProperties.HeadProperties;
import io.github.jacidprogrammer.verifactu.spring.VerifactuProperties.KeyStoreProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.time.Clock;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Creates one {@link RecordChain} and one {@link AeatClient} from {@code verifactu.*} when
 * {@code verifactu.enabled=true}. Without that property no bean is registered. A {@link Clock} bean, if the
 * application defines one, stamps the records; otherwise the system clock does.
 *
 * <p>The chain bean is a singleton and {@code RecordChain} is not thread-safe: the application must serialize the
 * calls that append records, typically with the lock of the store that persists them.
 */
@AutoConfiguration
@ConditionalOnProperty(name = "verifactu.enabled", havingValue = "true")
@EnableConfigurationProperties(VerifactuProperties.class)
public class VerifactuAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RecordChain verifactuRecordChain(VerifactuProperties properties, ObjectProvider<Clock> clock) {
        Clock effective = clock.getIfAvailable(Clock::systemDefaultZone);
        HeadProperties head = properties.chain() == null ? null : properties.chain().head();
        if (head == null || head.isEmpty()) {
            return RecordChain.start(effective);
        }
        if (!head.isComplete()) {
            throw new IllegalStateException("verifactu.chain.head needs issuer-tax-id, series-number, issue-date, hash"
                    + " and generated-at, all of them or none");
        }
        return RecordChain.resume(head.toLink(), head.generatedAt(), effective);
    }

    @Bean
    @ConditionalOnMissingBean
    public AeatClient verifactuAeatClient(VerifactuProperties properties) {
        KeyStoreProperties ks = properties.keyStore();
        if (ks == null || ks.path() == null || ks.path().isBlank()) {
            throw new IllegalStateException("verifactu.key-store.path is required when verifactu.enabled=true");
        }
        KeyStore keyStore = loadPkcs12(Path.of(ks.path()), ks.password());
        return switch (properties.certificateType()) {
            case CERTIFICATE -> AeatClient.withCertificate(properties.environment(), keyStore, ks.keyPasswordOrDefault());
            case SEAL -> AeatClient.withSeal(properties.environment(), keyStore, ks.keyPasswordOrDefault());
        };
    }

    static KeyStore loadPkcs12(Path path, String password) {
        try (InputStream in = Files.newInputStream(path)) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(in, password == null ? null : password.toCharArray());
            return keyStore;
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException("cannot load PKCS12 key store " + path + " (verifactu.key-store.*)", e);
        }
    }
}
