package io.github.jacidprogrammer.verifactu.examples;

import io.github.jacidprogrammer.verifactu.ReadmeExample;
import io.github.jacidprogrammer.verifactu.client.AeatResponse;
import io.github.jacidprogrammer.verifactu.xml.Submission;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

/**
 * Sends the README example to the AEAT test environment ({@code prewww1.aeat.es}) with a real certificate. The
 * library maintainers have no certificate, so this has never been run; it exists so that whoever has one can try it:
 *
 * <pre>
 * ./mvnw -q -DskipTests install
 * ./mvnw -q -pl verifactu-core dependency:build-classpath -Dmdep.outputFile=target/cp.txt
 * java -cp "verifactu-core/target/classes:verifactu-core/target/test-classes:$(cat verifactu-core/target/cp.txt)" \
 *     io.github.jacidprogrammer.verifactu.examples.SendToAeatTestEnvironment /path/to/certificate.p12 'password'
 * </pre>
 *
 * The example registers invoice {@code 2026/A-001} of NIF {@code 89890001K}: edit {@link ReadmeExample#firstInvoice}
 * to use your own NIF first, otherwise the AEAT rejects the message because the certificate does not match the issuer.
 */
public final class SendToAeatTestEnvironment {

    private SendToAeatTestEnvironment() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("usage: SendToAeatTestEnvironment <keystore.p12> <password>");
            System.exit(2);
        }
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream in = Files.newInputStream(Path.of(args[0]))) {
            keyStore.load(in, args[1].toCharArray());
        }
        Submission submission = ReadmeExample.firstInvoice();
        AeatResponse response = ReadmeExample.send(submission, keyStore, args[1].toCharArray());
        response.lines().forEach(line -> System.out.println("  " + line));
        System.out.println("wait " + response.waitSeconds() + " s before the next submission");
    }
}
