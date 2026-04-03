package com.business.frontend.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AuthUiController {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BACKEND = "http://localhost:8085";

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String signup,
            Model model) {
        if (error  != null) model.addAttribute("error",   "Invalid credentials. Please try again.");
        if (logout != null) model.addAttribute("message", "You have been signed out successfully.");
        if (signup != null) model.addAttribute("message", "Account created! Please log in.");
        return "login";
    }

    @PostMapping("/auth/do-login")
    public String doLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("username", username);
            body.put("password", password);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    BACKEND + "/auth/login", entity, Map.class);

            Map<?, ?> resp = response.getBody();
            String token = (String) resp.get("jwt");

            session.setAttribute("jwt_token", token);
            session.setAttribute("jwt_username", username);

            return "redirect:/ui/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Login failed: " + e.getMessage());
            return "login";
        }
    }

    @PostMapping("/auth/do-signup")
    public String doSignup(
            @RequestParam String username,
            @RequestParam String password,
            Model model) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, String> body = new HashMap<>();
            body.put("username", username);
            body.put("password", password);
            
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(BACKEND + "/auth/signup", entity, Map.class);
            return "redirect:/login?signup=true";
        } catch (Exception e) {
            model.addAttribute("error", "Signup failed: " + e.getMessage());
            return "login";
        }
    }
}