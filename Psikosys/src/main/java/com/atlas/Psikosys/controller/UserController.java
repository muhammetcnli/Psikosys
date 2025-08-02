package com.atlas.Psikosys.controller;

import com.atlas.Psikosys.entity.User;
import com.atlas.Psikosys.service.ChatService;
import com.atlas.Psikosys.service.LanguageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private LanguageService languageService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping("/profile")
    public String profile(OAuth2AuthenticationToken token,
                          Model model,
                          HttpServletRequest request,
                          @RequestParam(value = "showPasswordForm", required = false) String showPasswordForm) {
        try {
            // Mevcut kullanıcı bilgilerini al
            User user = chatService.getCurrentUser();
            String currentLanguage = languageService.getCurrentLanguage(request);

            // OAuth2 token'dan gelen bilgileri güvenli şekilde ekle
            if (token != null && token.getPrincipal() != null) {
                Object name = token.getPrincipal().getAttributes().get("name");
                Object email = token.getPrincipal().getAttribute("email");
                Object photo = token.getPrincipal().getAttribute("picture");

                model.addAttribute("name", name != null ? name.toString() : "");
                model.addAttribute("email", email != null ? email.toString() : "");
                model.addAttribute("photo", photo != null ? photo.toString() : "");
            } else {
                // OAuth2 token yoksa (local user için)
                model.addAttribute("name", user.getFirstName() != null ? user.getLastName() : "");
                model.addAttribute("email", user.getEmail() != null ? user.getEmail() : "");
                model.addAttribute("photo", ""); // Local user'ların fotoğrafı yok
            }

            // Kullanıcının şifresi var mı kontrol et
            boolean hasPassword = user.getPassword() != null && !user.getPassword().isEmpty();
            model.addAttribute("hasPassword", hasPassword);

            // showPasswordForm parametresini kontrol et
            boolean showPasswordFormFlag = "true".equals(showPasswordForm);
            model.addAttribute("showPasswordForm", showPasswordFormFlag);

            // Mevcut dil bilgisini ekle
            model.addAttribute("currentLang", currentLanguage);

            // Debug bilgisi (production'da kaldırılacak)
            System.out.println("Debug - hasPassword: " + hasPassword);
            System.out.println("Debug - showPasswordForm: " + showPasswordFormFlag);

            return "user-profile";
        } catch (RuntimeException e) {
            String errorMessage = languageService.getCurrentLanguage(request).equals("en") ?
                    "Error loading profile information." :
                    "Profil bilgileri yüklenirken hata oluştu.";
            model.addAttribute("error", errorMessage);
            return "redirect:/chat";
        }
    }

    @PostMapping("/profile/language")
    public String updateLanguage(@RequestParam("language") String language,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Geçerli dil kontrolü
            if (!isValidLanguage(language)) {
                String errorMessage = languageService.getCurrentLanguage(request).equals("en") ?
                        "Invalid language selection." :
                        "Geçersiz dil seçimi.";
                redirectAttributes.addFlashAttribute("error", errorMessage);
                return "redirect:/profile";
            }

            User user = chatService.getCurrentUser();
            user.setPreferredLanguage(language);
            chatService.saveUser(user);

            // Session'da dil bilgisini güncelle
            languageService.setLanguage(request, language);

            String successMessage = language.equals("en") ?
                    "Language updated successfully!" :
                    "Dil başarıyla güncellendi!";
            redirectAttributes.addFlashAttribute("success", successMessage);
        } catch (RuntimeException e) {
            String errorMessage = languageService.getCurrentLanguage(request).equals("en") ?
                    "Failed to update language." :
                    "Dil güncellenirken hata oluştu.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
        }

        return "redirect:/profile";
    }

    @PostMapping("/profile/password")
    public String updatePassword(@RequestParam(value = "currentPassword", required = false) String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = chatService.getCurrentUser();
            String currentLang = languageService.getCurrentLanguage(request);

            // Şifre onayı kontrolü
            if (!newPassword.equals(confirmPassword)) {
                String errorMessage = currentLang.equals("en") ?
                        "Passwords do not match." :
                        "Şifreler eşleşmiyor.";
                redirectAttributes.addFlashAttribute("error", errorMessage);
                // showPasswordForm parametresini koruyalım
                return "redirect:/profile?showPasswordForm=true";
            }

            // Minimum uzunluk kontrolü
            if (newPassword.length() < 6) {
                String errorMessage = currentLang.equals("en") ?
                        "Password must be at least 6 characters long." :
                        "Şifre en az 6 karakter olmalıdır.";
                redirectAttributes.addFlashAttribute("error", errorMessage);
                return "redirect:/profile?showPasswordForm=true";
            }

            // Şifre güçlülük kontrolü
            if (!isPasswordStrong(newPassword)) {
                String errorMessage = currentLang.equals("en") ?
                        "Password must contain at least one letter and one number." :
                        "Şifre en az bir harf ve bir rakam içermelidir.";
                redirectAttributes.addFlashAttribute("error", errorMessage);
                return "redirect:/profile?showPasswordForm=true";
            }

            // Mevcut şifre varsa doğrulama
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                if (currentPassword == null || currentPassword.isEmpty()) {
                    String errorMessage = currentLang.equals("en") ?
                            "Current password is required." :
                            "Mevcut şifre gereklidir.";
                    redirectAttributes.addFlashAttribute("error", errorMessage);
                    return "redirect:/profile?showPasswordForm=true";
                }

                // BCrypt ile şifre doğrulama
                if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                    String errorMessage = currentLang.equals("en") ?
                            "Current password is incorrect." :
                            "Mevcut şifre yanlış.";
                    redirectAttributes.addFlashAttribute("error", errorMessage);
                    return "redirect:/profile?showPasswordForm=true";
                }
            }

            // Yeni şifreyi hash'leyerek kaydet
            user.setPassword(passwordEncoder.encode(newPassword));
            chatService.saveUser(user);

            String successMessage = currentLang.equals("en") ?
                    "Password updated successfully!" :
                    "Şifre başarıyla güncellendi!";
            redirectAttributes.addFlashAttribute("success", successMessage);

        } catch (RuntimeException e) {
            String errorMessage = languageService.getCurrentLanguage(request).equals("en") ?
                    "Failed to update password." :
                    "Şifre güncellenirken hata oluştu.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/profile?showPasswordForm=true";
        }

        return "redirect:/profile";
    }

    // Yardımcı metodlar
    private boolean isValidLanguage(String language) {
        return language != null && (language.equals("en") || language.equals("tr"));
    }

    private boolean isPasswordStrong(String password) {
        return password.matches("^(?=.*[A-Za-z])(?=.*\\d).+$");
    }
}