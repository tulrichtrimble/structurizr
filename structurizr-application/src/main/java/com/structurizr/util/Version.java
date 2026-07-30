package com.structurizr.util;

import java.io.InputStream;
import java.util.Properties;

public class Version {

    private static final String APP_VERSION_KEY = "app.version";
    private static final String APP_VERSION_ENVIRONMENT_VARIABLE = "APP_VERSION";

    private static String version = "";

    static {
        try {
            String environmentVersion = System.getenv(APP_VERSION_ENVIRONMENT_VARIABLE);
            if (environmentVersion != null && !environmentVersion.isBlank()) {
                version = environmentVersion;
            } else {
                Properties properties = new Properties();
                InputStream in = Version.class.getClassLoader().getResourceAsStream("application.properties");
                if (in != null) {
                    properties.load(in);
                    version = properties.getProperty(APP_VERSION_KEY);
                    in.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getBuildNumber() {
        return version;
    }

}