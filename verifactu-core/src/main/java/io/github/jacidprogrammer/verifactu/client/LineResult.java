package io.github.jacidprogrammer.verifactu.client;

import io.github.jacidprogrammer.verifactu.model.InvoiceId;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * {@code RespuestaLinea}: the AEAT verdict on one record of the submission.
 *
 * @param invoiceId the invoice the record referred to
 * @param operation registration or cancellation
 * @param status accepted, accepted with errors or rejected
 * @param errorCode {@code CodigoErrorRegistro} (see the AEAT validations and errors document)
 * @param errorDescription {@code DescripcionErrorRegistro}
 * @param duplicate present when a registration was rejected as a duplicate
 */
public record LineResult(InvoiceId invoiceId, Operation operation, RecordStatus status, OptionalInt errorCode,
        Optional<String> errorDescription, Optional<DuplicateInfo> duplicate) {

    public boolean isRegistered() {
        return status != RecordStatus.REJECTED;
    }
}
