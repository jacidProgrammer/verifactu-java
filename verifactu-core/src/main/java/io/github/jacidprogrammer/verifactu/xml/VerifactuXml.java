package io.github.jacidprogrammer.verifactu.xml;

import io.github.jacidprogrammer.verifactu.model.AeatFormat;
import io.github.jacidprogrammer.verifactu.model.BillingRecord;
import io.github.jacidprogrammer.verifactu.model.CancellationRecord;
import io.github.jacidprogrammer.verifactu.model.ChainLink;
import io.github.jacidprogrammer.verifactu.model.InvoiceContent;
import io.github.jacidprogrammer.verifactu.model.InvoiceId;
import io.github.jacidprogrammer.verifactu.model.InvoicingSystem;
import io.github.jacidprogrammer.verifactu.model.Party;
import io.github.jacidprogrammer.verifactu.model.RegistrationRecord;
import io.github.jacidprogrammer.verifactu.model.TaxLine;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Builds the {@code RegFactuSistemaFacturacion} document of a {@link Submission} with the JDK DOM API, following
 * {@code SuministroLR.xsd} and {@code SuministroInformacion.xsd} v1.0. Validate it with {@link XsdValidator}.
 */
public final class VerifactuXml {

    public static final String NS_LR =
            "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroLR.xsd";
    public static final String NS_SI =
            "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd";
    static final String VERSION = "1.0";
    static final String HASH_TYPE_SHA256 = "01";
    static final String VAT = "01";
    static final String GENERAL_REGIME = "01";

    private VerifactuXml() {
    }

    /** A new document whose root is {@code RegFactuSistemaFacturacion}. */
    public static Document toDocument(Submission submission) {
        Document doc = newDocument();
        doc.appendChild(createRoot(doc, submission));
        return doc;
    }

