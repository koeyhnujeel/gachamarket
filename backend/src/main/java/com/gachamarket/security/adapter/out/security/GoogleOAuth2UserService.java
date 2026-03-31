package com.gachamarket.security.adapter.out.security;

import com.gachamarket.security.application.port.out.MemberPort;
import com.gachamarket.security.application.port.out.MemberPort.MemberInfo;
import com.gachamarket.security.domain.AuthUser;
import com.gachamarket.shared.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberPort memberPort;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String email = oauth2User.getAttribute("email");

        MemberInfo member = memberPort.findByEmail(email)
                .orElseGet(() -> memberPort.create(email, generateNickname(), Role.USER));

        return new GoogleOAuth2User(oauth2User, member);
    }

    private String generateNickname() {
        return "user" + System.currentTimeMillis();
    }

    public record GoogleOAuth2User(OAuth2User delegate, MemberInfo memberInfo) implements OAuth2User {

        @Override
        public Map<String, Object> getAttributes() {
            return delegate.getAttributes();
        }

        @Override
        @SuppressWarnings("unchecked")
        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return delegate.getAuthorities();
        }

        @Override
        public String getName() {
            return memberInfo.email();
        }

        public AuthUser toAuthUser() {
            return new AuthUser(
                    memberInfo.id(),
                    memberInfo.email(),
                    memberInfo.role()
            );
        }
    }
}
