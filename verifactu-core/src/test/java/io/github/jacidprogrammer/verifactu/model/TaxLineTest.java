package io.github.jacidprogrammer.verifactu.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class TaxLineTest {

    @Test
    void subjectLinesNeedRateAndAmount() {
        TaxLine vat = TaxLine.vat(new BigDecimal("21"), new BigDecimal("100"), new BigDecimal("21"));
        assertThat(vat.rate()).isEqualTo(new BigDecimal("21.00"));
        assertThat(vat.taxAmount()).isEqualTo(new BigDecimal("21.00"));
        assertThatThrownBy(() -> new TaxLine(OperationQualification.S1, null, null, BigDecimal.TEN, BigDecimal.ONE))
                .isInstanceOf(NullPointerException.class).hasMessageContaining("rate");
    }

    @Test
    void exemptAndNotSubjectLinesForbidRateAndAmount() {
        assertThat(TaxLine.exempt(ExemptionCause.E2, BigDecimal.TEN).rate()).isNull();
        assertThat(TaxLine.notSubject(OperationQualification.N1, BigDecimal.TEN).qualification()).isEqualTo(OperationQualification.N1);
        assertThatThrownBy(() -> new TaxLine(null, ExemptionCause.E1, BigDecimal.ONE, BigDecimal.TEN, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("only allowed");
        assertThatThrownBy(() -> TaxLine.notSubject(OperationQualification.S1, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void exactlyOneOfQualificationOrExemption() {
        assertThatThrownBy(() -> new TaxLine(null, null, null, BigDecimal.TEN, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TaxLine(OperationQualification.N1, ExemptionCause.E1, null, BigDecimal.TEN, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void contentLimitsFromTheXsd() {
        TaxLine line = TaxLine.exempt(ExemptionCause.E1, BigDecimal.TEN);
        assertThatThrownBy(() -> new InvoiceContent("x", List.of(), List.of())).hasMessageContaining("1 to 12");
        assertThatThrownBy(() -> new InvoiceContent("x".repeat(501), List.of(line), List.of())).hasMessageContaining("500");
        assertThatThrownBy(() -> new Party("n".repeat(121), "89890001K")).hasMessageContaining("120");
        assertThatThrownBy(() -> new Party("n", "123")).hasMessageContaining("9 characters");
        assertThatThrownBy(() -> new InvoicingSystem(new Party("p", "89890001K"), "n", "123", "v", "i", true, false, false))
                .hasMessageContaining("at most 2");
    }
}
