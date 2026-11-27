package com.antonio.miflix.dao;

import com.antonio.miflix.exception.IntegridadReferencialException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers(disabledWithoutDocker = true)
class PeliculaTransaccionTest {
    @Container
    private static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withDatabaseName("miflix_test")
            .withUsername("test")
            .withPassword("test");

    private static HikariDataSource dataSource;
    private JdbcPeliculaDAO peliculaDAO;
    private JdbcPeliculaGeneroDAO peliculaGeneroDAO;
    private GestorTransacciones gestorTransacciones;

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
        gestorTransacciones = new GestorTransacciones(dataSource);
    }

    @Test
    void crear_conGeneroInexistente_revierteLaInsercionDeLaPelicula() {
        assertThrows(IntegridadReferencialException.class, () -> gestorTransacciones.ejecutar(conexion -> {
            int peliculaId = peliculaDAO.insertar(conexion,
                    new Pelicula(0, "Película atómica", 2024, 10, null, "atomica.mp4", null, null, null));
            peliculaGeneroDAO.insertarGeneros(conexion, peliculaId, List.of(1, 999));
            return peliculaId;
        }));

        assertEquals(8, peliculaDAO.buscarTodos(null, null, null).size());
    }

    @Test
    void actualizar_conGeneroInexistente_conservaPeliculaYGenerosAnteriores() {
        PeliculaResumen peliculaOriginal = peliculaDAO.buscarPorId(1).orElseThrow();
        List<Integer> generosOriginales = peliculaGeneroDAO.buscarGenerosPorPeliculas(List.of(1)).get(1).stream()
                .map(Genero::getId).toList();

        assertThrows(IntegridadReferencialException.class, () -> gestorTransacciones.ejecutar(conexion -> {
            peliculaDAO.actualizar(conexion, new Pelicula(1, "Nombre temporal", 2008, 10, null,
                    "ruta-temporal.mp4", null, 1, null));
            peliculaGeneroDAO.eliminarGenerosDePelicula(conexion, 1);
            peliculaGeneroDAO.insertarGeneros(conexion, 1, List.of(999));
            return null;
        }));

        PeliculaResumen peliculaRestaurada = peliculaDAO.buscarPorId(1).orElseThrow();
        List<Integer> generosRestaurados = peliculaGeneroDAO.buscarGenerosPorPeliculas(List.of(1)).get(1).stream()
                .map(Genero::getId).toList();
        assertEquals(peliculaOriginal.getTitulo(), peliculaRestaurada.getTitulo());
        assertEquals(peliculaOriginal.getRutaArchivo(), peliculaRestaurada.getRutaArchivo());
        assertEquals(generosOriginales, generosRestaurados);
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