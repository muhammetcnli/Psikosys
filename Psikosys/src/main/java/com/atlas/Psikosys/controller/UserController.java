package com.atlas.Psikosys.controller;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserController {

    @RequestMapping("/profile")
    public String profile(OAuth2AuthenticationToken token, Model model) {

        // add the user's name, email, and photo to the model
        model.addAttribute("name", token.getPrincipal().getAttributes().get("name"));
        model.addAttribute("email", token.getPrincipal().getAttribute("email"));
        model.addAttribute("photo", token.getPrincipal().getAttribute("picture"));

        return "user-profile";
    }


}
