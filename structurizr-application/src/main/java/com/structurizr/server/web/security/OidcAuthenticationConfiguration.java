package com.structurizr.server.web.security;

import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.util.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@Profile("authentication-oidc")
class OidcAuthenticationConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
                .requestMatchers("/api/**", "/health", "/error", "/favicon.ico", "/signin", "/oauth2/**", "/login/**").permitAll()
                .anyRequest().authenticated()
        );

        http.oauth2Login(oauth2Login -> oauth2Login
                .loginPage("/signin")
                .defaultSuccessUrl("/", true)
        );

        http.logout(logout -> logout
                .logoutUrl("/signout")
                .logoutSuccessUrl("/signin?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
        );

        http.headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
        );

        SecurityUtils.setAuthenticationConfigured(true);

        return http.build();
    }

    @Bean
    ClientRegistrationRepository clientRegistrationRepository() {
        String registrationId = ApiAuthenticationUtils.getOidcRegistrationId();
        String issuerUri = ApiAuthenticationUtils.getOidcIssuerUri();

        String authorizationUri = com.structurizr.configuration.Configuration.getInstance().getProperty(StructurizrProperties.AUTHENTICATION_OIDC_AUTHORIZATION_URI);
        if (StringUtils.isNullOrEmpty(authorizationUri)) {
            authorizationUri = issuerUri + "/oauth/authorize";
        }

        String tokenUri = com.structurizr.configuration.Configuration.getInstance().getProperty(StructurizrProperties.AUTHENTICATION_OIDC_TOKEN_URI);
        if (StringUtils.isNullOrEmpty(tokenUri)) {
            tokenUri = issuerUri + "/oauth/token";
        }

        String jwkSetUri = com.structurizr.configuration.Configuration.getInstance().getProperty(StructurizrProperties.AUTHENTICATION_OIDC_JWK_SET_URI);
        if (StringUtils.isNullOrEmpty(jwkSetUri)) {
            jwkSetUri = issuerUri + "/.well-known/jwks.json";
        }

        String userInfoUri = com.structurizr.configuration.Configuration.getInstance().getProperty(StructurizrProperties.AUTHENTICATION_OIDC_USER_INFO_URI);
        if (StringUtils.isNullOrEmpty(userInfoUri)) {
            userInfoUri = issuerUri + "/oauth/userinfo";
        }

        List<String> scopes = new ArrayList<>(ApiAuthenticationUtils.parseConfigValues(com.structurizr.configuration.Configuration.getInstance().getProperty(StructurizrProperties.AUTHENTICATION_OIDC_SCOPES)));
        if (scopes.isEmpty()) {
            scopes = List.of("openid", "profile", "email");
        }

        String clientSecret = com.structurizr.configuration.Configuration.getInstance().getProperty(StructurizrProperties.AUTHENTICATION_OIDC_CLIENT_SECRET);

        ClientRegistration.Builder builder = ClientRegistration
                .withRegistrationId(registrationId)
                .clientId(com.structurizr.configuration.Configuration.getInstance().getProperty(StructurizrProperties.AUTHENTICATION_OIDC_CLIENT_ID))
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope(scopes.toArray(new String[0]))
                .authorizationUri(authorizationUri)
                .tokenUri(tokenUri)
                .jwkSetUri(jwkSetUri)
                .issuerUri(issuerUri)
                .userInfoUri(userInfoUri)
                .userNameAttributeName(com.structurizr.configuration.Configuration.getInstance().getProperty(StructurizrProperties.AUTHENTICATION_OIDC_USERNAME_CLAIM));

        if (StringUtils.isNullOrEmpty(clientSecret)) {
            builder.clientAuthenticationMethod(ClientAuthenticationMethod.NONE);
        } else {
            builder.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
            builder.clientSecret(clientSecret);
        }

        return new InMemoryClientRegistrationRepository(builder.build());
    }

    @Bean
    OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    @Controller
    static class SignInController {

        @GetMapping("/signin")
        String signIn() {
            return "redirect:/oauth2/authorization/" + ApiAuthenticationUtils.getOidcRegistrationId();
        }

    }

}
