package com.structurizr.server.web.security;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.domain.AuthenticationMethod;
import com.structurizr.server.domain.User;
import com.structurizr.util.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

class OidcAuthenticationExtractor implements AuthenticationExtractor {

    @Override
    public User extract(Authentication authentication) {
        OAuth2AuthenticationToken oauth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        OAuth2AuthenticatedPrincipal principal = oauth2AuthenticationToken.getPrincipal();

        String usernameClaim = Configuration.getInstance().getProperty(StructurizrProperties.AUTHENTICATION_OIDC_USERNAME_CLAIM);
        String username = principal.getAttribute(usernameClaim);
        if (StringUtils.isNullOrEmpty(username)) {
            username = principal.getAttribute("email");
        }
        if (StringUtils.isNullOrEmpty(username)) {
            username = principal.getAttribute("preferred_username");
        }
        if (StringUtils.isNullOrEmpty(username)) {
            username = principal.getName();
        }

        Set<String> roles = new HashSet<>();
        for (GrantedAuthority grantedAuthority : oauth2AuthenticationToken.getAuthorities()) {
            roles.add(grantedAuthority.getAuthority().toLowerCase(Locale.ROOT));
        }

        return new User(username, roles, AuthenticationMethod.OIDC);
    }

}
