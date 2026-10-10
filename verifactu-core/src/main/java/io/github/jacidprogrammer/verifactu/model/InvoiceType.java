package io.github.jacidprogrammer.verifactu.model;

/**
 * Invoice type codes ({@code TipoFactura}) from the AEAT schema {@code SuministroInformacion.xsd} v1.0.
 */
public enum InvoiceType {
    /** Standard invoice (art. 6, 7.2 and 7.3 of RD 1619/2012). */
    F1,
    /** Simplified invoice or receipt (art. 6.1.d, 7.2 and 7.3). */
    F2,
    /** Invoice replacing simplified invoices that were already issued and declared. */
    F3,
    /** Corrective invoice: art. 80.1, 80.2 and error based on law. */
    R1,
    /** Corrective invoice: art. 80.3. */
    R2,
    /** Corrective invoice: art. 80.4. */
    R3,
    /** Corrective invoice: other cases. */
    R4,
    /** Corrective invoice of simplified invoices. */
    R5
}
