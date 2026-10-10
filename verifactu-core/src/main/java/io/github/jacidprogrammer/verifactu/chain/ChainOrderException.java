package io.github.jacidprogrammer.verifactu.chain;

/** Thrown when a record would break the hash chain: wrong predecessor or timestamp going backwards. */
public class ChainOrderException extends IllegalStateException {

    public ChainOrderException(String message) {
        super(message);
    }
}
