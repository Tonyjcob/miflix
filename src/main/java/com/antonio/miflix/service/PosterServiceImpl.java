package com.antonio.miflix.service;

import com.antonio.miflix.dao.PeliculaDAO;
import com.antonio.miflix.dto.PosterDTO;
import com.antonio.miflix.exception.RecursoNoEncontradoException;
import com.antonio.miflix.model.PeliculaResumen;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.StreamingOutput;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

public class PosterServiceImpl implements PosterService {
    private static final int TAMANO_BLOQUE = 64 * 1024;

    private final PeliculaDAO peliculaDAO;
    private final AlmacenMedios almacenMedios;

    @Inject
    public PosterServiceImpl(PeliculaDAO peliculaDAO, AlmacenMedios almacenMedios) {
        this.peliculaDAO = peliculaDAO;
        this.almacenMedios = almacenMedios;
    }

    @Override
    public PosterDTO obtenerPoster(int peliculaId) {
        PeliculaResumen pelicula = peliculaDAO.buscarPorId(peliculaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la película con id " + peliculaId + "."));
        String rutaPoster = pelicula.getRutaPoster();
        if (rutaPoster == null || rutaPoster.isBlank()) {
            throw posterNoEncontrado();
        }

        Path ruta = obtenerRuta(rutaPoster);
        String tipoContenido = determinarTipoContenido(rutaPoster);
        long longitud;
        try {
            longitud = Files.size(ruta);
        } catch (IOException excepcion) {
            throw posterNoEncontrado();
        }
        StreamingOutput contenido = salida -> {
            try (var entrada = Files.newInputStream(ruta)) {
                byte[] bloque = new byte[TAMANO_BLOQUE];
                int leidos;
                while ((leidos = entrada.read(bloque)) != -1) {
                    salida.write(bloque, 0, leidos);
                }
            }
        };
        return new PosterDTO(longitud, tipoContenido, contenido);
    }

    private Path obtenerRuta(String rutaPoster) {
        Optional<Path> ruta = almacenMedios.resolverRutaSegura(rutaPoster);
        return ruta.orElseThrow(this::posterNoEncontrado);
    }

    private String determinarTipoContenido(String rutaPoster) {
        String ruta = rutaPoster.toLowerCase(Locale.ROOT);
        if (ruta.endsWith(".jpg")) {
            return "image/jpeg";
        }
        if (ruta.endsWith(".png")) {
            return "image/png";
        }
        if (ruta.endsWith(".webp")) {
            return "image/webp";
        }
        throw posterNoEncontrado();
    }

    private RecursoNoEncontradoException posterNoEncontrado() {
        return new RecursoNoEncontradoException("No se encontró el póster de la película.");
    }
}