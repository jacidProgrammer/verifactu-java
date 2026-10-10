package io.github.jacidprogrammer.verifactu.xml;

/** A document does not conform to the AEAT XSD v1.0 schemas bundled with the library. */
public class SchemaViolationException extends RuntimeException {

    public SchemaViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
