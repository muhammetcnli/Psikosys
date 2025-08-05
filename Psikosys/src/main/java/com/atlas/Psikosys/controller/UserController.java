package com.atlas.Psikosys.controller;

import com.atlas.Psikosys.entity.User;
import com.atlas.Psikosys.repository.UserRepository;
import com.atlas.Psikosys.service.LanguageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LanguageService languageService;

    @GetMapping
    public String profile(Authentication authentication, Model model, HttpServletRequest request,
                          @RequestParam(value = "showPasswordForm", required = false) Boolean showPasswordForm) {
        try {
            User user = getCurrentUser(authentication);
            String currentLanguage = languageService.getCurrentLanguage(request);

            // Kullanıcı bilgilerini modele ekle
            model.addAttribute("name", user.getName());
            model.addAttribute("email", user.getEmail());
            model.addAttribute("hasPassword", user.getPassword() != null);
            model.addAttribute("currentLang", currentLanguage);
            model.addAttribute("showPasswordForm", showPasswordForm != null && showPasswordForm);

            // OAuth2 kullanıcısı için profil fotoğrafını al
            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2User oauth2User = ((OAuth2AuthenticationToken) authentication).getPrincipal();
                String photo = oauth2User.getAttribute("picture");
                model.addAttribute("photo", photo);
            } else {
                model.addAttribute("photo", user.getPicture());
            }

            return "user-profile";
        } catch (Exception e) {
            return "redirect:/chat?error=profile_access_failed";
        }
    }

    @PostMapping("/language")
    public String updateLanguage(@RequestParam("language") String language,
                                 Authentication authentication,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(authentication);
            user.setPreferredLanguage(language);
            userRepository.save(user);

            // Cookie'yi güncelle
            languageService.setLanguageCookie(request, language);

            String successMessage = "en".equals(language)
                    ? "Language updated successfully!"
                    : "Dil başarıyla güncellendi!";
            redirectAttributes.addFlashAttribute("success", successMessage);

        } catch (Exception e) {
            String currentLanguage = languageService.getCurrentLanguage(request);
            String errorMessage = "en".equals(currentLanguage)
                    ? "Error updating language. Please try again."
                    : "Dil güncellenirken hata oluştu. Lütfen tekrar deneyin.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
        }

        return "redirect:/profile";
    }

    @PostMapping("/password")
    public String updatePassword(@RequestParam(value = "currentPassword", required = false) String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Authentication authentication,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(authentication);
            String currentLanguage = languageService.getCurrentLanguage(request);

            // Şifre eşleştirme kontrolü
            if (!newPassword.equals(confirmPassword)) {
                String errorMessage = "en".equals(currentLanguage)
                        ? "Passwords do not match."
                        : "Şifreler eşleşmiyor.";
                redirectAttributes.addFlashAttribute("error", errorMessage);
                return "redirect:/profile?showPasswordForm=true";
            }

            // Şifre uzunluk kontrolü
            if (newPassword.length() < 6) {
                String errorMessage = "en".equals(currentLanguage)
                        ? "Password must be at least 6 characters long."
                        : "Şifre en az 6 karakter olmalıdır.";
                redirectAttributes.addFlashAttribute("error", errorMessage);
                return "redirect:/profile?showPasswordForm=true";
            }

            // Mevcut şifre kontrolü (şifre varsa)
            if (user.getPassword() != null) {
                if (currentPassword == null || currentPassword.isEmpty()) {
                    String errorMessage = "en".equals(currentLanguage)
                            ? "Current password is required."
                            : "Mevcut şifre gereklidir.";
                    redirectAttributes.addFlashAttribute("error", errorMessage);
                    return "redirect:/profile?showPasswordForm=true";
                }

                if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                    String errorMessage = "en".equals(currentLanguage)
                            ? "Current password is incorrect."
                            : "Mevcut şifre yanlış.";
                    redirectAttributes.addFlashAttribute("error", errorMessage);
                    return "redirect:/profile?showPasswordForm=true";
                }
            }

            // Yeni şifreyi kaydet
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            String successMessage = "en".equals(currentLanguage)
                    ? "Password updated successfully!"
                    : "Şifre başarıyla güncellendi!";
            redirectAttributes.addFlashAttribute("success", successMessage);

        } catch (Exception e) {
            String currentLanguage = languageService.getCurrentLanguage(request);
            String errorMessage = "en".equals(currentLanguage)
                    ? "Error updating password. Please try again."
                    : "Şifre güncellenirken hata oluştu. Lütfen tekrar deneyin.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
        }

        return "redirect:/profile";
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