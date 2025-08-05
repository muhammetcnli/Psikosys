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

    public String getHtmlResponseWithPersonality(String question, String personalityPrompt, String language) throws IOException {
        if (personalityPrompt == null || personalityPrompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Personality prompt cannot be null or empty");
        }

        System.out.println("=== AI SERVICE DEBUG ===");
        System.out.println("Question: " + question);
        System.out.println("Language: " + language);
        System.out.println("Original prompt: " + personalityPrompt);

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

    // Backward compatibility için
    public String getHtmlResponseWithPersonality(String question, String personalityPrompt) throws IOException {
        return getHtmlResponseWithPersonality(question, personalityPrompt, "tr");
    }

    public String getHtmlResponseWithMessageHistoryAndPersonality(List<Message> messages, String newQuestion,
                                                                  String personalityPrompt, int maxMessages, String language) throws IOException {
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

    // Backward compatibility için
    public String getHtmlResponseWithMessageHistoryAndPersonality(List<Message> messages, String newQuestion,
                                                                  String personalityPrompt, int maxMessages) throws IOException {
        return getHtmlResponseWithMessageHistoryAndPersonality(messages, newQuestion, personalityPrompt, maxMessages, "tr");
    }

    public String generateChatTitle(String question, String language) throws IOException {
        String titlePrompt = getTitlePromptByLanguage(language, question);
        String title = callAIAndCleanResponse(titlePrompt);
        return title.length() > 30 ? title.substring(0, 30) : title;
    }

    // Backward compatibility için
    public String generateChatTitle(String question) throws IOException {
        return generateChatTitle(question, "tr");
    }

    private String getTitlePromptByLanguage(String language, String question) {
        return switch (language) {
            case "en" -> String.format("""
                You are a wise psychologist therapist. Create a concise (≤30 characters) chat title \
                summarizing this message. Only return the title, no extra explanation:
                "%s" """, question);
            default -> String.format("""
                Sen bilge bir psikologsun. Bu mesajı özetleyen kısa (≤30 karakter) bir sohbet başlığı oluştur. \
                Sadece başlığı döndür, ekstra açıklama yapma:
                "%s" """, question);
        };
    }

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