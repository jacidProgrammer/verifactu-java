package io.github.jacidprogrammer.verifactu.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class AeatFormatTest {

    @Test
    void dateIsDayMonthYearWithDashes() {
        assertThat(AeatFormat.date(LocalDate.of(2024, 1, 1))).isEqualTo("01-01-2024");
        assertThat(AeatFormat.date(LocalDate.of(2026, 12, 31))).isEqualTo("31-12-2026");
    }

    @Test
    void dateTimeIsIso8601WithOffsetAndWholeSeconds() {
        assertThat(AeatFormat.dateTime(OffsetDateTime.parse("2024-01-01T19:20:30+01:00"))).isEqualTo("2024-01-01T19:20:30+01:00");
        assertThat(AeatFormat.dateTime(OffsetDateTime.of(2024, 7, 1, 0, 0, 0, 0, ZoneOffset.UTC))).isEqualTo("2024-07-01T00:00:00+00:00");
        assertThat(AeatFormat.dateTime(OffsetDateTime.of(2024, 7, 1, 9, 5, 7, 0, ZoneOffset.ofHours(2)))).isEqualTo("2024-07-01T09:05:07+02:00");
    }

    @ParameterizedTest
    @CsvSource({"12.35,12.35", "123.45,123.45", "241.4,241.40", "241,241.00", "0,0.00", "-5.5,-5.50", "1234567890.1,1234567890.10"})
    void amountAlwaysHasTwoDecimalsAndADot(String in, String expected) {
        assertThat(AeatFormat.amount(new BigDecimal(in))).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"241.40,241.4", "241.4,241.4", "241.00,241", "123.45,123.45", "0.00,0", "1E+2,100"})
    void qrAmountDropsTrailingZerosLikeTheAeatExample(String in, String expected) {
        assertThat(AeatFormat.qrAmount(new BigDecimal(in))).isEqualTo(expected);
    }
}
