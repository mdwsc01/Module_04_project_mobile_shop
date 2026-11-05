package com.example.mobileshop.controller;

import com.example.mobileshop.dto.RegisterRequest;
import com.example.mobileshop.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegistrationController {
    private final RegistrationService service;
    private final boolean enabled;

    public RegistrationController(RegistrationService service,
                                  @Value("${app.registration.enabled:true}") boolean enabled) {
        this.service = service;
        this.enabled = enabled;
    }

    @GetMapping("/register")
    public String form(Model model) {
        if (!enabled) return "redirect:/login?registrationDisabled";
        if (!model.containsAttribute("request")) model.addAttribute("request", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("request") RegisterRequest request, BindingResult binding, Model model) {
        if (!enabled) return "redirect:/login?registrationDisabled";
        if (binding.hasErrors()) return "register";
        try {
            service.register(request);
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            model.addAttribute("registerError", e instanceof DataIntegrityViolationException ? "Username đã tồn tại" : e.getMessage());
            return "register";
        }
        return "redirect:/login?registered";
    }
}
