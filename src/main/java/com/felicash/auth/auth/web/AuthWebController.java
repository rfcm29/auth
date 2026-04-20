package com.felicash.auth.auth.web;

import com.felicash.auth.auth.RegisterRequest;
import com.felicash.auth.auth.UserRegistrationFacade;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthWebController {

    private final UserRegistrationFacade userRegistrationFacade;
    private final AuthenticationManager authenticationManager;

    public AuthWebController(UserRegistrationFacade userRegistrationFacade,
                             AuthenticationManager authenticationManager) {
        this.userRegistrationFacade = userRegistrationFacade;
        this.authenticationManager = authenticationManager;
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
            userRegistrationFacade.register(new RegisterRequest(name, email, password));

            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContext ctx = SecurityContextHolder.createEmptyContext();
            ctx.setAuthentication(auth);
            SecurityContextHolder.setContext(ctx);
            new HttpSessionSecurityContextRepository().saveContext(ctx, request, response);

            return "redirect:/dashboard";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/register";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed. Please try again.");
            return "redirect:/register";
        }
    }
}

