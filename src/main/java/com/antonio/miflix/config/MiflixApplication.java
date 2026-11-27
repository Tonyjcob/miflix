package com.antonio.miflix.config;

import com.antonio.miflix.controller.GeneroResource;
import com.antonio.miflix.controller.SaludResource;
import com.antonio.miflix.dao.GeneroDAO;
import com.antonio.miflix.dao.JdbcGeneroDAO;
import com.antonio.miflix.dao.JdbcSaludDAO;
import com.antonio.miflix.dao.SaludDAO;
import com.antonio.miflix.exception.ConflictoExceptionMapper;
import com.antonio.miflix.exception.ErrorExceptionMapper;
import com.antonio.miflix.exception.RecursoNoEncontradoExceptionMapper;
import com.antonio.miflix.exception.ValidacionExceptionMapper;
import com.antonio.miflix.service.GeneroService;
import com.antonio.miflix.service.GeneroServiceImpl;
import com.antonio.miflix.service.SaludService;
import com.antonio.miflix.service.SaludServiceImpl;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.sql.DataSource;

@ApplicationPath("/api")
public class MiflixApplication extends ResourceConfig {
    public MiflixApplication() {
        register(SaludResource.class);
        register(GeneroResource.class);
        register(ValidacionExceptionMapper.class);
        register(RecursoNoEncontradoExceptionMapper.class);
        register(ConflictoExceptionMapper.class);
        register(ErrorExceptionMapper.class);
        register(JacksonObjectMapperProvider.class);
        register(JacksonFeature.class);
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(DataSourceFactory.class).to(DataSource.class);
                bind(JdbcSaludDAO.class).to(SaludDAO.class);
                bind(SaludServiceImpl.class).to(SaludService.class);
                bind(JdbcGeneroDAO.class).to(GeneroDAO.class);
                bind(GeneroServiceImpl.class).to(GeneroService.class);
            }
        });
    }
}