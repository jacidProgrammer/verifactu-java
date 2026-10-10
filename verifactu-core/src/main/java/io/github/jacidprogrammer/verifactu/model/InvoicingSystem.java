package io.github.jacidprogrammer.verifactu.model;

import java.util.Objects;

/**
 * {@code SistemaInformatico}: the invoicing software that generates the records, as declared in its
 * responsible declaration ({@code SuministroInformacion.xsd} v1.0, {@code SistemaInformaticoType}).
 *
 * @param producer {@code NombreRazon} and {@code NIF} of the software producer
 * @param name {@code NombreSistemaInformatico}, up to 30 characters
 * @param id {@code IdSistemaInformatico}, up to 2 characters
 * @param version {@code Version}, up to 50 characters
 * @param installationNumber {@code NumeroInstalacion}, up to 100 characters
 * @param verifactuOnly {@code TipoUsoPosibleSoloVerifactu}: the software can only work as Verifactu
 * @param supportsMultipleTaxpayers {@code TipoUsoPosibleMultiOT}: the software can serve several taxpayers
 * @param multipleTaxpayersInstalled {@code IndicadorMultiplesOT}: this installation serves several taxpayers
 */
public record InvoicingSystem(Party producer, String name, String id, String version, String installationNumber,
        boolean verifactuOnly, boolean supportsMultipleTaxpayers, boolean multipleTaxpayersInstalled) {

    public InvoicingSystem {
        Objects.requireNonNull(producer, "producer");
        name = Values.trimmed("name", name);
        id = Values.trimmed("id", id);
        version = Values.trimmed("version", version);
        installationNumber = Values.trimmed("installationNumber", installationNumber);
        Values.maxLength("name", name, 30);
        Values.maxLength("id", id, 2);
        Values.maxLength("version", version, 50);
        Values.maxLength("installationNumber", installationNumber, 100);
    }
}
