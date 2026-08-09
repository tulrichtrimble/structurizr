package com.structurizr.configuration;

public class StructurizrProperties {

    public static final String DATA_DIRECTORY = "structurizr.datadirectory";
    public static final String CONFIGURATION_FILENAME = "structurizr.properties";

    public static final String DEBUG = "structurizr.debug";
    public static final String LOGGING_FILENAME = "structurizr.logging.filename";
    public static final String LOGGING_LEVEL_STRUCTURIZR = "structurizr.logging.level.structurizr";
    public static final String LOGGING_LEVEL_OTHER = "structurizr.logging.level.other";

    public static final String EDITABLE_PROPERTY = "structurizr.editable";

    public static final String WORKSPACE_THREADS = "structurizr.workspace.threads";
    public static final String MAX_WORKSPACE_SIZE = "structurizr.workspace.maxsize";
    public static final String MAX_WORKSPACE_VERSIONS = "structurizr.workspace.maxversions";

    public static final String URL = "structurizr.url";
    public static final String ENCRYPTION_PASSPHRASE = "structurizr.encryption";
    public static final String API_KEY = "structurizr.apikey";
    public static final String ADMIN_USERS_AND_ROLES = "structurizr.admin";

    public static final String NETWORK_URLS_ALLOWED = "structurizr.network.urls.allowed";
    public static final String NETWORK_TIMEOUT = "structurizr.network.timeout";

    public static final String THEMES = "structurizr.themes";
    public static final String DEFAULT_THEMES_PATH = "themes";

    public static final String AUTHENTICATION_IMPLEMENTATION = "structurizr.authentication";
    public static final String AUTHENTICATION_VARIANT_NONE = "none";
    public static final String AUTHENTICATION_VARIANT_FILE = "file";
    public static final String AUTHENTICATION_VARIANT_OIDC = "oidc";

    public static final String AUTHENTICATION_OIDC_REGISTRATION_ID = "structurizr.authentication.oidc.registrationid";
    public static final String AUTHENTICATION_OIDC_ISSUER_URI = "structurizr.authentication.oidc.issueruri";
    public static final String AUTHENTICATION_OIDC_CLIENT_ID = "structurizr.authentication.oidc.clientid";
    public static final String AUTHENTICATION_OIDC_CLIENT_SECRET = "structurizr.authentication.oidc.clientsecret";
    public static final String AUTHENTICATION_OIDC_SCOPES = "structurizr.authentication.oidc.scopes";
    public static final String AUTHENTICATION_OIDC_AUTHORIZATION_URI = "structurizr.authentication.oidc.authorizationuri";
    public static final String AUTHENTICATION_OIDC_TOKEN_URI = "structurizr.authentication.oidc.tokenuri";
    public static final String AUTHENTICATION_OIDC_JWK_SET_URI = "structurizr.authentication.oidc.jwkseturi";
    public static final String AUTHENTICATION_OIDC_USER_INFO_URI = "structurizr.authentication.oidc.userinfouri";
    public static final String AUTHENTICATION_OIDC_USERNAME_CLAIM = "structurizr.authentication.oidc.usernameclaim";

    public static final String AUTHENTICATION_API_SHARED_TOKEN = "structurizr.authentication.api.sharedtoken";
    public static final String AUTHENTICATION_API_TOKEN_ISSUER_URI = "structurizr.authentication.api.issueruri";
    public static final String AUTHENTICATION_API_TOKEN_JWK_SET_URI = "structurizr.authentication.api.jwkseturi";
    public static final String AUTHENTICATION_API_TOKEN_AUDIENCE = "structurizr.authentication.api.audience";
    public static final String AUTHENTICATION_API_TOKEN_SCOPES = "structurizr.authentication.api.scopes";

    public static final String SESSION_IMPLEMENTATION = "structurizr.session";
    public static final String SESSION_VARIANT_LOCAL = "local";

    public static final String DEFAULT_DATA_DIRECTORY_PATH = "/usr/local/structurizr";

    public static final String DEFAULT_WORKSPACE_THREADS = "10";
    public static final String DEFAULT_MAX_WORKSPACE_SIZE = "1MB";
    public static final String DEFAULT_MAX_WORKSPACE_VERSIONS = "30";

    public static final String DEFAULT_NETWORK_TIMEOUT_OF_SIXTY_SECONDS = "" + (1000 * 60);

    public static final String DATA_STORAGE_IMPLEMENTATION = "structurizr.data";
    public static final String DATA_STORAGE_VARIANT_FILE = "file";

    public static final String SEARCH_IMPLEMENTATION = "structurizr.search";
    public static final String SEARCH_VARIANT_NONE = "none";
    public static final String SEARCH_VARIANT_LUCENE = "lucene";

    public static final String CACHE_IMPLEMENTATION = "structurizr.cache";
    public static final String CACHE_EXPIRY_IN_MINUTES = "structurizr.cache.expiry";
    public static final String DEFAULT_CACHE_EXPIRY_IN_MINUTES = "5";
    public static final String CACHE_VARIANT_NONE = "none";

    public static final String WORKSPACES_PROPERTY = "structurizr.workspaces";
    public static final String SINGLE_WORKSPACE = "1";

    public static final String AUTO_SAVE_INTERVAL_PROPERTY = "structurizr.autosaveinterval";
    public static final String DEFAULT_AUTO_SAVE_INTERVAL_IN_MILLISECONDS = "5000";
    public static final String AUTO_REFRESH_INTERVAL_PROPERTY = "structurizr.autorefreshinterval";
    public static final String DEFAULT_AUTO_REFRESH_INTERVAL_IN_MILLISECONDS = "0";

}