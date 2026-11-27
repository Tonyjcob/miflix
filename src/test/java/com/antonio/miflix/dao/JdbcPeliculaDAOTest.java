package com.antonio.miflix.dao;

import com.antonio.miflix.exception.IntegridadReferencialException;
import com.antonio.miflix.exception.ValidacionDatosException;
import com.antonio.miflix.model.Genero;
import com.antonio.miflix.model.Pelicula;
import com.antonio.miflix.model.PeliculaResumen;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class JdbcPeliculaDAOTest {
    @Container
    private static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withDatabaseName("miflix_test")
            .withUsername("test")
            .withPassword("test");

    private static HikariDataSource dataSource;
    private JdbcPeliculaDAO peliculaDAO;
    private JdbcPeliculaGeneroDAO peliculaGeneroDAO;

    @BeforeAll
    static void prepararBaseDeDatos() {
        HikariConfig configuracion = new HikariConfig();
        configuracion.setJdbcUrl(MYSQL.getJdbcUrl());
        configuracion.setUsername(MYSQL.getUsername());
        configuracion.setPassword(MYSQL.getPassword());
        dataSource = new HikariDataSource(configuracion);
    }

    @AfterAll
    static void cerrarDataSource() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @BeforeEach
    void restablecerBaseDeDatos() throws IOException, SQLException {
        cargarScriptInicial();
        peliculaDAO = new JdbcPeliculaDAO(dataSource);
        peliculaGeneroDAO = new JdbcPeliculaGeneroDAO(dataSource);
    }

    @Test
    void buscarTodos_sinFiltros_devuelveLasOchoPeliculasOrdenadasPorTitulo() {
        List<PeliculaResumen> peliculas = peliculaDAO.buscarTodos(null, null, null);
        List<String> titulos = peliculas.stream().map(PeliculaResumen::getTitulo).toList();

        assertEquals(8, peliculas.size());
        assertEquals(titulos.stream().sorted().toList(), titulos);
    }

    @Test
    void buscarTodos_conFiltrosIndividualesYCombinados_devuelveCoincidencias() {
        assertEquals(1, peliculaDAO.buscarTodos("bUnNy", null, null).size());
        assertEquals(7, peliculaDAO.buscarTodos(null, 1, null).size());
        assertEquals(1, peliculaDAO.buscarTodos(null, null, 1).size());
        List<PeliculaResumen> filtradas = peliculaDAO.buscarTodos("bunny", 1, 1);

        assertEquals(1, filtradas.size());
        assertEquals("Big Buck Bunny", filtradas.getFirst().getTitulo());
    }

    @Test
    void buscarPorId_conPeliculaExistente_devuelvePeliculaDirectorYGenerosOrdenados() {
        PeliculaResumen pelicula = peliculaDAO.buscarPorId(1).orElseThrow();
        Map<Integer, List<Genero>> generos = peliculaGeneroDAO.buscarGenerosPorPeliculas(List.of(1));

        assertEquals("Big Buck Bunny", pelicula.getTitulo());
        assertEquals(1, pelicula.getDirectorId());
        assertEquals("Sacha Goedegebure", pelicula.getNombreDirector());
        assertNotNull(pelicula.getFechaAlta());
        assertEquals(List.of("Animación", "Comedia"),
                generos.get(1).stream().map(Genero::getNombre).toList());
    }

    @Test
    void buscarPorId_conPeliculaInexistente_devuelveOptionalVacio() {
        assertTrue(peliculaDAO.buscarPorId(999).isEmpty());
    }

    @Test
    void insertarYActualizar_conConexionCompartida_persisteLosCambios() throws SQLException {
        int id;
        try (Connection conexion = dataSource.getConnection()) {
            conexion.setAutoCommit(false);
            id = peliculaDAO.insertar(conexion, pelicula("Película nueva", "pelicula-nueva.mp4", null, 2024));
            conexion.commit();
        }
        assertTrue(id > 0);
        assertEquals("Película nueva", peliculaDAO.buscarPorId(id).orElseThrow().getTitulo());

        Pelicula actualizada = peliculaDAO.buscarPorId(id).map(resumen -> pelicula(resumen.getTitulo(),
                "pelicula-nueva.webm", 2, 2024)).orElseThrow();
        actualizada.setId(id);
        try (Connection conexion = dataSource.getConnection()) {
            conexion.setAutoCommit(false);
            assertTrue(peliculaDAO.actualizar(conexion, actualizada));
            conexion.commit();
        }

        PeliculaResumen guardada = peliculaDAO.buscarPorId(id).orElseThrow();
        assertEquals("pelicula-nueva.webm", guardada.getRutaArchivo());
        assertEquals(2, guardada.getDirectorId());
    }

    @Test
    void insertar_conAnioFueraDelCheck_lanzaValidacionDatosException() throws SQLException {
        try (Connection conexion = dataSource.getConnection()) {
            assertThrows(ValidacionDatosException.class,
                    () -> peliculaDAO.insertar(conexion, pelicula("Año inválido", "anio-invalido.mp4", null, 1800)));
        }
    }

    @Test
    void insertar_conDirectorInexistente_lanzaIntegridadReferencialException() throws SQLException {
        try (Connection conexion = dataSource.getConnection()) {
            assertThrows(IntegridadReferencialException.class,
                    () -> peliculaDAO.insertar(conexion,
                            pelicula("Director inválido", "director-invalido.mp4", 999, 2024)));
        }
    }

    @Test
    void asociaciones_batchCargaEnLoteYEliminaGeneros_dejaListaVacia() throws SQLException {
        int peliculaId;
        try (Connection conexion = dataSource.getConnection()) {
            conexion.setAutoCommit(false);
            peliculaId = peliculaDAO.insertar(conexion,
                    pelicula("Asociación nueva", "asociacion-nueva.mp4", null, 2024));
            peliculaGeneroDAO.insertarGeneros(conexion, peliculaId, List.of(1, 2));
            conexion.commit();
        }

        assertEquals(List.of("Animación", "Comedia"), peliculaGeneroDAO.buscarGenerosPorPeliculas(List.of(peliculaId))
                .get(peliculaId).stream().map(Genero::getNombre).toList());

        try (Connection conexion = dataSource.getConnection()) {
            conexion.setAutoCommit(false);
            peliculaGeneroDAO.eliminarGenerosDePelicula(conexion, peliculaId);
            conexion.commit();
        }
        assertTrue(peliculaGeneroDAO.buscarGenerosPorPeliculas(List.of(peliculaId)).get(peliculaId).isEmpty());
        assertEquals(Map.of(), peliculaGeneroDAO.buscarGenerosPorPeliculas(List.of()));
    }

    @Test
    void eliminar_conPeliculaExistente_eliminaRegistro() throws SQLException {
        try (Connection conexion = dataSource.getConnection()) {
            conexion.setAutoCommit(false);
            assertTrue(peliculaDAO.eliminar(conexion, 1));
            conexion.commit();
        }

        assertFalse(peliculaDAO.buscarPorId(1).isPresent());
    }

    private static Pelicula pelicula(String titulo, String ruta, Integer directorId, int anio) {
        return new Pelicula(0, titulo, anio, 10, "Sinopsis de prueba", ruta, null, directorId, null);
    }

    private static void cargarScriptInicial() throws IOException, SQLException {
        String script = Files.readString(Path.of("db", "init.sql"), StandardCharsets.UTF_8);
        String scriptSinComentariosDeLinea = script.lines()
                .filter(linea -> !linea.stripLeading().startsWith("--"))
                .reduce("", (acumulado, linea) -> acumulado + linea + "\n");

        try (Connection conexion = dataSource.getConnection();
             Statement sentencia = conexion.createStatement()) {
            for (String sql : Arrays.stream(scriptSinComentariosDeLinea.split(";"))
                    .map(String::trim)
                    .filter(fragmento -> !fragmento.isEmpty())
                    .toList()) {
                sentencia.execute(sql);
            }
        }
    }
}