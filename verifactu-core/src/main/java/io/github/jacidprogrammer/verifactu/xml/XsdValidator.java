package io.github.jacidprogrammer.verifactu.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

/**
 * Validates request and response documents against the AEAT schemas v1.0 bundled under {@code /xsd}
 * ({@code SuministroLR.xsd}, {@code SuministroInformacion.xsd}, {@code RespuestaSuministro.xsd}). The W3C
 * XML-Signature schema they import is resolved from the bundled copy, so validation never reaches the network.
 */
public final class XsdValidator {

    private static final String XMLDSIG_LOCATION = "http://www.w3.org/TR/xmldsig-core/xmldsig-core-schema.xsd";
    private static final Schema REQUEST = load("/xsd/SuministroLR.xsd");
    private static final Schema RESPONSE = load("/xsd/RespuestaSuministro.xsd");

    private XsdValidator() {
    }

    /** @throws SchemaViolationException when the document is not a valid {@code RegFactuSistemaFacturacion} */
    public static void validateRequest(Document document) {
        validate(REQUEST, document, "RegFactuSistemaFacturacion");
    }

    /** @throws SchemaViolationException when the document is not a valid {@code RespuestaRegFactuSistemaFacturacion} */
    public static void validateResponse(Document document) {
        validate(RESPONSE, document, "RespuestaRegFactuSistemaFacturacion");
    }

    private static void validate(Schema schema, Document document, String what) {
        try {
            Validator validator = schema.newValidator();
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            validator.validate(new DOMSource(document));
        } catch (SAXException e) {
            throw new SchemaViolationException(what + " does not validate against the AEAT XSD v1.0: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Schema load(String resource) {
        URL url = XsdValidator.class.getResource(resource);
        if (url == null) {
            throw new IllegalStateException("missing bundled schema " + resource);
        }
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "file");
            factory.setResourceResolver(new BundledResolver());
            return factory.newSchema(new StreamSource(url.toExternalForm()));
        } catch (SAXException e) {
            throw new IllegalStateException("cannot load bundled schema " + resource, e);
        }
    }

    /** Serves the XML-Signature schema from the classpath; everything else resolves relative to the including file. */
    private static final class BundledResolver implements LSResourceResolver {
        @Override
        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
            if (XMLDSIG_LOCATION.equals(systemId)) {
                InputStream in = XsdValidator.class.getResourceAsStream("/xsd/xmldsig-core-schema.xsd");
                return new StreamInput(systemId, in);
            }
            return null;
        }
    }

    private static final class StreamInput implements LSInput {
        private final String systemId;
        private final InputStream stream;

        StreamInput(String systemId, InputStream stream) {
            this.systemId = systemId;
            this.stream = stream;
        }

        @Override public InputStream getByteStream() { return stream; }
        @Override public String getSystemId() { return systemId; }
        @Override public java.io.Reader getCharacterStream() { return null; }
        @Override public String getStringData() { return null; }
        @Override public String getPublicId() { return null; }
        @Override public String getBaseURI() { return null; }
        @Override public String getEncoding() { return null; }
        @Override public boolean getCertifiedText() { return false; }
        @Override public void setCharacterStream(java.io.Reader r) { }
        @Override public void setByteStream(InputStream s) { }
        @Override public void setStringData(String s) { }
        @Override public void setSystemId(String s) { }
        @Override public void setPublicId(String s) { }
        @Override public void setBaseURI(String s) { }
        @Override public void setEncoding(String s) { }
        @Override public void setCertifiedText(boolean b) { }
    }
}
