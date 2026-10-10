package io.github.jacidprogrammer.verifactu.xml;

import io.github.jacidprogrammer.verifactu.model.BillingRecord;
import io.github.jacidprogrammer.verifactu.model.CancellationRecord;
import io.github.jacidprogrammer.verifactu.model.InvoiceContent;
import io.github.jacidprogrammer.verifactu.model.InvoicingSystem;
import io.github.jacidprogrammer.verifactu.model.Party;
import io.github.jacidprogrammer.verifactu.model.RegistrationRecord;
import java.util.List;
import java.util.Objects;

/**
 * One {@code RegFactuSistemaFacturacion} message: the issuer ({@code ObligadoEmision}), the invoicing system and
 * 1 to 1000 records ({@code SuministroLR.xsd} v1.0, {@code RegistroFactura maxOccurs=1000}).
 *
 * @param issuer the taxpayer that issues the invoices; every record must carry its NIF as {@code IDEmisorFactura}
 * @param system the software generating the records
 * @param items the records, in chain order
 */
public record Submission(Party issuer, InvoicingSystem system, List<Item> items) {

    /** Maximum records per message (SWeb description v1.0.3, section 3; XSD {@code maxOccurs}). */
    public static final int MAX_ITEMS = 1000;

    public Submission {
        Objects.requireNonNull(issuer, "issuer");
        Objects.requireNonNull(system, "system");
        items = List.copyOf(Objects.requireNonNull(items, "items"));
        if (items.isEmpty() || items.size() > MAX_ITEMS) {
            throw new IllegalArgumentException("a submission carries 1 to " + MAX_ITEMS + " records, got " + items.size());
        }
        for (Item item : items) {
            String recordIssuer = item.record().invoiceId().issuerTaxId();
            if (!recordIssuer.equals(issuer.taxId())) {
                throw new IllegalArgumentException("record " + item.record().invoiceId() + " is issued by " + recordIssuer
                        + " but the submission issuer is " + issuer.taxId());
            }
        }
    }

    public static Item registration(RegistrationRecord record, InvoiceContent content) {
        return new Registration(record, content);
    }

    public static Item cancellation(CancellationRecord record) {
        return new Cancellation(record);
    }

    /** A {@code RegistroFactura} entry: either a registration or a cancellation. */
    public sealed interface Item permits Registration, Cancellation {
        BillingRecord record();
    }

    public record Registration(RegistrationRecord record, InvoiceContent content) implements Item {
        public Registration {
            Objects.requireNonNull(record, "record");
            Objects.requireNonNull(content, "content");
        }
    }

    public record Cancellation(CancellationRecord record) implements Item {
        public Cancellation {
            Objects.requireNonNull(record, "record");
        }
    }
}
