package io.github.jacidprogrammer.verifactu;

import io.github.jacidprogrammer.verifactu.chain.RecordChain;
import io.github.jacidprogrammer.verifactu.client.AeatClient;
import io.github.jacidprogrammer.verifactu.client.AeatResponse;
import io.github.jacidprogrammer.verifactu.model.InvoiceContent;
import io.github.jacidprogrammer.verifactu.model.InvoiceId;
import io.github.jacidprogrammer.verifactu.model.InvoiceType;
import io.github.jacidprogrammer.verifactu.model.InvoicingSystem;
import io.github.jacidprogrammer.verifactu.model.Party;
import io.github.jacidprogrammer.verifactu.model.RegistrationRecord;
import io.github.jacidprogrammer.verifactu.model.TaxLine;
import io.github.jacidprogrammer.verifactu.qr.QrCode;
import io.github.jacidprogrammer.verifactu.qr.VerificationUrl;
import io.github.jacidprogrammer.verifactu.xml.Submission;
import java.math.BigDecimal;
import java.security.KeyStore;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * The code shown in {@code README.md} ("Quick start"). {@code ReadmeExampleTest} checks that every line of the README
 * snippets is literally in this file, so the README cannot drift from something that compiles.
 */
public final class ReadmeExample {

    private ReadmeExample() {
    }

    /** README snippet 1: first invoice of a new invoicing system, its QR and the message for the AEAT. */
    public static Submission firstInvoice() {
        RecordChain chain = RecordChain.start(Clock.systemDefaultZone());
        Party issuer = new Party("Empresa Ejemplo SL", "89890001K");
        InvoiceId invoiceId = new InvoiceId(issuer.taxId(), "2026/A-001", LocalDate.of(2026, 1, 15));
        RegistrationRecord record = chain.register(invoiceId, InvoiceType.F1, new BigDecimal("21.00"), new BigDecimal("121.00"));
        InvoiceContent content = InvoiceContent.of("Consulting services", new Party("Cliente Ejemplo", "12345678Z"),
                TaxLine.vat(new BigDecimal("21"), new BigDecimal("100.00"), new BigDecimal("21.00")));
        InvoicingSystem system = new InvoicingSystem(new Party("Software Factory SA", "A12345674"),
                "my-erp", "01", "1.0", "INST-0001", false, true, false);
        byte[] qrPng = QrCode.of(VerificationUrl.verifiable(AeatEnvironment.TESTING, invoiceId, record.totalAmount()), 300).toPng();
        Submission submission = new Submission(issuer, system, List.of(Submission.registration(record, content)));
        return submission;
    }

    /** README snippet 2: sending with the taxpayer's certificate. */
    public static AeatResponse send(Submission submission, KeyStore keyStore, char[] keyPassword) {
        AeatClient client = AeatClient.withCertificate(AeatEnvironment.TESTING, keyStore, keyPassword);
        AeatResponse response = client.submit(submission);
        System.out.println(response.status() + " CSV=" + response.csv().orElse("-") + " rejected=" + response.rejected());
        return response;
    }
}
