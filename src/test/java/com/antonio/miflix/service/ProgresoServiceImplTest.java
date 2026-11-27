package com.antonio.miflix.service;

import com.antonio.miflix.dao.PeliculaDAO;
import com.antonio.miflix.dao.PerfilDAO;
import com.antonio.miflix.dao.ProgresoDAO;
import com.antonio.miflix.dto.ProgresoDTO;
import com.antonio.miflix.dto.ProgresoRequestDTO;
import com.antonio.miflix.exception.RecursoNoEncontradoException;
import com.antonio.miflix.exception.ValidacionException;
import com.antonio.miflix.model.PeliculaResumen;
import com.antonio.miflix.model.Perfil;
import com.antonio.miflix.model.Progreso;
import com.antonio.miflix.model.ProgresoContinuarViendoResumen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgresoServiceImplTest {
    @Mock
    private ProgresoDAO progresoDAO;
    @Mock
    private PerfilDAO perfilDAO;
    @Mock
    private PeliculaDAO peliculaDAO;

    private ProgresoServiceImpl progresoService;
    private Progreso progresoGuardado;

    @BeforeEach
    void prepararServicio() {
        progresoService = new ProgresoServiceImpl(progresoDAO, perfilDAO, peliculaDAO);
    }

    @Test
    void guardar_conProgresoMenorAl90PorCiento_noLoMarcaCompletado() {
        prepararRecursosBasicos();
        prepararUpsert();

        ProgresoDTO resultado = progresoService.guardar(1, 1, solicitud(539));
        ArgumentCaptor<Progreso> captor = ArgumentCaptor.forClass(Progreso.class);
        verify(progresoDAO).guardar(captor.capture());

        assertFalse(captor.getValue().isCompletada());
        assertEquals(89, resultado.getPorcentaje());
        assertFalse(resultado.isCompletada());
        assertEquals(539, resultado.getSegundoActual());
    }

    @Test
    void guardar_conProgresoIgualAl90PorCiento_loMarcaCompletado() {
        prepararRecursosBasicos();
        prepararUpsert();

        ProgresoDTO resultado = progresoService.guardar(1, 1, solicitud(540));
        ArgumentCaptor<Progreso> captor = ArgumentCaptor.forClass(Progreso.class);
        verify(progresoDAO).guardar(captor.capture());

        assertTrue(captor.getValue().isCompletada());
        assertEquals(90, resultado.getPorcentaje());
        assertTrue(resultado.isCompletada());
        assertEquals(1, resultado.getPeliculaId());
        assertNotNull(resultado.getUltimaVez());
    }

    @Test
    void guardar_conSegundoNegativoOMayorALaDuracion_lanzaValidacionExceptionSinGuardar() {
        prepararRecursosBasicos();

        assertThrows(ValidacionException.class, () -> progresoService.guardar(1, 1, solicitud(-1)));
        assertThrows(ValidacionException.class, () -> progresoService.guardar(1, 1, solicitud(601)));
        assertThrows(ValidacionException.class, () -> progresoService.guardar(1, 1, null));
        assertThrows(ValidacionException.class, () -> progresoService.guardar(1, 1, solicitud(null)));

        verify(progresoDAO, never()).guardar(any(Progreso.class));
    }

    @Test
    void guardar_conPerfilOPeliculaInexistente_lanzaRecursoNoEncontradoException() {
        prepararPerfilExistente();
        when(perfilDAO.buscarPorId(999)).thenReturn(Optional.empty());
        when(peliculaDAO.buscarPorId(999)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> progresoService.guardar(999, 1, solicitud(1)));
        assertThrows(RecursoNoEncontradoException.class,
                () -> progresoService.guardar(1, 999, solicitud(1)));
        verify(progresoDAO, never()).guardar(any(Progreso.class));
    }

    @Test
    void buscar_conProgresoInexistente_devuelveValoresIniciales() {
        prepararRecursosBasicos();
        when(progresoDAO.buscarPorPerfilYPelicula(1, 1)).thenReturn(Optional.empty());

        ProgresoDTO resultado = progresoService.buscarPorPerfilYPelicula(1, 1);

        assertEquals(0, resultado.getSegundoActual());
        assertEquals(0, resultado.getPorcentaje());
        assertFalse(resultado.isCompletada());
        assertNull(resultado.getUltimaVez());
    }

    @Test
    void continuarViendo_devuelveDatosBasicosYPorcentaje() {
        prepararPerfilExistente();
        LocalDateTime ultimaVez = LocalDateTime.now();
        when(progresoDAO.buscarNoCompletadosPorPerfil(1)).thenReturn(List.of(
                new ProgresoContinuarViendoResumen(2, "Película", 2024, 10, "poster.jpg", 300, ultimaVez)));

        var resultado = progresoService.continuarViendo(1);

        assertEquals(1, resultado.size());
        assertEquals(2, resultado.getFirst().getPeliculaId());
        assertEquals("Película", resultado.getFirst().getTitulo());
        assertEquals(50, resultado.getFirst().getPorcentaje());
        assertEquals(ultimaVez, resultado.getFirst().getUltimaVez());
    }

    @Test
    void continuarViendo_conPerfilInexistente_lanzaRecursoNoEncontradoException() {
        when(perfilDAO.buscarPorId(999)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> progresoService.continuarViendo(999));
        verify(progresoDAO, never()).buscarNoCompletadosPorPerfil(999);
    }

    private void prepararRecursosBasicos() {
        prepararPerfilExistente();
        when(peliculaDAO.buscarPorId(1)).thenReturn(Optional.of(pelicula(1, 10)));
    }

    private void prepararPerfilExistente() {
        when(perfilDAO.buscarPorId(1)).thenReturn(Optional.of(new Perfil(1, "Perfil", null, false, null)));
    }

    private void prepararUpsert() {
        doAnswer(invocacion -> {
            progresoGuardado = invocacion.getArgument(0);
            progresoGuardado.setUltimaVez(LocalDateTime.now());
            return null;
        }).when(progresoDAO).guardar(any(Progreso.class));
        when(progresoDAO.buscarPorPerfilYPelicula(anyInt(), anyInt()))
                .thenAnswer(invocacion -> Optional.ofNullable(progresoGuardado));
    }

    private static ProgresoRequestDTO solicitud(Integer segundoActual) {
        ProgresoRequestDTO solicitud = new ProgresoRequestDTO();
        solicitud.setSegundoActual(segundoActual);
        return solicitud;
    }

    private static PeliculaResumen pelicula(int id, int duracionMin) {
        return new PeliculaResumen(id, "Película", 2024, duracionMin, null, "video.mp4", null,
                null, null, LocalDateTime.now());
    }
}