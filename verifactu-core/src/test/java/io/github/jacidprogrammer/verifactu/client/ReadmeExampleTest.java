package io.github.jacidprogrammer.verifactu.client;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jacidprogrammer.verifactu.ReadmeExample;
import io.github.jacidprogrammer.verifactu.xml.Submission;
import io.github.jacidprogrammer.verifactu.xml.VerifactuXml;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/** TASK criterion 8: the README example compiles ({@link ReadmeExample}) and runs. */
class ReadmeExampleTest {

    static final Path README = Path.of("..", "README.md");
    static final Path SOURCE = Path.of("src", "test", "java", "io", "github", "jacidprogrammer", "verifactu", "ReadmeExample.java");

    @Test
    void everyJavaLineOfTheReadmeIsInTheCompiledExample() throws IOException {
        List<String> source = Files.readAllLines(SOURCE).stream().map(String::strip).toList();
        List<String> snippetLines = javaSnippetLines(Files.readString(README));
        assertThat(snippetLines).as("README has java snippets").hasSizeGreaterThanOrEqualTo(10);
        assertThat(snippetLines).as("every README line must exist, stripped, in ReadmeExample.java").isSubsetOf(source);
    }

    @Test
    void theExampleBuildsAValidSubmissionAndTheStubAcceptsIt() throws Exception {
        Submission submission = ReadmeExample.firstInvoice();
        VerifactuXml.toDocument(submission); // validates against SuministroLR.xsd
        try (SoapStub stub = new SoapStub()) {
            stub.body = AeatClientTest.response("Correcto", "<sfR:CSV>CSV0001</sfR:CSV>",
                    AeatClientTest.line("2026/A-001", "Alta", "Correcto", ""));
            AeatClient client = new AeatClient(HttpClient.newHttpClient(), stub.endpoint(), Duration.ofSeconds(5));
            AeatResponse response = client.submit(submission);
            assertThat(response.status()).isEqualTo(SubmissionStatus.ACCEPTED);
            assertThat(response.csv()).contains("CSV0001");
            assertThat(new String(stub.received.get(0).body(), StandardCharsets.UTF_8)).contains("<sf:NumSerieFactura>2026/A-001</sf:NumSerieFactura>");
        }
    }

    static List<String> javaSnippetLines(String markdown) {
        Matcher m = Pattern.compile("```java\\n(.*?)```", Pattern.DOTALL).matcher(markdown);
        List<String> lines = new ArrayList<>();
        while (m.find()) {
            for (String line : m.group(1).split("\n")) {
                if (!line.isBlank()) {
                    lines.add(line.strip());
                }
            }
        }
        return lines;
    }
}
