package io.github.jacidprogrammer.verifactu.xml;

import static io.github.jacidprogrammer.verifactu.Fixtures.C3;
import static io.github.jacidprogrammer.verifactu.Fixtures.CONTENT;
import static io.github.jacidprogrammer.verifactu.Fixtures.H1;
import static io.github.jacidprogrammer.verifactu.Fixtures.H2;
import static io.github.jacidprogrammer.verifactu.Fixtures.H3;
import static io.github.jacidprogrammer.verifactu.Fixtures.ISSUER;
import static io.github.jacidprogrammer.verifactu.Fixtures.R1;
import static io.github.jacidprogrammer.verifactu.Fixtures.R2;
import static io.github.jacidprogrammer.verifactu.Fixtures.SYSTEM;
import static io.github.jacidprogrammer.verifactu.Fixtures.officialThree;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jacidprogrammer.verifactu.Fixtures;
import io.github.jacidprogrammer.verifactu.model.ExemptionCause;
import io.github.jacidprogrammer.verifactu.model.InvoiceContent;
import io.github.jacidprogrammer.verifactu.model.InvoiceId;
import io.github.jacidprogrammer.verifactu.model.InvoiceType;
import io.github.jacidprogrammer.verifactu.model.Party;
import io.github.jacidprogrammer.verifactu.model.RegistrationRecord;
import io.github.jacidprogrammer.verifactu.model.TaxLine;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Criterion 4 of the contract: the generated XML validates against SuministroLR.xsd / SuministroInformacion.xsd v1.0. */
class VerifactuXmlTest {

    @Test
    void officialRecordsValidateAgainstTheBundledXsd() {
        Document doc = VerifactuXml.toDocument(officialThree());
        assertThatCode(() -> XsdValidator.validateRequest(doc)).doesNotThrowAnyException();
    }

    @Test
    void registrationFieldsComeFromTheRecord() {
        Document doc = VerifactuXml.toDocument(officialThree());
        List<Element> altas = elements(doc, "RegistroAlta");
        assertThat(altas).hasSize(2);
        Element first = altas.get(0);
        assertThat(text(first, "IDVersion")).isEqualTo("1.0");
        assertThat(text(child(first, "IDFactura"), "NumSerieFactura")).isEqualTo("12345678/G33");
        assertThat(text(child(first, "IDFactura"), "FechaExpedicionFactura")).isEqualTo("01-01-2024");
        assertThat(text(first, "NombreRazonEmisor")).isEqualTo(ISSUER.name());
        assertThat(text(first, "TipoFactura")).isEqualTo("F1");
        assertThat(text(first, "CuotaTotal")).isEqualTo("12.35");
        assertThat(text(first, "ImporteTotal")).isEqualTo("123.45");
        assertThat(text(child(first, "Encadenamiento"), "PrimerRegistro")).isEqualTo("S");
        assertThat(text(first, "FechaHoraHusoGenRegistro")).isEqualTo("2024-01-01T19:20:30+01:00");
        assertThat(text(first, "TipoHuella")).isEqualTo("01");
        assertThat(text(first, "Huella")).isEqualTo(H1);

        Element second = altas.get(1);
        Element previous = child(child(second, "Encadenamiento"), "RegistroAnterior");
        assertThat(text(previous, "NumSerieFactura")).isEqualTo("12345678/G33");
        assertThat(text(previous, "Huella")).isEqualTo(H1);
        assertThat(text(second, "Huella")).isEqualTo(H2);
    }

    @Test
    void cancellationCarriesTheCancelledInvoiceAndItsPredecessor() {
        Document doc = VerifactuXml.toDocument(officialThree());
        Element anulacion = elements(doc, "RegistroAnulacion").get(0);
        assertThat(text(child(anulacion, "IDFactura"), "NumSerieFacturaAnulada")).isEqualTo("12345679/G34");
        Element previous = child(child(anulacion, "Encadenamiento"), "RegistroAnterior");
        assertThat(text(previous, "NumSerieFactura")).isEqualTo("12345679/G34");
        assertThat(text(previous, "Huella")).isEqualTo(H2);
        assertThat(text(anulacion, "Huella")).isEqualTo(H3);
        assertThat(text(anulacion, "FechaHoraHusoGenRegistro")).isEqualTo("2024-01-01T19:20:40+01:00");
    }

    @Test
    void headerAndSystemAreWritten() {
        Document doc = VerifactuXml.toDocument(officialThree());
        Element header = elements(doc, "Cabecera").get(0);
        assertThat(text(child(header, "ObligadoEmision"), "NIF")).isEqualTo("89890001K");
        Element system = elements(doc, "SistemaInformatico").get(0);
        assertThat(text(system, "NombreRazon")).isEqualTo("Software Factory SA");
        assertThat(text(system, "IdSistemaInformatico")).isEqualTo("01");
        assertThat(text(system, "TipoUsoPosibleSoloVerifactu")).isEqualTo("N");
        assertThat(text(system, "TipoUsoPosibleMultiOT")).isEqualTo("S");
        assertThat(text(system, "IndicadorMultiplesOT")).isEqualTo("N");
    }

