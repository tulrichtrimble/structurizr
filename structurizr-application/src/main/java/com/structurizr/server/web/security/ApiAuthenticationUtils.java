package com.structurizr.server.web.security;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ApiAuthenticationUtils {

    private static final Log log = LogFactory.getLog(ApiAuthenticationUtils.class);
    private static final ConcurrentMap<String, JwtDecoder> JWT_DECODER_CACHE = new ConcurrentHashMap<>();

    private static volatile JwtDecoder jwtDecoderOverride;

    private ApiAuthenticationUtils() {
    }

    public static String getOidcRegistrationId() {
        String registrationId = Configuration.getInstance().getProperty(StructurizrProperties.AUTHENTICATION_OIDC_REGISTRATION_ID);
        if (StringUtils.isNullOrEmpty(registrationId)) {
            return "trimble";
        }

        return registrationId;
    }

    public static String getOidcIssuerUri() {
        String issuerUri = Configuration.getInstance().getProperty(StructurizrProperties.AUTHENTICATION_OIDC_ISSUER_URI);
        if (StringUtils.isNullOrEmpty(issuerUri)) {
            return "";
        }

        return removeTrailingSlash(issuerUri);
    }

    public static Set<String> parseConfigValues(String value) {
        if (StringUtils.isNullOrEmpty(value)) {
            return Collections.emptySet();
        }

        String[] tokens = value.split("[,\\s]+");
        Set<String> values = new LinkedHashSet<>();
        for (String token : tokens) {
            String trimmed = token.trim();
            if (!trimmed.isEmpty()) {
                values.add(trimmed);
            }
        }

        return values;
    }

    public static boolean isSharedApiTokenValid(String providedSecret) {
        String sharedToken = Configuration.getInstance().getProperty(StructurizrProperties.AUTHENTICATION_API_SHARED_TOKEN);
        if (StringUtils.isNullOrEmpty(sharedToken)) {
            return false;
        }

        if (sharedToken.startsWith("$2a$") || sharedToken.startsWith("$2b$") || sharedToken.startsWith("$2y$")) {
            return new BCryptPasswordEncoder().matches(providedSecret, sharedToken);
        }

        byte[] provided = providedSecret.getBytes(StandardCharsets.UTF_8);
        byte[] expected = sharedToken.getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(provided, expected);
    }

    public static boolean isJwtTokenValid(String token) {
        String issuerUri = removeTrailingSlash(Configuration.getInstance().getProperty(StructurizrProperties.AUTHENTICATION_API_TOKEN_ISSUER_URI));
        String jwkSetUri = Configuration.getInstance().getProperty(StructurizrProperties.AUTHENTICATION_API_TOKEN_JWK_SET_URI);

        if (StringUtils.isNullOrEmpty(jwkSetUri) && !StringUtils.isNullOrEmpty(issuerUri)) {
            jwkSetUri = issuerUri + "/.well-known/jwks.json";
        }

        if (StringUtils.isNullOrEmpty(jwkSetUri)) {
            return false;
        }

        Jwt jwt;
        try {
            jwt = getJwtDecoder(issuerUri, jwkSetUri).decode(token);
        } catch (JwtException e) {
            log.warn("API token rejected due to JWT decode/validation error", e);
            return false;
        }

        if (!hasValidIssuer(jwt, issuerUri)) {
            log.warn("API token rejected due to issuer mismatch");
            return false;
        }

        if (!hasValidTimestamps(jwt)) {
            log.warn("API token rejected due to invalid token timestamps");
            return false;
        }

        Set<String> requiredAudiences = parseConfigValues(Configuration.getInstance().getProperty(StructurizrProperties.AUTHENTICATION_API_TOKEN_AUDIENCE));
        if (!requiredAudiences.isEmpty() && !hasRequiredAudience(jwt, requiredAudiences)) {
            log.warn("API token rejected due to missing audience");
            return false;
        }

        Set<String> requiredScopes = parseConfigValues(Configuration.getInstance().getProperty(StructurizrProperties.AUTHENTICATION_API_TOKEN_SCOPES));
        if (!requiredScopes.isEmpty() && !hasRequiredScopes(jwt, requiredScopes)) {
            log.warn("API token rejected due to missing scope");
            return false;
        }

        return true;
    }

    private static JwtDecoder getJwtDecoder(String issuerUri, String jwkSetUri) {
        JwtDecoder override = jwtDecoderOverride;
        if (override != null) {
            return override;
        }

        String key = issuerUri + "|" + jwkSetUri;
        return JWT_DECODER_CACHE.computeIfAbsent(key, unused -> NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build());
    }

    private static boolean hasValidIssuer(Jwt jwt, String requiredIssuer) {
        if (StringUtils.isNullOrEmpty(requiredIssuer)) {
            return true;
        }

        if (jwt.getIssuer() == null) {
            return false;
        }

        return requiredIssuer.equalsIgnoreCase(removeTrailingSlash(jwt.getIssuer().toString()));
    }

    private static boolean hasValidTimestamps(Jwt jwt) {
        Instant now = Instant.now();
        if (jwt.getExpiresAt() != null && !jwt.getExpiresAt().isAfter(now)) {
            return false;
        }

        if (jwt.getNotBefore() != null && jwt.getNotBefore().isAfter(now)) {
            return false;
        }

        return true;
    }

    private static boolean hasRequiredAudience(Jwt jwt, Set<String> requiredAudiences) {
        if (requiredAudiences.isEmpty()) {
            return true;
        }

        Set<String> audiences = new LinkedHashSet<>(jwt.getAudience());
        for (String requiredAudience : requiredAudiences) {
            if (audiences.contains(requiredAudience)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasRequiredScopes(Jwt jwt, Set<String> requiredScopes) {
        if (requiredScopes.isEmpty()) {
            return true;
        }

        Set<String> scopes = new LinkedHashSet<>();

        String scope = jwt.getClaimAsString("scope");
        if (!StringUtils.isNullOrEmpty(scope)) {
            scopes.addAll(Arrays.asList(scope.split("\\s+")));
        }

        Object scpClaim = jwt.getClaim("scp");
        if (scpClaim instanceof String) {
            String scp = (String) scpClaim;
            scopes.addAll(Arrays.asList(scp.split("\\s+")));
        } else if (scpClaim instanceof Collection<?>) {
            for (Object item : (Collection<?>) scpClaim) {
                scopes.add(String.valueOf(item));
            }
        }

        Set<String> lowerScopes = new LinkedHashSet<>();
        for (String configuredScope : scopes) {
            lowerScopes.add(configuredScope.toLowerCase(Locale.ROOT));
        }

        for (String requiredScope : requiredScopes) {
            if (lowerScopes.contains(requiredScope.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }

        return false;
    }

    private static String removeTrailingSlash(String value) {
        if (StringUtils.isNullOrEmpty(value)) {
            return "";
        }

        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }

        return value;
    }

    public static void setJwtDecoderOverrideForTesting(JwtDecoder jwtDecoder) {
        jwtDecoderOverride = jwtDecoder;
    }

    public static void clearJwtDecoderOverrideForTesting() {
        jwtDecoderOverride = null;
        JWT_DECODER_CACHE.clear();
    }

}
