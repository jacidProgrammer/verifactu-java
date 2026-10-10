package io.github.jacidprogrammer.verifactu.hash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** SHA-256 over the UTF-8 bytes of a string, rendered as 64 upper-case hex characters (hash spec v0.1.2, section 4). */
public final class Sha256 {

    private static final HexFormat HEX = HexFormat.of().withUpperCase();

    private Sha256() {
    }

    public static String hexUpper(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HEX.formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is mandatory in every JVM", e);
        }
    }
}
