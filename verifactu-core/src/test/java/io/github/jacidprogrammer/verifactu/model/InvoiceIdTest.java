package io.github.jacidprogrammer.verifactu.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class InvoiceIdTest {

    static final LocalDate DATE = LocalDate.of(2024, 1, 1);

    @Test
    void stripsBlanksLikeTheAeatDoesBeforeHashing() {
        InvoiceId id = new InvoiceId(" 89890001K ", "  12345678 / G33 ", DATE);
        assertThat(id.issuerTaxId()).isEqualTo("89890001K");
        assertThat(id.seriesNumber()).isEqualTo("12345678 / G33");
    }

    @Test
    void rejectsTaxIdThatIsNotNineCharacters() {
        assertThatThrownBy(() -> new InvoiceId("8989000K", "1", DATE)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("9 characters");
    }

    @Test
    void rejectsBlankSeriesNumber() {
        assertThatThrownBy(() -> new InvoiceId("89890001K", "   ", DATE)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void rejectsSeriesNumberOver60Chars() {
        assertThatThrownBy(() -> new InvoiceId("89890001K", "X".repeat(61), DATE)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("60");
        assertThat(new InvoiceId("89890001K", "X".repeat(60), DATE).seriesNumber()).hasSize(60);
    }

    @Test
    void rejectsNonPrintableAsciiInSeriesNumber() {
        assertThatThrownBy(() -> new InvoiceId("89890001K", "FAC-ñ-1", DATE)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("printable ASCII");
        assertThatThrownBy(() -> new InvoiceId("89890001K", "A\tB", DATE)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullDate() {
        assertThatThrownBy(() -> new InvoiceId("89890001K", "1", null)).isInstanceOf(NullPointerException.class);
    }
}
