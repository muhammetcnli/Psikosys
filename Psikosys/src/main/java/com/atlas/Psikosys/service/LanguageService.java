package com.atlas.Psikosys.service;

import com.atlas.Psikosys.entity.User;
import com.atlas.Psikosys.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class LanguageService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatService chatService;

    public String getCurrentLanguage(HttpServletRequest request) {
        try {
            // Önce kullanıcının veritabanındaki dil tercihini kontrol et
            User currentUser = chatService.getCurrentUser();
            if (currentUser != null && currentUser.getPreferredLanguage() != null) {
                System.out.println("Veritabanından dil: " + currentUser.getPreferredLanguage());
                return currentUser.getPreferredLanguage();
            }
        } catch (Exception e) {
            System.out.println("Kullanıcı bulunamadı, cookie kontrol ediliyor: " + e.getMessage());
        }

        // Kullanıcı yoksa cookie'den al
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("user_language".equals(cookie.getName())) {
                    System.out.println("Cookie'den dil: " + cookie.getValue());
                    return cookie.getValue();
                }
            }
        }

        System.out.println("Varsayılan dil: tr");
        return "tr"; // Varsayılan
    }

    public void setLanguageCookie(HttpServletRequest request, String language) {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
        if (response != null) {
            Cookie cookie = new Cookie("user_language", language);
            cookie.setMaxAge(365 * 24 * 60 * 60); // 1 yıl
            cookie.setPath("/");
            cookie.setHttpOnly(false); // JavaScript'ten erişilebilir olması için
            response.addCookie(cookie);
        }
    }

    public void updateUserLanguage(String email, String language) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            user.setPreferredLanguage(language); // Düzeltildi
            userRepository.save(user);
            System.out.println("Kullanıcı dili güncellendi: " + email + " -> " + language);
        }
    }

    public void setLanguage(HttpServletRequest request, String language) {
        HttpSession session = request.getSession();
        session.setAttribute("language", language);
    }
}