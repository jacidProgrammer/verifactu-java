package io.github.jacidprogrammer.verifactu.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Identifies an invoice the way the AEAT does: issuer tax id, series plus number, and issue date.
 *
 * @param issuerTaxId {@code IDEmisorFactura}: Spanish NIF of the issuer, 9 characters
 * @param seriesNumber {@code NumSerieFactura}: series and number concatenated, 1 to 60 printable ASCII characters
 * @param issueDate {@code FechaExpedicionFactura}
 */
public record InvoiceId(String issuerTaxId, String seriesNumber, LocalDate issueDate) {

    public InvoiceId {
        issuerTaxId = Values.trimmed("issuerTaxId", issuerTaxId);
        seriesNumber = Values.trimmed("seriesNumber", seriesNumber);
        Objects.requireNonNull(issueDate, "issueDate");
        if (issuerTaxId.length() != 9) {
            throw new IllegalArgumentException("issuerTaxId must have 9 characters, got '" + issuerTaxId + "'");
        }
        if (seriesNumber.length() > 60) {
            throw new IllegalArgumentException("seriesNumber must have at most 60 characters");
        }
        Values.requirePrintableAscii("seriesNumber", seriesNumber);
    }
}
