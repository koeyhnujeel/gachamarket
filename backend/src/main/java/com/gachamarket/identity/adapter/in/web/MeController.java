package com.gachamarket.identity.adapter.in.web;

import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {

    @GetMapping
    public Map<String, String> me(@AuthenticationPrincipal OAuth2User principal) {
        return Map.of(
            "email", String.valueOf(principal.getAttributes().getOrDefault("email", "")),
            "name", principal.getName()
        );
    }
}
