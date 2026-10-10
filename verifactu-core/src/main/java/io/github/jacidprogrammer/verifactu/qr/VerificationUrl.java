package io.github.jacidprogrammer.verifactu.qr;

import io.github.jacidprogrammer.verifactu.AeatEnvironment;
import io.github.jacidprogrammer.verifactu.model.AeatFormat;
import io.github.jacidprogrammer.verifactu.model.InvoiceId;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * The URL printed in the invoice QR code, which lets the recipient check the invoice at the AEAT
 * (QR spec v0.5.0, sections 4 to 6). Parameters are {@code nif}, {@code numserie}, {@code fecha} (dd-MM-yyyy)
 * and {@code importe}, in that order, URL-encoded as UTF-8 with {@link URLEncoder}.
 *
 * <p>Example from the spec, testing environment, series {@code 12345678&G33}:
 * {@code https://prewww2.aeat.es/wlpl/TIKE-CONT/ValidarQR?nif=89890001K&numserie=12345678%26G33&fecha=01-01-2024&importe=241.4}
 */
public final class VerificationUrl {

    private final URI uri;

    private VerificationUrl(URI uri) {
        this.uri = uri;
    }

    /** URL for a system that issues verifiable invoices (Verifactu mode): {@code ValidarQR}. */
    public static VerificationUrl verifiable(AeatEnvironment environment, InvoiceId invoiceId, BigDecimal totalAmount) {
        return build(environment, "ValidarQR", invoiceId, totalAmount);
    }

    /** URL for a system that does not send records to the AEAT (non-Verifactu mode): {@code ValidarQRNoVerifactu}. */
    public static VerificationUrl nonVerifiable(AeatEnvironment environment, InvoiceId invoiceId, BigDecimal totalAmount) {
        return build(environment, "ValidarQRNoVerifactu", invoiceId, totalAmount);
    }

    private static VerificationUrl build(AeatEnvironment environment, String service, InvoiceId invoiceId, BigDecimal totalAmount) {
        Objects.requireNonNull(environment, "environment");
        Objects.requireNonNull(invoiceId, "invoiceId");
        Objects.requireNonNull(totalAmount, "totalAmount");
        String url = environment.qrBaseUrl() + service
                + "?nif=" + encode(invoiceId.issuerTaxId())
                + "&numserie=" + encode(invoiceId.seriesNumber())
                + "&fecha=" + encode(AeatFormat.date(invoiceId.issueDate()))
                + "&importe=" + encode(AeatFormat.qrAmount(totalAmount));
        return new VerificationUrl(URI.create(url));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public URI toUri() {
        return uri;
    }

    @Override
    public String toString() {
        return uri.toString();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof VerificationUrl other && uri.equals(other.uri);
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }
}
