package io.github.jacidprogrammer.verifactu.model;

/** {@code CalificacionOperacion} ({@code SuministroInformacion.xsd} v1.0, {@code CalificacionOperacionType}). */
public enum OperationQualification {
    /** Subject and not exempt, without reverse charge. */
    S1,
    /** Subject and not exempt, with reverse charge. */
    S2,
    /** Not subject: articles 7, 14 and others. */
    N1,
    /** Not subject because of location rules. */
    N2;

    public boolean isSubject() {
        return this == S1 || this == S2;
    }
}
