package io.github.jacidprogrammer.verifactu.model;

import java.math.BigDecimal;
import java.util.Objects;

/** Validation rules shared by the record model. */
final class Values {

    private Values() {
    }

    /**
     * The AEAT strips leading and trailing blanks from every value before storing it and before hashing it
     * (hash spec v0.1.2, section 3), so the model never keeps them.
     */
    static String trimmed(String name, String value) {
        Objects.requireNonNull(value, name);
        String trimmed = value.strip();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return trimmed;
    }

    /** QR spec v0.5.0, section 4: text values may only contain printable ASCII (codes 32 to 126). */
    static void requirePrintableAscii(String name, String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < 32 || c > 126) {
                throw new IllegalArgumentException(
                        name + " may only contain printable ASCII, found U+" + Integer.toHexString(c) + " at " + i);
            }
        }
    }

    /** Amounts carry at most two decimals; the AEAT formats them with a dot and two decimals. */
    static BigDecimal amount(String name, BigDecimal value) {
        Objects.requireNonNull(value, name);
        if (value.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(name + " must have at most 2 decimals, got " + value.toPlainString());
        }
        if (value.precision() - value.scale() > 12) {
            throw new IllegalArgumentException(name + " must have at most 12 integer digits");
        }
        return value.setScale(2);
    }

    static void maxLength(String name, String value, int max) {
        if (value.length() > max) {
            throw new IllegalArgumentException(name + " must have at most " + max + " characters, got " + value.length());
        }
    }
}
