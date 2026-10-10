package io.github.jacidprogrammer.verifactu.client;

import static io.github.jacidprogrammer.verifactu.Fixtures.C3;
import static io.github.jacidprogrammer.verifactu.Fixtures.CONTENT;
import static io.github.jacidprogrammer.verifactu.Fixtures.ISSUER;
import static io.github.jacidprogrammer.verifactu.Fixtures.R2;
import static io.github.jacidprogrammer.verifactu.Fixtures.SYSTEM;
import static io.github.jacidprogrammer.verifactu.Fixtures.officialThree;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jacidprogrammer.verifactu.xml.Submission;
import io.github.jacidprogrammer.verifactu.xml.VerifactuXml;
import io.github.jacidprogrammer.verifactu.xml.XsdValidator;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Criterion 6 of the contract: SOAP envelope, mTLS wiring aside, and parsing of the three global states. */
@Timeout(30)
class AeatClientTest {

    static final String NS_R =
            "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaSuministro.xsd";

    SoapStub stub;
    AeatClient client;

    @BeforeEach
    void start() throws IOException {
        stub = new SoapStub();
        client = new AeatClient(HttpClient.newHttpClient(), stub.endpoint(), Duration.ofSeconds(5));
    }

    @AfterEach
    void stop() {
        stub.close();
    }

