package com.atlas.Psikosys.controller;

import com.atlas.Psikosys.entity.Chat;
import com.atlas.Psikosys.entity.Message;
import com.atlas.Psikosys.entity.User;
import com.atlas.Psikosys.repository.UserRepository;
import com.atlas.Psikosys.service.AIService;
import com.atlas.Psikosys.service.ChatService;
import com.atlas.Psikosys.service.LanguageService;
import com.atlas.Psikosys.service.PersonalityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
public class ChatViewController {

    private final ChatService chatService;
    private final AIService aiService;
    private final PersonalityService personalityService;
    private final LanguageService languageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public ChatViewController(ChatService chatService, AIService aiService,
                              PersonalityService personalityService, LanguageService languageService) {
        this.chatService = chatService;
        this.aiService = aiService;
        this.personalityService = personalityService;
        this.languageService = languageService;
    }

    @GetMapping("/chat")
    public String chat(Authentication authentication, Model model, HttpServletRequest request) {
        try {
            User user = getCurrentUser(authentication);
            String currentLanguage = languageService.getCurrentLanguage(request);

            // OAuth2 login durumunda kullanıcı bilgilerini al
            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
                OAuth2User oauth2User = oauth2Token.getPrincipal();
                model.addAttribute("userName", oauth2User.getAttribute("name"));
                model.addAttribute("userPhoto", oauth2User.getAttribute("picture"));
            } else {
                // Form login durumunda database'den kullanıcı bilgilerini al
                model.addAttribute("userName", user.getName());
                model.addAttribute("userPhoto", user.getPicture());
            }

            model.addAttribute("userChats", user.getChats());
            model.addAttribute("personalities", personalityService.getAllPersonalities(currentLanguage));
            model.addAttribute("currentLang", currentLanguage);
            return "chat";
        } catch (Exception e) {
            model.addAttribute("error", "No user info.");
            return "error";
        }
    }

    @GetMapping("/chat/{id}")
    public String viewChat(@PathVariable(value = "id") UUID id, Authentication authentication,
                           Model model, HttpServletRequest request) {
        try {
            User user = getCurrentUser(authentication);
            Chat chat = chatService.findChatByIdWithOrderedMessages(id);
            String currentLanguage = languageService.getCurrentLanguage(request);

            if (!chat.getUser().getId().equals(user.getId())) {
                return "redirect:/chat";
            }

            List<Message> messages = chatService.getMessagesByChatId(id);

            // OAuth2 login durumunda kullanıcı bilgilerini al
            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
                OAuth2User oauth2User = oauth2Token.getPrincipal();
                model.addAttribute("userName", oauth2User.getAttribute("name"));
                model.addAttribute("userPhoto", oauth2User.getAttribute("picture"));
            } else {
                // Form login durumunda database'den kullanıcı bilgilerini al
                model.addAttribute("userName", user.getName());
                model.addAttribute("userPhoto", user.getPicture());
            }

            model.addAttribute("userChats", user.getChats());
            model.addAttribute("chat", chat);
            model.addAttribute("chatId", id);
            model.addAttribute("messages", messages);
            model.addAttribute("personalities", personalityService.getAllPersonalities(currentLanguage));
            model.addAttribute("selectedPersonality", chat.getSelectedPersonality());
            model.addAttribute("currentLang", currentLanguage);

            return "chat";
        } catch (Exception e) {
            model.addAttribute("error", "Chat bulunamadı veya erişim hatası.");
            return "redirect:/chat";
        }
    }

    @PostMapping("/chat")
    public String createNewChat(
            @RequestParam("question") String question,
            @RequestParam(value = "personality", required = false) String personality,
            HttpServletRequest request) {

        String currentLanguage = languageService.getCurrentLanguage(request);
        Chat chat = chatService.createChat();

        String selectedPersonality = (personality != null && !personality.isEmpty()) ? personality : "Jungian";

        chatService.updateChatPersonality(chat.getId(), selectedPersonality);
        chatService.addMessageToChat(chat.getId(), question, true);

        try {
            String personalityPrompt = personalityService.getPersonalityPrompt(selectedPersonality, currentLanguage);
            String response = aiService.getHtmlResponseWithPersonality(question, personalityPrompt, currentLanguage);
            chatService.addMessageToChat(chat.getId(), response, false);
        } catch (Exception e) {
            String errorMessage = currentLanguage.equals("en") ?
                    "Cannot get response: " + e.getMessage() :
                    "Yanıt alınamadı: " + e.getMessage();
            chatService.addMessageToChat(chat.getId(), errorMessage, false);
        }

        return "redirect:/chat/" + chat.getId();
    }

    @PostMapping("/chat/{id}")
    public String sendMessage(
            @PathVariable(value = "id") UUID id,
            @RequestParam("question") String question,
            @RequestParam(value = "personality", required = false) String personality,
            HttpServletRequest request) {
        try {
            String currentLanguage = languageService.getCurrentLanguage(request);

            String currentPersonality = personality;
            if (currentPersonality == null || currentPersonality.isEmpty()) {
                Chat currentChat = chatService.findChatById(id);
                currentPersonality = currentChat.getSelectedPersonality();
            }

            if (currentPersonality == null || currentPersonality.isEmpty()) {
                currentPersonality = "Jungian";
            }

            chatService.updateChatPersonality(id, currentPersonality);
            chatService.addMessageToChat(id, question, true);

            List<Message> previousMessages = chatService.getMessagesByChatId(id);
            String personalityPrompt = personalityService.getPersonalityPrompt(currentPersonality, currentLanguage);

            String response = aiService.getHtmlResponseWithMessageHistoryAndPersonality(
                    previousMessages, question, personalityPrompt, 10, currentLanguage);

            chatService.addMessageToChat(id, response, false);

            return "redirect:/chat/" + id;
        } catch (Exception e) {
            String currentLanguage = languageService.getCurrentLanguage(request);
            String errorMessage = currentLanguage.equals("en") ?
                    "Cannot get response: " + e.getMessage() :
                    "Yanıt alınamadı: " + e.getMessage();
            chatService.addMessageToChat(id, errorMessage, false);
            return "redirect:/chat/" + id;
        }
    }

    @PostMapping("/chat/{id}/delete")
    public String deleteChat(@PathVariable(value = "id") UUID id, HttpServletRequest request) {
        try {
            chatService.deleteChat(id);
            return "redirect:/chat";
        } catch (Exception e) {
            return "redirect:/chat?error=delete_failed";
        }
    }

    private User getCurrentUser(Authentication authentication) {
        String email = null;

        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            email = oauth2User.getAttribute("email");
        } else if (authentication instanceof UsernamePasswordAuthenticationToken) {
            email = authentication.getName();
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}