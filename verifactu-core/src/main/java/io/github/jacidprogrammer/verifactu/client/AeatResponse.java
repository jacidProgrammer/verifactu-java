package io.github.jacidprogrammer.verifactu.client;

import java.util.List;
import java.util.Optional;

/**
 * Parsed {@code RespuestaRegFactuSistemaFacturacion} ({@code RespuestaSuministro.xsd} v1.0).
 *
 * @param csv {@code CSV}: secure verification code of the submission; absent when every record was rejected
 * @param waitSeconds {@code TiempoEsperaEnvio}: seconds to wait before the next submission, unless 1000 records
 *     are already pending. The library only reports it; scheduling is up to the caller.
 * @param status global result
 * @param lines one verdict per record, in submission order
 */
public record AeatResponse(Optional<String> csv, int waitSeconds, SubmissionStatus status, List<LineResult> lines) {

    public AeatResponse {
        lines = List.copyOf(lines);
    }

    public List<LineResult> rejected() {
        return lines.stream().filter(l -> l.status() == RecordStatus.REJECTED).toList();
    }
}
