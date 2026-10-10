package io.github.jacidprogrammer.verifactu.client;

/** {@code EstadoEnvio}: global result of a submission (SWeb description v1.0.3, list L18). */
public enum SubmissionStatus {
    /** Every record was accepted. */
    ACCEPTED("Correcto"),
    /** Some records were rejected or accepted with errors. */
    PARTIALLY_ACCEPTED("ParcialmenteCorrecto"),
    /** Every record was rejected; no CSV is issued. */
    REJECTED("Incorrecto");

    private final String aeatValue;

    SubmissionStatus(String aeatValue) {
        this.aeatValue = aeatValue;
    }

    public String aeatValue() {
        return aeatValue;
    }

    static SubmissionStatus fromAeat(String value) {
        for (SubmissionStatus s : values()) {
            if (s.aeatValue.equals(value)) {
                return s;
            }
        }
        throw new AeatClientException("unknown EstadoEnvio '" + value + "'");
    }
}
