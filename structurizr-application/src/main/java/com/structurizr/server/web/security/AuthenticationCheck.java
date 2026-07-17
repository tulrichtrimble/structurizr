package com.structurizr.server.web.security;

import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.util.StringUtils;
import com.structurizr.server.Server;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;

@Configuration
@Profile("command-server")
public class AuthenticationCheck {

    private static final Log log = LogFactory.getLog(Server.class);

    @EventListener
    public void onApplicationEvent(final ServletWebServerInitializedEvent event) {
        String authenticationImplementation = com.structurizr.configuration.Configuration.getInstance().getProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION);

        if (StructurizrProperties.AUTHENTICATION_VARIANT_OIDC.equalsIgnoreCase(authenticationImplementation)) {
            String issuerUri = com.structurizr.configuration.Configuration.getInstance().getProperty(StructurizrProperties.AUTHENTICATION_OIDC_ISSUER_URI);
            String clientId = com.structurizr.configuration.Configuration.getInstance().getProperty(StructurizrProperties.AUTHENTICATION_OIDC_CLIENT_ID);

            if (StringUtils.isNullOrEmpty(issuerUri) || StringUtils.isNullOrEmpty(clientId)) {
                log.fatal("Authentication is configured for OIDC but required properties are missing: " +
                        StructurizrProperties.AUTHENTICATION_OIDC_ISSUER_URI + " and/or " + StructurizrProperties.AUTHENTICATION_OIDC_CLIENT_ID);
                System.exit(1);
            }
        }

        if (!SecurityUtils.isAuthenticationConfigured()) {
            log.fatal("Authentication has not been configured: " +
                    StructurizrProperties.AUTHENTICATION_IMPLEMENTATION +
                    "=" +
                    authenticationImplementation +
                    " is not supported in this build");
            System.exit(1);
        }
    }

}