package com.antonio.miflix.service;

import com.antonio.miflix.dao.PeliculaDAO;
import com.antonio.miflix.dto.MedioVideoDTO;
import com.antonio.miflix.model.PeliculaResumen;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedioServiceImplTest {
    @Mock
    private AlmacenMedios almacenMedios;
    @Mock
    private PeliculaDAO peliculaDAO;
    @InjectMocks
    private MedioServiceImpl medioService;

    @Test
    void listarVideos_indicaQueArchivosYaEstanRegistrados() {
        when(almacenMedios.listarVideos()).thenReturn(List.of("peliculas/uno.mp4", "nuevo.webm"));
        when(peliculaDAO.buscarTodos(null, null, null)).thenReturn(List.of(
                new PeliculaResumen(1, "Una película", 2020, 90, null, "peliculas/uno.mp4", null,
                        null, null, LocalDateTime.now())));

        List<MedioVideoDTO> resultado = medioService.listarVideos();

        assertEquals(2, resultado.size());
        assertEquals("peliculas/uno.mp4", resultado.get(0).getRutaArchivo());
        assertTrue(resultado.get(0).isRegistrado());
        assertEquals("nuevo.webm", resultado.get(1).getRutaArchivo());
        assertFalse(resultado.get(1).isRegistrado());
        verify(peliculaDAO).buscarTodos(null, null, null);
        verify(almacenMedios).listarVideos();
    }
}