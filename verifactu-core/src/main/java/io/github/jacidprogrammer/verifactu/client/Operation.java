package io.github.jacidprogrammer.verifactu.client;

/** {@code TipoOperacion} echoed in each response line. */
public enum Operation {
    REGISTRATION("Alta"),
    CANCELLATION("Anulacion");

    private final String aeatValue;

    Operation(String aeatValue) {
        this.aeatValue = aeatValue;
    }

    public String aeatValue() {
        return aeatValue;
    }

    static Operation fromAeat(String value) {
        for (Operation o : values()) {
            if (o.aeatValue.equals(value)) {
                return o;
            }
        }
        throw new AeatClientException("unknown TipoOperacion '" + value + "'");
    }
}
