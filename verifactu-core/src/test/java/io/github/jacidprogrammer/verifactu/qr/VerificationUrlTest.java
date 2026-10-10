package io.github.jacidprogrammer.verifactu.qr;

import io.github.jacidprogrammer.verifactu.AeatEnvironment;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.jacidprogrammer.verifactu.model.InvoiceId;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Examples copied from AEAT QR spec v0.5.0 (10/12/2025), sections 4 and 8. */
class VerificationUrlTest {

    static final LocalDate DATE = LocalDate.of(2024, 1, 1);

    @Test
    void testingUrlMatchesTheUrlEncodingExampleOfTheSpec() {
        VerificationUrl url = VerificationUrl.verifiable(AeatEnvironment.TESTING,
                new InvoiceId("89890001K", "12345678&G33", DATE), new BigDecimal("241.4"));
        assertThat(url.toString()).isEqualTo(
                "https://prewww2.aeat.es/wlpl/TIKE-CONT/ValidarQR?nif=89890001K&numserie=12345678%26G33&fecha=01-01-2024&importe=241.4");
    }

    @Test
    void productionUrlUsesTheProductionHost() {
        VerificationUrl url = VerificationUrl.verifiable(AeatEnvironment.PRODUCTION,
                new InvoiceId("89890001K", "12345678&G33", DATE), new BigDecimal("241.40"));
        assertThat(url.toString()).isEqualTo(
                "https://www2.agenciatributaria.gob.es/wlpl/TIKE-CONT/ValidarQR?nif=89890001K&numserie=12345678%26G33&fecha=01-01-2024&importe=241.4");
    }

    @Test
    void nonVerifiableSystemsUseTheOtherService() {
        VerificationUrl url = VerificationUrl.nonVerifiable(AeatEnvironment.TESTING,
                new InvoiceId("89890001K", "12345678", DATE), new BigDecimal("100.00"));
        assertThat(url.toString()).isEqualTo(
                "https://prewww2.aeat.es/wlpl/TIKE-CONT/ValidarQRNoVerifactu?nif=89890001K&numserie=12345678&fecha=01-01-2024&importe=100");
    }

    @Test
    void slashAndSpaceInSeriesAreUrlEncoded() {
        VerificationUrl url = VerificationUrl.verifiable(AeatEnvironment.TESTING,
                new InvoiceId("89890001K", "12345678 / G33", DATE), new BigDecimal("12.3"));
        assertThat(url.toString()).contains("numserie=12345678+%2F+G33&");
    }

    @Test
    void parametersKeepTheSpecOrder() {
        String q = VerificationUrl.verifiable(AeatEnvironment.TESTING, new InvoiceId("89890001K", "A", DATE), BigDecimal.ONE)
                .toUri().getRawQuery();
        assertThat(q.split("&")).extracting(p -> p.substring(0, p.indexOf('='))).containsExactly("nif", "numserie", "fecha", "importe");
    }

    @Test
    void equalByUrl() {
        InvoiceId id = new InvoiceId("89890001K", "A", DATE);
        assertThat(VerificationUrl.verifiable(AeatEnvironment.TESTING, id, new BigDecimal("1.0")))
                .isEqualTo(VerificationUrl.verifiable(AeatEnvironment.TESTING, id, new BigDecimal("1.00")))
                .isNotEqualTo(VerificationUrl.verifiable(AeatEnvironment.PRODUCTION, id, new BigDecimal("1.00")));
    }
}
