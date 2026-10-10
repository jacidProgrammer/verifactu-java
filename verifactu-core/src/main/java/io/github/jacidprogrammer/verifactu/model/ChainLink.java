package io.github.jacidprogrammer.verifactu.model;

import java.util.Objects;

/**
 * What a record keeps about its predecessor: the invoice it referred to and its hash. The hash feeds the
 * {@code Huella} field of the hash input; the invoice id fills {@code RegistroAnterior} in the XML
 * ({@code SuministroInformacion.xsd} v1.0, {@code EncadenamientoFacturaAnteriorType}).
 *
 * @param invoiceId invoice of the previous record (for a cancellation, the invoice it cancelled)
 * @param hash {@code Huella} of the previous record, 64 upper-case hex characters
 */
public record ChainLink(InvoiceId invoiceId, String hash) {

    public ChainLink {
        Objects.requireNonNull(invoiceId, "invoiceId");
        hash = Hashes.required(hash);
    }

    public static ChainLink of(BillingRecord record) {
        return new ChainLink(record.invoiceId(), record.hash());
    }
}
