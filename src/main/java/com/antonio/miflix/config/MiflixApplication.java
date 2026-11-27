package com.antonio.miflix.config;

import com.antonio.miflix.controller.DirectorResource;
import com.antonio.miflix.controller.GeneroResource;
import com.antonio.miflix.controller.PosterResource;
import com.antonio.miflix.controller.PerfilResource;
import com.antonio.miflix.controller.PeliculaResource;
import com.antonio.miflix.controller.ProgresoResource;
import com.antonio.miflix.controller.SaludResource;
import com.antonio.miflix.controller.StreamResource;
import com.antonio.miflix.dao.DirectorDAO;
import com.antonio.miflix.dao.GeneroDAO;
import com.antonio.miflix.dao.JdbcDirectorDAO;
import com.antonio.miflix.dao.JdbcGeneroDAO;
import com.antonio.miflix.dao.JdbcPerfilDAO;
import com.antonio.miflix.dao.JdbcPeliculaDAO;
import com.antonio.miflix.dao.JdbcPeliculaGeneroDAO;
import com.antonio.miflix.dao.JdbcProgresoDAO;
import com.antonio.miflix.dao.JdbcSaludDAO;
import com.antonio.miflix.dao.GestorTransacciones;
import com.antonio.miflix.dao.PerfilDAO;
import com.antonio.miflix.dao.PeliculaDAO;
import com.antonio.miflix.dao.PeliculaGeneroDAO;
import com.antonio.miflix.dao.ProgresoDAO;
import com.antonio.miflix.dao.SaludDAO;
import com.antonio.miflix.exception.ConflictoExceptionMapper;
import com.antonio.miflix.exception.ErrorExceptionMapper;
import com.antonio.miflix.exception.RecursoNoEncontradoExceptionMapper;
import com.antonio.miflix.exception.ValidacionExceptionMapper;
import com.antonio.miflix.service.DirectorService;
import com.antonio.miflix.service.DirectorServiceImpl;
import com.antonio.miflix.service.GeneroService;
import com.antonio.miflix.service.GeneroServiceImpl;
import com.antonio.miflix.service.ParserRango;
import com.antonio.miflix.service.PosterService;
import com.antonio.miflix.service.PosterServiceImpl;
import com.antonio.miflix.service.PerfilService;
import com.antonio.miflix.service.PerfilServiceImpl;
import com.antonio.miflix.service.AlmacenMedios;
import com.antonio.miflix.service.PeliculaService;
import com.antonio.miflix.service.PeliculaServiceImpl;
import com.antonio.miflix.service.ProgresoService;
import com.antonio.miflix.service.ProgresoServiceImpl;
import com.antonio.miflix.service.SaludService;
import com.antonio.miflix.service.SaludServiceImpl;
import com.antonio.miflix.service.StreamService;
import com.antonio.miflix.service.StreamServiceImpl;
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
        register(DirectorResource.class);
        register(PerfilResource.class);
        register(PeliculaResource.class);
        register(ProgresoResource.class);
        register(StreamResource.class);
        register(PosterResource.class);
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
                bind(JdbcDirectorDAO.class).to(DirectorDAO.class);
                bind(DirectorServiceImpl.class).to(DirectorService.class);
                bind(JdbcPerfilDAO.class).to(PerfilDAO.class);
                bind(PerfilServiceImpl.class).to(PerfilService.class);
                bind(JdbcPeliculaDAO.class).to(PeliculaDAO.class);
                bind(JdbcPeliculaGeneroDAO.class).to(PeliculaGeneroDAO.class);
                bind(JdbcProgresoDAO.class).to(ProgresoDAO.class);
                bindAsContract(GestorTransacciones.class);
                bindAsContract(ParserRango.class);
                bind(AlmacenMediosSistemaArchivos.class).to(AlmacenMedios.class);
                bind(PeliculaServiceImpl.class).to(PeliculaService.class);
                bind(ProgresoServiceImpl.class).to(ProgresoService.class);
                bind(StreamServiceImpl.class).to(StreamService.class);
                bind(PosterServiceImpl.class).to(PosterService.class);
            }
        });
    }
}