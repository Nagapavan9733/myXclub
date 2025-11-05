package com.metrolinx.bookexchange.controller;

import com.metrolinx.bookexchange.model.User;
import com.metrolinx.bookexchange.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password!");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully!");
        }
        return "login";
    }

    @GetMapping("/")
    public String showEmailValidationPage() {
        return "email-validation";
    }
    
    @GetMapping("/home")
    public String showHomePage() {
        return "home";
    }

    @PostMapping("/validate-email")
    public String validateEmail(@RequestParam String email, 
                               RedirectAttributes redirectAttributes) {
        try {
            boolean isAuthorized = userService.isEmailAuthorized(email);
            
            if (isAuthorized) {
                boolean userExists = userService.userExists(email);
                
                if (userExists) {
                    redirectAttributes.addFlashAttribute("email", email);
                    return "redirect:/login";
                } else {
                    redirectAttributes.addFlashAttribute("email", email);
                    return "redirect:/signup";
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Email not authorized.");
                return "redirect:/";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/signup")
    public String showSignupPage(Model model) {
        if (!model.containsAttribute("email")) {
            return "redirect:/";
        }
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUser(@RequestParam String email,
                              @RequestParam String name,
                              @RequestParam String password,
                              @RequestParam String confirmPassword,
                              RedirectAttributes redirectAttributes) {
        try {
            // Simple validation
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match!");
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/signup";
            }

            // Register user
            userService.registerUser(email, password, name);
            
            redirectAttributes.addFlashAttribute("success", "Account created successfully! Please login.");
            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed. Please try again.");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/signup";
        }
    }
}