    @Test
    void postsSoap11EnvelopeWithTheValidatedPayload() {
        stub.body = response("Correcto", "<sfR:CSV>CSV0001</sfR:CSV>", line("12345678/G33", "Alta", "Correcto", ""),
                line("12345679/G34", "Alta", "Correcto", ""), line("12345679/G34", "Anulacion", "Correcto", ""));

        client.submit(officialThree());

        assertThat(stub.received).hasSize(1);
        SoapStub.Received req = stub.received.get(0);
        assertThat(req.headers().get("Content-type")).containsExactly("text/xml; charset=UTF-8");
        assertThat(req.headers().get("Soapaction")).containsExactly("\"\"");
        String xml = new String(req.body(), StandardCharsets.UTF_8);
        assertThat(xml).contains("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"")
                .contains("<soapenv:Body>").contains("<sfLR:RegFactuSistemaFacturacion");

        Document envelope = ResponseParser.parse(req.body());
        Element payload = (Element) envelope.getElementsByTagNameNS(VerifactuXml.NS_LR, "RegFactuSistemaFacturacion").item(0);
        assertThat(payload.getParentNode().getLocalName()).isEqualTo("Body");
        assertThatCode(() -> XsdValidator.validateRequest(detach(payload))).doesNotThrowAnyException();
    }

    @Test
    void parsesFullyAcceptedResponse() {
        stub.body = response("Correcto", "<sfR:CSV>CSV0001</sfR:CSV>", line("12345678/G33", "Alta", "Correcto", ""),
                line("12345679/G34", "Alta", "Correcto", ""), line("12345679/G34", "Anulacion", "Correcto", ""));
        assertResponseValidates();

        AeatResponse r = client.submit(officialThree());

        assertThat(r.status()).isEqualTo(SubmissionStatus.ACCEPTED);
        assertThat(r.csv()).contains("CSV0001");
        assertThat(r.waitSeconds()).isEqualTo(60);
        assertThat(r.lines()).hasSize(3);
        assertThat(r.lines().get(2).operation()).isEqualTo(Operation.CANCELLATION);
        assertThat(r.lines().get(2).invoiceId().seriesNumber()).isEqualTo("12345679/G34");
        assertThat(r.rejected()).isEmpty();
        assertThat(r.lines()).allMatch(LineResult::isRegistered);
    }

    @Test
    void parsesPartiallyAcceptedResponseWithErrorsAndDuplicate() {
        stub.body = response("ParcialmenteCorrecto", "<sfR:CSV>CSV0002</sfR:CSV>",
                line("12345678/G33", "Alta", "AceptadoConErrores",
                        "<sfR:CodigoErrorRegistro>2001</sfR:CodigoErrorRegistro>"
                        + "<sfR:DescripcionErrorRegistro>Aviso admisible</sfR:DescripcionErrorRegistro>"),
                line("12345679/G34", "Alta", "Incorrecto",
                        "<sfR:CodigoErrorRegistro>3000</sfR:CodigoErrorRegistro>"
                        + "<sfR:DescripcionErrorRegistro>Registro de facturacion duplicado</sfR:DescripcionErrorRegistro>"
                        + "<sfR:RegistroDuplicado><sf:IdPeticionRegistroDuplicado>PET-1</sf:IdPeticionRegistroDuplicado>"
                        + "<sf:EstadoRegistroDuplicado>Correcta</sf:EstadoRegistroDuplicado></sfR:RegistroDuplicado>"),
                line("12345679/G34", "Anulacion", "Correcto", ""));
        assertResponseValidates();

        AeatResponse r = client.submit(officialThree());

        assertThat(r.status()).isEqualTo(SubmissionStatus.PARTIALLY_ACCEPTED);
        LineResult warned = r.lines().get(0);
        assertThat(warned.status()).isEqualTo(RecordStatus.ACCEPTED_WITH_ERRORS);
        assertThat(warned.errorCode()).hasValue(2001);
        assertThat(warned.errorDescription()).contains("Aviso admisible");
        assertThat(warned.isRegistered()).isTrue();
        LineResult dup = r.lines().get(1);
        assertThat(dup.status()).isEqualTo(RecordStatus.REJECTED);
        assertThat(dup.errorCode()).hasValue(3000);
        assertThat(dup.duplicate()).isPresent();
        assertThat(dup.duplicate().get().requestId()).isEqualTo("PET-1");
        assertThat(dup.duplicate().get().status()).isEqualTo("Correcta");
        assertThat(dup.duplicate().get().errorCode()).isEmpty();
        assertThat(r.rejected()).containsExactly(dup);
    }

    @Test
    void parsesRejectedResponseWithoutCsv() {
        stub.body = response("Incorrecto", "",
                line("12345678/G33", "Alta", "Incorrecto",
                        "<sfR:CodigoErrorRegistro>1100</sfR:CodigoErrorRegistro>"
                        + "<sfR:DescripcionErrorRegistro>Valor del campo no valido</sfR:DescripcionErrorRegistro>"));
        assertResponseValidates();

        AeatResponse r = client.submit(new Submission(ISSUER, SYSTEM, List.of(Submission.registration(R2, CONTENT))));

        assertThat(r.status()).isEqualTo(SubmissionStatus.REJECTED);
        assertThat(r.csv()).isEmpty();
        assertThat(r.rejected()).hasSize(1);
        assertThat(r.lines().get(0).errorCode()).hasValue(1100);
    }

    /** The SOAP fault example of the SWeb description v1.0.3, section 3.1. */
    @Test
    void soapFaultBecomesAeatFaultException() {
        stub.status = 500;
        stub.body = """
                <?xml version="1.0" encoding="UTF-8"?>
                <env:Envelope xmlns:env="http://schemas.xmlsoap.org/soap/envelope/">
                <env:Body>
                <env:Fault>
                <faultcode>env:Client</faultcode>
                <faultstring>Codigo[4104].El NIF del titular en la cabecera no está identificado.
                NIF:iii. NOMBRE_RAZON:xxx
                </faultstring>
                <detail><callstack>WSExcepcion [faultcode=null, detailMap=null, version=0,........</callstack></detail>
                </env:Fault>
                </env:Body>
                </env:Envelope>
                """;

        assertThatThrownBy(() -> client.submit(officialThree())).isInstanceOf(AeatFaultException.class)
                .hasMessageContaining("4104")
                .satisfies(e -> {
                    AeatFaultException fault = (AeatFaultException) e;
                    assertThat(fault.faultCode()).isEqualTo("env:Client");
                    assertThat(fault.isServerFault()).isFalse();
                });
    }

    @Test
    void unexpectedHttpStatusAndGarbageAreClientExceptions() {
        stub.status = 503;
        stub.body = "maintenance";
        assertThatThrownBy(() -> client.submit(officialThree())).isInstanceOf(AeatClientException.class)
                .hasMessageContaining("HTTP 503");

        stub.status = 200;
        stub.body = "<html>not soap</html>";
        assertThatThrownBy(() -> client.submit(officialThree())).isInstanceOf(AeatClientException.class)
                .hasMessageContaining("neither");
    }

    @Test
    void refusesToSend1001RecordsBeforeTouchingTheNetwork() {
        List<Submission.Item> items = new ArrayList<>();
        for (int i = 0; i < 1001; i++) {
            items.add(Submission.cancellation(C3));
        }
        assertThatThrownBy(() -> client.submit(new Submission(ISSUER, SYSTEM, items)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("1000");
        assertThat(stub.received).isEmpty();
    }

    @Test
    void unreachableEndpointIsAClientException() {
        stub.close();
        assertThatThrownBy(() -> client.submit(officialThree())).isInstanceOf(AeatClientException.class)
                .hasMessageContaining("cannot reach");
    }

    /** Calibration of the response validator: an EstadoEnvio outside the XSD enumeration must be rejected. */
    @Test
    void responseValidatorRejectsUnknownState() {
        stub.body = response("Pendiente", "", line("12345678/G33", "Alta", "Correcto", ""));
        Document envelope = ResponseParser.parse(stub.body.getBytes(StandardCharsets.UTF_8));
        Element root = (Element) envelope.getElementsByTagNameNS(NS_R, "RespuestaRegFactuSistemaFacturacion").item(0);
        assertThatThrownBy(() -> XsdValidator.validateResponse(detach(root)))
                .isInstanceOf(io.github.jacidprogrammer.verifactu.xml.SchemaViolationException.class)
                .hasMessageContaining("Pendiente");
        assertThatThrownBy(() -> client.submit(officialThree())).isInstanceOf(AeatClientException.class)
                .hasMessageContaining("unknown EstadoEnvio");
    }

    /** The canned responses themselves conform to RespuestaSuministro.xsd, so the parser is tested on realistic input. */
    void assertResponseValidates() {
        Document envelope = ResponseParser.parse(stub.body.getBytes(StandardCharsets.UTF_8));
        Element root = (Element) envelope.getElementsByTagNameNS(NS_R, "RespuestaRegFactuSistemaFacturacion").item(0);
        assertThatCode(() -> XsdValidator.validateResponse(detach(root))).doesNotThrowAnyException();
    }

    static Document detach(Element element) {
        Document doc = VerifactuXml.toDocument(officialThree());
        doc.removeChild(doc.getDocumentElement());
        doc.appendChild(doc.importNode(element, true));
        return doc;
    }

    static String response(String estadoEnvio, String csv, String... lines) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                <soapenv:Body>
                <sfR:RespuestaRegFactuSistemaFacturacion xmlns:sfR="%s" xmlns:sf="%s">
                %s
                <sfR:DatosPresentacion><sf:NIFPresentador>89890001K</sf:NIFPresentador>
                <sf:TimestampPresentacion>2024-01-01T19:20:41+01:00</sf:TimestampPresentacion></sfR:DatosPresentacion>
                <sfR:Cabecera><sf:ObligadoEmision><sf:NombreRazon>Empresa Ejemplo SL</sf:NombreRazon><sf:NIF>89890001K</sf:NIF></sf:ObligadoEmision></sfR:Cabecera>
                <sfR:TiempoEsperaEnvio>60</sfR:TiempoEsperaEnvio>
                <sfR:EstadoEnvio>%s</sfR:EstadoEnvio>
                %s
                </sfR:RespuestaRegFactuSistemaFacturacion>
                </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(NS_R, VerifactuXml.NS_SI, csv, estadoEnvio, String.join("\n", lines));
    }

    static String line(String series, String operation, String estado, String extra) {
        return """
                <sfR:RespuestaLinea>
                <sfR:IDFactura><sf:IDEmisorFactura>89890001K</sf:IDEmisorFactura><sf:NumSerieFactura>%s</sf:NumSerieFactura>
                <sf:FechaExpedicionFactura>01-01-2024</sf:FechaExpedicionFactura></sfR:IDFactura>
                <sfR:Operacion><sf:TipoOperacion>%s</sf:TipoOperacion></sfR:Operacion>
                <sfR:EstadoRegistro>%s</sfR:EstadoRegistro>
                %s
                </sfR:RespuestaLinea>
                """.formatted(series, operation, estado, extra);
    }
}
