package com.structurizr.server.web.security;

import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.domain.AuthenticationMethod;
import com.structurizr.server.domain.User;
import com.structurizr.server.web.AbstractTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OidcAuthenticationExtractorTests extends AbstractTestsBase {

    @Test
    void extract_UsesConfiguredUsernameClaimAndCapturesAuthorities() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_OIDC_USERNAME_CLAIM, "email");
        configureAsServer(properties);

        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ARCHITECT"),
                new SimpleGrantedAuthority("SCOPE_structurizr.upload")
        );

        DefaultOAuth2User principal = new DefaultOAuth2User(
                authorities,
                Map.of(
                        "sub", "123",
                        "email", "architect@example.com"
                ),
                "email"
        );

        OAuth2AuthenticationToken authenticationToken = new OAuth2AuthenticationToken(principal, authorities, "trimble");
        User user = new OidcAuthenticationExtractor().extract(authenticationToken);

        assertEquals("architect@example.com", user.getUsername());
        assertEquals(AuthenticationMethod.OIDC, user.getAuthenticationMethod());
        assertEquals(Set.of("role_architect", "scope_structurizr.upload"), user.getRoles());
        assertTrue(user.isAuthenticated());
    }

}
