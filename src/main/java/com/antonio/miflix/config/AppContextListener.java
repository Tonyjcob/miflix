package com.antonio.miflix.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent evento) {
        new AppConfig();
    }

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        DataSourceProvider.cerrar();
    }
}