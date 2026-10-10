# Memoria del proyecto (para Claude)

- Estado 2026-10-10: 8/8 criterios de `.claude/TASK.md` hechos en tres sesiones; 94 tests verdes; sin commits ni remoto. Detalle en `.claude/HANDOFF.md`, decisiones D1-D20 en `docs/DECISIONES.md`.
- No verificado y por qué: envío real a la AEAT (sin certificado), deploy a Central (sin cuenta/GPG), starter en app Boot real.
- Cómo repetir la verificación: `./mvnw -q clean verify` y sumar `Tests run` de `*/target/surefire-reports/*.txt`; `./mvnw -q -DskipTests package -Prelease` debe dejar 6 jars.
