package com.atlas.Psikosys.controller;

import com.atlas.Psikosys.entity.Role;
import com.atlas.Psikosys.entity.User;
import com.atlas.Psikosys.repository.RoleRepository;
import com.atlas.Psikosys.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model,
                        @RequestParam(value = "error", required = false) String error) {

        // Cookie'den dil bilgisini al
        String currentLang = getCurrentLanguageFromCookie(request);
        model.addAttribute("currentLang", currentLang);

        // Normal login hatası
        if ("true".equals(error)) {
            String errorMessage = "en".equals(currentLang)
                    ? "Invalid email or password."
                    : "Geçersiz e-posta veya şifre.";
            model.addAttribute("errorMessage", errorMessage);
        }

        return "login";
    }

    @GetMapping("/register")
    public String register(HttpServletRequest request, Model model) {
        String currentLang = getCurrentLanguageFromCookie(request);
        model.addAttribute("currentLang", currentLang);
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam("email") String email,
            @RequestParam("fullName") String fullName,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {

        String currentLang = getCurrentLanguageFromCookie(request);
        model.addAttribute("currentLang", currentLang);

        // Şifre eşleştirme kontrolü
        if (!password.equals(confirmPassword)) {
            String errorMessage = "en".equals(currentLang)
                    ? "Passwords do not match."
                    : "Şifreler eşleşmiyor.";
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("email", email);
            model.addAttribute("fullName", fullName);
            return "register";
        }

        // E-posta kontrolü
        if (userRepository.findByEmail(email).isPresent()) {
            String errorMessage = "en".equals(currentLang)
                    ? "An account with this email already exists."
                    : "Bu e-posta adresiyle zaten bir hesap mevcut.";
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("email", email);
            model.addAttribute("fullName", fullName);
            return "register";
        }

        try {
            // Varsayılan role'u al veya oluştur (USER)
            Role userRole = roleRepository.findByName("USER")
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setName("USER");
                        return roleRepository.save(newRole);
                    });

            // Yeni kullanıcı oluştur
            User newUser = User.builder()
                    .email(email)
                    .name(fullName.trim())
                    .password(passwordEncoder.encode(password))
                    .preferredLanguage(currentLang)
                    .role(userRole)
                    .messageLimit(50)
                    .limitResetDate(1)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            userRepository.save(newUser);

            // Başarı mesajı
            String successMessage = "en".equals(currentLang)
                    ? "Account created successfully! You can now log in."
                    : "Hesap başarıyla oluşturuldu! Artık giriş yapabilirsiniz.";
            redirectAttributes.addFlashAttribute("successMessage", successMessage);

            return "redirect:/login";

        } catch (Exception e) {
            String errorMessage = "en".equals(currentLang)
                    ? "An error occurred during registration. Please try again."
                    : "Kayıt sırasında bir hata oluştu. Lütfen tekrar deneyin.";
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("email", email);
            model.addAttribute("fullName", fullName);
            return "register";
        }
    }

    private String getCurrentLanguageFromCookie(HttpServletRequest request) {
        String currentLang = "tr"; // varsayılan
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("user_language".equals(cookie.getName())) {
                    currentLang = cookie.getValue();
                    break;
                }
            }
        }
        return currentLang;
    }
}