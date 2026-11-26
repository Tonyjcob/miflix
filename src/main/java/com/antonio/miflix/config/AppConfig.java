package com.antonio.miflix.config;

import java.util.Map;

public final class AppConfig {
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private final String mediaPath;

    public AppConfig() {
        this(System.getenv());
    }

    AppConfig(Map<String, String> variablesEntorno) {
        dbUrl = valorObligatorio(variablesEntorno, "DB_URL");
        dbUser = valorObligatorio(variablesEntorno, "DB_USER");
        dbPassword = valorObligatorio(variablesEntorno, "DB_PASSWORD");
        mediaPath = valorObligatorio(variablesEntorno, "MEDIA_PATH");
    }

    private static String valorObligatorio(Map<String, String> variablesEntorno, String nombre) {
        String valor = variablesEntorno.get(nombre);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta la variable de entorno obligatoria: " + nombre);
        }
        return valor;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getMediaPath() {
        return mediaPath;
    }
}