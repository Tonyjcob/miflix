package com.antonio.miflix.config;

import com.zaxxer.hikari.HikariConfig;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataSourceProviderTest {
    @Test
    void crearConfiguracionPool_configuraDriverMysqlYDeshabilitaFalloInicial() {
        AppConfig configuracion = new AppConfig(Map.of(
                "DB_URL", "jdbc:mysql://localhost/miflix",
                "DB_USER", "usuario",
                "DB_PASSWORD", "clave",
                "MEDIA_PATH", "media"));

        HikariConfig configuracionPool = DataSourceProvider.crearConfiguracionPool(configuracion);

        assertEquals("com.mysql.cj.jdbc.Driver", configuracionPool.getDriverClassName());
        assertEquals(-1, configuracionPool.getInitializationFailTimeout());
    }
}