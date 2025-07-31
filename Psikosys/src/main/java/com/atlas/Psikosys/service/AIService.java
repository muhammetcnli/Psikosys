package com.atlas.Psikosys.service;

import com.atlas.Psikosys.entity.Message;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
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
     * Returns HTML-formatted response from AI for a single message with specified personality.
     */
    public String getHtmlResponseWithPersonality(String question, String personalityPrompt) throws IOException {
        if (personalityPrompt == null || personalityPrompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Personality prompt cannot be null or empty");
        }

        System.out.println("=== AI SERVICE DEBUG ===");
        System.out.println("Question: " + question);
        System.out.println("Original prompt: " + personalityPrompt);

        // {message} placeholder'ını kontrol et
        if (!personalityPrompt.contains("{message}")) {
            System.out.println("UYARI: Prompt'ta {message} placeholder'ı yok!");
            personalityPrompt = personalityPrompt + "\n\nUser message: {message}";
        }

        Prompt prompt = new PromptTemplate(personalityPrompt).create(Map.of("message", question));
        String finalPrompt = prompt.toString();
        System.out.println("Final prompt sent to AI: " + finalPrompt);

        String response = callAIAndCleanResponse(finalPrompt);
        System.out.println("AI Response: " + response);

        return htmlService.markdownToHtml(response);
    }

    /**
     * Returns HTML-formatted response from AI based on message history and personality.
     */
    public String getHtmlResponseWithMessageHistoryAndPersonality(List<Message> messages, String newQuestion,
                                                                  String personalityPrompt, int maxMessages) throws IOException {
        if (personalityPrompt == null || personalityPrompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Personality prompt cannot be null or empty");
        }

        StringBuilder history = new StringBuilder(personalityPrompt).append("\n\n");

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
     * Generates a short title for a new chat.
     */
    public String generateChatTitle(String question) throws IOException {
        String titlePrompt = """
        You are a wise Jungian therapist. Create a concise (≤30 characters) chat title \
        summarizing this message. Only return the title, no extra explanation:
        \"%s\"""".formatted(question);
        Prompt prompt = new PromptTemplate(titlePrompt).create(Map.of());
        String title = callAIAndCleanResponse(prompt.toString());
        return title.length() > 30 ? title.substring(0, 30) : title;
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