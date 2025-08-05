package com.atlas.Psikosys.service;

import com.atlas.Psikosys.entity.User;
import com.atlas.Psikosys.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");

        // Cookie'den dil tercihini al
        String preferredLanguage = getCurrentLanguageFromCookie();

        System.out.println("OAuth2 giriş - Email: " + email);
        System.out.println("OAuth2 giriş - Cookie'deki dil: " + preferredLanguage);

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // Yeni kullanıcı oluştur - cookie'deki dili kullan
            user = User.builder()
                    .email(email)
                    .name(name)
                    .picture(picture)
                    .preferredLanguage(preferredLanguage) // Doğru field adı
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            userRepository.save(user);
            System.out.println("✅ Yeni kullanıcı oluşturuldu: " + email + " - Dil: " + preferredLanguage);
        } else {
            // Mevcut kullanıcı - sadece profil bilgilerini güncelle, dili KORUR
            boolean updated = false;

            if (name != null && !name.equals(user.getName())) {
                user.setName(name);
                updated = true;
            }

            if (picture != null && !picture.equals(user.getPicture())) {
                user.setPicture(picture);
                updated = true;
            }

            if (updated) {
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                System.out.println("✅ Mevcut kullanıcı güncellendi: " + email + " - Dil korundu: " + user.getPreferredLanguage());
            } else {
                System.out.println("✅ Mevcut kullanıcı giriş yaptı: " + email + " - Dil: " + user.getPreferredLanguage());
            }
        }

        return oauth2User;
    }

    // Cookie'den mevcut dil tercihini al
    private String getCurrentLanguageFromCookie() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("user_language".equals(cookie.getName())) {
                        String language = cookie.getValue();
                        System.out.println("🍪 Cookie'den dil bulundu: " + language);
                        return language;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Cookie okuma hatası: " + e.getMessage());
        }

        System.out.println("⚠️ Cookie'de dil bulunamadı, varsayılan 'tr' kullanılıyor");
        return "tr"; // Varsayılan dil
    }
}