package com.atlas.Psikosys.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/debug/token")
    public Map<String, Object> debugToken(OAuth2AuthenticationToken token) {
        Map<String, Object> tokenInfo = new HashMap<>();

        tokenInfo.put("name", token.getName());
        tokenInfo.put("authorities", token.getAuthorities());
        tokenInfo.put("authenticated", token.isAuthenticated());
        tokenInfo.put("details", token.getDetails());
        tokenInfo.put("authorizedClientRegistrationId", token.getAuthorizedClientRegistrationId());

        Map<String, Object> attributes = new HashMap<>(token.getPrincipal().getAttributes());
        tokenInfo.put("principal_attributes", attributes);
        tokenInfo.put("principal_name", token.getPrincipal().getName());

        return tokenInfo;
    }

    @GetMapping("/ai/generate")
    public Map<String, String> generate(@RequestParam(value = "message", defaultValue = "Introduce yourself") String message) {
        try {
            // JSON içeriğini classpath'ten oku
            ObjectMapper objectMapper = new ObjectMapper();
            Resource resource = new ClassPathResource("personalities.json");

            if (!resource.exists()) {
                throw new IOException("File 'personalities.json' not found.");
            }

            Map<String, Map<String, String>> personalities = objectMapper.readValue(resource.getInputStream(), Map.class);

            // Jungian prompt'u al
            String jungianPrompt = personalities.getOrDefault("Jungian", Map.of()).get("prompt");

            if (jungianPrompt == null || jungianPrompt.isBlank()) {
                throw new IOException("'Jungian' prompt is missing or empty in personalities.json");
            }

            // Prompt oluştur ve AI çağır
            PromptTemplate promptTemplate = new PromptTemplate(jungianPrompt);
            Prompt prompt = promptTemplate.create(Map.of("message", message));
            String promptText = prompt.toString();

            String response = chatClient.prompt().user(promptText).call().content();

            return Map.of("generation", response);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    // ToDo: add personality to generateStream
    @GetMapping("/ai/generateStream")
    public Flux<String> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return chatClient.prompt().user(message).stream().content();
    }
}
