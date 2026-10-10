# HANDOFF — verifactu-java (sesión C cerrada 2026-10-10)

## Tarea
Contrato en `.claude/TASK.md`: los 8 criterios están implementados. Sesión A = F1+F2, B = F3+F4, C = F5 (starter) + F6 (README, pom de Central). Queda: commits (el repo sigue sin ninguno) y, si el dueño quiere, publicar.

## Hecho / verificado (sesión C)
- `./mvnw -q clean verify`: Tests run 94, Failures 0, Errors 0, Skipped 0 (core 84, starter 10).
- Criterio 7: `verifactu-spring-boot-starter` (Boot 4.1.1). `VerifactuAutoConfigurationTest` con `ApplicationContextRunner`: sin `verifactu.enabled` → ningún bean (ni `VerifactuProperties`); `enabled=false` igual; `enabled=true` + keystore → `RecordChain` vacío + `AeatClient` al endpoint de pruebas; `production`+`seal` → endpoint sello; `chain.head.*` completo → `resume`; `chain.head` parcial → arranque falla; sin `key-store.path` → falla nombrando la propiedad; contraseña mala → falla; `Clock` de la app sella los registros; bean `RecordChain` de la app gana.
- Mutación enseñada: borrar `@ConditionalOnProperty` → 2 tests en rojo (`withoutEnabledNothingIsCreated`, `enabledFalseIsTheSameAsAbsent`). Restaurado.
- Criterio 8: README en inglés; `ReadmeExample` (test sources del core) contiene el código de los dos bloques ```java; `ReadmeExampleTest` exige cada línea literal (sin indentación) y ejecuta el bloque 1 contra `SoapStub` (Correcto, CSV0001, el XML lleva `2026/A-001`). Mutación: cambiar `300`→`301` en el README → 1 test en rojo. Restaurado.
- `META-INF/spring-configuration-metadata.json` con las 11 propiedades `verifactu.*` (compiler-plugin 3.14 necesitaba `proc=full` + `annotationProcessorPaths`).
- Perfil `release` del pom raíz: `./mvnw -q -DskipTests package -Prelease` genera los 6 jars (jar, sources, javadoc × 2 módulos). GPG saltado por `gpg.skip=true` por defecto; `central-publishing-maven-plugin` 0.11.0 con `autoPublish=false`. `scm` y `developers` añadidos.
- `SendToAeatTestEnvironment` (main en test sources del core): sin args imprime el uso y sale con 2. Comando completo en su Javadoc y en el README.

## NO verificado
- Envío real a `prewww1.aeat.es` (sin certificado). Deploy a Central (sin cuenta/clave GPG configuradas; el perfil solo se ha probado hasta `package`).
- El starter dentro de una app Boot real (solo `ApplicationContextRunner`).
- Que `IsoOffsetFormatter` + `@DateTimeFormat` acepten todos los formatos de `generated-at`: solo probado `2024-01-01T19:20:30+01:00`.

## Decisiones mías pendientes de revisión
- D17 sin `verifactu.nif` (contrato lo citaba; nada lo consumía) · D18 keystore obligatorio con `enabled` · D19 un solo módulo, `Clock` por `ObjectProvider` · D20 README verificado línea a línea. Todas en `docs/DECISIONES.md`.
- README afirma fechas 1-ene-2027 (sociedades) y 1-jul-2027 (resto) por RDL 15/2025: tomadas de `docs/aeat-spec-notes.md`, no re-verificadas contra el BOE en esta sesión.

## Decisiones pendientes del usuario
- Nombre en `<developers>` del pom: puse "Jose Antonio Cid"; cambiar si quiere otro.
- Hacer los commits (propuesta de 3 en el cierre de la sesión C) y crear el repo remoto `jacidProgrammer/verifactu-java`.
- `S2`/`IDOtro` siguen fuera (D14).

## Siguientes pasos
1. Commits: ver propuesta del cierre; `git add` por rutas y `git status` después.
2. Si publica: cuenta en Central Portal, server `central` en `~/.m2/settings.xml`, clave GPG; `./mvnw -Prelease -Dgpg.skip=false deploy`. Quitar `-SNAPSHOT` antes.
3. Si llega un certificado: editar el NIF de `ReadmeExample.firstInvoice` y correr `SendToAeatTestEnvironment` (comando en su Javadoc).

## Ficheros tocados en la sesión C
- Nuevos: `verifactu-spring-boot-starter/**` (pom, `spring/VerifactuProperties`, `spring/VerifactuAutoConfiguration`, `META-INF/spring/*.imports`, `VerifactuAutoConfigurationTest`), `verifactu-core/src/test/.../ReadmeExample.java`, `client/ReadmeExampleTest.java`, `examples/SendToAeatTestEnvironment.java`.
- Modificados: `pom.xml` (módulo, scm, developers, pluginManagement, perfil `release`, `gpg.skip`), `README.md` (reescrito), `docs/DECISIONES.md` (D17-D20), `CLAUDE.md`, `.claude/settings.json`.

## Trampas
- `./mvnw -pl verifactu-spring-boot-starter test` falla sin `-am`: el core no está instalado en `.m2`.
- `assertThat(ctx.getStartupFailure()).rootCause()` ve la excepción del JDK, no la envolvente: usar `hasStackTraceContaining` para el nombre de la propiedad.
- Las de sesiones A/B (hook de `mvnw`, `getFirst`, proxy) siguen vigentes.
