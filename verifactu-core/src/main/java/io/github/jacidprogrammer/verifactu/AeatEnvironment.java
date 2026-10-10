package io.github.jacidprogrammer.verifactu;

import java.net.URI;

/**
 * AEAT execution environments: QR verification base URLs (QR spec v0.5.0, section 5) and SOAP endpoints of the
 * {@code RegFactuSistemaFacturacion} operation ({@code SistemaFacturacion.wsdl} v1.0, ports {@code SistemaVerifactu}
 * and {@code SistemaVerifactuSello}).
 */
public enum AeatEnvironment {
    /** Portal de Pruebas Externas. */
    TESTING("https://prewww2.aeat.es/wlpl/TIKE-CONT/",
            "https://prewww1.aeat.es/wlpl/TIKE-CONT/ws/SistemaFacturacion/VerifactuSOAP",
            "https://prewww10.aeat.es/wlpl/TIKE-CONT/ws/SistemaFacturacion/VerifactuSOAP"),
    PRODUCTION("https://www2.agenciatributaria.gob.es/wlpl/TIKE-CONT/",
            "https://www1.agenciatributaria.gob.es/wlpl/TIKE-CONT/ws/SistemaFacturacion/VerifactuSOAP",
            "https://www10.agenciatributaria.gob.es/wlpl/TIKE-CONT/ws/SistemaFacturacion/VerifactuSOAP");

    private final String qrBase;
    private final URI verifactuEndpoint;
    private final URI verifactuSealEndpoint;

    AeatEnvironment(String qrBase, String verifactuEndpoint, String verifactuSealEndpoint) {
        this.qrBase = qrBase;
        this.verifactuEndpoint = URI.create(verifactuEndpoint);
        this.verifactuSealEndpoint = URI.create(verifactuSealEndpoint);
    }

    /** Base for the verification service: {@code ValidarQR} or {@code ValidarQRNoVerifactu} is appended. */
    public String qrBaseUrl() {
        return qrBase;
    }

    /** Voluntary submission endpoint for a taxpayer or representative certificate (WSDL port {@code SistemaVerifactu}). */
    public URI verifactuEndpoint() {
        return verifactuEndpoint;
    }

    /** Voluntary submission endpoint for an electronic seal certificate (WSDL port {@code SistemaVerifactuSello}). */
    public URI verifactuSealEndpoint() {
        return verifactuSealEndpoint;
    }
}
