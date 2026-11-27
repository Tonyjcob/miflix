package com.antonio.miflix.service;

import com.antonio.miflix.dao.PeliculaDAO;
import com.antonio.miflix.dto.PosterDTO;
import com.antonio.miflix.exception.RecursoNoEncontradoException;
import com.antonio.miflix.model.PeliculaResumen;
import jakarta.ws.rs.core.StreamingOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PosterServiceImplTest {
    @TempDir
    Path directorioMedios;

    @Mock
    private PeliculaDAO peliculaDAO;
    @Mock
    private AlmacenMedios almacenMedios;

    private PosterServiceImpl posterService;
    private Path poster;

    @BeforeEach
    void prepararServicio() throws IOException {
        posterService = new PosterServiceImpl(peliculaDAO, almacenMedios);
        poster = directorioMedios.resolve("poster.jpg");
        Files.write(poster, new byte[]{1, 2, 3, 4});
    }

    @Test
    void obtenerPoster_conArchivoJpg_existenteDevuelveTipoYContenido() throws IOException {
        when(peliculaDAO.buscarPorId(1)).thenReturn(Optional.of(pelicula(1, "poster.jpg")));
        when(almacenMedios.resolverRutaSegura("poster.jpg")).thenReturn(Optional.of(poster));

        PosterDTO respuesta = posterService.obtenerPoster(1);

        assertEquals(4, respuesta.getLongitud());
        assertEquals("image/jpeg", respuesta.getTipoContenido());
        assertArrayEquals(new byte[]{1, 2, 3, 4}, leer(respuesta.getContenido()));
    }

    @Test
    void obtenerPoster_conPeliculaOPosterInexistente_lanzaRecursoNoEncontradoException() {
        when(peliculaDAO.buscarPorId(999)).thenReturn(Optional.empty());
        when(peliculaDAO.buscarPorId(1)).thenReturn(Optional.of(pelicula(1, "poster.jpg")));
        when(almacenMedios.resolverRutaSegura("poster.jpg")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> posterService.obtenerPoster(999));
        assertThrows(RecursoNoEncontradoException.class, () -> posterService.obtenerPoster(1));
    }

    @Test
    void obtenerPoster_sinRutaDePoster_lanzaRecursoNoEncontradoException() {
        when(peliculaDAO.buscarPorId(2)).thenReturn(Optional.of(pelicula(2, null)));

        assertThrows(RecursoNoEncontradoException.class, () -> posterService.obtenerPoster(2));
    }

    private static byte[] leer(StreamingOutput contenido) throws IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        contenido.write(salida);
        return salida.toByteArray();
    }

    private static PeliculaResumen pelicula(int id, String rutaPoster) {
        return new PeliculaResumen(id, "Película", 2024, 10, null, "video.mp4", rutaPoster, null, null,
                LocalDateTime.now());
    }
}