package com.akillikutup.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ApiServer {

    private HttpServer server;

    public void startServer(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);

            // API Endpoints
            server.createContext("/api/status", new StatusHandler());
            server.createContext("/api/kitaplar", new JsonFileHandler("data/materials.json"));
            server.createContext("/api/kullanicilar", new JsonFileHandler("data/users.json"));
            server.createContext("/api/chat", new ChatHandler());

            // Frontend Static Files (catch-all for '/')
            server.createContext("/", new StaticFileHandler("frontend"));

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
            byte[] responseBytes = response.getBytes("UTF-8");
            t.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = t.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }

    static class JsonFileHandler implements HttpHandler {
        private final String filePath;
        public JsonFileHandler(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            Path path = Paths.get(filePath);
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");

            if (Files.exists(path)) {
                byte[] data = Files.readAllBytes(path);
                t.sendResponseHeaders(200, data.length);
                try (OutputStream os = t.getResponseBody()) {
                    os.write(data);
                }
            } else {
                String error = "[]"; // Empty array if not found
                byte[] errorBytes = error.getBytes("UTF-8");
                t.sendResponseHeaders(200, errorBytes.length);
                try (OutputStream os = t.getResponseBody()) {
                    os.write(errorBytes);
                }
            }
        }
    }

    static class StaticFileHandler implements HttpHandler {
        private final String baseDir;

        public StaticFileHandler(String baseDir) {
            this.baseDir = baseDir;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            String requestURI = t.getRequestURI().getPath();
            
            // Default to index.html if root is requested
            if (requestURI.equals("/")) {
                requestURI = "/index.html";
            }

            Path path = Paths.get(baseDir, requestURI);

            if (!Files.exists(path) || Files.isDirectory(path)) {
                // If not found, try to redirect to dashboard.html or index.html to allow SPA handling
                path = Paths.get(baseDir, "/index.html");
                if(!Files.exists(path)){
                     String response = "404 (Not Found)\n";
                     t.sendResponseHeaders(404, response.length());
                     try(OutputStream os = t.getResponseBody()){ os.write(response.getBytes()); }
                     return;
                }
            }

            String contentType = getContentType(path.toString());
            t.getResponseHeaders().set("Content-Type", contentType);
            
            byte[] fileBytes = Files.readAllBytes(path);
            t.sendResponseHeaders(200, fileBytes.length);
            try (OutputStream os = t.getResponseBody()) {
                os.write(fileBytes);
            }
        }

        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=utf-8";
            if (path.endsWith(".css")) return "text/css; charset=utf-8";
            if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
            if (path.endsWith(".json")) return "application/json; charset=utf-8";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            if (path.endsWith(".svg")) return "image/svg+xml; charset=utf-8";
            return "text/plain; charset=utf-8";
        }
    }

    static class ChatHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                String requestBody = new String(t.getRequestBody().readAllBytes(), "UTF-8");
                String prompt = "";
                String keyStr = "\"prompt\":\"";
                int start = requestBody.indexOf(keyStr);
                if (start != -1) {
                    start += keyStr.length();
                    int end = requestBody.indexOf("\"", start);
                    if (end != -1) {
                        prompt = requestBody.substring(start, end);
                    }
                }

                String aiResponse = "";
                if (!prompt.isEmpty()) {
                    aiResponse = GeminiClient.askQuestion(prompt);
                } else {
                    aiResponse = "Lutfen bir soru girin.";
                }

                String escapedResponse = aiResponse.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
                String jsonResponse = "{\"response\": \"" + escapedResponse + "\"}";

                byte[] responseBytes = jsonResponse.getBytes("UTF-8");
                t.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                t.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = t.getResponseBody()) {
                    os.write(responseBytes);
                }
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }
}
