package io.github.jacidprogrammer.verifactu.spring;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jacidprogrammer.verifactu.AeatEnvironment;
import io.github.jacidprogrammer.verifactu.chain.RecordChain;
import io.github.jacidprogrammer.verifactu.client.AeatClient;
import io.github.jacidprogrammer.verifactu.model.ChainLink;
import io.github.jacidprogrammer.verifactu.model.InvoiceId;
import io.github.jacidprogrammer.verifactu.model.InvoiceType;
import io.github.jacidprogrammer.verifactu.model.RegistrationRecord;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** TASK criterion 7: nothing without {@code verifactu.enabled=true}, chain and client with it. */
class VerifactuAutoConfigurationTest {

    static final String HASH = "3C464DAF61ACB827C65FDA19F352A4E3BDC2C640E9E9FC4CC058073F38F12F60";

    @TempDir
    Path dir;
    Path keyStore;

    final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(VerifactuAutoConfiguration.class));

    @BeforeEach
    void emptyPkcs12() throws Exception {
        // An empty PKCS12 is enough for KeyManagerFactory; no private key is committed to the repository.
        keyStore = dir.resolve("test.p12");
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        try (OutputStream out = Files.newOutputStream(keyStore)) {
            ks.store(out, "secret".toCharArray());
        }
    }

    String[] enabledWithKeyStore() {
        return new String[] {"verifactu.enabled=true", "verifactu.key-store.path=" + keyStore,
                "verifactu.key-store.password=secret"};
    }

    @Test
    void withoutEnabledNothingIsCreated() {
        runner.withPropertyValues("verifactu.key-store.path=" + keyStore).run(ctx -> {
            assertThat(ctx).doesNotHaveBean(RecordChain.class);
            assertThat(ctx).doesNotHaveBean(AeatClient.class);
            assertThat(ctx).doesNotHaveBean(VerifactuProperties.class);
        });
    }

    @Test
    void enabledFalseIsTheSameAsAbsent() {
        runner.withPropertyValues("verifactu.enabled=false", "verifactu.key-store.path=" + keyStore).run(ctx -> {
            assertThat(ctx).doesNotHaveBean(RecordChain.class);
            assertThat(ctx).doesNotHaveBean(AeatClient.class);
        });
    }

    @Test
    void enabledCreatesAnEmptyChainAndATestingClient() {
        runner.withPropertyValues(enabledWithKeyStore()).run(ctx -> {
            assertThat(ctx).hasSingleBean(RecordChain.class);
            assertThat(ctx).hasSingleBean(AeatClient.class);
            assertThat(ctx.getBean(RecordChain.class).isEmpty()).isTrue();
            assertThat(ctx.getBean(AeatClient.class).endpoint()).isEqualTo(AeatEnvironment.TESTING.verifactuEndpoint());
        });
    }

    @Test
    void productionWithSealUsesTheSealEndpoint() {
        runner.withPropertyValues(enabledWithKeyStore())
                .withPropertyValues("verifactu.environment=production", "verifactu.certificate-type=seal")
                .run(ctx -> assertThat(ctx.getBean(AeatClient.class).endpoint())
                        .isEqualTo(AeatEnvironment.PRODUCTION.verifactuSealEndpoint()));
    }

    @Test
    void chainHeadPropertiesResumeTheChain() {
        runner.withPropertyValues(enabledWithKeyStore())
                .withPropertyValues("verifactu.chain.head.issuer-tax-id=89890001K",
                        "verifactu.chain.head.series-number=12345678/G33", "verifactu.chain.head.issue-date=2024-01-01",
                        "verifactu.chain.head.hash=" + HASH, "verifactu.chain.head.generated-at=2024-01-01T19:20:30+01:00")
                .run(ctx -> {
                    RecordChain chain = ctx.getBean(RecordChain.class);
                    assertThat(chain.head()).contains(
                            new ChainLink(new InvoiceId("89890001K", "12345678/G33", LocalDate.of(2024, 1, 1)), HASH));
                    RegistrationRecord next = chain.register(new InvoiceId("89890001K", "12345679/G34",
                            LocalDate.of(2024, 1, 1)), InvoiceType.F1, new BigDecimal("12.35"), new BigDecimal("123.45"));
                    assertThat(next.previous()).map(ChainLink::hash).contains(HASH);
                });
    }

    @Test
    void partialChainHeadFailsInsteadOfStartingANewChain() {
        runner.withPropertyValues(enabledWithKeyStore())
                .withPropertyValues("verifactu.chain.head.hash=" + HASH)
                .run(ctx -> {
                    assertThat(ctx).hasFailed();
                    assertThat(ctx.getStartupFailure()).rootCause().hasMessageContaining("verifactu.chain.head");
                });
    }

    @Test
    void enabledWithoutKeyStoreFailsWithTheMissingProperty() {
        runner.withPropertyValues("verifactu.enabled=true").run(ctx -> {
            assertThat(ctx).hasFailed();
            assertThat(ctx.getStartupFailure()).rootCause().hasMessageContaining("verifactu.key-store.path");
        });
    }

    @Test
    void wrongKeyStorePasswordFails() {
        runner.withPropertyValues("verifactu.enabled=true", "verifactu.key-store.path=" + keyStore,
                "verifactu.key-store.password=wrong").run(ctx -> {
                    assertThat(ctx).hasFailed();
                    assertThat(ctx.getStartupFailure()).hasStackTraceContaining("verifactu.key-store")
                            .rootCause().hasMessageContaining("PKCS12 integrity");
                });
    }

    @Test
    void applicationClockStampsTheRecords() {
        runner.withUserConfiguration(FixedClock.class).withPropertyValues(enabledWithKeyStore()).run(ctx -> {
            RegistrationRecord record = ctx.getBean(RecordChain.class).register(
                    new InvoiceId("89890001K", "1", LocalDate.of(2024, 1, 1)), InvoiceType.F1, BigDecimal.ONE, BigDecimal.TEN);
            assertThat(record.generatedAt()).isEqualTo(OffsetDateTime.parse("2024-01-01T19:20:30+01:00"));
        });
    }

    @Test
    void applicationBeansWin() {
        runner.withUserConfiguration(OwnChain.class).withPropertyValues(enabledWithKeyStore()).run(ctx -> {
            assertThat(ctx).hasSingleBean(RecordChain.class);
            assertThat(ctx.getBean(RecordChain.class).headHash()).contains(HASH);
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class FixedClock {
        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2024-01-01T18:20:30Z"), ZoneOffset.ofHours(1));
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class OwnChain {
        @Bean
        RecordChain recordChain() {
            return RecordChain.resume(new ChainLink(new InvoiceId("89890001K", "12345678/G33", LocalDate.of(2024, 1, 1)), HASH),
                    OffsetDateTime.parse("2024-01-01T19:20:30+01:00"), Clock.systemDefaultZone());
        }
    }
}
