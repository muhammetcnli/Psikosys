package com.atlas.Psikosys.controller;

import com.atlas.Psikosys.entity.Chat;
import com.atlas.Psikosys.entity.Message;
import com.atlas.Psikosys.entity.User;
import com.atlas.Psikosys.service.AIService;
import com.atlas.Psikosys.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
public class ChatViewController {

    private final ChatService chatService;
    private final AIService aiService;

    @Autowired
    public ChatViewController(ChatService chatService, AIService aiService) {
        this.chatService = chatService;
        this.aiService = aiService;
    }

    @GetMapping("/chat")
    public String chat(Model model) {
        try {
            // Get current user
            User user = chatService.getCurrentUser();

            // Add current user to model
            model.addAttribute("userChats", user.getChats());
            return "chat";
        } catch (Exception e) {
            model.addAttribute("error", "No user info.");
            return "error";
        }
    }

    @PostMapping("/chat")
    public String createNewChat(@RequestParam("question") String question) {
        // create chat with question
        Chat chat = chatService.createChat();

        // Add user message
        chatService.addMessageToChat(chat.getId(), question, true);

        try {
            // İlk mesaj olduğu için sadece soruyu gönder
            String response = aiService.getHtmlResponse(question);
            chatService.addMessageToChat(chat.getId(), response, false);
        } catch (Exception e) {
            chatService.addMessageToChat(chat.getId(), "Cannot get response: " + e.getMessage(), false);
        }

        // Redirect to new chat page
        return "redirect:/chat/" + chat.getId();
    }

    @GetMapping("/chat/{id}")
    public String getChatById(@PathVariable(value = "id") UUID id, Model model) {
        // Add chat ID to model, necessary for form
        model.addAttribute("chatId", id);

        try {
            // Find current chat
            Chat chat = chatService.findChatByIdWithOrderedMessages(id);
            model.addAttribute("chat", chat);
            model.addAttribute("messages", chat.getMessages());

            // Get current user
            User user = chatService.getCurrentUser();
            model.addAttribute("userChats", user.getChats());

            // Show current chat if there is not a question
            return "chat";
        } catch (Exception e) {
            model.addAttribute("error", "Chat could not be loaded: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/chat/{id}")
    public String sendMessage(@PathVariable(value = "id") UUID id,
                              @RequestParam("question") String question) {
        try {
            // Sadece mesaj listesini al, Chat entity'sini değil
            List<Message> previousMessages = chatService.getMessagesByChatId(id);
            System.out.println(previousMessages);

            // Add user message
            chatService.addMessageToChat(id, question, true);

            // AI'ya history ile birlikte soruyu gönder
            String response = aiService.getHtmlResponseWithMessageHistory(previousMessages, question, 10);
            chatService.addMessageToChat(id, response, false);

            // Redirect to clean URL
            return "redirect:/chat/" + id;
        } catch (Exception e) {
            chatService.addMessageToChat(id, "Yanıt alınamadı: " + e.getMessage(), false);
            return "redirect:/chat/" + id;
        }
    }
}