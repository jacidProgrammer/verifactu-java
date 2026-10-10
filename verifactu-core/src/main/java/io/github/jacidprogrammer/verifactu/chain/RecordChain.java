package io.github.jacidprogrammer.verifactu.chain;

import io.github.jacidprogrammer.verifactu.model.BillingRecord;
import io.github.jacidprogrammer.verifactu.model.ChainLink;
import io.github.jacidprogrammer.verifactu.model.CancellationRecord;
import io.github.jacidprogrammer.verifactu.model.InvoiceId;
import io.github.jacidprogrammer.verifactu.model.InvoiceType;
import io.github.jacidprogrammer.verifactu.model.RegistrationRecord;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * The issuer's chain of billing records for one invoicing system. Each new record takes the hash of the head as
 * {@code Huella} and becomes the new head. The chain is not thread-safe: an invoicing system is one sequence, so
 * callers serialize access (typically a database row lock on the head).
 *
 * <p>State is kept to a minimum on purpose: the head (invoice id and hash) and its timestamp. Restore a chain from
 * storage with {@link #resume(ChainLink, OffsetDateTime, Clock)}.
 */
public final class RecordChain {

    private final Clock clock;
    private ChainLink head;
    private OffsetDateTime headGeneratedAt;

    private RecordChain(ChainLink head, OffsetDateTime headGeneratedAt, Clock clock) {
        this.head = head;
        this.headGeneratedAt = headGeneratedAt;
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /** A chain with no records yet; the next record will be {@code PrimerRegistro = S}. */
    public static RecordChain start(Clock clock) {
        return new RecordChain(null, null, clock);
    }

    /** Continues an existing chain from its last stored record. */
    public static RecordChain resume(ChainLink head, OffsetDateTime headGeneratedAt, Clock clock) {
        Objects.requireNonNull(head, "head");
        Objects.requireNonNull(headGeneratedAt, "headGeneratedAt");
        return new RecordChain(head, headGeneratedAt, clock);
    }

    /** The last record appended, as the next record will reference it. */
    public Optional<ChainLink> head() {
        return Optional.ofNullable(head);
    }

    public Optional<String> headHash() {
        return head().map(ChainLink::hash);
    }

    public boolean isEmpty() {
        return head == null;
    }

    /** Registers an invoice, stamped with the clock's current time. */
    public RegistrationRecord register(InvoiceId invoiceId, InvoiceType type, BigDecimal totalTax, BigDecimal totalAmount) {
        return append(new RegistrationRecord(invoiceId, type, totalTax, totalAmount, head, now()));
    }

    /** Cancels an invoice, stamped with the clock's current time. */
    public CancellationRecord cancel(InvoiceId cancelledInvoiceId) {
        return append(new CancellationRecord(cancelledInvoiceId, head, now()));
    }

    /**
     * Appends a record built elsewhere. It must point to the current head and not be older than it;
     * otherwise the chain would be broken for the AEAT and the record is rejected.
     *
     * @throws ChainOrderException when the record does not continue this chain
     */
    public <R extends BillingRecord> R append(R record) {
        Objects.requireNonNull(record, "record");
        ChainLink expected = head;
        ChainLink actual = record.previous().orElse(null);
        if (!Objects.equals(expected, actual)) {
            throw new ChainOrderException("record points to previous " + actual + " but the chain head is " + expected);
        }
        if (headGeneratedAt != null && record.generatedAt().isBefore(headGeneratedAt)) {
            throw new ChainOrderException("record generated at " + record.generatedAt()
                    + " is older than the chain head generated at " + headGeneratedAt);
        }
        head = ChainLink.of(record);
        headGeneratedAt = record.generatedAt();
        return record;
    }

    private OffsetDateTime now() {
        OffsetDateTime now = OffsetDateTime.now(clock).withNano(0);
        if (headGeneratedAt != null && now.isBefore(headGeneratedAt)) {
            throw new ChainOrderException("clock went backwards: now " + now + " is before head " + headGeneratedAt);
        }
        return now;
    }
}