    @Test
    void breakdownLinesFollowTheXsdOrder() {
        Document doc = VerifactuXml.toDocument(officialThree());
        Element detail = elements(doc, "DetalleDesglose").get(0);
        List<String> names = new ArrayList<>();
        for (int i = 0; i < detail.getChildNodes().getLength(); i++) {
            names.add(detail.getChildNodes().item(i).getLocalName());
        }
        assertThat(names).containsExactly("Impuesto", "ClaveRegimen", "CalificacionOperacion", "TipoImpositivo",
                "BaseImponibleOimporteNoSujeto", "CuotaRepercutida");
        assertThat(text(detail, "TipoImpositivo")).isEqualTo("21.00");
        assertThat(text(detail, "BaseImponibleOimporteNoSujeto")).isEqualTo("102.07");
    }

    @Test
    void exemptAndSimplifiedInvoiceWithoutRecipientValidates() {
        RegistrationRecord simplified = new RegistrationRecord(new InvoiceId("89890001K", "T-1", Fixtures.DATE),
                InvoiceType.F2, BigDecimal.ZERO, new BigDecimal("50.00"), null,
                OffsetDateTime.parse("2024-01-01T10:00:00+01:00"));
        InvoiceContent content = InvoiceContent.withoutRecipient("Ticket",
                TaxLine.exempt(ExemptionCause.E1, new BigDecimal("50")));
        Document doc = VerifactuXml.toDocument(new Submission(ISSUER, SYSTEM,
                List.of(Submission.registration(simplified, content))));
        assertThatCode(() -> XsdValidator.validateRequest(doc)).doesNotThrowAnyException();
        assertThat(elements(doc, "Destinatarios")).isEmpty();
        assertThat(text(elements(doc, "DetalleDesglose").get(0), "OperacionExenta")).isEqualTo("E1");
        assertThat(elements(doc, "TipoImpositivo")).isEmpty();
    }

    /** Calibration: the validator must reject a document missing a mandatory block, otherwise the green above means nothing. */
    @Test
    void validatorRejectsDocumentWithoutBreakdown() {
        Document doc = VerifactuXml.toDocument(officialThree());
        Element desglose = elements(doc, "Desglose").get(0);
        desglose.getParentNode().removeChild(desglose);
        assertThatThrownBy(() -> XsdValidator.validateRequest(doc)).isInstanceOf(SchemaViolationException.class)
                .hasMessageContaining("Desglose");
    }

    @Test
    void validatorRejectsHashLongerThan64() {
        Document doc = VerifactuXml.toDocument(officialThree());
        Element huella = (Element) elements(doc, "RegistroAlta").get(0).getElementsByTagNameNS("*", "Huella").item(0);
        huella.setTextContent(H1 + "0");
        assertThatThrownBy(() -> XsdValidator.validateRequest(doc)).isInstanceOf(SchemaViolationException.class);
    }

    @Test
    void serialisesAsUtf8WithDeclarationAndPrefixes() {
        String xml = VerifactuXml.toString(VerifactuXml.toDocument(officialThree()));
        assertThat(xml).startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"");
        assertThat(xml).contains("<sfLR:RegFactuSistemaFacturacion").contains("<sf:RegistroAlta>").contains("<sf:RegistroAnulacion>");
        assertThat(xml).contains("xmlns:sfLR=\"" + VerifactuXml.NS_LR + "\"").contains("xmlns:sf=\"" + VerifactuXml.NS_SI + "\"");
    }

    @Test
    void submissionRejectsRecordsOfAnotherIssuer() {
        Party other = new Party("Otra SL", "B12345674");
        assertThatThrownBy(() -> new Submission(other, SYSTEM, List.of(Submission.registration(R1, CONTENT))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("B12345674");
    }

    @Test
    void submissionRejectsEmptyAndMoreThan1000Records() {
        assertThatThrownBy(() -> new Submission(ISSUER, SYSTEM, List.of())).isInstanceOf(IllegalArgumentException.class);
        List<Submission.Item> items = new ArrayList<>();
        for (int i = 0; i < 1001; i++) {
            items.add(Submission.cancellation(C3));
        }
        assertThatThrownBy(() -> new Submission(ISSUER, SYSTEM, items)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1001");
        assertThat(new Submission(ISSUER, SYSTEM, items.subList(0, 1000)).items()).hasSize(1000);
    }

    @Test
    void thousandRecordsValidate() {
        List<Submission.Item> items = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            items.add(Submission.registration(R2, CONTENT));
        }
        Document doc = VerifactuXml.toDocument(new Submission(ISSUER, SYSTEM, items));
        assertThatCode(() -> XsdValidator.validateRequest(doc)).doesNotThrowAnyException();
        assertThat(elements(doc, "RegistroFactura")).hasSize(1000);
    }

    static List<Element> elements(Document doc, String localName) {
        NodeList list = doc.getElementsByTagNameNS("*", localName);
        List<Element> out = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            out.add((Element) list.item(i));
        }
        return out;
    }

    static Element child(Element parent, String localName) {
        for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
            if (parent.getChildNodes().item(i) instanceof Element e && localName.equals(e.getLocalName())) {
                return e;
            }
        }
        throw new AssertionError("no child " + localName + " under " + parent.getLocalName());
    }

    static String text(Element parent, String localName) {
        return child(parent, localName).getTextContent();
    }
}
