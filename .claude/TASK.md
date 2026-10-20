# TASK — validar verifactu-java (2026-10-10)

El contrato anterior (v0.1, 8 criterios) está cerrado y publicado; queda en el historial de git.

## Objetivo
Demostrar con evidencia independiente de los propios tests que la librería hace lo que promete el README y que el starter funciona dentro de una aplicación Spring Boot 4.1 real.

## Terminado significa
1. **Huella**: un script Python (`-I`, en scratchpad) recompone la cadena de entrada de los 3 vectores oficiales según el PDF huella v0.1.2 y obtiene 3C464DAF…, F7B94CFD… y 177547C0… sin usar código Java; además una mutación en `HashInput` (cambiar el separador `&`) pone en rojo `OfficialHashVectorsTest` y se restaura.
2. **QR**: la URL que genera `VerificationUrl` para el ejemplo oficial se compara byte a byte con la del PDF QR v0.5.0 (texto en `verifactu-pdf/qr.txt`, no con la constante del test); el PNG se decodifica con una herramienta externa (`zbarimg` si está, si no ZXing desde un `main` nuevo) y devuelve esa URL.
3. **XML**: el `RegFactuSistemaFacturacion` de `ReadmeExample` se valida con `xmllint --schema` contra `SuministroLR.xsd` descargado de nuevo desde la AEAT (sha256 igual al del repo o se reporta la diferencia).
4. **SOAP**: `curl` manual contra `SoapStub` no es posible (es test); en su lugar el punto 5 cubre el envío.
5. **Integración real**: una app Spring Boot 4.1 mínima que depende del starter vía `~/.m2` (`./mvnw install`), con un PKCS12 autofirmado generado con `keytool`, arranca con `verifactu.enabled=true`, registra una factura por un endpoint REST, devuelve el QR (PNG) y el XML, y envía el registro a un stub SOAP local levantado en la propia app: respuesta `Correcto` con CSV. Se prueba con `curl` y se guarda la salida.
6. Mutación de integración: con `verifactu.enabled` ausente, el mismo endpoint responde 500 por falta de bean (demuestra que el starter es el que aporta los beans).
7. `./mvnw -q verify` del repo sigue en 94/94 al terminar; el repo queda sin cambios de código salvo lo decidido en D1.

## Fuera de alcance / no tocar
- Envío real a `prewww1.aeat.es` / entorno de pruebas (sin certificado del contribuyente).
- Firma XAdES, `S2`, `IDOtro`, rectificativas, eventos, ConsultaLR (D14 del v0.1).
- Publicar en Maven Central, cambiar versiones, tocar `fiverr-gigs.md`.
- Corregir defectos que aparezcan: se reportan con file:line, no se arreglan en esta sesión.

## Decisiones del usuario
- D1 Dónde vive la app de integración: A) módulo `verifactu-sample` en el repo, como `ratelimit-sample` en tu starter público (queda como prueba visible, +1 módulo, CI lo compila). B) carpeta en scratchpad, se borra al terminar (solo evidencia en el informe).
- D2 Sondear el endpoint real de la AEAT con el cert autofirmado: A) sí, un solo `curl` esperando rechazo TLS/HTTP (prueba de que la URL existe y responde; no envía datos válidos). B) no tocar la AEAT.

## Plan y presupuesto
- F1 (hilo principal, ~8 llamadas): huella en Python, mutación, QR vs PDF, decodificar PNG, xmllint.
- F2 (subagente con brief cerrado, ~20 llamadas): app Boot + stub + keytool + curl; devuelve ≤20 líneas y guarda salida en `docs/validacion-2026-10-10.md` (D1-A) o scratchpad (D1-B).
- F3 (hilo principal, ~5): verify final, informe /verify. Total ≈35 llamadas, <100K contexto.

## Verificación prevista
Evidencia: salidas literales de python, xmllint, zbarimg/ZXing, curl y `Tests run`. NO verificado: AEAT real, Central, comportamiento con certificado FNMT de verdad.
