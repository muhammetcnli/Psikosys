package com.atlas.Psikosys.controller;

import com.atlas.Psikosys.entity.Chat;
import com.atlas.Psikosys.entity.Message;
import com.atlas.Psikosys.entity.User;
import com.atlas.Psikosys.service.AIService;
import com.atlas.Psikosys.service.ChatService;
import com.atlas.Psikosys.service.PersonalityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class ChatViewController {

    private final ChatService chatService;
    private final AIService aiService;
    private final PersonalityService personalityService;

    @Autowired
    public ChatViewController(ChatService chatService, AIService aiService, PersonalityService personalityService) {
        this.chatService = chatService;
        this.aiService = aiService;
        this.personalityService = personalityService;
    }

    @GetMapping("/chat")
    public String chat(OAuth2AuthenticationToken token, Model model) {
        try {
            User user = chatService.getCurrentUser();

            // OAuth'dan kullanıcı bilgilerini al
            if (token != null) {

                System.out.println(token.getPrincipal().getAttributes());
                model.addAttribute("userName", token.getPrincipal().getAttributes().get("name"));
                model.addAttribute("userPhoto", token.getPrincipal().getAttribute("picture"));
            }

            model.addAttribute("userChats", user.getChats());
            model.addAttribute("personalities", personalityService.getAllPersonalities());
            return "chat";
        } catch (Exception e) {
            model.addAttribute("error", "No user info.");
            return "error";
        }
    }

    @PostMapping("/chat")
    public String createNewChat(
            @RequestParam("question") String question,
            @RequestParam(value = "personality", required = false) String personality) {

        System.out.println("=== YENİ CHAT OLUŞTURULUYOR ===");
        System.out.println("Gelen soru: " + question);
        System.out.println("Gelen personality: " + personality);

        Chat chat = chatService.createChat();

        // Kişiliği belirle - eğer seçilmemişse default "Jungian" kullan
        String selectedPersonality = (personality != null && !personality.isEmpty()) ? personality : "Jungian";
        System.out.println("Kullanılacak personality: " + selectedPersonality);

        // Chat'e personality'yi kaydet
        chatService.updateChatPersonality(chat.getId(), selectedPersonality);

        // Kullanıcı mesajını ekle
        chatService.addMessageToChat(chat.getId(), question, true);

        try {
            // Personality prompt'u al ve AI'dan cevap iste
            String personalityPrompt = personalityService.getPersonalityPrompt(selectedPersonality);
            System.out.println("Personality prompt: " + personalityPrompt);

            String response = aiService.getHtmlResponseWithPersonality(question, personalityPrompt);
            System.out.println("AI Yanıtı alındı, uzunluk: " + response.length());

            chatService.addMessageToChat(chat.getId(), response, false);
        } catch (Exception e) {
            System.out.println("AI Yanıt hatası: " + e.getMessage());
            chatService.addMessageToChat(chat.getId(), "Cannot get response: " + e.getMessage(), false);
        }

        return "redirect:/chat/" + chat.getId();
    }

    @GetMapping("/chat/{id}")
    public String viewChat(@PathVariable(value = "id") UUID id, OAuth2AuthenticationToken token, Model model) {
        try {
            User user = chatService.getCurrentUser();
            Chat chat = chatService.findChatByIdWithOrderedMessages(id);

            // Chat sahibinin kontrolü
            if (!chat.getUser().getId().equals(user.getId())) {
                return "redirect:/chat";
            }

            List<Message> messages = chatService.getMessagesByChatId(id);

            // OAuth'dan kullanıcı bilgilerini al
            if (token != null) {
                model.addAttribute("userName", token.getPrincipal().getAttributes().get("name"));
                model.addAttribute("userPhoto", token.getPrincipal().getAttribute("picture"));
            }

            model.addAttribute("userChats", user.getChats());
            model.addAttribute("chat", chat);
            model.addAttribute("chatId", id);
            model.addAttribute("messages", messages);
            model.addAttribute("personalities", personalityService.getAllPersonalities());
            model.addAttribute("selectedPersonality", chat.getSelectedPersonality());

            return "chat";
        } catch (Exception e) {
            model.addAttribute("error", "Chat bulunamadı veya erişim hatası.");
            return "redirect:/chat";
        }
    }

    @PostMapping("/chat/{id}")
    public String sendMessage(
            @PathVariable(value = "id") UUID id,
            @RequestParam("question") String question,
            @RequestParam(value = "personality", required = false) String personality) {
        try {
            // Mevcut personality'yi belirle
            String currentPersonality = personality;
            if (currentPersonality == null || currentPersonality.isEmpty()) {
                Chat currentChat = chatService.findChatById(id);
                currentPersonality = currentChat.getSelectedPersonality();
            }

            // Hala null ise default "Jungian" kullan
            if (currentPersonality == null || currentPersonality.isEmpty()) {
                currentPersonality = "Jungian";
            }

            // Personality'yi güncelle
            chatService.updateChatPersonality(id, currentPersonality);

            // Kullanıcı mesajını ekle
            chatService.addMessageToChat(id, question, true);

            // Mesaj geçmişini al
            List<Message> previousMessages = chatService.getMessagesByChatId(id);

            // AI'dan cevap al
            String personalityPrompt = personalityService.getPersonalityPrompt(currentPersonality);
            System.out.println("Selected personality: " + currentPersonality);
            System.out.println("Personality prompt: " + personalityPrompt);
            String response = aiService.getHtmlResponseWithMessageHistoryAndPersonality(
                    previousMessages, question, personalityPrompt, 10);

            chatService.addMessageToChat(id, response, false);

            return "redirect:/chat/" + id;
        } catch (Exception e) {
            chatService.addMessageToChat(id, "Yanıt alınamadı: " + e.getMessage(), false);
            return "redirect:/chat/" + id;
        }
    }
}