package com.felicash.auth.controller;

import com.felicash.auth.domain.User;
import com.felicash.auth.dto.RegisterRequest;
import com.felicash.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web")
public class WebController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    public WebController(AuthService authService, AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
    }

    // ─── Root redirect ─────────────────────────────────────────────────────────

    @GetMapping
    public String webRoot() {
        return "redirect:/web/dashboard";
    }

    // ─── Login ─────────────────────────────────────────────────────────────────

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out successfully.");
        }
        return "login";
    }

    // ─── Register ──────────────────────────────────────────────────────────────

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           RedirectAttributes redirectAttributes) {
        try {
            authService.register(new RegisterRequest(name, email, password));

            // Auto-login after successful registration
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContext ctx = SecurityContextHolder.createEmptyContext();
            ctx.setAuthentication(auth);
            SecurityContextHolder.setContext(ctx);
            new HttpSessionSecurityContextRepository().saveContext(ctx, request, response);

            return "redirect:/web/dashboard";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/web/register";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed. Please try again.");
            return "redirect:/web/register";
        }
    }

    // ─── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            model.addAttribute("userName", user.getName());
            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("roles", user.getRoles());
            model.addAttribute("memberSince", user.getCreatedAt());
        }
        return "dashboard";
    }

    // ─── Profile ───────────────────────────────────────────────────────────────

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            model.addAttribute("userName", user.getName());
            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("roles", user.getRoles());
            model.addAttribute("memberSince", user.getCreatedAt());
        }
        return "profile";
    }
}

