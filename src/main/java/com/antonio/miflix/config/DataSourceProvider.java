package com.antonio.miflix.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class DataSourceProvider {
    private static final Object LOCK = new Object();
    private static volatile HikariDataSource dataSource;

    private DataSourceProvider() {
    }

    public static HikariDataSource obtenerDataSource() {
        HikariDataSource actual = dataSource;
        if (actual == null || actual.isClosed()) {
            synchronized (LOCK) {
                actual = dataSource;
                if (actual == null || actual.isClosed()) {
                    AppConfig configuracion = new AppConfig();
                    actual = new HikariDataSource(crearConfiguracionPool(configuracion));
                    dataSource = actual;
                }
            }
        }
        return actual;
    }

    static HikariConfig crearConfiguracionPool(AppConfig configuracion) {
        HikariConfig configuracionPool = new HikariConfig();
        configuracionPool.setJdbcUrl(configuracion.getDbUrl());
        configuracionPool.setUsername(configuracion.getDbUser());
        configuracionPool.setPassword(configuracion.getDbPassword());
        configuracionPool.setDriverClassName("com.mysql.cj.jdbc.Driver");
        configuracionPool.setInitializationFailTimeout(-1);
        configuracionPool.setMaximumPoolSize(10);
        configuracionPool.setPoolName("miflix-pool");
        return configuracionPool;
    }

    public static void cerrar() {
        synchronized (LOCK) {
            if (dataSource != null) {
                dataSource.close();
                dataSource = null;
            }
        }
    }
}