package com.antonio.miflix.service;

import com.antonio.miflix.dao.PerfilDAO;
import com.antonio.miflix.dto.PerfilDTO;
import com.antonio.miflix.dto.PerfilRequestDTO;
import com.antonio.miflix.exception.ClaveDuplicadaException;
import com.antonio.miflix.exception.ConflictoException;
import com.antonio.miflix.exception.RecursoNoEncontradoException;
import com.antonio.miflix.exception.ValidacionException;
import com.antonio.miflix.model.Perfil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerfilServiceImplTest {
    private static final LocalDateTime FECHA_CREACION = LocalDateTime.of(2026, 11, 27, 10, 0);

    @Mock
    private PerfilDAO perfilDAO;

    @InjectMocks
    private PerfilServiceImpl perfilService;

    private Perfil perfilCreado;

    @Test
    void crear_conSolicitudNula_lanzaValidacionException() {
        assertThrows(ValidacionException.class, () -> perfilService.crear(null));
    }

    @Test
    void crear_conNombreNulo_lanzaValidacionException() {
        assertThrows(ValidacionException.class, () -> perfilService.crear(solicitud(null, null, null)));
    }

    @Test
    void crear_conNombreVacio_lanzaValidacionException() {
        assertThrows(ValidacionException.class, () -> perfilService.crear(solicitud("", null, null)));
    }

    @Test
    void crear_conNombreDeSoloEspacios_lanzaValidacionException() {
        assertThrows(ValidacionException.class, () -> perfilService.crear(solicitud("   ", null, null)));
    }

    @Test
    void crear_conNombreDemasiadoCorto_lanzaValidacionException() {
        assertThrows(ValidacionException.class, () -> perfilService.crear(solicitud("A", null, null)));
    }

    @Test
    void crear_conNombreMinimoAceptaDosCaracteres() {
        prepararCreacion();

        PerfilDTO resultado = perfilService.crear(solicitud("AB", null, null));

        assertEquals("AB", resultado.getNombre());
    }

    @Test
    void crear_conNombreMaximoAceptaCuarentaCaracteres() {
        String nombre = "P".repeat(40);
        prepararCreacion();

        PerfilDTO resultado = perfilService.crear(solicitud(nombre, null, null));

        assertEquals(nombre, resultado.getNombre());
    }

    @Test
    void crear_conNombreDemasiadoLargo_lanzaValidacionException() {
        assertThrows(ValidacionException.class,
                () -> perfilService.crear(solicitud("P".repeat(41), null, null)));
    }

    @Test
    void crear_conNombreYAvatarEnBlanco_aplicaTrimYConvierteAvatarEnNull() {
        prepararCreacion();

        PerfilDTO resultado = perfilService.crear(solicitud("  Perfil Nuevo  ", "  ", null));

        Perfil perfilInsertado = perfilInsertado();
        assertEquals("Perfil Nuevo", perfilInsertado.getNombre());
        assertNull(perfilInsertado.getAvatar());
        assertFalse(perfilInsertado.isEsInfantil());
        assertEquals("Perfil Nuevo", resultado.getNombre());
        assertNull(resultado.getAvatar());
        assertFalse(resultado.isEsInfantil());
    }

    @Test
    void crear_conAvatarMaximoAceptaDoscientosCincuentaYCincoCaracteres() {
        String avatar = "a".repeat(255);
        prepararCreacion();

        PerfilDTO resultado = perfilService.crear(solicitud("Perfil", avatar, false));

        assertEquals(avatar, resultado.getAvatar());
        assertEquals(avatar, perfilInsertado().getAvatar());
    }

    @Test
    void crear_conAvatarDemasiadoLargo_lanzaValidacionException() {
        assertThrows(ValidacionException.class,
                () -> perfilService.crear(solicitud("Perfil", "a".repeat(256), false)));
    }

    @Test
    void crear_conEsInfantilNulo_aplicaFalse() {
        prepararCreacion();

        PerfilDTO resultado = perfilService.crear(solicitud("Perfil", null, null));

        assertFalse(resultado.isEsInfantil());
        assertFalse(perfilInsertado().isEsInfantil());
    }

    @Test
    void crear_conPerfilInfantil_almacenaElValorVerdadero() {
        prepararCreacion();

        PerfilDTO resultado = perfilService.crear(solicitud("Perfil", null, true));

        assertTrue(resultado.isEsInfantil());
        assertTrue(perfilInsertado().isEsInfantil());
    }

    @Test
    void crear_conNombreDuplicado_lanzaConflictoException() {
        when(perfilDAO.contarTodos()).thenReturn(3);
        when(perfilDAO.insertar(any(Perfil.class)))
                .thenThrow(new ClaveDuplicadaException("Duplicado", new RuntimeException()));

        ConflictoException excepcion = assertThrows(ConflictoException.class,
                () -> perfilService.crear(solicitud("Antonio", null, false)));

        assertTrue(excepcion.getMessage().contains("Antonio"));
    }

    @Test
    void crear_conQuintoPerfil_devuelveElPerfilCreado() {
        when(perfilDAO.contarTodos()).thenReturn(4);
        when(perfilDAO.insertar(any(Perfil.class))).thenReturn(5);
        when(perfilDAO.buscarPorId(5)).thenReturn(Optional.of(perfil(5, "Perfil Cinco", null, false)));

        PerfilDTO resultado = perfilService.crear(solicitud("Perfil Cinco", null, false));

        assertEquals(5, resultado.getId());
        verify(perfilDAO).insertar(any(Perfil.class));
    }

    @Test
    void crear_conSextoPerfil_lanzaConflictoYNoLlamaAInsertar() {
        when(perfilDAO.contarTodos()).thenReturn(5);

        ConflictoException excepcion = assertThrows(ConflictoException.class,
                () -> perfilService.crear(solicitud("Perfil Seis", null, false)));

        assertEquals("Se ha alcanzado el máximo de 5 perfiles", excepcion.getMessage());
        verify(perfilDAO, never()).insertar(any(Perfil.class));
    }

    @Test
    void buscarPorId_conPerfilInexistente_lanzaRecursoNoEncontradoException() {
        when(perfilDAO.buscarPorId(99)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> perfilService.buscarPorId(99));
    }

    @Test
    void actualizar_conPerfilInexistente_lanzaRecursoNoEncontradoException() {
        when(perfilDAO.buscarPorId(99)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> perfilService.actualizar(99, solicitud("Perfil", null, false)));
        verify(perfilDAO, never()).actualizar(any(Perfil.class));
    }

    @Test
    void actualizar_conNombreDuplicado_lanzaConflictoException() {
        when(perfilDAO.buscarPorId(2)).thenReturn(Optional.of(perfil(2, "Invitado", null, false)));
        when(perfilDAO.actualizar(any(Perfil.class)))
                .thenThrow(new ClaveDuplicadaException("Duplicado", new RuntimeException()));

        assertThrows(ConflictoException.class,
                () -> perfilService.actualizar(2, solicitud("Antonio", null, false)));
    }

    @Test
    void actualizar_conPerfilExistente_devuelveElPerfilActualizado() {
        when(perfilDAO.buscarPorId(2)).thenReturn(Optional.of(perfil(2, "Invitado", null, false)))
                .thenReturn(Optional.of(perfil(2, "Perfil Editado", "nuevo.png", true)));
        when(perfilDAO.actualizar(any(Perfil.class))).thenReturn(true);

        PerfilDTO resultado = perfilService.actualizar(2, solicitud("Perfil Editado", "nuevo.png", true));

        assertEquals("Perfil Editado", resultado.getNombre());
        assertTrue(resultado.isEsInfantil());
    }

    @Test
    void eliminar_conPerfilInexistente_lanzaRecursoNoEncontradoException() {
        when(perfilDAO.buscarPorId(99)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> perfilService.eliminar(99));
    }

    @Test
    void eliminar_conPerfilExistente_eliminaElPerfil() {
        when(perfilDAO.buscarPorId(1)).thenReturn(Optional.of(perfil(1, "Antonio", null, false)));
        when(perfilDAO.eliminar(1)).thenReturn(true);

        perfilService.eliminar(1);

        verify(perfilDAO).eliminar(1);
    }

    private void prepararCreacion() {
        when(perfilDAO.contarTodos()).thenReturn(3);
        when(perfilDAO.insertar(any(Perfil.class))).thenAnswer(invocacion -> {
            Perfil datos = invocacion.getArgument(0);
            perfilCreado = perfil(4, datos.getNombre(), datos.getAvatar(), datos.isEsInfantil());
            return 4;
        });
        when(perfilDAO.buscarPorId(4)).thenAnswer(invocacion -> Optional.ofNullable(perfilCreado));
    }

    private Perfil perfilInsertado() {
        ArgumentCaptor<Perfil> captor = ArgumentCaptor.forClass(Perfil.class);
        verify(perfilDAO).insertar(captor.capture());
        return captor.getValue();
    }

    private Perfil perfil(int id, String nombre, String avatar, boolean esInfantil) {
        return new Perfil(id, nombre, avatar, esInfantil, FECHA_CREACION);
    }

    private PerfilRequestDTO solicitud(String nombre, String avatar, Boolean esInfantil) {
        PerfilRequestDTO solicitud = new PerfilRequestDTO();
        solicitud.setNombre(nombre);
        solicitud.setAvatar(avatar);
        solicitud.setEsInfantil(esInfantil);
        return solicitud;
    }
}