# TASK — verifactu-java v0.1

## Objetivo
Una librería Java 21 sin frameworks que genere registros de facturación Verifactu (alta y anulación) con huella encadenada, QR de cotejo y XML conforme a los esquemas de la AEAT, verificable contra los vectores oficiales, publicable en Maven Central.

## Terminado significa
1. `./mvnw -q verify` verde; cifra `Tests run: N, Failures: 0, Errors: 0, Skipped: 0` en el cierre (N ≥ 40).
2. Los 3 vectores oficiales de huella (PDF huella v0.1.2, pág. 10-12) pasan como tests: alta 1 → 3C464DAF…, alta 2 encadenada → F7B94CFD…, anulación → 177547C0…. Cualquier cambio en el separador, orden o codificación los pone en rojo (mutación enseñada una vez).
3. La URL del QR coincide byte a byte con el ejemplo del PDF QR v0.5.0 (`nif=89890001K&numserie=12345678%26G33&fecha=01-01-2024&importe=241.4`, serie `12345678&G33` codificada), para producción y pruebas; el PNG generado se decodifica con ZXing y devuelve esa URL; nivel M.
4. El XML `RegFactuSistemaFacturacion` generado valida contra `SuministroLR.xsd` / `SuministroInformacion.xsd` v1.0 descargados al repo (test con javax.xml.validation).
5. Una cadena de registros se puede construir offline: `RecordChain` acepta altas y anulaciones, mantiene `PrimerRegistro`, la huella anterior y `FechaHoraHusoGenRegistro`, y rechaza encadenar fuera de orden.
6. Cliente SOAP `AeatClient` (JDK HttpClient + mTLS desde un `KeyStore` PKCS12 del usuario) construye `RegFactuSistemaFacturacion` desde el WSDL v1.0, parsea `EstadoEnvio`/`EstadoRegistro`/`TiempoEsperaEnvio`/`RegistroDuplicado` y respeta el límite de 1.000 registros por envío. Probado contra un stub SOAP local (`com.sun.net.httpserver`) con respuestas Correcto, ParcialmenteCorrecto e Incorrecto tomadas del PDF SWeb; test que falla si se envían 1.001.
7. Starter `verifactu-spring-boot-starter` (Boot 4.1): autoconfigura `RecordChain` y `AeatClient` desde propiedades `verifactu.*` (nif, entorno pruebas/producción, ruta y clave del keystore); `ApplicationContextRunner` demuestra que sin `verifactu.enabled=true` no se crea ningún bean y con él se crean los dos.
8. README en inglés con ejemplo de 15 líneas que compila (test que lo ejecuta), fechas correctas de obligatoriedad y aviso "not yet exercised against the AEAT test environment".

## Fuera de alcance / no tocar
- Reintentos automáticos, cola persistente y scheduler de `TiempoEsperaEnvio`: la librería expone el valor, quien integra decide cuándo reenviar.
- Firma XAdES del registro (modalidad no-Verifactu): fuera; la especificación de firma ni se ha leído.
- Registros de evento (`RegistroEvento`), consulta (`ConsultaLR`), subsanación, facturas rectificativas con desglose completo de regímenes: solo los campos obligatorios de alta/anulación; el resto se modela como "no soportado" explícito, no se finge.
- Publicación en Maven Central: pom preparado (sources, javadoc, licencia, scm) pero sin firmar ni publicar.
- Ninguna dependencia nueva fuera de: ZXing (QR), JAXB runtime (XML), JUnit 5, AssertJ; en el starter, `spring-boot-starter` y `spring-boot-test` 4.1. Sin Apache CXF ni Spring WS: SOAP a mano sobre JAXB.

## Decisiones del usuario (cerradas 2026-10-10)
- D1 Alcance v0.1: B, incluye el cliente SOAP. No se ejecutará contra la AEAT sin tu certificado.
- D2 Starter Spring Boot: B, incluido.
- D3 Importes: A, `BigDecimal` escala 2 con formateadores por destino.

## Plan y presupuesto
Sesión A (esta): F1 dominio, formateadores, `HashChain` y vectores oficiales (~12 llamadas) · F2 QR con ZXing (~8) · commit por fase.
Sesión B (/handoff): F3 XSD v1.0 al repo, JAXB y validación (subagente para el mapeo de campos, ~15) · F4 `AeatClient` + stub SOAP (~15) · commit.
Sesión C: F5 starter Boot 4.1 (~15) · F6 README, pom de Central, /verify final (~8).
Total ≈75 llamadas, ≈170K de contexto repartido en tres sesiones; cada una cierra con /verify y /handoff.

## Verificación prevista
- Se entrega: salida de `./mvnw verify` con cifras, los 3 vectores en verde y uno en rojo tras mutar el separador, URL del QR comparada con el PDF, XML validado contra XSD.
- Se entrega además: respuesta parseada del stub SOAP para los 3 estados, test rojo con 1.001 registros, `ApplicationContextRunner` con y sin `verifactu.enabled`.
- NO se verifica: envío real al entorno de pruebas de la AEAT (necesita tu certificado; se deja un `main` de ejemplo documentado para que lo corras tú), cotejo del QR en `prewww2.aeat.es`, firma XAdES.

## Fuentes
docs/aeat-spec-notes.md (huella v0.1.2 27/08/2024, QR v0.5.0 10/12/2025, SWeb v1.0.3 28/07/2025, RD 1007/2023 consolidado).
