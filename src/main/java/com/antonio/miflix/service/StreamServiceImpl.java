package com.antonio.miflix.service;

import com.antonio.miflix.dao.PeliculaDAO;
import com.antonio.miflix.dto.StreamDTO;
import com.antonio.miflix.exception.RecursoNoEncontradoException;
import com.antonio.miflix.model.PeliculaResumen;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.StreamingOutput;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

public class StreamServiceImpl implements StreamService {
    private static final int TAMANO_BLOQUE = 64 * 1024;

    private final PeliculaDAO peliculaDAO;
    private final AlmacenMedios almacenMedios;
    private final ParserRango parserRango;

    @Inject
    public StreamServiceImpl(PeliculaDAO peliculaDAO, AlmacenMedios almacenMedios, ParserRango parserRango) {
        this.peliculaDAO = peliculaDAO;
        this.almacenMedios = almacenMedios;
        this.parserRango = parserRango;
    }

    @Override
    public StreamDTO obtenerVideo(int peliculaId, String encabezadoRange) {
        PeliculaResumen pelicula = peliculaDAO.buscarPorId(peliculaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la película con id " + peliculaId + "."));
        Path ruta = obtenerRuta(pelicula);
        String tipoContenido = determinarTipoContenido(pelicula.getRutaArchivo());
        long tamano = obtenerTamano(ruta);
        RangoBytes rango = parserRango.parsear(encabezadoRange, tamano);

        if (rango.getEstado() == RangoBytes.Estado.NO_SATISFACIBLE) {
            return new StreamDTO(416, 0, "bytes */" + tamano, tipoContenido, null);
        }

        long inicio = rango.getInicio();
        long longitud = tamano == 0 ? 0 : rango.getFin() - inicio + 1;
        int estado = rango.getEstado() == RangoBytes.Estado.PARCIAL ? 206 : 200;
        String rangoContenido = estado == 206 ? "bytes " + inicio + "-" + rango.getFin() + "/" + tamano : null;
        StreamingOutput contenido = salida -> copiarEnBloques(ruta, inicio, longitud, salida);
        return new StreamDTO(estado, longitud, rangoContenido, tipoContenido, contenido);
    }

    private Path obtenerRuta(PeliculaResumen pelicula) {
        Optional<Path> ruta = almacenMedios.resolverRutaSegura(pelicula.getRutaArchivo());
        return ruta.orElseThrow(() -> new RecursoNoEncontradoException(
                "No se encontró el archivo de vídeo de la película."));
    }

    private long obtenerTamano(Path ruta) {
        try {
            return Files.size(ruta);
        } catch (IOException excepcion) {
            throw new RecursoNoEncontradoException("No se encontró el archivo de vídeo de la película.");
        }
    }

    private String determinarTipoContenido(String rutaArchivo) {
        String ruta = rutaArchivo.toLowerCase(Locale.ROOT);
        if (ruta.endsWith(".mp4")) {
            return "video/mp4";
        }
        if (ruta.endsWith(".webm")) {
            return "video/webm";
        }
        throw new RecursoNoEncontradoException("El formato del archivo de vídeo no está disponible.");
    }

    private void copiarEnBloques(Path ruta, long inicio, long longitud, OutputStream salida) throws IOException {
        try (InputStream entrada = Files.newInputStream(ruta)) {
            entrada.skipNBytes(inicio);
            byte[] bloque = new byte[TAMANO_BLOQUE];
            long restantes = longitud;
            while (restantes > 0) {
                int tamanoLectura = (int) Math.min(bloque.length, restantes);
                int leidos = entrada.read(bloque, 0, tamanoLectura);
                if (leidos < 0) {
                    throw new IOException("El archivo de vídeo terminó antes de completar el rango solicitado.");
                }
                if (leidos > 0) {
                    salida.write(bloque, 0, leidos);
                    restantes -= leidos;
                }
            }
        }
    }
}