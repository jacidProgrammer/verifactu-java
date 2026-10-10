package io.github.jacidprogrammer.verifactu.hash;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jacidprogrammer.verifactu.model.CancellationRecord;
import io.github.jacidprogrammer.verifactu.model.ChainLink;
import io.github.jacidprogrammer.verifactu.model.InvoiceId;
import io.github.jacidprogrammer.verifactu.model.InvoiceType;
import io.github.jacidprogrammer.verifactu.model.RegistrationRecord;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

/**
 * The three worked examples of AEAT "Detalle de las especificaciones técnicas para generación de la huella"
 * v0.1.2 (27/08/2024), pages 10 to 13. Inputs and outputs are copied verbatim from the PDF.
 */
class OfficialHashVectorsTest {

    static final String ALTA_1_INPUT = "IDEmisorFactura=89890001K&NumSerieFactura=12345678/G33&FechaExpedicionFactura=01-01-2024"
            + "&TipoFactura=F1&CuotaTotal=12.35&ImporteTotal=123.45&Huella=&FechaHoraHusoGenRegistro=2024-01-01T19:20:30+01:00";
    static final String ALTA_1_HASH = "3C464DAF61ACB827C65FDA19F352A4E3BDC2C640E9E9FC4CC058073F38F12F60";

    static final String ALTA_2_INPUT = "IDEmisorFactura=89890001K&NumSerieFactura=12345679/G34&FechaExpedicionFactura=01-01-2024"
            + "&TipoFactura=F1&CuotaTotal=12.35&ImporteTotal=123.45&Huella=" + ALTA_1_HASH
            + "&FechaHoraHusoGenRegistro=2024-01-01T19:20:35+01:00";
    static final String ALTA_2_HASH = "F7B94CFD8924EDFF273501B01EE5153E4CE8F259766F88CF6ACB8935802A2B97";

    static final String ANULACION_INPUT = "IDEmisorFacturaAnulada=89890001K&NumSerieFacturaAnulada=12345679/G34"
            + "&FechaExpedicionFacturaAnulada=01-01-2024&Huella=" + ALTA_2_HASH
            + "&FechaHoraHusoGenRegistro=2024-01-01T19:20:40+01:00";
    static final String ANULACION_HASH = "177547C0D57AC74748561D054A9CEC14B4C4EA23D1BEFD6F2E69E3A388F90C68";

    static final LocalDate ISSUE_DATE = LocalDate.of(2024, 1, 1);

    @Test
    void sha256OfVector1String() {
        assertThat(Sha256.hexUpper(ALTA_1_INPUT)).isEqualTo(ALTA_1_HASH);
    }

    @Test
    void sha256OfVector2String() {
        assertThat(Sha256.hexUpper(ALTA_2_INPUT)).isEqualTo(ALTA_2_HASH);
    }

    @Test
    void sha256OfCancellationString() {
        assertThat(Sha256.hexUpper(ANULACION_INPUT)).isEqualTo(ANULACION_HASH);
    }

    @Test
    void firstRegistrationReproducesVector1() {
        RegistrationRecord first = new RegistrationRecord(
                new InvoiceId("89890001K", "12345678/G33", ISSUE_DATE), InvoiceType.F1,
                new BigDecimal("12.35"), new BigDecimal("123.45"), null,
                OffsetDateTime.parse("2024-01-01T19:20:30+01:00"));

        assertThat(first.hashInput()).isEqualTo(ALTA_1_INPUT);
        assertThat(first.hash()).isEqualTo(ALTA_1_HASH);
        assertThat(first.isFirst()).isTrue();
    }

    @Test
    void chainedRegistrationReproducesVector2() {
        RegistrationRecord second = new RegistrationRecord(
                new InvoiceId("89890001K", "12345679/G34", ISSUE_DATE), InvoiceType.F1,
                new BigDecimal("12.35"), new BigDecimal("123.45"),
                new ChainLink(new InvoiceId("89890001K", "12345678/G33", ISSUE_DATE), ALTA_1_HASH),
                OffsetDateTime.parse("2024-01-01T19:20:35+01:00"));

        assertThat(second.hashInput()).isEqualTo(ALTA_2_INPUT);
        assertThat(second.hash()).isEqualTo(ALTA_2_HASH);
        assertThat(second.isFirst()).isFalse();
    }

    @Test
    void cancellationReproducesVector3() {
        CancellationRecord cancellation = new CancellationRecord(
                new InvoiceId("89890001K", "12345679/G34", ISSUE_DATE),
                new ChainLink(new InvoiceId("89890001K", "12345679/G34", ISSUE_DATE), ALTA_2_HASH),
                OffsetDateTime.parse("2024-01-01T19:20:40+01:00"));

        assertThat(cancellation.hashInput()).isEqualTo(ANULACION_INPUT);
        assertThat(cancellation.hash()).isEqualTo(ANULACION_HASH);
    }
}
