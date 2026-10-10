package io.github.jacidprogrammer.verifactu.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * One {@code DetalleDesglose} line of the invoice breakdown, always for VAT ({@code Impuesto = 01}) under the
 * general regime ({@code ClaveRegimen = 01}). Other taxes and regimes are not supported in this version.
 *
 * <p>Exactly one of {@code qualification} or {@code exemption} is set. Rate and tax amount are required for subject
 * operations (S1, S2) and must be absent otherwise.
 *
 * @param qualification {@code CalificacionOperacion}, or {@code null} for an exempt operation
 * @param exemption {@code OperacionExenta}, or {@code null} for a non-exempt operation
 * @param rate {@code TipoImpositivo} as a percentage, e.g. {@code 21}
 * @param base {@code BaseImponibleOimporteNoSujeto}
 * @param taxAmount {@code CuotaRepercutida}
 */
public record TaxLine(OperationQualification qualification, ExemptionCause exemption, BigDecimal rate, BigDecimal base,
        BigDecimal taxAmount) {

    public TaxLine {
        if ((qualification == null) == (exemption == null)) {
            throw new IllegalArgumentException("exactly one of qualification or exemption must be set");
        }
        base = Values.amount("base", base);
        boolean subject = qualification != null && qualification.isSubject();
        if (subject) {
            rate = Values.amount("rate", Objects.requireNonNull(rate, "rate is required for subject operations"));
            taxAmount = Values.amount("taxAmount", Objects.requireNonNull(taxAmount, "taxAmount is required for subject operations"));
            if (rate.signum() < 0 || rate.compareTo(new BigDecimal("999.99")) > 0) {
                throw new IllegalArgumentException("rate must be between 0 and 999.99, got " + rate.toPlainString());
            }
        } else if (rate != null || taxAmount != null) {
            throw new IllegalArgumentException("rate and taxAmount are only allowed for subject operations (S1, S2)");
        }
    }

    /** Subject, not exempt, without reverse charge ({@code S1}). */
    public static TaxLine vat(BigDecimal rate, BigDecimal base, BigDecimal taxAmount) {
        return new TaxLine(OperationQualification.S1, null, rate, base, taxAmount);
    }

    /** Exempt operation: no rate, no tax amount. */
    public static TaxLine exempt(ExemptionCause cause, BigDecimal base) {
        return new TaxLine(null, Objects.requireNonNull(cause, "cause"), null, base, null);
    }

    /** Not subject ({@code N1} or {@code N2}). */
    public static TaxLine notSubject(OperationQualification qualification, BigDecimal amount) {
        Objects.requireNonNull(qualification, "qualification");
        if (qualification.isSubject()) {
            throw new IllegalArgumentException("notSubject requires N1 or N2, got " + qualification);
        }
        return new TaxLine(qualification, null, null, amount, null);
    }
}
