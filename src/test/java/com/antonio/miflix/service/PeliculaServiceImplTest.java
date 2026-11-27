package com.antonio.miflix.service;

import com.antonio.miflix.dao.DirectorDAO;
import com.antonio.miflix.dao.GeneroDAO;
import com.antonio.miflix.dao.GestorTransacciones;
import com.antonio.miflix.dao.PeliculaDAO;
import com.antonio.miflix.dao.PeliculaGeneroDAO;
import com.antonio.miflix.dto.PeliculaDTO;
import com.antonio.miflix.dto.PeliculaRequestDTO;
import com.antonio.miflix.exception.ClaveDuplicadaException;
import com.antonio.miflix.exception.ConflictoException;
import com.antonio.miflix.exception.IntegridadReferencialException;
import com.antonio.miflix.exception.RecursoNoEncontradoException;
import com.antonio.miflix.exception.ValidacionDatosException;
import com.antonio.miflix.exception.ValidacionException;
import com.antonio.miflix.model.Director;
import com.antonio.miflix.model.Genero;
import com.antonio.miflix.model.Pelicula;
import com.antonio.miflix.model.PeliculaResumen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeliculaServiceImplTest {
    @Mock
    private PeliculaDAO peliculaDAO;
    @Mock
    private PeliculaGeneroDAO peliculaGeneroDAO;
    @Mock
    private DirectorDAO directorDAO;
    @Mock
    private GeneroDAO generoDAO;
    @Mock
    private GestorTransacciones gestorTransacciones;
    @Mock
    private AlmacenMedios almacenMedios;

    private PeliculaServiceImpl peliculaService;

    @BeforeEach
    void prepararServicio() {
        peliculaService = new PeliculaServiceImpl(peliculaDAO, peliculaGeneroDAO, directorDAO, generoDAO,
                gestorTransacciones, almacenMedios);
        lenient().when(almacenMedios.esRutaSegura(anyString())).thenReturn(true);
        lenient().when(almacenMedios.existe(anyString())).thenReturn(true);
        lenient().when(generoDAO.buscarPorId(1)).thenReturn(Optional.of(new Genero(1, "Animación")));
        lenient().when(directorDAO.buscarPorId(1)).thenReturn(Optional.of(new Director(1, "Directora", null)));
        lenient().when(gestorTransacciones.ejecutar(any())).thenAnswer(invocacion -> {
            Function<Connection, Object> operacion = invocacion.getArgument(0);
            return operacion.apply(mock(Connection.class));
        });
    }

    @Test
    void crear_conSolicitudNula_lanzaValidacionException() {
        assertThrows(ValidacionException.class, () -> peliculaService.crear(null));
        verificarNoIniciaTransaccion();
    }

    @Test
    void crear_conTituloNuloVacioOVacioTrasTrim_lanzaValidacionException() {
        for (String titulo : new String[]{null, "", "   "}) {
            PeliculaRequestDTO solicitud = solicitudValida();
            solicitud.setTitulo(titulo);
            assertThrows(ValidacionException.class, () -> peliculaService.crear(solicitud));
        }
        verificarNoIniciaTransaccion();
    }

    @Test
    void crear_conTituloMayorDe200Caracteres_lanzaValidacionException() {
        PeliculaRequestDTO solicitud = solicitudValida();
        solicitud.setTitulo("T".repeat(201));

        assertThrows(ValidacionException.class, () -> peliculaService.crear(solicitud));
        verificarNoIniciaTransaccion();
    }

    @Test
    void crear_conAnioNuloMenorAlMinimoOFuturo_lanzaValidacionException() {
        for (Integer anio : new Integer[]{null, 1887, Year.now().getValue() + 1}) {
            PeliculaRequestDTO solicitud = solicitudValida();
            solicitud.setAnio(anio);
            assertThrows(ValidacionException.class, () -> peliculaService.crear(solicitud));
        }
        verificarNoIniciaTransaccion();
    }

    @Test
    void crear_conDuracionNulaCeroOMayorA999_lanzaValidacionException() {
        for (Integer duracion : new Integer[]{null, 0, 1000}) {
            PeliculaRequestDTO solicitud = solicitudValida();
            solicitud.setDuracionMin(duracion);
            assertThrows(ValidacionException.class, () -> peliculaService.crear(solicitud));
        }
        verificarNoIniciaTransaccion();
    }

    @Test
    void crear_conSinopsisMayorDe2000Caracteres_lanzaValidacionException() {
        PeliculaRequestDTO solicitud = solicitudValida();
        solicitud.setSinopsis("S".repeat(2001));

        assertThrows(ValidacionException.class, () -> peliculaService.crear(solicitud));
        verificarNoIniciaTransaccion();
    }

    @Test
    void crear_conSinopsisDe2000CaracteresYLimitesMinimos_losAcepta() {
        PeliculaRequestDTO solicitud = solicitudValida();
        solicitud.setTitulo("A");
        solicitud.setAnio(1888);
        solicitud.setDuracionMin(1);
        solicitud.setSinopsis("S".repeat(2000));
        configurarCreacion(16, null);

        peliculaService.crear(solicitud);

        ArgumentCaptor<Pelicula> peliculaCaptor = ArgumentCaptor.forClass(Pelicula.class);
        verify(peliculaDAO).insertar(any(Connection.class), peliculaCaptor.capture());
        assertEquals("A", peliculaCaptor.getValue().getTitulo());
        assertEquals(1, peliculaCaptor.getValue().getDuracionMin());
        assertEquals(2000, peliculaCaptor.getValue().getSinopsis().length());
    }

    @Test
    void crear_conRutaArchivoNulaOVacia_lanzaValidacionException() {
        for (String ruta : new String[]{null, "", "   "}) {
            PeliculaRequestDTO solicitud = solicitudValida();
            solicitud.setRutaArchivo(ruta);
            assertThrows(ValidacionException.class, () -> peliculaService.crear(solicitud));
        }
        verificarNoIniciaTransaccion();
    }

    @Test
    void crear_conExtensionNoPermitida_lanzaValidacionException() {
        PeliculaRequestDTO solicitud = solicitudValida();
        solicitud.setRutaArchivo("video.avi");

        assertThrows(ValidacionException.class, () -> peliculaService.crear(solicitud));
        verify(almacenMedios, never()).existe("video.avi");
        verificarNoIniciaTransaccion();
    }

    @Test
    void crear_conPathTraversal_lanzaValidacionException() {
        PeliculaRequestDTO solicitud = solicitudValida();
        solicitud.setRutaArchivo("../secreto.mp4");
        when(almacenMedios.esRutaSegura("../secreto.mp4")).thenReturn(false);

        assertThrows(ValidacionException.class, () -> peliculaService.crear(solicitud));
        verify(almacenMedios, never()).existe("../secreto.mp4");
        verificarNoIniciaTransaccion();
    }

    @Test
    void crear_conArchivoInexistente_lanzaValidacionException() {
        PeliculaRequestDTO solicitud = solicitudValida();
        when(almacenMedios.existe("video.mp4")).thenReturn(false);

        assertThrows(ValidacionException.class, () -> peliculaService.crear(solicitud));
        verificarNoIniciaTransaccion();
    }

    @Test
    void crear_conRutaPosterInseguraOMayorDe500_lanzaValidacionException() {
        PeliculaRequestDTO rutaInsegura = solicitudValida();
        rutaInsegura.setRutaPoster("../secreto.jpg");
        when(almacenMedios.esRutaSegura("../secreto.jpg")).thenReturn(false);
        assertThrows(ValidacionException.class, () -> peliculaService.crear(rutaInsegura));

        PeliculaRequestDTO rutaLarga = solicitudValida();
        rutaLarga.setRutaPoster("p".repeat(501));
        assertThrows(ValidacionException.class, () -> peliculaService.crear(rutaLarga));
        verificarNoIniciaTransaccion();
    }

    @Test
    void crear_conRutaPosterDe500Caracteres_laAceptaSinComprobarExistencia() {
        PeliculaRequestDTO solicitud = solicitudValida();
        String rutaPoster = "p".repeat(496) + ".jpg";
        solicitud.setRutaPoster(rutaPoster);
        when(almacenMedios.esRutaSegura(rutaPoster)).thenReturn(true);
        configurarCreacion(17, null);

        peliculaService.crear(solicitud);

        ArgumentCaptor<Pelicula> peliculaCaptor = ArgumentCaptor.forClass(Pelicula.class);
        verify(peliculaDAO).insertar(any(Connection.class), peliculaCaptor.capture());
        assertEquals(rutaPoster, peliculaCaptor.getValue().getRutaPoster());
        verify(almacenMedios, never()).existe(rutaPoster);
    }

    @Test
    void crear_sinGeneros_lanzaValidacionException() {
        PeliculaRequestDTO solicitud = solicitudValida();
        solicitud.setGeneroIds(List.of());

        assertThrows(ValidacionException.class, () -> peliculaService.crear(solicitud));
        verificarNoIniciaTransaccion();
    }

    @Test
    void crear_conDirectorInexistente_lanzaValidacionConIdentificador() {
        PeliculaRequestDTO solicitud = solicitudValida();
        solicitud.setDirectorId(99);
        when(directorDAO.buscarPorId(99)).thenReturn(Optional.empty());

        ValidacionException excepcion = assertThrows(ValidacionException.class,
                () -> peliculaService.crear(solicitud));

        assertEquals("El director con id 99 no existe.", excepcion.getMessage());
        verificarNoIniciaTransaccion();
    }

    @Test
    void crear_conGeneroInexistente_lanzaValidacionConIdentificador() {
        PeliculaRequestDTO solicitud = solicitudValida();
        solicitud.setGeneroIds(List.of(999));
        when(generoDAO.buscarPorId(999)).thenReturn(Optional.empty());

        ValidacionException excepcion = assertThrows(ValidacionException.class,
                () -> peliculaService.crear(solicitud));

        assertEquals("El género con id 999 no existe.", excepcion.getMessage());
        verificarNoIniciaTransaccion();
    }

    @Test
    void crear_conSolicitudValida_normalizaCamposDeduplicaGenerosYDevuelveDetalle() {
        PeliculaRequestDTO solicitud = solicitudValida();
        solicitud.setTitulo("  Mi película  ");
        solicitud.setAnio(1888);
        solicitud.setDuracionMin(999);
        solicitud.setSinopsis("   ");
        solicitud.setRutaArchivo(" video.MP4 ");
        solicitud.setRutaPoster("   ");
        solicitud.setDirectorId(1);
        solicitud.setGeneroIds(List.of(1, 1, 1));
        when(almacenMedios.esRutaSegura("video.MP4")).thenReturn(true);
        when(almacenMedios.existe("video.MP4")).thenReturn(true);
        configurarCreacion(15, 1);

        PeliculaDTO resultado = peliculaService.crear(solicitud);

        assertEquals(15, resultado.getId());
        assertEquals("Mi película", resultado.getTitulo());
        assertEquals("Directora", resultado.getDirector().getNombre());
        assertEquals(List.of("Animación"), resultado.getGeneros().stream().map(genero -> genero.getNombre()).toList());
        ArgumentCaptor<Pelicula> peliculaCaptor = ArgumentCaptor.forClass(Pelicula.class);
        verify(peliculaDAO).insertar(any(Connection.class), peliculaCaptor.capture());
        assertEquals("Mi película", peliculaCaptor.getValue().getTitulo());
        assertEquals(1888, peliculaCaptor.getValue().getAnio());
        assertEquals(999, peliculaCaptor.getValue().getDuracionMin());
        assertNull(peliculaCaptor.getValue().getSinopsis());
        assertEquals("video.MP4", peliculaCaptor.getValue().getRutaArchivo());
        assertNull(peliculaCaptor.getValue().getRutaPoster());
        verify(peliculaGeneroDAO).insertarGeneros(any(Connection.class), eq(15), eq(List.of(1)));
    }

    @Test
    void crear_conClaveDuplicada_lanzaConflictoConMensajeClaro() {
        configurarGenero(1);
        when(peliculaDAO.insertar(any(Connection.class), any(Pelicula.class)))
                .thenThrow(new ClaveDuplicadaException("Ya existe una película con el mismo título y año.", null));

        ConflictoException excepcion = assertThrows(ConflictoException.class,
                () -> peliculaService.crear(solicitudValida()));

        assertTrue(excepcion.getMessage().contains("título y año"));
    }

    @Test
    void crear_conErrorDeIntegridadOCheck_lanzaValidacionException() {
        configurarGenero(1);
        when(peliculaDAO.insertar(any(Connection.class), any(Pelicula.class)))
                .thenThrow(new IntegridadReferencialException("La referencia apunta a un registro que no existe.", null));

        assertThrows(ValidacionException.class, () -> peliculaService.crear(solicitudValida()));

        when(peliculaDAO.insertar(any(Connection.class), any(Pelicula.class)))
                .thenThrow(new ValidacionDatosException("Los datos incumplen una restricción CHECK.", null));
        assertThrows(ValidacionException.class, () -> peliculaService.crear(solicitudValida()));
    }

    @Test
    void buscarTodos_cargaGenerosEnUnaConsultaYMapeaElDirector() {
        PeliculaResumen una = resumen(1, "Una", 1, "Directora");
        PeliculaResumen dos = resumen(2, "Dos", null, null);
        when(peliculaDAO.buscarTodos("a", 1, null)).thenReturn(List.of(una, dos));
        when(peliculaGeneroDAO.buscarGenerosPorPeliculas(List.of(1, 2))).thenReturn(Map.of(
                1, List.of(new Genero(2, "Comedia")),
                2, List.of(new Genero(1, "Animación"))));

        List<PeliculaDTO> resultado = peliculaService.buscarTodos("a", 1, null);

        assertEquals(2, resultado.size());
        assertEquals("Directora", resultado.getFirst().getDirector().getNombre());
        assertNull(resultado.get(1).getDirector());
        verify(peliculaGeneroDAO).buscarGenerosPorPeliculas(List.of(1, 2));
    }

    @Test
    void buscarPorId_conPeliculaInexistente_lanzaRecursoNoEncontradoException() {
        when(peliculaDAO.buscarPorId(999)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> peliculaService.buscarPorId(999));
    }

    @Test
    void actualizar_conPeliculaInexistente_lanza404AntesDeTransaccion() {
        when(peliculaDAO.buscarPorId(999)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> peliculaService.actualizar(999, solicitudValida()));
        verificarNoIniciaTransaccion();
    }

    @Test
    void actualizar_conPeliculaExistente_actualizaDatosYAsociacionesEnLaMismaTransaccion() {
        when(peliculaDAO.buscarPorId(5)).thenReturn(Optional.of(resumen(5, "Anterior", null, null)))
                .thenReturn(Optional.of(resumen(5, "Nueva", null, null)));
        when(peliculaDAO.actualizar(any(Connection.class), any(Pelicula.class))).thenReturn(true);
        when(peliculaGeneroDAO.buscarGenerosPorPeliculas(List.of(5))).thenReturn(Map.of(5, List.of()));
        configurarGenero(1);

        PeliculaDTO resultado = peliculaService.actualizar(5, solicitudValida());

        assertEquals("Nueva", resultado.getTitulo());
        verify(peliculaDAO).actualizar(any(Connection.class), any(Pelicula.class));
        verify(peliculaGeneroDAO).eliminarGenerosDePelicula(any(Connection.class), eq(5));
        verify(peliculaGeneroDAO).insertarGeneros(any(Connection.class), eq(5), eq(List.of(1)));
    }

    @Test
    void eliminar_conPeliculaInexistente_lanzaRecursoNoEncontradoException() {
        when(peliculaDAO.buscarPorId(999)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> peliculaService.eliminar(999));
        verificarNoIniciaTransaccion();
    }

    @Test
    void eliminar_conPeliculaExistente_ejecutaBorradoTransaccional() {
        when(peliculaDAO.buscarPorId(8)).thenReturn(Optional.of(resumen(8, "Sprite Fright", null, null)));
        when(peliculaDAO.eliminar(any(Connection.class), eq(8))).thenReturn(true);

        peliculaService.eliminar(8);

        verify(peliculaDAO).eliminar(any(Connection.class), eq(8));
    }

    private void configurarCreacion(int id, Integer directorId) {
        configurarGenero(1);
        if (directorId != null) {
            when(directorDAO.buscarPorId(directorId))
                    .thenReturn(Optional.of(new Director(directorId, "Directora", null)));
        }
        when(peliculaDAO.insertar(any(Connection.class), any(Pelicula.class))).thenReturn(id);
        when(peliculaDAO.buscarPorId(id)).thenReturn(Optional.of(resumen(id, "Mi película", directorId,
                directorId == null ? null : "Directora")));
        when(peliculaGeneroDAO.buscarGenerosPorPeliculas(List.of(id)))
                .thenReturn(Map.of(id, List.of(new Genero(1, "Animación"))));
    }

    private void configurarGenero(int id) {
        lenient().when(generoDAO.buscarPorId(id)).thenReturn(Optional.of(new Genero(id, "Animación")));
    }

    private void verificarNoIniciaTransaccion() {
        verify(gestorTransacciones, never()).ejecutar(any());
    }

    private PeliculaRequestDTO solicitudValida() {
        PeliculaRequestDTO solicitud = new PeliculaRequestDTO();
        solicitud.setTitulo("Mi película");
        solicitud.setAnio(2024);
        solicitud.setDuracionMin(100);
        solicitud.setSinopsis("Una sinopsis");
        solicitud.setRutaArchivo("video.mp4");
        solicitud.setRutaPoster("poster.jpg");
        solicitud.setGeneroIds(List.of(1));
        return solicitud;
    }

    private PeliculaResumen resumen(int id, String titulo, Integer directorId, String nombreDirector) {
        return new PeliculaResumen(id, titulo, 2024, 100, null, "video.mp4", null, directorId, nombreDirector,
                LocalDateTime.now());
    }
}