package io.github.jacidprogrammer.verifactu.chain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jacidprogrammer.verifactu.model.CancellationRecord;
import io.github.jacidprogrammer.verifactu.model.ChainLink;
import io.github.jacidprogrammer.verifactu.model.InvoiceId;
import io.github.jacidprogrammer.verifactu.model.InvoiceType;
import io.github.jacidprogrammer.verifactu.model.RegistrationRecord;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

/** The chain reproduces the three official vectors when driven with the PDF's clock values. */
class RecordChainTest {

    static final String H1 = "3C464DAF61ACB827C65FDA19F352A4E3BDC2C640E9E9FC4CC058073F38F12F60";
    static final String H2 = "F7B94CFD8924EDFF273501B01EE5153E4CE8F259766F88CF6ACB8935802A2B97";
    static final String H3 = "177547C0D57AC74748561D054A9CEC14B4C4EA23D1BEFD6F2E69E3A388F90C68";
    static final ZoneId MADRID = ZoneId.of("Europe/Madrid");
    static final LocalDate DATE = LocalDate.of(2024, 1, 1);
    static final BigDecimal TAX = new BigDecimal("12.35");
    static final BigDecimal TOTAL = new BigDecimal("123.45");
    static final ChainLink L1 = new ChainLink(new InvoiceId("89890001K", "12345678/G33", DATE), H1);
    static final ChainLink L2 = new ChainLink(new InvoiceId("89890001K", "12345679/G34", DATE), H2);

    /** A clock that advances 5 seconds per call, starting at 19:20:30 Madrid time, like the PDF examples. */
    static Clock pdfClock() {
        return new Clock() {
            Instant t = Instant.parse("2024-01-01T18:20:25Z");

            @Override
            public ZoneId getZone() {
                return MADRID;
            }

            @Override
            public Clock withZone(ZoneId zone) {
                return this;
            }

            @Override
            public Instant instant() {
                t = t.plusSeconds(5);
                return t;
            }
        };
    }

    @Test
    void emptyChainThenThreeOfficialRecords() {
        RecordChain chain = RecordChain.start(pdfClock());
        assertThat(chain.isEmpty()).isTrue();
        assertThat(chain.headHash()).isEmpty();

        RegistrationRecord r1 = chain.register(new InvoiceId("89890001K", "12345678/G33", DATE), InvoiceType.F1, TAX, TOTAL);
        assertThat(r1.isFirst()).isTrue();
        assertThat(r1.hash()).isEqualTo(H1);

        RegistrationRecord r2 = chain.register(new InvoiceId("89890001K", "12345679/G34", DATE), InvoiceType.F1, TAX, TOTAL);
        assertThat(r2.previousHash()).contains(H1);
        assertThat(r2.hash()).isEqualTo(H2);

        CancellationRecord c = chain.cancel(new InvoiceId("89890001K", "12345679/G34", DATE));
        assertThat(c.previousHash()).contains(H2);
        assertThat(c.hash()).isEqualTo(H3);
        assertThat(chain.headHash()).contains(H3);
        assertThat(chain.head()).contains(new ChainLink(c.cancelledInvoiceId(), H3));
    }

    @Test
    void resumeContinuesFromStoredHead() {
        RecordChain chain = RecordChain.resume(L2, OffsetDateTime.parse("2024-01-01T19:20:35+01:00"),
                Clock.fixed(Instant.parse("2024-01-01T18:20:40Z"), MADRID));
        assertThat(chain.isEmpty()).isFalse();
        assertThat(chain.cancel(new InvoiceId("89890001K", "12345679/G34", DATE)).hash()).isEqualTo(H3);
    }

    @Test
    void appendRejectsRecordPointingToAnotherHead() {
        RecordChain chain = RecordChain.resume(L1, OffsetDateTime.parse("2024-01-01T19:20:30+01:00"), Clock.systemUTC());
        RegistrationRecord stray = new RegistrationRecord(new InvoiceId("89890001K", "X-1", DATE), InvoiceType.F1, TAX, TOTAL, L2,
                OffsetDateTime.parse("2024-01-01T19:21:00+01:00"));

        assertThatThrownBy(() -> chain.append(stray)).isInstanceOf(ChainOrderException.class).hasMessageContaining(H1);
        assertThat(chain.headHash()).contains(H1);
    }

    @Test
    void appendRejectsFirstRecordOnNonEmptyChain() {
        RecordChain chain = RecordChain.resume(L1, OffsetDateTime.parse("2024-01-01T19:20:30+01:00"), Clock.systemUTC());
        RegistrationRecord first = new RegistrationRecord(new InvoiceId("89890001K", "X-1", DATE), InvoiceType.F1, TAX, TOTAL, null,
                OffsetDateTime.parse("2024-01-01T19:21:00+01:00"));
        assertThatThrownBy(() -> chain.append(first)).isInstanceOf(ChainOrderException.class);
    }

    @Test
    void appendRejectsRecordOlderThanHead() {
        RecordChain chain = RecordChain.resume(L1, OffsetDateTime.parse("2024-01-01T19:20:30+01:00"), Clock.systemUTC());
        RegistrationRecord older = new RegistrationRecord(new InvoiceId("89890001K", "X-1", DATE), InvoiceType.F1, TAX, TOTAL, L1,
                OffsetDateTime.parse("2024-01-01T19:20:29+01:00"));
        assertThatThrownBy(() -> chain.append(older)).isInstanceOf(ChainOrderException.class).hasMessageContaining("older");
    }

    @Test
    void appendAcceptsSameSecondAsHead() {
        OffsetDateTime at = OffsetDateTime.parse("2024-01-01T19:20:30+01:00");
        RecordChain chain = RecordChain.resume(L1, at, Clock.systemUTC());
        RegistrationRecord same = new RegistrationRecord(new InvoiceId("89890001K", "X-1", DATE), InvoiceType.F1, TAX, TOTAL, L1, at);
        assertThat(chain.append(same)).isSameAs(same);
        assertThat(chain.headHash()).contains(same.hash());
    }

    @Test
    void clockGoingBackwardsIsRejectedInsteadOfBreakingTheChain() {
        RecordChain chain = RecordChain.resume(L1, OffsetDateTime.parse("2024-01-01T19:20:30+01:00"),
                Clock.fixed(Instant.parse("2024-01-01T18:20:29Z"), ZoneOffset.UTC));
        assertThatThrownBy(() -> chain.cancel(new InvoiceId("89890001K", "X-1", DATE))).isInstanceOf(ChainOrderException.class)
                .hasMessageContaining("backwards");
        assertThat(chain.headHash()).contains(H1);
    }

    @Test
    void offsetFollowsTheClockZone() {
        RecordChain chain = RecordChain.start(Clock.fixed(Instant.parse("2024-07-01T10:00:00Z"), MADRID));
        RegistrationRecord r = chain.register(new InvoiceId("89890001K", "S-1", DATE), InvoiceType.F1, TAX, TOTAL);
        assertThat(r.hashInput()).endsWith("FechaHoraHusoGenRegistro=2024-07-01T12:00:00+02:00");
        assertThat(Duration.between(r.generatedAt().toInstant(), Instant.parse("2024-07-01T10:00:00Z"))).isZero();
    }
}
