package com.atlas.Psikosys.service;

import com.atlas.Psikosys.entity.Chat;
import com.atlas.Psikosys.entity.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    private final ChatClient chatClient;
    private final HtmlService htmlService;

    public AIService(ChatClient.Builder builder, HtmlService htmlService) {
        this.chatClient = builder.build();
        this.htmlService = htmlService;
    }

    /**
     * Returns AI response for a single message (used for initial messages).
     */
    public String getAIResponse(String question) throws IOException {
        String promptTemplate = getPersonaPrompt();
        Prompt prompt = new PromptTemplate(promptTemplate).create(Map.of("message", question));
        return callAIAndCleanResponse(prompt.toString());
    }

    /**
     * Returns HTML-formatted response from AI for a single message.
     */
    public String getHtmlResponse(String question) throws IOException {
        return htmlService.markdownToHtml(getAIResponse(question));
    }

    /**
     * Generates a short title for a new chat.
     */
    public String generateChatTitle(String question) throws IOException {
        String titlePrompt = """
        You are a wise Jungian therapist. Create a concise (≤30 characters) chat title \
        summarizing this message. Only return the title, no extra explanation:
        \"%s\"""".formatted(question);
        // promptTemplate yerine direkt şablon string gönderiyoruz
        Prompt prompt = new PromptTemplate(titlePrompt).create(Map.of());
        String title = callAIAndCleanResponse(prompt.toString());
        return title.length() > 30 ? title.substring(0, 30) : title;
    }

    /**
     * Returns HTML-formatted response from AI based on a given message list.
     */
    public String getHtmlResponseWithMessageHistory(List<Message> messages, String newQuestion, int maxMessages) throws IOException {
        StringBuilder history = new StringBuilder(getPersonaPrompt()).append("\n\n");

        if (messages != null && !messages.isEmpty()) {
            history.append("Recent conversation:\n");
            int startIndex = Math.max(0, messages.size() - maxMessages);

            for (int i = startIndex; i < messages.size(); i++) {
                Message message = messages.get(i);
                if (message != null && message.getContent() != null) {
                    history.append(message.getIsUser() ? "User: " : "Assistant: ")
                            .append(message.getContent()).append("\n");
                }
            }
        }

        history.append("User: ").append(newQuestion).append("\nAssistant: ");
        String response = callAIAndCleanResponse(history.toString());
        return htmlService.markdownToHtml(response);
    }

    /**
     * Loads and returns persona prompt from JSON, using default 'Jungian'.
     */
    private String getPersonaPrompt() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Resource resource = new ClassPathResource("personalities.json");

        if (!resource.exists()) {
            throw new IOException("File 'personalities.json' not found.");
        }

        Map<String, Map<String, String>> personalities = objectMapper.readValue(resource.getInputStream(), Map.class);
        String prompt = personalities.getOrDefault("Jungian", Map.of()).get("prompt");

        if (prompt == null || prompt.isBlank()) {
            throw new IOException("'Jungian' prompt is missing or empty in personalities.json");
        }

        return prompt;
    }


    /**
     * Calls the AI model and cleans the raw response.
     */
    private String callAIAndCleanResponse(String promptText) {
        return chatClient.prompt()
                .user(promptText)
                .call()
                .content()
                .replaceAll("(?s)<think>.*?</think>", "")
                .replaceAll("^\"(.*)\"$", "$1")
                .trim();
    }
}
