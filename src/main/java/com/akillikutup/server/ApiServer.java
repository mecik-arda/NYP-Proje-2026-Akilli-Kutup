package com.akillikutup.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class ApiServer {

    private HttpServer server;

    public void startServer(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext("/api/status", new StatusHandler());

            server.setExecutor(null);
            server.start();
            System.out.println("API Sunucusu baslatildi: http://localhost:" + port);
        } catch (IOException e) {
            System.err.println("Sunucu baslatilamadi: " + e.getMessage());
        }
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("API Sunucusu durduruldu.");
        }
    }

    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "{\"status\":\"UP\"}";
            t.getResponseHeaders().set("Content-Type", "application/json");
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