    /** Serialises a document as UTF-8 with an XML declaration; the AEAT examples are indented, so this is too. */
    public static byte[] toBytes(Document document) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(document), new StreamResult(out));
            return out.toByteArray();
        } catch (TransformerException e) {
            throw new IllegalStateException("cannot serialise XML", e);
        }
    }

    public static String toString(Document document) {
        return new String(toBytes(document), StandardCharsets.UTF_8);
    }

    /** Creates the {@code RegFactuSistemaFacturacion} element in {@code doc} without attaching it, for embedding in a SOAP body. */
    public static Element createRoot(Document doc, Submission submission) {
        Element root = doc.createElementNS(NS_LR, "sfLR:RegFactuSistemaFacturacion");
        root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:sfLR", NS_LR);
        root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:sf", NS_SI);

        Element header = lr(doc, root, "Cabecera");
        party(doc, si(doc, header, "ObligadoEmision"), submission.issuer());

        for (Submission.Item item : submission.items()) {
            Element entry = lr(doc, root, "RegistroFactura");
            switch (item) {
                case Submission.Registration r -> registration(doc, entry, r.record(), r.content(), submission);
                case Submission.Cancellation c -> cancellation(doc, entry, c.record(), submission.system());
            }
        }
        return root;
    }

    private static void registration(Document doc, Element parent, RegistrationRecord record, InvoiceContent content,
            Submission submission) {
        Element alta = si(doc, parent, "RegistroAlta");
        text(doc, alta, "IDVersion", VERSION);
        Element id = si(doc, alta, "IDFactura");
        text(doc, id, "IDEmisorFactura", record.invoiceId().issuerTaxId());
        text(doc, id, "NumSerieFactura", record.invoiceId().seriesNumber());
        text(doc, id, "FechaExpedicionFactura", AeatFormat.date(record.invoiceId().issueDate()));
        text(doc, alta, "NombreRazonEmisor", submission.issuer().name());
        text(doc, alta, "TipoFactura", record.invoiceType().name());
        text(doc, alta, "DescripcionOperacion", content.description());
        if (!content.recipients().isEmpty()) {
            Element recipients = si(doc, alta, "Destinatarios");
            for (Party recipient : content.recipients()) {
                party(doc, si(doc, recipients, "IDDestinatario"), recipient);
            }
        }
        Element breakdown = si(doc, alta, "Desglose");
        for (TaxLine line : content.breakdown()) {
            Element detail = si(doc, breakdown, "DetalleDesglose");
            text(doc, detail, "Impuesto", VAT);
            text(doc, detail, "ClaveRegimen", GENERAL_REGIME);
            if (line.qualification() != null) {
                text(doc, detail, "CalificacionOperacion", line.qualification().name());
            } else {
                text(doc, detail, "OperacionExenta", line.exemption().name());
            }
            if (line.rate() != null) {
                text(doc, detail, "TipoImpositivo", AeatFormat.amount(line.rate()));
            }
            text(doc, detail, "BaseImponibleOimporteNoSujeto", AeatFormat.amount(line.base()));
            if (line.taxAmount() != null) {
                text(doc, detail, "CuotaRepercutida", AeatFormat.amount(line.taxAmount()));
            }
        }
        text(doc, alta, "CuotaTotal", AeatFormat.amount(record.totalTax()));
        text(doc, alta, "ImporteTotal", AeatFormat.amount(record.totalAmount()));
        tail(doc, alta, record, submission.system());
    }

    private static void cancellation(Document doc, Element parent, CancellationRecord record, InvoicingSystem system) {
        Element anulacion = si(doc, parent, "RegistroAnulacion");
        text(doc, anulacion, "IDVersion", VERSION);
        Element id = si(doc, anulacion, "IDFactura");
        text(doc, id, "IDEmisorFacturaAnulada", record.cancelledInvoiceId().issuerTaxId());
        text(doc, id, "NumSerieFacturaAnulada", record.cancelledInvoiceId().seriesNumber());
        text(doc, id, "FechaExpedicionFacturaAnulada", AeatFormat.date(record.cancelledInvoiceId().issueDate()));
        tail(doc, anulacion, record, system);
    }

    /** Encadenamiento, SistemaInformatico, FechaHoraHusoGenRegistro, TipoHuella and Huella: common to both record types. */
    private static void tail(Document doc, Element parent, BillingRecord record, InvoicingSystem system) {
        Element chaining = si(doc, parent, "Encadenamiento");
        if (record.previous().isPresent()) {
            ChainLink previous = record.previous().get();
            InvoiceId prevId = previous.invoiceId();
            Element prev = si(doc, chaining, "RegistroAnterior");
            text(doc, prev, "IDEmisorFactura", prevId.issuerTaxId());
            text(doc, prev, "NumSerieFactura", prevId.seriesNumber());
            text(doc, prev, "FechaExpedicionFactura", AeatFormat.date(prevId.issueDate()));
            text(doc, prev, "Huella", previous.hash());
        } else {
            text(doc, chaining, "PrimerRegistro", "S");
        }
        Element sys = si(doc, parent, "SistemaInformatico");
        party(doc, sys, system.producer());
        text(doc, sys, "NombreSistemaInformatico", system.name());
        text(doc, sys, "IdSistemaInformatico", system.id());
        text(doc, sys, "Version", system.version());
        text(doc, sys, "NumeroInstalacion", system.installationNumber());
        text(doc, sys, "TipoUsoPosibleSoloVerifactu", yesNo(system.verifactuOnly()));
        text(doc, sys, "TipoUsoPosibleMultiOT", yesNo(system.supportsMultipleTaxpayers()));
        text(doc, sys, "IndicadorMultiplesOT", yesNo(system.multipleTaxpayersInstalled()));
        text(doc, parent, "FechaHoraHusoGenRegistro", AeatFormat.dateTime(record.generatedAt()));
        text(doc, parent, "TipoHuella", HASH_TYPE_SHA256);
        text(doc, parent, "Huella", record.hash());
    }

    private static void party(Document doc, Element parent, Party party) {
        text(doc, parent, "NombreRazon", party.name());
        text(doc, parent, "NIF", party.taxId());
    }

    private static String yesNo(boolean value) {
        return value ? "S" : "N";
    }

    private static Element lr(Document doc, Element parent, String name) {
        Element e = doc.createElementNS(NS_LR, "sfLR:" + name);
        parent.appendChild(e);
        return e;
    }

    private static Element si(Document doc, Element parent, String name) {
        Element e = doc.createElementNS(NS_SI, "sf:" + name);
        parent.appendChild(e);
        return e;
    }

    private static void text(Document doc, Element parent, String name, String value) {
        si(doc, parent, name).setTextContent(value);
    }

    static Document newDocument() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            return factory.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }
}
