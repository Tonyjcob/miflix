package com.antonio.miflix.dao;

import com.antonio.miflix.exception.ClaveDuplicadaException;
import com.antonio.miflix.model.Director;
import com.antonio.miflix.model.DirectorResumen;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class JdbcDirectorDAOTest {
    @Container
    private static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withDatabaseName("miflix_test")
            .withUsername("test")
            .withPassword("test");

    private static HikariDataSource dataSource;
    private JdbcDirectorDAO directorDAO;

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
        directorDAO = new JdbcDirectorDAO(dataSource);
    }

    @Test
    void buscarTodos_devuelveLosNueveDirectoresConTotalDePeliculas() {
        List<DirectorResumen> directores = directorDAO.buscarTodos();
        Map<String, Integer> totales = directores.stream()
                .collect(Collectors.toMap(DirectorResumen::getNombre, DirectorResumen::getTotalPeliculas));

        assertEquals(9, directores.size());
        assertEquals(1, totales.get("Sacha Goedegebure"));
        assertEquals(1, totales.get("Bassam Kurdali"));
        assertEquals(0, totales.get("Director Sin Películas"));
        assertNull(directores.stream()
                .filter(director -> director.getNombre().equals("Director Sin Películas"))
                .findFirst().orElseThrow().getNacionalidad());
    }

    @Test
    void buscarPorId_conDirectorExistente_devuelveElDirector() {
        Director director = directorDAO.buscarPorId(1).orElseThrow();

        assertEquals("Sacha Goedegebure", director.getNombre());
        assertEquals("Países Bajos", director.getNacionalidad());
    }

    @Test
    void buscarPorId_conDirectorInexistente_devuelveOptionalVacio() {
        assertTrue(directorDAO.buscarPorId(99).isEmpty());
    }

    @Test
    void insertar_conNombreNuevo_devuelveElIdGenerado() {
        int id = directorDAO.insertar(new Director(0, "Directora Nueva", "España"));

        assertTrue(id > 0);
        Director director = directorDAO.buscarPorId(id).orElseThrow();
        assertEquals("Directora Nueva", director.getNombre());
        assertEquals("España", director.getNacionalidad());
    }

    @Test
    void insertar_conNombreDuplicado_lanzaClaveDuplicadaException() {
        ClaveDuplicadaException excepcion = assertThrows(ClaveDuplicadaException.class,
                () -> directorDAO.insertar(new Director(0, "Sacha Goedegebure", null)));

        assertTrue(excepcion.getCause() instanceof SQLException);
    }

    @Test
    void actualizar_conDirectorExistente_devuelveVerdadero() {
        assertTrue(directorDAO.actualizar(new Director(9, "Director Actualizado", "Chile")));

        Director director = directorDAO.buscarPorId(9).orElseThrow();
        assertEquals("Director Actualizado", director.getNombre());
        assertEquals("Chile", director.getNacionalidad());
    }

    @Test
    void actualizar_conDirectorInexistente_devuelveFalso() {
        assertFalse(directorDAO.actualizar(new Director(99, "Director Nuevo", null)));
    }

    @Test
    void actualizar_conNombreDuplicado_lanzaClaveDuplicadaException() {
        assertThrows(ClaveDuplicadaException.class,
                () -> directorDAO.actualizar(new Director(9, "Sacha Goedegebure", null)));
    }

    @Test
    void eliminar_conDirectorSinPeliculas_devuelveVerdadero() {
        assertTrue(directorDAO.eliminar(9));
        assertTrue(directorDAO.buscarPorId(9).isEmpty());
    }

    @Test
    void eliminar_conDirectorConPeliculas_dejaBigBuckBunnySinDirector() throws SQLException {
        assertTrue(directorDAO.eliminar(1));

        assertTrue(directorDAO.buscarPorId(1).isEmpty());
        assertEquals(0, directorDAO.contarPeliculas(1));
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(
                     "SELECT director_id FROM pelicula WHERE titulo = ?")) {
            sentencia.setString(1, "Big Buck Bunny");
            try (ResultSet resultado = sentencia.executeQuery()) {
                assertTrue(resultado.next());
                assertNull(resultado.getObject("director_id"));
            }
        }
    }

    @Test
    void contarPeliculas_devuelveElTotalAsociadoAlDirector() {
        assertEquals(1, directorDAO.contarPeliculas(1));
        assertEquals(0, directorDAO.contarPeliculas(9));
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