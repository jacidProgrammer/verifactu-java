package io.github.jacidprogrammer.verifactu.client;

/** {@code EstadoRegistro}: result of one record inside a submission (SWeb description v1.0.3, list L19). */
public enum RecordStatus {
    /** Registered. */
    ACCEPTED("Correcto"),
    /** Registered although it has admissible errors. */
    ACCEPTED_WITH_ERRORS("AceptadoConErrores"),
    /** Not registered; fix and resend. */
    REJECTED("Incorrecto");

    private final String aeatValue;

    RecordStatus(String aeatValue) {
        this.aeatValue = aeatValue;
    }

    public String aeatValue() {
        return aeatValue;
    }

    static RecordStatus fromAeat(String value) {
        for (RecordStatus s : values()) {
            if (s.aeatValue.equals(value)) {
                return s;
            }
        }
        throw new AeatClientException("unknown EstadoRegistro '" + value + "'");
    }
}
