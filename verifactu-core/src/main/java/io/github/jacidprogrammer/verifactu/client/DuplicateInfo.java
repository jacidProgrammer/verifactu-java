package io.github.jacidprogrammer.verifactu.client;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * {@code RegistroDuplicado}: returned when a registration is rejected because the AEAT already holds that invoice.
 *
 * @param requestId {@code IdPeticionRegistroDuplicado}: the earlier submission that registered it
 * @param status {@code EstadoRegistroDuplicado}: Correcta, AceptadaConErrores or Anulada (list L21)
 * @param errorCode {@code CodigoErrorRegistro} of the stored record, if any
 * @param errorDescription {@code DescripcionErrorRegistro} of the stored record, if any
 */
public record DuplicateInfo(String requestId, String status, OptionalInt errorCode, Optional<String> errorDescription) {
}
