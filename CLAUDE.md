# verifactu-java

Librería Java para Verifactu (sistema de registros de facturación de la AEAT). Repo personal de portfolio, pensado para publicarse en Maven Central.

## Stack
- Java 21 (compilado con `maven.compiler.release=21`; en esta máquina hay JDK 25 Temurin), Maven 3.9 vía `./mvnw`.
- Multi-módulo: `verifactu-core` (sin Spring ni frameworks) y `verifactu-spring-boot-starter` (Boot 4.1.1, BOM importado solo en ese módulo; `proc=full` para los metadatos de propiedades).
- Tests: JUnit 5 + AssertJ. Sin Testcontainers en el core.
- GroupId `io.github.jacidprogrammer` (namespace de Maven Central por GitHub).

## Comandos
- Build completo con tests: `./mvnw -q verify`
- Solo tests: `./mvnw -q test`
- Un fichero: `./mvnw -q -pl verifactu-core test -Dtest=SmokeTest` (el starter necesita `-am`: `./mvnw -q -pl verifactu-spring-boot-starter -am test`)
- Artefactos de release (sources+javadoc, GPG saltado): `./mvnw -q -DskipTests package -Prelease`
- Sin tests: `./mvnw -q -DskipTests package`
- No hay servidor de desarrollo.

## Verificación de una entrega
- `./mvnw verify` verde y leer la línea `Tests run: N, Failures: 0, Errors: 0, Skipped: 0`; la cifra va en el cierre.
- Cada regla de la especificación AEAT que se implemente se cita con documento y versión en el test que la cubre.
- Hash, QR y firma se comprueban contra vectores de prueba conocidos, no solo contra la propia implementación.
- Cualquier llamada al entorno de pruebas de la AEAT requiere certificado del usuario; si no se ha hecho, se declara en "Qué NO se ha verificado".

## No tocar
- Certificados y claves (`*.p12`, `*.pfx`, `*.jks`, `*.pem`, `.env`): nunca en el repo ni en ficheros de la app.
- No se añaden módulos ni dependencias fuera de la lista del brief (`.claude/TASK.md`).

## Convenciones
- Código, Javadoc, README y commits en inglés. Conversación en español.
- Commits sin atribución a Claude ni Co-Authored-By.
- `git add` con rutas explícitas y `git status` después.
- Trampas del stack: @.claude/rules/java-spring.md
- Memoria del proyecto (decisiones, estado): `docs/claude-memoria.md` cuando exista.
