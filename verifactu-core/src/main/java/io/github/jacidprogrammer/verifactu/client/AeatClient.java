package io.github.jacidprogrammer.verifactu.client;

import io.github.jacidprogrammer.verifactu.AeatEnvironment;
import io.github.jacidprogrammer.verifactu.xml.Submission;
import io.github.jacidprogrammer.verifactu.xml.VerifactuXml;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Objects;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Sends {@code RegFactuSistemaFacturacion} messages to the AEAT over SOAP 1.1 with mutual TLS, using only the JDK
 * HTTP client. One instance per endpoint; thread-safe.
 *
 * <p>Not yet exercised against the AEAT test environment: it needs a taxpayer certificate. The SOAP envelope,
 * headers and response parsing are covered by tests against a local stub.
 */
public final class AeatClient {

    private final HttpClient http;
    private final URI endpoint;
    private final Duration timeout;

    /** Uses a caller-provided client (already configured with mTLS) against any endpoint; handy for stubs. */
    public AeatClient(HttpClient http, URI endpoint, Duration timeout) {
        this.http = Objects.requireNonNull(http, "http");
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        this.timeout = Objects.requireNonNull(timeout, "timeout");
    }

    /**
     * Client for the voluntary Verifactu endpoint with a taxpayer or representative certificate.
     *
     * @param keyStore a PKCS12 (or other loaded) key store holding the certificate and its private key
     * @param keyPassword password of the private key
     */
    public static AeatClient withCertificate(AeatEnvironment environment, KeyStore keyStore, char[] keyPassword) {
        return new AeatClient(mutualTls(keyStore, keyPassword), environment.verifactuEndpoint(), Duration.ofSeconds(60));
    }

    /** Same as {@link #withCertificate} but for an electronic seal certificate (WSDL port {@code SistemaVerifactuSello}). */
    public static AeatClient withSeal(AeatEnvironment environment, KeyStore keyStore, char[] keyPassword) {
        return new AeatClient(mutualTls(keyStore, keyPassword), environment.verifactuSealEndpoint(), Duration.ofSeconds(60));
    }

    public URI endpoint() {
        return endpoint;
    }

    /**
     * Sends the records and returns the parsed verdict.
     *
     * @throws AeatFaultException when the AEAT rejects the whole message with a SOAP fault
     * @throws AeatClientException on transport errors or unexpected responses
     */
    public AeatResponse submit(Submission submission) {
        byte[] body = soapRequest(submission);
        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(timeout)
                .header("Content-Type", "text/xml; charset=UTF-8")
                .header("SOAPAction", "\"\"")
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        HttpResponse<byte[]> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException e) {
            throw new AeatClientException("cannot reach " + endpoint + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AeatClientException("interrupted while calling " + endpoint, e);
        }
        if (response.statusCode() != 200 && response.statusCode() != 500) {
            throw new AeatClientException("HTTP " + response.statusCode() + " from " + endpoint);
        }
        return ResponseParser.toResponse(ResponseParser.parse(response.body()));
    }

    /** The exact SOAP 1.1 envelope that {@link #submit} posts; useful for logging or for sending by other means. */
    public byte[] soapRequest(Submission submission) {
        Document doc = VerifactuXml.toDocument(submission);
        Element payload = doc.getDocumentElement();
        doc.removeChild(payload);
        Element envelope = doc.createElementNS(ResponseParser.SOAP_NS, "soapenv:Envelope");
        Element header = doc.createElementNS(ResponseParser.SOAP_NS, "soapenv:Header");
        Element body = doc.createElementNS(ResponseParser.SOAP_NS, "soapenv:Body");
        envelope.appendChild(header);
        envelope.appendChild(body);
        body.appendChild(payload);
        doc.appendChild(envelope);
        return VerifactuXml.toBytes(doc);
    }

    static HttpClient mutualTls(KeyStore keyStore, char[] keyPassword) {
        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, keyPassword);
            SSLContext ssl = SSLContext.getInstance("TLS");
            ssl.init(kmf.getKeyManagers(), null, null);
            return HttpClient.newBuilder().sslContext(ssl).connectTimeout(Duration.ofSeconds(20)).build();
        } catch (GeneralSecurityException e) {
            throw new AeatClientException("cannot set up mutual TLS from the key store", e);
        }
    }
}
