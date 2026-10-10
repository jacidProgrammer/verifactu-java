package io.github.jacidprogrammer.verifactu.client;

/**
 * The AEAT answered with a SOAP {@code Fault}: the whole message was rejected before business validation
 * (SWeb description v1.0.3, section 3.1), typically a schema error or an unidentified NIF in the header.
 */
public class AeatFaultException extends AeatClientException {

    private final String faultCode;
    private final String faultString;

    public AeatFaultException(String faultCode, String faultString) {
        super("SOAP fault " + faultCode + ": " + faultString);
        this.faultCode = faultCode;
        this.faultString = faultString;
    }

    /** {@code env:Client} (fix the message) or {@code env:Server} (resend later). */
    public String faultCode() {
        return faultCode;
    }

    public String faultString() {
        return faultString;
    }

    /** True for {@code Server} faults, which the AEAT documents as "resend the message". */
    public boolean isServerFault() {
        return faultCode != null && faultCode.endsWith("Server");
    }
}
