package io.github.jacidprogrammer.verifactu.model;

import io.github.jacidprogrammer.verifactu.hash.HashInput;
import io.github.jacidprogrammer.verifactu.hash.Sha256;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * {@code RegistroAnulacion}: cancels a previously registered invoice. Immutable; the hash is computed on construction.
 */
public final class CancellationRecord implements BillingRecord {

    private final InvoiceId cancelledInvoiceId;
    private final ChainLink previous;
    private final OffsetDateTime generatedAt;
    private final String hashInput;
    private final String hash;

    /**
     * @param cancelledInvoiceId the invoice being cancelled
     * @param previous link to the previous record, or {@code null} for the first record of the system
     * @param generatedAt {@code FechaHoraHusoGenRegistro}
     */
    public CancellationRecord(InvoiceId cancelledInvoiceId, ChainLink previous, OffsetDateTime generatedAt) {
        this.cancelledInvoiceId = Objects.requireNonNull(cancelledInvoiceId, "cancelledInvoiceId");
        this.previous = previous;
        this.generatedAt = Objects.requireNonNull(generatedAt, "generatedAt").withNano(0);
        this.hashInput = HashInput.of()
                .field("IDEmisorFacturaAnulada", cancelledInvoiceId.issuerTaxId())
                .field("NumSerieFacturaAnulada", cancelledInvoiceId.seriesNumber())
                .field("FechaExpedicionFacturaAnulada", AeatFormat.date(cancelledInvoiceId.issueDate()))
                .field("Huella", previous == null ? null : previous.hash())
                .field("FechaHoraHusoGenRegistro", AeatFormat.dateTime(this.generatedAt))
                .toString();
        this.hash = Sha256.hexUpper(hashInput);
    }

    public InvoiceId cancelledInvoiceId() {
        return cancelledInvoiceId;
    }

    @Override
    public InvoiceId invoiceId() {
        return cancelledInvoiceId;
    }

    @Override
    public Optional<ChainLink> previous() {
        return Optional.ofNullable(previous);
    }

    @Override
    public OffsetDateTime generatedAt() {
        return generatedAt;
    }

    @Override
    public String hash() {
        return hash;
    }

    @Override
    public String hashInput() {
        return hashInput;
    }

    @Override
    public String toString() {
        return "CancellationRecord[" + cancelledInvoiceId + ", hash=" + hash + "]";
    }
}
