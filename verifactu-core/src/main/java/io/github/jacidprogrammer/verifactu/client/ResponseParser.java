package io.github.jacidprogrammer.verifactu.client;

import io.github.jacidprogrammer.verifactu.model.InvoiceId;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** Reads a SOAP response body: either a {@code Fault} or a {@code RespuestaRegFactuSistemaFacturacion}. */
final class ResponseParser {

    static final String SOAP_NS = "http://schemas.xmlsoap.org/soap/envelope/";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private ResponseParser() {
    }

    static Document parse(byte[] body) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return factory.newDocumentBuilder().parse(new ByteArrayInputStream(body));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new AeatClientException("response is not well-formed XML", e);
        }
    }

    /** @throws AeatFaultException when the body carries a SOAP fault */
    static AeatResponse toResponse(Document doc) {
        Element fault = first(doc.getDocumentElement(), "Fault");
        if (fault != null) {
            throw new AeatFaultException(text(fault, "faultcode"), text(fault, "faultstring"));
        }
        Element root = first(doc.getDocumentElement(), "RespuestaRegFactuSistemaFacturacion");
        if (root == null) {
            throw new AeatClientException("response carries neither RespuestaRegFactuSistemaFacturacion nor Fault");
        }
        String csv = childText(root, "CSV");
        int wait = Integer.parseInt(required(root, "TiempoEsperaEnvio"));
        SubmissionStatus status = SubmissionStatus.fromAeat(required(root, "EstadoEnvio"));
        List<LineResult> lines = new ArrayList<>();
        for (Element line : children(root, "RespuestaLinea")) {
            lines.add(line(line));
        }
        return new AeatResponse(Optional.ofNullable(csv), wait, status, lines);
    }

    private static LineResult line(Element line) {
        Element id = child(line, "IDFactura");
        InvoiceId invoiceId = new InvoiceId(required(id, "IDEmisorFactura"), required(id, "NumSerieFactura"),
                LocalDate.parse(required(id, "FechaExpedicionFactura"), DATE));
        Operation operation = Operation.fromAeat(required(child(line, "Operacion"), "TipoOperacion"));
        RecordStatus status = RecordStatus.fromAeat(required(line, "EstadoRegistro"));
        Optional<DuplicateInfo> duplicate = Optional.ofNullable(child(line, "RegistroDuplicado")).map(d ->
                new DuplicateInfo(required(d, "IdPeticionRegistroDuplicado"), required(d, "EstadoRegistroDuplicado"),
                        code(d), Optional.ofNullable(childText(d, "DescripcionErrorRegistro"))));
        return new LineResult(invoiceId, operation, status, code(line),
                Optional.ofNullable(childText(line, "DescripcionErrorRegistro")), duplicate);
    }

    private static OptionalInt code(Element e) {
        String code = childText(e, "CodigoErrorRegistro");
        return code == null ? OptionalInt.empty() : OptionalInt.of(Integer.parseInt(code.strip()));
    }

    /** First descendant with that local name, in document order. */
    private static Element first(Element scope, String localName) {
        NodeList all = scope.getElementsByTagNameNS("*", localName);
        return all.getLength() == 0 ? null : (Element) all.item(0);
    }

    private static String text(Element scope, String localName) {
        Element e = first(scope, localName);
        return e == null ? null : e.getTextContent().strip();
    }

    private static List<Element> children(Element parent, String localName) {
        List<Element> out = new ArrayList<>();
        for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element e && localName.equals(e.getLocalName())) {
                out.add(e);
            }
        }
        return out;
    }

    private static Element child(Element parent, String localName) {
        List<Element> c = children(parent, localName);
        return c.isEmpty() ? null : c.get(0);
    }

    private static String childText(Element parent, String localName) {
        Element e = child(parent, localName);
        return e == null ? null : e.getTextContent().strip();
    }

    private static String required(Element parent, String localName) {
        if (parent == null) {
            throw new AeatClientException("response lacks " + localName);
        }
        String v = childText(parent, localName);
        if (v == null) {
            throw new AeatClientException("response lacks " + localName);
        }
        return v;
    }
}
