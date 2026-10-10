package io.github.jacidprogrammer.verifactu.model;

import java.util.regex.Pattern;

/** Validation of hash values received from outside (previous records loaded from a database, for instance). */
final class Hashes {

    private static final Pattern HEX64_UPPER = Pattern.compile("[0-9A-F]{64}");

    private Hashes() {
    }

    /** Requires 64 upper-case hex characters, as the AEAT prints them (hash spec v0.1.2, section 5). */
    static String required(String hash) {
        if (hash == null || !HEX64_UPPER.matcher(hash).matches()) {
            throw new IllegalArgumentException("hash must be 64 upper-case hex characters, got '" + hash + "'");
        }
        return hash;
    }
}
