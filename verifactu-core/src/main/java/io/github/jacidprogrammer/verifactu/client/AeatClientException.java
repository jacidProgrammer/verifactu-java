package io.github.jacidprogrammer.verifactu.client;

/** Transport or protocol failure while talking to the AEAT: not a business rejection (see {@link AeatResponse}). */
public class AeatClientException extends RuntimeException {

    public AeatClientException(String message) {
        super(message);
    }

    public AeatClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
