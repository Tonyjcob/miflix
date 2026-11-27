package com.antonio.miflix.service;

import com.antonio.miflix.dao.DirectorDAO;
import com.antonio.miflix.dto.DirectorDTO;
import com.antonio.miflix.dto.DirectorRequestDTO;
import com.antonio.miflix.exception.ClaveDuplicadaException;
import com.antonio.miflix.exception.ConflictoException;
import com.antonio.miflix.exception.RecursoNoEncontradoException;
import com.antonio.miflix.exception.ValidacionException;
import com.antonio.miflix.model.Director;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DirectorServiceImplTest {
    @Mock
    private DirectorDAO directorDAO;

    @InjectMocks
    private DirectorServiceImpl directorService;

    @Test
    void crear_conSolicitudNula_lanzaValidacionException() {
        assertThrows(ValidacionException.class, () -> directorService.crear(null));
    }

    @Test
    void crear_conNombreNulo_lanzaValidacionException() {
        assertThrows(ValidacionException.class, () -> directorService.crear(solicitud(null, null)));
    }

    @Test
    void crear_conNombreVacio_lanzaValidacionException() {
        assertThrows(ValidacionException.class, () -> directorService.crear(solicitud("", null)));
    }

    @Test
    void crear_conNombreDeSoloEspacios_lanzaValidacionException() {
        assertThrows(ValidacionException.class, () -> directorService.crear(solicitud("   ", null)));
    }

    @Test
    void crear_conNombreDemasiadoCorto_lanzaValidacionException() {
        assertThrows(ValidacionException.class, () -> directorService.crear(solicitud("A", null)));
    }

    @Test
    void crear_conNombreMinimoAceptaDosCaracteres() {
        prepararCreacion(10);

        DirectorDTO resultado = directorService.crear(solicitud("AB", null));

        assertEquals("AB", resultado.getNombre());
    }

    @Test
    void crear_conNombreMaximoAceptaCientoVeinteCaracteres() {
        String nombre = "D".repeat(120);
        prepararCreacion(10);

        DirectorDTO resultado = directorService.crear(solicitud(nombre, null));

        assertEquals(nombre, resultado.getNombre());
    }

    @Test
    void crear_conNombreDemasiadoLargo_lanzaValidacionException() {
        assertThrows(ValidacionException.class,
                () -> directorService.crear(solicitud("D".repeat(121), null)));
    }

    @Test
    void crear_conNombreYDatosOpcionalesEnBlanco_aplicaTrimYConvierteNacionalidadEnNull() {
        when(directorDAO.insertar(any(Director.class))).thenAnswer(invocacion -> {
            Director director = invocacion.getArgument(0);
            assertEquals("Nombre Director", director.getNombre());
            assertNull(director.getNacionalidad());
            return 10;
        });

        DirectorDTO resultado = directorService.crear(solicitud("  Nombre Director  ", "  "));

        assertEquals("Nombre Director", resultado.getNombre());
        assertNull(resultado.getNacionalidad());
    }

    @Test
    void crear_conNacionalidadMaximaAceptaSesentaCaracteres() {
        String nacionalidad = "N".repeat(60);
        when(directorDAO.insertar(any(Director.class))).thenAnswer(invocacion -> {
            assertEquals(nacionalidad, ((Director) invocacion.getArgument(0)).getNacionalidad());
            return 10;
        });

        DirectorDTO resultado = directorService.crear(solicitud("Director", nacionalidad));

        assertEquals(nacionalidad, resultado.getNacionalidad());
    }

    @Test
    void crear_conNacionalidadDemasiadoLarga_lanzaValidacionException() {
        assertThrows(ValidacionException.class,
                () -> directorService.crear(solicitud("Director", "N".repeat(61))));
    }

    @Test
    void crear_conNombreDuplicado_lanzaConflictoException() {
        when(directorDAO.insertar(any(Director.class)))
                .thenThrow(new ClaveDuplicadaException("Duplicado", new RuntimeException()));

        ConflictoException excepcion = assertThrows(ConflictoException.class,
                () -> directorService.crear(solicitud("Director", null)));

        assertTrue(excepcion.getMessage().contains("Director"));
    }

    @Test
    void buscarPorId_conDirectorInexistente_lanzaRecursoNoEncontradoException() {
        when(directorDAO.buscarPorId(99)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> directorService.buscarPorId(99));
    }

    @Test
    void actualizar_conDirectorInexistente_lanzaRecursoNoEncontradoException() {
        when(directorDAO.buscarPorId(99)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> directorService.actualizar(99, solicitud("Director", null)));
        verify(directorDAO, never()).actualizar(any(Director.class));
    }

    @Test
    void actualizar_conNombreDuplicado_lanzaConflictoException() {
        when(directorDAO.buscarPorId(9)).thenReturn(Optional.of(new Director(9, "Sin Películas", null)));
        when(directorDAO.actualizar(any(Director.class)))
                .thenThrow(new ClaveDuplicadaException("Duplicado", new RuntimeException()));

        assertThrows(ConflictoException.class,
                () -> directorService.actualizar(9, solicitud("Sacha Goedegebure", null)));
    }

    @Test
    void eliminar_conDirectorInexistente_lanzaRecursoNoEncontradoException() {
        when(directorDAO.buscarPorId(99)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> directorService.eliminar(99));
    }

    @Test
    void eliminar_conPeliculasAsociadas_borraElDirectorSinBloquearPorLasPeliculas() {
        when(directorDAO.buscarPorId(1)).thenReturn(Optional.of(new Director(1, "Sacha", null)));
        when(directorDAO.eliminar(1)).thenReturn(true);

        directorService.eliminar(1);

        verify(directorDAO).eliminar(1);
        verify(directorDAO, never()).contarPeliculas(1);
    }

    private void prepararCreacion(int id) {
        when(directorDAO.insertar(any(Director.class))).thenReturn(id);
    }

    private DirectorRequestDTO solicitud(String nombre, String nacionalidad) {
        DirectorRequestDTO solicitud = new DirectorRequestDTO();
        solicitud.setNombre(nombre);
        solicitud.setNacionalidad(nacionalidad);
        return solicitud;
    }
}