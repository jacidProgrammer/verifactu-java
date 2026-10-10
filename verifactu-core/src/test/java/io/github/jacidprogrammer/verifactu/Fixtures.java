package io.github.jacidprogrammer.verifactu;

import io.github.jacidprogrammer.verifactu.model.CancellationRecord;
import io.github.jacidprogrammer.verifactu.model.ChainLink;
import io.github.jacidprogrammer.verifactu.model.InvoiceContent;
import io.github.jacidprogrammer.verifactu.model.InvoiceId;
import io.github.jacidprogrammer.verifactu.model.InvoiceType;
import io.github.jacidprogrammer.verifactu.model.InvoicingSystem;
import io.github.jacidprogrammer.verifactu.model.Party;
import io.github.jacidprogrammer.verifactu.model.RegistrationRecord;
import io.github.jacidprogrammer.verifactu.model.TaxLine;
import io.github.jacidprogrammer.verifactu.xml.Submission;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/** The three official hash vectors (hash spec v0.1.2, section 6) dressed with the fields the XML needs. */
public final class Fixtures {

    public static final String H1 = "3C464DAF61ACB827C65FDA19F352A4E3BDC2C640E9E9FC4CC058073F38F12F60";
    public static final String H2 = "F7B94CFD8924EDFF273501B01EE5153E4CE8F259766F88CF6ACB8935802A2B97";
    public static final String H3 = "177547C0D57AC74748561D054A9CEC14B4C4EA23D1BEFD6F2E69E3A388F90C68";
    public static final LocalDate DATE = LocalDate.of(2024, 1, 1);
    public static final Party ISSUER = new Party("Empresa Ejemplo SL", "89890001K");
    public static final Party RECIPIENT = new Party("Cliente Ejemplo", "12345678Z");
    public static final InvoicingSystem SYSTEM = new InvoicingSystem(new Party("Software Factory SA", "A12345674"),
            "verifactu-java demo", "01", "0.1.0", "INST-0001", false, true, false);
    public static final InvoiceId ID1 = new InvoiceId("89890001K", "12345678/G33", DATE);
    public static final InvoiceId ID2 = new InvoiceId("89890001K", "12345679/G34", DATE);
    public static final InvoiceContent CONTENT = InvoiceContent.of("Consultoria enero", RECIPIENT,
            TaxLine.vat(new BigDecimal("21"), new BigDecimal("102.07"), new BigDecimal("21.43")),
            TaxLine.vat(new BigDecimal("0"), new BigDecimal("0.00"), new BigDecimal("0.00")));

    public static final RegistrationRecord R1 = new RegistrationRecord(ID1, InvoiceType.F1, new BigDecimal("12.35"),
            new BigDecimal("123.45"), null, OffsetDateTime.parse("2024-01-01T19:20:30+01:00"));
    public static final RegistrationRecord R2 = new RegistrationRecord(ID2, InvoiceType.F1, new BigDecimal("12.35"),
            new BigDecimal("123.45"), new ChainLink(ID1, H1), OffsetDateTime.parse("2024-01-01T19:20:35+01:00"));
    public static final CancellationRecord C3 = new CancellationRecord(ID2, new ChainLink(ID2, H2),
            OffsetDateTime.parse("2024-01-01T19:20:40+01:00"));

    private Fixtures() {
    }

    public static Submission officialThree() {
        return new Submission(ISSUER, SYSTEM, List.of(Submission.registration(R1, CONTENT),
                Submission.registration(R2, CONTENT), Submission.cancellation(C3)));
    }
}
