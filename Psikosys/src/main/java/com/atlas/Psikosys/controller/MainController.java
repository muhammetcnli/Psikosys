package com.atlas.Psikosys.controller;

import com.atlas.Psikosys.entity.User;
import com.atlas.Psikosys.service.ChatService;
import com.atlas.Psikosys.service.LanguageService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MainController {

    private final LanguageService languageService;
    private final ChatService chatService;

    @Autowired
    public MainController(LanguageService languageService, ChatService chatService) {
        this.languageService = languageService;
        this.chatService = chatService;
    }

    @RequestMapping("/")
    public String index(Model model, HttpServletRequest request) {
        String currentLanguage = languageService.getCurrentLanguage(request);
        model.addAttribute("currentLang", currentLanguage);
        return "index";
    }

    @PostMapping("/change-language")
    @ResponseBody
    public ResponseEntity<String> changeLanguage(
            @RequestParam String language,
            HttpServletRequest request,
            HttpServletResponse response) {

        System.out.println("Dil değiştirme isteği: " + language);

        // Cookie'ye kaydet (her zaman çalışır)
        Cookie languageCookie = new Cookie("user_language", language);
        languageCookie.setMaxAge(365 * 24 * 60 * 60); // 1 yıl
        languageCookie.setPath("/");
        languageCookie.setHttpOnly(false);
        response.addCookie(languageCookie);

        System.out.println("Cookie oluşturuldu: user_language=" + language);

        // Kullanıcı giriş yaptıysa veritabanını güncelle (optional)
        try {
            User currentUser = chatService.getCurrentUser();
            if (currentUser != null) {
                languageService.updateUserLanguage(currentUser.getEmail(), language);
                System.out.println("Veritabanı güncellendi: " + currentUser.getEmail() + " -> " + language);
            }
        } catch (Exception e) {
            // Kullanıcı giriş yapmamış - sadece cookie kullan
            System.out.println("Kullanıcı giriş yapmamış, sadece cookie kaydedildi: " + e.getMessage());
        }

        return ResponseEntity.ok("Language updated to: " + language);
    }
}