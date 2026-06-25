package com.akillikutup.chat;

import com.akillikutup.chat.dto.ChatRequest;
import com.akillikutup.chat.dto.ChatResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStreamReader;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final RagService ragService;

    public ChatController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(jakarta.servlet.http.HttpServletRequest request) {
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            com.google.gson.JsonObject body = gson.fromJson(
                new InputStreamReader(request.getInputStream(), "UTF-8"),
                com.google.gson.JsonObject.class);
            if (body == null) body = new com.google.gson.JsonObject();
            String prompt = body.has("prompt") ? body.get("prompt").getAsString() : "";
            boolean useRag = body.has("useRag") && body.get("useRag").getAsBoolean();
            String aiResponse;
            if (prompt.isEmpty()) {
                aiResponse = "Lutfen bir soru girin.";
            } else if (useRag) {
                aiResponse = ragService.askWithContext(prompt);
            } else {
                aiResponse = GeminiClient.askQuestion(prompt, null);
            }
            ChatResponse resp = new ChatResponse();
            resp.setResponse(aiResponse);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            ChatResponse resp = new ChatResponse();
            resp.setResponse("AI hatasi.");
            return ResponseEntity.internalServerError().body(resp);
        }
    }
}
