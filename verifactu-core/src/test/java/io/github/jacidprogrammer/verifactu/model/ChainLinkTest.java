package io.github.jacidprogrammer.verifactu.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class ChainLinkTest {

    static final InvoiceId ID = new InvoiceId("89890001K", "A-1", LocalDate.of(2024, 1, 1));
    static final String HASH = "3C464DAF61ACB827C65FDA19F352A4E3BDC2C640E9E9FC4CC058073F38F12F60";

    @Test
    void rejectsBlankOrNullHash() {
        assertThatThrownBy(() -> new ChainLink(ID, "  ")).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("64 upper-case hex");
        assertThatThrownBy(() -> new ChainLink(ID, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ChainLink(null, HASH)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsMalformedHash() {
        assertThatThrownBy(() -> new ChainLink(ID, "abc")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ChainLink(ID, HASH.toLowerCase())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ChainLink(ID, "a".repeat(64))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ofRecordTakesItsInvoiceAndHash() {
        OffsetDateTime at = OffsetDateTime.parse("2024-01-01T19:20:30+01:00");
        RegistrationRecord first = new RegistrationRecord(ID, InvoiceType.F1, BigDecimal.ZERO, BigDecimal.ONE, null, at);
        ChainLink link = ChainLink.of(first);
        assertThat(link.invoiceId()).isEqualTo(ID);
        assertThat(link.hash()).isEqualTo(first.hash());

        InvoiceId cancelled = new InvoiceId("89890001K", "B-2", LocalDate.of(2024, 1, 2));
        CancellationRecord cancellation = new CancellationRecord(cancelled, link, at);
        assertThat(ChainLink.of(cancellation).invoiceId()).isEqualTo(cancelled);
        assertThat(cancellation.previousHash()).contains(first.hash());
    }
}
