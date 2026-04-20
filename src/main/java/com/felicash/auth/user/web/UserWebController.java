package com.felicash.auth.user.web;

import com.felicash.auth.user.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserWebController {

    // ─── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            model.addAttribute("userName", principal.getUser().getName());
            model.addAttribute("userEmail", principal.getUser().getEmail());
            model.addAttribute("roles", principal.getUser().getRoles());
            model.addAttribute("memberSince", principal.getUser().getCreatedAt());
        }
        return "dashboard";
    }

    // ─── Profile ───────────────────────────────────────────────────────────────

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            model.addAttribute("userName", principal.getUser().getName());
            model.addAttribute("userEmail", principal.getUser().getEmail());
            model.addAttribute("roles", principal.getUser().getRoles());
            model.addAttribute("memberSince", principal.getUser().getCreatedAt());
        }
        return "profile";
    }
}

