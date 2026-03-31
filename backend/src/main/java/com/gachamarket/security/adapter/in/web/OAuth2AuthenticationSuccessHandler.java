package com.gachamarket.security.adapter.in.web;

import com.gachamarket.security.adapter.out.security.GoogleOAuth2UserService.GoogleOAuth2User;
import com.gachamarket.security.adapter.out.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        GoogleOAuth2User oauth2User = (GoogleOAuth2User) authentication.getPrincipal();
        var authUser = oauth2User.toAuthUser();

        String token = jwtTokenProvider.createToken(
                authUser.memberId(),
                authUser.email(),
                authUser.role().name()
        );

        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);

        response.sendRedirect(frontendUrl);
    }
}
