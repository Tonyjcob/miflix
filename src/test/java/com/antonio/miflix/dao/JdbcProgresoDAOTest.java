package com.antonio.miflix.dao;

import com.antonio.miflix.model.Progreso;
import com.antonio.miflix.model.ProgresoContinuarViendoResumen;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class JdbcProgresoDAOTest {
    @Container
    private static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withDatabaseName("miflix_test")
            .withUsername("test")
            .withPassword("test");

    private static HikariDataSource dataSource;
    private JdbcProgresoDAO progresoDAO;

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
        progresoDAO = new JdbcProgresoDAO(dataSource);
    }

    @Test
    void guardar_conClaveCompuestaExistente_actualizaElProgreso() {
        progresoDAO.guardar(new Progreso(1, 1, 120, false, null));
        Progreso inicial = progresoDAO.buscarPorPerfilYPelicula(1, 1).orElseThrow();

        progresoDAO.guardar(new Progreso(1, 1, 570, true, null));
        Progreso actualizado = progresoDAO.buscarPorPerfilYPelicula(1, 1).orElseThrow();

        assertEquals(570, actualizado.getSegundoActual());
        assertTrue(actualizado.isCompletada());
        assertNotNull(actualizado.getUltimaVez());
        assertFalse(actualizado.getUltimaVez().isBefore(inicial.getUltimaVez()));
    }

    @Test
    void buscarNoCompletadosPorPerfil_excluyeCompletadasYOrdenaPorUltimaVezDescendente() {
        List<ProgresoContinuarViendoResumen> progresos = progresoDAO.buscarNoCompletadosPorPerfil(1);

        assertEquals(List.of("Cosmos Laundromat", "Sintel"),
                progresos.stream().map(ProgresoContinuarViendoResumen::getTitulo).toList());
        assertTrue(progresos.getFirst().getUltimaVez().isAfter(progresos.getLast().getUltimaVez()));
    }

    @Test
    void eliminarPerfil_oPelicula_eliminaProgresosEnCascada() throws SQLException {
        PerfilDAO perfilDAO = new JdbcPerfilDAO(dataSource);
        assertTrue(progresoDAO.buscarPorPerfilYPelicula(1, 3).isPresent());
        assertTrue(perfilDAO.eliminar(1));
        assertTrue(progresoDAO.buscarPorPerfilYPelicula(1, 3).isEmpty());
        assertTrue(progresoDAO.buscarPorPerfilYPelicula(2, 1).isPresent());

        PeliculaDAO peliculaDAO = new JdbcPeliculaDAO(dataSource);
        try (Connection conexion = dataSource.getConnection()) {
            assertTrue(peliculaDAO.eliminar(conexion, 1));
        }

        assertTrue(progresoDAO.buscarPorPerfilYPelicula(2, 1).isEmpty());
        assertTrue(progresoDAO.buscarPorPerfilYPelicula(3, 1).isEmpty());
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