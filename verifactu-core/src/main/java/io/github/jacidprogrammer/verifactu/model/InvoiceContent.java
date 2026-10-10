package io.github.jacidprogrammer.verifactu.model;

import java.util.List;
import java.util.Objects;

/**
 * The parts of a {@code RegistroAlta} that the XML requires but the hash does not cover
 * ({@code SuministroInformacion.xsd} v1.0): the operation description, the tax breakdown and the recipients.
 *
 * @param description {@code DescripcionOperacion}, 1 to 500 characters
 * @param breakdown {@code Desglose}: 1 to 12 lines
 * @param recipients {@code Destinatarios}: up to 1000; empty for simplified invoices (F2) without recipient
 */
public record InvoiceContent(String description, List<TaxLine> breakdown, List<Party> recipients) {

    public InvoiceContent {
        description = Values.trimmed("description", description);
        Values.maxLength("description", description, 500);
        breakdown = List.copyOf(Objects.requireNonNull(breakdown, "breakdown"));
        recipients = List.copyOf(Objects.requireNonNull(recipients, "recipients"));
        if (breakdown.isEmpty() || breakdown.size() > 12) {
            throw new IllegalArgumentException("breakdown must have 1 to 12 lines, got " + breakdown.size());
        }
        if (recipients.size() > 1000) {
            throw new IllegalArgumentException("at most 1000 recipients, got " + recipients.size());
        }
    }

    public static InvoiceContent of(String description, Party recipient, TaxLine... breakdown) {
        return new InvoiceContent(description, List.of(breakdown), List.of(recipient));
    }

    /** Content without recipient, as in a simplified invoice. */
    public static InvoiceContent withoutRecipient(String description, TaxLine... breakdown) {
        return new InvoiceContent(description, List.of(breakdown), List.of());
    }
}
