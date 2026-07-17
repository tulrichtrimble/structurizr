package com.structurizr.server.web.security;

import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.web.AbstractTestsBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiAuthenticationUtilsTests extends AbstractTestsBase {

    @AfterEach
    void tearDown() {
        ApiAuthenticationUtils.clearJwtDecoderOverrideForTesting();
    }

    @Test
    void isSharedApiTokenValid_ReturnsTrueForConfiguredStaticSharedToken() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_API_SHARED_TOKEN, "shared-upload-token");
        configureAsServerWithAuthenticationEnabled(properties);

        assertTrue(ApiAuthenticationUtils.isSharedApiTokenValid("shared-upload-token"));
        assertFalse(ApiAuthenticationUtils.isSharedApiTokenValid("wrong-token"));
    }

    @Test
    void isJwtTokenValid_ReturnsTrueWhenIssuerAudienceAndScopeAreValid() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_API_TOKEN_ISSUER_URI, "https://id.trimble.com");
        properties.setProperty(StructurizrProperties.AUTHENTICATION_API_TOKEN_JWK_SET_URI, "https://id.trimble.com/.well-known/jwks.json");
        properties.setProperty(StructurizrProperties.AUTHENTICATION_API_TOKEN_AUDIENCE, "structurizr-upload");
        properties.setProperty(StructurizrProperties.AUTHENTICATION_API_TOKEN_SCOPES, "structurizr.upload");
        configureAsServerWithAuthenticationEnabled(properties);

        ApiAuthenticationUtils.setJwtDecoderOverrideForTesting(token -> Jwt.withTokenValue(token)
                .header("alg", "RS256")
                .issuer("https://id.trimble.com")
                .audience(List.of("structurizr-upload"))
                .claim("scope", "structurizr.upload")
                .issuedAt(Instant.now().minusSeconds(30))
                .expiresAt(Instant.now().plusSeconds(300))
                .build());

        assertTrue(ApiAuthenticationUtils.isJwtTokenValid("token"));
    }

    @Test
    void isJwtTokenValid_ReturnsFalseWhenScopeIsMissing() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_API_TOKEN_ISSUER_URI, "https://id.trimble.com");
        properties.setProperty(StructurizrProperties.AUTHENTICATION_API_TOKEN_JWK_SET_URI, "https://id.trimble.com/.well-known/jwks.json");
        properties.setProperty(StructurizrProperties.AUTHENTICATION_API_TOKEN_AUDIENCE, "structurizr-upload");
        properties.setProperty(StructurizrProperties.AUTHENTICATION_API_TOKEN_SCOPES, "structurizr.upload");
        configureAsServerWithAuthenticationEnabled(properties);

        ApiAuthenticationUtils.setJwtDecoderOverrideForTesting(token -> Jwt.withTokenValue(token)
                .header("alg", "RS256")
                .issuer("https://id.trimble.com")
                .audience(List.of("structurizr-upload"))
                .claim("scope", "other.scope")
                .issuedAt(Instant.now().minusSeconds(30))
                .expiresAt(Instant.now().plusSeconds(300))
                .build());

        assertFalse(ApiAuthenticationUtils.isJwtTokenValid("token"));
    }

    @Test
    void isJwtTokenValid_ReturnsFalseWhenTokenCannotBeDecoded() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_API_TOKEN_JWK_SET_URI, "https://id.trimble.com/.well-known/jwks.json");
        configureAsServerWithAuthenticationEnabled(properties);

        ApiAuthenticationUtils.setJwtDecoderOverrideForTesting(token -> {
            throw new JwtException("invalid token");
        });

        assertFalse(ApiAuthenticationUtils.isJwtTokenValid("token"));
    }

}
