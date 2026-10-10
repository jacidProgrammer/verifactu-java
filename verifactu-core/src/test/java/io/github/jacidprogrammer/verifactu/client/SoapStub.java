package io.github.jacidprogrammer.verifactu.client;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Minimal SOAP endpoint on localhost: records every request and answers with a canned body and status. */
final class SoapStub implements AutoCloseable {

    record Received(Map<String, List<String>> headers, byte[] body) {
    }

    private final HttpServer server;
    final List<Received> received = new ArrayList<>();
    volatile int status = 200;
    volatile String body = "";

    SoapStub() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/soap", exchange -> {
            byte[] in = exchange.getRequestBody().readAllBytes();
            received.add(new Received(exchange.getRequestHeaders(), in));
            byte[] out = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/xml; charset=UTF-8");
            exchange.sendResponseHeaders(status, out.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(out);
            }
        });
        server.start();
    }

    URI endpoint() {
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/soap");
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
