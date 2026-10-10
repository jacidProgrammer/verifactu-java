package io.github.jacidprogrammer.verifactu.spring;

import io.github.jacidprogrammer.verifactu.AeatEnvironment;
import io.github.jacidprogrammer.verifactu.model.ChainLink;
import io.github.jacidprogrammer.verifactu.model.InvoiceId;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

/**
 * {@code verifactu.*} properties. Nothing is created unless {@code verifactu.enabled=true}; once enabled, the key
 * store is mandatory because the {@code AeatClient} cannot exist without a certificate.
 *
 * @param enabled {@code verifactu.enabled}: switches the whole auto-configuration on
 * @param environment {@code verifactu.environment}: {@code testing} (default) or {@code production}
 * @param certificateType {@code verifactu.certificate-type}: {@code certificate} (default, taxpayer or
 *        representative) or {@code seal} (electronic seal, WSDL port {@code SistemaVerifactuSello})
 * @param keyStore PKCS12 key store with the certificate and its private key
 * @param chain where the chain was left; absent for a system that has never issued a record
 */
@ConfigurationProperties("verifactu")
public record VerifactuProperties(boolean enabled, @DefaultValue("testing") AeatEnvironment environment,
        @DefaultValue("certificate") CertificateType certificateType, KeyStoreProperties keyStore,
        ChainProperties chain) {

    public enum CertificateType {
        CERTIFICATE, SEAL
    }

    /**
     * @param path {@code verifactu.key-store.path}: file system path of the PKCS12 file
     * @param password {@code verifactu.key-store.password}: password of the file
     * @param keyPassword {@code verifactu.key-store.key-password}: password of the private key; defaults to
     *        {@code password}
     */
    public record KeyStoreProperties(String path, String password, String keyPassword) {

        public char[] keyPasswordOrDefault() {
            String effective = keyPassword != null ? keyPassword : password;
            return effective == null ? new char[0] : effective.toCharArray();
        }
    }

    /**
     * Last record of the chain, so the application resumes it instead of starting a new one. Either all five
     * values are present or none is.
     *
     * @param head the record the next one will point to
     */
    public record ChainProperties(HeadProperties head) {
    }

    /**
     * @param issuerTaxId {@code verifactu.chain.head.issuer-tax-id}: {@code IDEmisorFactura} of the last record
     * @param seriesNumber {@code verifactu.chain.head.series-number}: {@code NumSerieFactura} of the last record
     * @param issueDate {@code verifactu.chain.head.issue-date}: {@code FechaExpedicionFactura}, ISO date
     * @param hash {@code verifactu.chain.head.hash}: {@code Huella} of the last record, 64 upper-case hex characters
     * @param generatedAt {@code verifactu.chain.head.generated-at}: {@code FechaHoraHusoGenRegistro}, ISO date-time
     *        with offset
     */
    public record HeadProperties(String issuerTaxId, String seriesNumber, @DateTimeFormat(iso = ISO.DATE) LocalDate issueDate,
            String hash, @DateTimeFormat(iso = ISO.DATE_TIME) OffsetDateTime generatedAt) {

        boolean isEmpty() {
            return issuerTaxId == null && seriesNumber == null && issueDate == null && hash == null && generatedAt == null;
        }

        boolean isComplete() {
            return issuerTaxId != null && seriesNumber != null && issueDate != null && hash != null && generatedAt != null;
        }

        ChainLink toLink() {
            return new ChainLink(new InvoiceId(issuerTaxId, seriesNumber, issueDate), hash);
        }
    }
}
