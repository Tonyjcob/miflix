package com.antonio.miflix.service;

import com.antonio.miflix.dao.GeneroDAO;
import com.antonio.miflix.dto.GeneroDTO;
import com.antonio.miflix.dto.GeneroRequestDTO;
import com.antonio.miflix.exception.ClaveDuplicadaException;
import com.antonio.miflix.exception.ConflictoException;
import com.antonio.miflix.exception.RecursoNoEncontradoException;
import com.antonio.miflix.exception.ValidacionException;
import com.antonio.miflix.model.Genero;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeneroServiceImplTest {
    @Mock
    private GeneroDAO generoDAO;

    @InjectMocks
    private GeneroServiceImpl generoService;

    @Test
    void crear_conSolicitudNula_lanzaValidacionException() {
        assertThrows(ValidacionException.class, () -> generoService.crear(null));
    }

    @Test
    void crear_conNombreNulo_lanzaValidacionException() {
        assertThrows(ValidacionException.class, () -> generoService.crear(solicitud(null)));
    }

    @Test
    void crear_conNombreVacio_lanzaValidacionException() {
        assertThrows(ValidacionException.class, () -> generoService.crear(solicitud("")));
    }

    @Test
    void crear_conNombreDeSoloEspacios_lanzaValidacionException() {
        assertThrows(ValidacionException.class, () -> generoService.crear(solicitud("   ")));
    }

    @Test
    void crear_conNombreDemasiadoCorto_lanzaValidacionException() {
        assertThrows(ValidacionException.class, () -> generoService.crear(solicitud("A")));
    }

    @Test
    void crear_conNombreDemasiadoLargo_lanzaValidacionException() {
        assertThrows(ValidacionException.class, () -> generoService.crear(solicitud("G".repeat(51))));
    }

    @Test
    void crear_conNombreConEspaciosSobrantes_aplicaTrim() {
        when(generoDAO.insertar(any(Genero.class))).thenAnswer(invocacion -> {
            Genero genero = invocacion.getArgument(0);
            assertEquals("Drama", genero.getNombre());
            return 9;
        });

        GeneroDTO resultado = generoService.crear(solicitud("  Drama  "));

        assertEquals(9, resultado.getId());
        assertEquals("Drama", resultado.getNombre());
        assertEquals(0, resultado.getTotalPeliculas());
    }

    @Test
    void crear_conNombreDuplicado_lanzaConflictoException() {
        when(generoDAO.insertar(any(Genero.class)))
                .thenThrow(new ClaveDuplicadaException("Duplicado", new RuntimeException()));

        ConflictoException excepcion = assertThrows(ConflictoException.class,
                () -> generoService.crear(solicitud("Drama")));

        assertTrue(excepcion.getMessage().contains("Drama"));
    }

    @Test
    void buscarPorId_conGeneroInexistente_lanzaRecursoNoEncontradoException() {
        when(generoDAO.buscarPorId(99)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> generoService.buscarPorId(99));
    }

    @Test
    void actualizar_conGeneroInexistente_lanzaRecursoNoEncontradoException() {
        when(generoDAO.buscarPorId(99)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> generoService.actualizar(99, solicitud("Drama")));
        verify(generoDAO, never()).actualizar(any(Genero.class));
    }

    @Test
    void actualizar_conGeneroExistente_devuelveElGeneroActualizado() {
        when(generoDAO.buscarPorId(8)).thenReturn(Optional.of(new Genero(8, "Documental")))
                .thenReturn(Optional.of(new Genero(8, "Historia")));
        when(generoDAO.actualizar(any(Genero.class))).thenReturn(true);
        when(generoDAO.contarPeliculas(8)).thenReturn(0);

        GeneroDTO resultado = generoService.actualizar(8, solicitud("Historia"));

        assertEquals("Historia", resultado.getNombre());
        verify(generoDAO).actualizar(any(Genero.class));
    }

    @Test
    void actualizar_conNombreDuplicado_lanzaConflictoException() {
        when(generoDAO.buscarPorId(8)).thenReturn(Optional.of(new Genero(8, "Documental")));
        when(generoDAO.actualizar(any(Genero.class)))
                .thenThrow(new ClaveDuplicadaException("Duplicado", new RuntimeException()));

        assertThrows(ConflictoException.class, () -> generoService.actualizar(8, solicitud("Drama")));
    }

    @Test
    void eliminar_conGeneroInexistente_lanzaRecursoNoEncontradoException() {
        when(generoDAO.buscarPorId(99)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> generoService.eliminar(99));
    }

    @Test
    void eliminar_conPeliculasAsociadas_lanzaConflictoYNoLlamaAlDaoEliminar() {
        when(generoDAO.buscarPorId(1)).thenReturn(Optional.of(new Genero(1, "Animación")));
        when(generoDAO.contarPeliculas(1)).thenReturn(8);

        ConflictoException excepcion = assertThrows(ConflictoException.class, () -> generoService.eliminar(1));

        assertTrue(excepcion.getMessage().contains("Animación"));
        assertTrue(excepcion.getMessage().contains("8"));
        verify(generoDAO, never()).eliminar(1);
    }

    @Test
    void eliminar_sinPeliculasAsociadas_eliminaElGenero() {
        when(generoDAO.buscarPorId(8)).thenReturn(Optional.of(new Genero(8, "Documental")));
        when(generoDAO.contarPeliculas(8)).thenReturn(0);
        when(generoDAO.eliminar(8)).thenReturn(true);

        generoService.eliminar(8);

        verify(generoDAO).eliminar(8);
    }

    private GeneroRequestDTO solicitud(String nombre) {
        GeneroRequestDTO solicitud = new GeneroRequestDTO();
        solicitud.setNombre(nombre);
        return solicitud;
    }
}