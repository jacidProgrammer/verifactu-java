package io.github.jacidprogrammer.verifactu.hash;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HashInputTest {

    @Test
    void joinsFieldsWithAmpersandAndEquals() {
        assertThat(HashInput.of().field("A", "1").field("B", "2").toString()).isEqualTo("A=1&B=2");
    }

    @Test
    void nullValueBecomesEmptyAfterEquals() {
        assertThat(HashInput.of().field("Huella", null).field("X", "y").toString()).isEqualTo("Huella=&X=y");
    }

    @Test
    void hashIsUpperCaseHexOf64Chars() {
        assertThat(Sha256.hexUpper("")).isEqualTo("E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855");
        assertThat(Sha256.hexUpper("ñ")).hasSize(64).matches("[0-9A-F]+");
    }

    @Test
    void hashesUtf8BytesNotLatin1() throws Exception {
        // "ñ" is C3 B1 in UTF-8 and F1 in ISO-8859-1; the spec mandates UTF-8 (section 3).
        byte[] utf8 = java.security.MessageDigest.getInstance("SHA-256").digest(new byte[] {(byte) 0xC3, (byte) 0xB1});
        byte[] latin1 = java.security.MessageDigest.getInstance("SHA-256").digest(new byte[] {(byte) 0xF1});
        String expected = java.util.HexFormat.of().withUpperCase().formatHex(utf8);
        assertThat(Sha256.hexUpper("ñ")).isEqualTo(expected)
                .isNotEqualTo(java.util.HexFormat.of().withUpperCase().formatHex(latin1));
    }
}
