package io.github.jacidprogrammer.verifactu.model;

/**
 * A Spanish person or company identified by NIF ({@code PersonaFisicaJuridicaESType} in
 * {@code SuministroInformacion.xsd} v1.0). Foreign identifiers ({@code IDOtro}) are not supported in this version.
 *
 * @param name {@code NombreRazon}, 1 to 120 characters
 * @param taxId {@code NIF}, 9 characters
 */
public record Party(String name, String taxId) {

    public Party {
        name = Values.trimmed("name", name);
        taxId = Values.trimmed("taxId", taxId);
        Values.maxLength("name", name, 120);
        if (taxId.length() != 9) {
            throw new IllegalArgumentException("taxId must have 9 characters, got '" + taxId + "'");
        }
    }
}
