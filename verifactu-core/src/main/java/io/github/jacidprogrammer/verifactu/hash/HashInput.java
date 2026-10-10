package io.github.jacidprogrammer.verifactu.hash;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the {@code field=value&field=value} string the hash is computed over (hash spec v0.1.2, section 3).
 * Field names are the XML element names; values are already trimmed by the model.
 */
public final class HashInput {

    private final List<String> parts = new ArrayList<>();

    private HashInput() {
    }

    public static HashInput of() {
        return new HashInput();
    }

    public HashInput field(String name, String value) {
        parts.add(name + "=" + (value == null ? "" : value));
        return this;
    }

    @Override
    public String toString() {
        return String.join("&", parts);
    }

    public String hash() {
        return Sha256.hexUpper(toString());
    }
}
