package com.structurizr.server.web.security;

import com.structurizr.server.domain.AuthenticationMethod;
import com.structurizr.server.domain.Role;
import com.structurizr.server.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SecurityUtilsTests {

    @Test
    void getUser_WithAnonymousAuthentication() {
        Authentication authentication = new AnonymousAuthenticationToken("anonymous", "anonymous", Set.of(new Role("role_anonymous")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = SecurityUtils.getUser();
        assertTrue(user.getUsername().matches("[0-9]*"));
        assertEquals(0, user.getRoles().size());
        assertEquals(AuthenticationMethod.NONE, user.getAuthenticationMethod());
        assertFalse(user.isAuthenticated());
    }

    @Test
    void getUser_WithUsernameAndPasswordAuthentication() {
        Set<Role> roles = new HashSet<>();
        roles.add(new Role("role1"));
        roles.add(new Role("role2"));
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("user", "password", roles);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, roles);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = SecurityUtils.getUser();
        assertEquals("user", user.getUsername());
        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().contains("role1"));
        assertTrue(user.getRoles().contains("role2"));
        assertTrue(user.isAuthenticated());
    }

    @Test
    void getUser_WithOidcAuthentication() {
        Authentication authentication = new OAuth2AuthenticationToken(
            new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority("ROLE_ARCHITECT"), new SimpleGrantedAuthority("SCOPE_structurizr.upload")),
                        Map.of("email", "oidc-user@example.com"),
                "email"
                ),
                Set.of(new SimpleGrantedAuthority("ROLE_ARCHITECT"), new SimpleGrantedAuthority("SCOPE_structurizr.upload")),
                "trimble"
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = SecurityUtils.getUser();
        assertEquals("oidc-user@example.com", user.getUsername());
        assertEquals(AuthenticationMethod.OIDC, user.getAuthenticationMethod());
        assertTrue(user.getRoles().contains("role_architect"));
        assertTrue(user.getRoles().contains("scope_structurizr.upload"));
        assertTrue(user.isAuthenticated());
    }

}