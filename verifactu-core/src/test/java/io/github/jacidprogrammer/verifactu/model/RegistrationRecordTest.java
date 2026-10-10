package io.github.jacidprogrammer.verifactu.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class RegistrationRecordTest {

    static final InvoiceId ID = new InvoiceId("89890001K", "A-1", LocalDate.of(2024, 1, 1));
    static final OffsetDateTime AT = OffsetDateTime.parse("2024-01-01T19:20:30+01:00");

    @Test
    void amountsAreNormalisedToTwoDecimals() {
        RegistrationRecord r = new RegistrationRecord(ID, InvoiceType.F1, new BigDecimal("1"), new BigDecimal("241.4"), null, AT);
        assertThat(r.totalTax()).isEqualTo(new BigDecimal("1.00"));
        assertThat(r.totalAmount()).isEqualTo(new BigDecimal("241.40"));
        assertThat(r.hashInput()).contains("CuotaTotal=1.00&ImporteTotal=241.40");
    }

    @Test
    void rejectsMoreThanTwoDecimals() {
        assertThatThrownBy(() -> new RegistrationRecord(ID, InvoiceType.F1, new BigDecimal("0.001"), BigDecimal.ONE, null, AT))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("2 decimals");
    }

    @Test
    void rejectsMoreThanTwelveIntegerDigits() {
        assertThatThrownBy(() -> new RegistrationRecord(ID, InvoiceType.F1, BigDecimal.ZERO, new BigDecimal("1234567890123.00"), null, AT))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("12 integer digits");
    }

    @Test
    void dropsNanosecondsBecauseTheSpecUsesWholeSeconds() {
        RegistrationRecord r = new RegistrationRecord(ID, InvoiceType.F1, BigDecimal.ZERO, BigDecimal.ONE, null,
                OffsetDateTime.parse("2024-01-01T19:20:30.123456+01:00"));
        assertThat(r.generatedAt()).isEqualTo(AT);
        assertThat(r.hashInput()).endsWith("FechaHoraHusoGenRegistro=2024-01-01T19:20:30+01:00");
    }

    @Test
    void sameInputsGiveSameHashAndAnyFieldChangesIt() {
        RegistrationRecord a = new RegistrationRecord(ID, InvoiceType.F1, BigDecimal.ZERO, BigDecimal.ONE, null, AT);
        RegistrationRecord b = new RegistrationRecord(ID, InvoiceType.F1, BigDecimal.ZERO, BigDecimal.ONE, null, AT);
        RegistrationRecord c = new RegistrationRecord(ID, InvoiceType.F2, BigDecimal.ZERO, BigDecimal.ONE, null, AT);
        assertThat(a.hash()).isEqualTo(b.hash()).isNotEqualTo(c.hash());
    }
}
