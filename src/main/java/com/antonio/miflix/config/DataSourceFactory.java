package com.antonio.miflix.config;

import org.glassfish.hk2.api.Factory;

import javax.sql.DataSource;

public class DataSourceFactory implements Factory<DataSource> {
    @Override
    public DataSource provide() {
        return DataSourceProvider.obtenerDataSource();
    }

    @Override
    public void dispose(DataSource dataSource) {
    }
}