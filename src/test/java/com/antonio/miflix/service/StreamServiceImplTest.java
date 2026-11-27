package com.antonio.miflix.service;

import com.antonio.miflix.dao.PeliculaDAO;
import com.antonio.miflix.dto.StreamDTO;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreamServiceImplTest {
    @TempDir
    Path directorioMedios;

    @Mock
    private PeliculaDAO peliculaDAO;
    @Mock
    private AlmacenMedios almacenMedios;

    private StreamServiceImpl streamService;
    private Path video;

    @BeforeEach
    void prepararServicio() throws IOException {
        streamService = new StreamServiceImpl(peliculaDAO, almacenMedios, new ParserRango());
        video = directorioMedios.resolve("prueba.mp4");
        Files.write(video, new byte[]{0, 1, 2, 3, 4, 5});
        when(peliculaDAO.buscarPorId(1)).thenReturn(Optional.of(pelicula(1, "prueba.mp4")));
        when(almacenMedios.resolverRutaSegura("prueba.mp4")).thenReturn(Optional.of(video));
    }

    @Test
    void obtenerVideo_sinRange_devuelveArchivoCompletoCon200() throws IOException {
        StreamDTO respuesta = streamService.obtenerVideo(1, null);

        assertEquals(200, respuesta.getEstado());
        assertEquals(6, respuesta.getLongitud());
        assertNull(respuesta.getRangoContenido());
        assertEquals("video/mp4", respuesta.getTipoContenido());
        assertArrayEquals(new byte[]{0, 1, 2, 3, 4, 5}, leer(respuesta.getContenido()));
    }

    @Test
    void obtenerVideo_conRangeValido_devuelveSoloLosBytesSolicitados() throws IOException {
        StreamDTO respuesta = streamService.obtenerVideo(1, "bytes=1-3");

        assertEquals(206, respuesta.getEstado());
        assertEquals(3, respuesta.getLongitud());
        assertEquals("bytes 1-3/6", respuesta.getRangoContenido());
        assertArrayEquals(new byte[]{1, 2, 3}, leer(respuesta.getContenido()));
    }

    @Test
    void obtenerVideo_conRangeFueraDeLimites_devuelve416() {
        StreamDTO respuesta = streamService.obtenerVideo(1, "bytes=6-");

        assertEquals(416, respuesta.getEstado());
        assertEquals("bytes */6", respuesta.getRangoContenido());
        assertNull(respuesta.getContenido());
    }

    @Test
    void obtenerVideo_conRangeMalformado_devuelveArchivoCompleto() {
        StreamDTO respuesta = streamService.obtenerVideo(1, "bytes=incorrecto");

        assertEquals(200, respuesta.getEstado());
        assertEquals(6, respuesta.getLongitud());
    }

    @Test
    void obtenerVideo_conPeliculaOArchivoInexistente_lanzaRecursoNoEncontradoException() {
        when(peliculaDAO.buscarPorId(999)).thenReturn(Optional.empty());
        when(almacenMedios.resolverRutaSegura("prueba.mp4")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> streamService.obtenerVideo(999, null));
        assertThrows(RecursoNoEncontradoException.class, () -> streamService.obtenerVideo(1, null));
    }

    private static byte[] leer(StreamingOutput contenido) throws IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        contenido.write(salida);
        return salida.toByteArray();
    }

    private static PeliculaResumen pelicula(int id, String rutaArchivo) {
        return new PeliculaResumen(id, "Película", 2024, 10, null, rutaArchivo, null, null, null,
                LocalDateTime.now());
    }
}