package com.antonio.miflix.dao;

import com.antonio.miflix.exception.ClaveDuplicadaException;
import com.antonio.miflix.exception.IntegridadReferencialException;
import com.antonio.miflix.model.Genero;
import com.antonio.miflix.model.GeneroResumen;
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
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class JdbcGeneroDAOTest {
    @Container
    private static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withDatabaseName("miflix_test")
            .withUsername("test")
            .withPassword("test");

    private static HikariDataSource dataSource;
    private JdbcGeneroDAO generoDAO;

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
        generoDAO = new JdbcGeneroDAO(dataSource);
    }

    @Test
    void buscarTodos_devuelveLosOchoGenerosConTotalDePeliculas() {
        List<GeneroResumen> generos = generoDAO.buscarTodos();
        assertEquals(List.of("Acción", "Animación", "Ciencia ficción", "Comedia", "Documental", "Drama",
                        "Fantasía", "Terror"),
                generos.stream().map(GeneroResumen::getNombre).toList());

        Map<String, Integer> totales = generos.stream()
                .collect(Collectors.toMap(GeneroResumen::getNombre, GeneroResumen::getTotalPeliculas));

        assertEquals(8, totales.size());
        assertEquals(7, totales.get("Animación"));
        assertEquals(4, totales.get("Comedia"));
        assertEquals(3, totales.get("Fantasía"));
        assertEquals(2, totales.get("Ciencia ficción"));
        assertEquals(2, totales.get("Acción"));
        assertEquals(1, totales.get("Drama"));
        assertEquals(1, totales.get("Terror"));
        assertEquals(0, totales.get("Documental"));
    }

    @Test
    void buscarPorId_conGeneroExistente_devuelveElGenero() {
        assertEquals("Animación", generoDAO.buscarPorId(1).orElseThrow().getNombre());
    }

    @Test
    void buscarPorId_conGeneroInexistente_devuelveOptionalVacio() {
        assertTrue(generoDAO.buscarPorId(99).isEmpty());
    }

    @Test
    void insertar_conNombreNuevo_devuelveElIdGenerado() {
        int id = generoDAO.insertar(new Genero(0, "Western"));

        assertTrue(id > 0);
        assertEquals("Western", generoDAO.buscarPorId(id).orElseThrow().getNombre());
    }

    @Test
    void insertar_conNombreDuplicado_lanzaClaveDuplicadaException() {
        ClaveDuplicadaException excepcion = assertThrows(ClaveDuplicadaException.class,
                () -> generoDAO.insertar(new Genero(0, "Animación")));

        assertTrue(excepcion.getCause() instanceof SQLException);
    }

    @Test
    void actualizar_conGeneroExistente_devuelveVerdadero() {
        assertTrue(generoDAO.actualizar(new Genero(8, "Archivo")));
        assertEquals("Archivo", generoDAO.buscarPorId(8).orElseThrow().getNombre());
    }

    @Test
    void actualizar_conGeneroInexistente_devuelveFalso() {
        assertFalse(generoDAO.actualizar(new Genero(99, "Archivo")));
    }

    @Test
    void eliminar_conGeneroSinPeliculas_devuelveVerdadero() {
        assertTrue(generoDAO.eliminar(8));
        assertTrue(generoDAO.buscarPorId(8).isEmpty());
    }

    @Test
    void eliminar_conGeneroConPeliculas_lanzaIntegridadReferencialException() {
        assertThrows(IntegridadReferencialException.class, () -> generoDAO.eliminar(1));
    }

    @Test
    void contarPeliculas_conGeneroVacioYAsociado_devuelveTotales() {
        assertEquals(0, generoDAO.contarPeliculas(8));
        assertEquals(7, generoDAO.contarPeliculas(1));
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