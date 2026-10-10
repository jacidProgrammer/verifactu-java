# verifactu-java

[![CI](https://github.com/jacidProgrammer/verifactu-java/actions/workflows/ci.yml/badge.svg)](https://github.com/jacidProgrammer/verifactu-java/actions/workflows/ci.yml)
[![License: Apache-2.0](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)

Java 21 library for **Verifactu**, the Spanish tax agency (AEAT) invoicing-record system. Under RD 1007/2023 as
amended by RDL 15/2025 it becomes mandatory for companies (corporate income tax payers) on **1 January 2027** and for
self-employed workers and everybody else on **1 July 2027**. Voluntary submissions are accepted since 2025.

What it does, with no framework dependency in the core:

- Billing records (`RegistroAlta`, `RegistroAnulacion`) with the SHA-256 **hash chain**, verified against the three
  official AEAT vectors (hash spec v0.1.2).
- The **verification QR** (QR spec v0.5.0): URL byte-identical to the official example, PNG via ZXing.
- `RegFactuSistemaFacturacion` **XML** validated against the bundled AEAT XSD v1.0 (no JAXB, DOM only).
- A **SOAP client** with mutual TLS over the JDK `HttpClient`, parsing `EstadoEnvio`, `EstadoRegistro`,
  `TiempoEsperaEnvio` and `RegistroDuplicado`, capped at the 1,000 records per message the AEAT allows.
- A **Spring Boot 4.1 starter** that wires a `RecordChain` and an `AeatClient` from `verifactu.*` properties.

> **Not yet exercised against the AEAT test environment.** That requires a taxpayer certificate the maintainers do
> not have. The SOAP client is tested against a local stub whose responses validate against `RespuestaSuministro.xsd`.
> `SendToAeatTestEnvironment` (test sources) is a ready-made `main` for whoever has a certificate.

Status: v0.1.0-SNAPSHOT, not yet on Maven Central (the `release` profile is prepared, nothing is signed or published).

## Quick start

Coordinates once released: `io.github.jacidprogrammer:verifactu-core:0.1.0`.

Register the first invoice of a new invoicing system, build its QR and the message for the AEAT
(this is `ReadmeExample.firstInvoice()`, compiled and run by `ReadmeExampleTest`):

```java
RecordChain chain = RecordChain.start(Clock.systemDefaultZone());
Party issuer = new Party("Empresa Ejemplo SL", "89890001K");
InvoiceId invoiceId = new InvoiceId(issuer.taxId(), "2026/A-001", LocalDate.of(2026, 1, 15));
RegistrationRecord record = chain.register(invoiceId, InvoiceType.F1, new BigDecimal("21.00"), new BigDecimal("121.00"));
InvoiceContent content = InvoiceContent.of("Consulting services", new Party("Cliente Ejemplo", "12345678Z"),
        TaxLine.vat(new BigDecimal("21"), new BigDecimal("100.00"), new BigDecimal("21.00")));
InvoicingSystem system = new InvoicingSystem(new Party("Software Factory SA", "A12345674"),
        "my-erp", "01", "1.0", "INST-0001", false, true, false);
byte[] qrPng = QrCode.of(VerificationUrl.verifiable(AeatEnvironment.TESTING, invoiceId, record.totalAmount()), 300).toPng();
Submission submission = new Submission(issuer, system, List.of(Submission.registration(record, content)));
```

Send it with the taxpayer's PKCS12 certificate (`ReadmeExample.send()`):

```java
AeatClient client = AeatClient.withCertificate(AeatEnvironment.TESTING, keyStore, keyPassword);
AeatResponse response = client.submit(submission);
System.out.println(response.status() + " CSV=" + response.csv().orElse("-") + " rejected=" + response.rejected());
```

`response.waitSeconds()` is the `TiempoEsperaEnvio` the AEAT asks you to respect before the next message; the library
exposes it and leaves scheduling, retries and persistence to you. Persist `chain.head()` and the record's
`generatedAt()` after each append and continue later with `RecordChain.resume(...)`. `RecordChain` is not thread-safe:
serialize appends with the lock of whatever stores the records.

### Spring Boot 4.1

```xml
<dependency>
    <groupId>io.github.jacidprogrammer</groupId>
    <artifactId>verifactu-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

```properties
verifactu.enabled=true                      # without it, no bean is created
verifactu.environment=testing               # or production
verifactu.certificate-type=certificate      # or seal (electronic seal certificate)
verifactu.key-store.path=/etc/myapp/cert.p12
verifactu.key-store.password=${CERT_PASSWORD}
verifactu.key-store.key-password=${CERT_PASSWORD}   # optional, defaults to the store password
# Resume an existing chain (all five or none):
verifactu.chain.head.issuer-tax-id=89890001K
verifactu.chain.head.series-number=12345678/G33
verifactu.chain.head.issue-date=2024-01-01
verifactu.chain.head.hash=3C464DAF61ACB827C65FDA19F352A4E3BDC2C640E9E9FC4CC058073F38F12F60
verifactu.chain.head.generated-at=2024-01-01T19:20:30+01:00
```

With `verifactu.enabled=true` the starter registers one `RecordChain` and one `AeatClient`; a `Clock` bean in your
application, if any, stamps the records. A missing key store or an incomplete `chain.head` fails startup instead of
silently creating something else.

## Scope of v0.1

Only the mandatory fields of registration and cancellation records; VAT in the general regime (`S1`, `N1`, `N2`,
exemptions `E1`-`E8`); Spanish recipients identified by NIF. Not modelled (rejected explicitly, never faked):
reverse charge `S2`, other taxes and regimes, foreign recipients (`IDOtro`), corrective invoices, event records,
`ConsultaLR`, XAdES signature (non-Verifactu mode), automatic retries.

## Build and test

Requires JDK 21 or newer; Maven comes with the wrapper.

```bash
git clone https://github.com/jacidProgrammer/verifactu-java.git
cd verifactu-java
./mvnw verify
```

`verify` compiles both modules and runs the whole suite (94 tests: 84 in `verifactu-core`, 10 in the starter). It ends
with one `Tests run: N, Failures: 0, Errors: 0, Skipped: 0` line per module. The same command runs in
[GitHub Actions](https://github.com/jacidProgrammer/verifactu-java/actions/workflows/ci.yml) on every push.

To run a single test class:

```bash
./mvnw -q -pl verifactu-core test -Dtest=OfficialHashVectorsTest
```

Release artifacts (sources, javadoc; GPG signing only with `-Dgpg.skip=false`):

```bash
./mvnw -q -DskipTests package -Prelease
```

## Sources

AEAT technical documentation (hash v0.1.2, QR v0.5.0, SWeb v1.0.3, XSD v1.0, WSDL v1.0) and RD 1007/2023;
see `docs/aeat-spec-notes.md`.

License: Apache-2.0.
