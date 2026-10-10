package io.github.jacidprogrammer.verifactu.model;

import io.github.jacidprogrammer.verifactu.hash.HashInput;
import io.github.jacidprogrammer.verifactu.hash.Sha256;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * {@code RegistroAlta}: an invoice registration. Immutable; the hash is computed on construction.
 */
public final class RegistrationRecord implements BillingRecord {

    private final InvoiceId invoiceId;
    private final InvoiceType invoiceType;
    private final BigDecimal totalTax;
    private final BigDecimal totalAmount;
    private final ChainLink previous;
    private final OffsetDateTime generatedAt;
    private final String hashInput;
    private final String hash;

    /**
     * @param invoiceId invoice identification
     * @param invoiceType {@code TipoFactura}
     * @param totalTax {@code CuotaTotal}: total tax amount, at most two decimals
     * @param totalAmount {@code ImporteTotal}: total invoice amount, at most two decimals
     * @param previous link to the previous record, or {@code null} for the first record of the system
     * @param generatedAt {@code FechaHoraHusoGenRegistro}
     */
    public RegistrationRecord(InvoiceId invoiceId, InvoiceType invoiceType, BigDecimal totalTax, BigDecimal totalAmount,
            ChainLink previous, OffsetDateTime generatedAt) {
        this.invoiceId = Objects.requireNonNull(invoiceId, "invoiceId");
        this.invoiceType = Objects.requireNonNull(invoiceType, "invoiceType");
        this.totalTax = Values.amount("totalTax", totalTax);
        this.totalAmount = Values.amount("totalAmount", totalAmount);
        this.previous = previous;
        this.generatedAt = Objects.requireNonNull(generatedAt, "generatedAt").withNano(0);
        this.hashInput = HashInput.of()
                .field("IDEmisorFactura", invoiceId.issuerTaxId())
                .field("NumSerieFactura", invoiceId.seriesNumber())
                .field("FechaExpedicionFactura", AeatFormat.date(invoiceId.issueDate()))
                .field("TipoFactura", invoiceType.name())
                .field("CuotaTotal", AeatFormat.amount(this.totalTax))
                .field("ImporteTotal", AeatFormat.amount(this.totalAmount))
                .field("Huella", previous == null ? null : previous.hash())
                .field("FechaHoraHusoGenRegistro", AeatFormat.dateTime(this.generatedAt))
                .toString();
        this.hash = Sha256.hexUpper(hashInput);
    }

    public InvoiceId invoiceId() {
        return invoiceId;
    }

    public InvoiceType invoiceType() {
        return invoiceType;
    }

    public BigDecimal totalTax() {
        return totalTax;
    }

    public BigDecimal totalAmount() {
        return totalAmount;
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
        return "RegistrationRecord[" + invoiceId + ", " + invoiceType + ", hash=" + hash + "]";
    }
}
