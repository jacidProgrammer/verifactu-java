package io.github.jacidprogrammer.verifactu.model;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * A record in the issuer's chain: either a registration ({@code RegistroAlta}) or a cancellation
 * ({@code RegistroAnulacion}). Every record carries a link to its predecessor and its own hash.
 */
public sealed interface BillingRecord permits RegistrationRecord, CancellationRecord {

    /** The invoice this record is about: the registered one, or the cancelled one. */
    InvoiceId invoiceId();

    /** Previous record in the chain; empty for the first record ({@code PrimerRegistro = S}). */
    Optional<ChainLink> previous();

    /** Hash of the previous record in the chain; empty for the first record. */
    default Optional<String> previousHash() {
        return previous().map(ChainLink::hash);
    }

    /** {@code FechaHoraHusoGenRegistro}: when this record was generated, with the issuer's UTC offset. */
    OffsetDateTime generatedAt();

    /** {@code Huella}: SHA-256 of this record, 64 upper-case hex characters. */
    String hash();

    /** The exact string the hash was computed over; useful for audits and for reproducing AEAT test vectors. */
    String hashInput();

    default boolean isFirst() {
        return previous().isEmpty();
    }
}
