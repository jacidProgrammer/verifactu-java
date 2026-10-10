package io.github.jacidprogrammer.verifactu.model;

/** {@code OperacionExenta} ({@code SuministroInformacion.xsd} v1.0, {@code OperacionExentaType}). */
public enum ExemptionCause {
    /** Exempt under article 20 of the VAT law. */
    E1,
    /** Article 21. */
    E2,
    /** Article 22. */
    E3,
    /** Articles 23 and 24. */
    E4,
    /** Article 25. */
    E5,
    /** Exempt for other reasons. */
    E6,
    /** Exemption code E7 of the AEAT list. */
    E7,
    /** Exemption code E8 of the AEAT list. */
    E8
}
