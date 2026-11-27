package com.antonio.miflix.dao;

import com.antonio.miflix.exception.ClaveDuplicadaException;
import com.antonio.miflix.model.Perfil;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class JdbcPerfilDAOTest {
    @Container
    private static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withDatabaseName("miflix_test")
            .withUsername("test")
            .withPassword("test");

    private static HikariDataSource dataSource;
    private JdbcPerfilDAO perfilDAO;

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
        perfilDAO = new JdbcPerfilDAO(dataSource);
    }

    @Test
    void buscarTodos_devuelveLosTresPerfilesConFechaDeCreacion() {
        List<Perfil> perfiles = perfilDAO.buscarTodos();

        assertEquals(3, perfiles.size());
        assertEquals(List.of("Antonio", "Invitado", "Peques"),
                perfiles.stream().map(Perfil::getNombre).toList());
        assertNotNull(perfiles.getFirst().getFechaCreacion());
    }

    @Test
    void buscarPorId_conPerfilExistente_devuelveElPerfil() {
        Perfil perfil = perfilDAO.buscarPorId(1).orElseThrow();

        assertEquals("Antonio", perfil.getNombre());
        assertEquals("avatares/rojo.png", perfil.getAvatar());
        assertFalse(perfil.isEsInfantil());
        assertNotNull(perfil.getFechaCreacion());
    }

    @Test
    void buscarPorId_conPerfilInexistente_devuelveOptionalVacio() {
        assertTrue(perfilDAO.buscarPorId(99).isEmpty());
    }

    @Test
    void insertar_conNombreNuevo_devuelveElIdGenerado() {
        int id = perfilDAO.insertar(new Perfil(0, "Perfil Nuevo", null, true, null));

        assertTrue(id > 0);
        Perfil perfil = perfilDAO.buscarPorId(id).orElseThrow();
        assertEquals("Perfil Nuevo", perfil.getNombre());
        assertTrue(perfil.isEsInfantil());
        assertNotNull(perfil.getFechaCreacion());
    }

    @Test
    void insertar_conNombreDuplicado_lanzaClaveDuplicadaException() {
        ClaveDuplicadaException excepcion = assertThrows(ClaveDuplicadaException.class,
                () -> perfilDAO.insertar(new Perfil(0, "Antonio", null, false, null)));

        assertTrue(excepcion.getCause() instanceof SQLException);
    }

    @Test
    void actualizar_conPerfilExistente_devuelveVerdadero() {
        assertTrue(perfilDAO.actualizar(new Perfil(2, "Invitado Actualizado", "nuevo.png", true, null)));

        Perfil perfil = perfilDAO.buscarPorId(2).orElseThrow();
        assertEquals("Invitado Actualizado", perfil.getNombre());
        assertEquals("nuevo.png", perfil.getAvatar());
        assertTrue(perfil.isEsInfantil());
    }

    @Test
    void actualizar_conPerfilInexistente_devuelveFalso() {
        assertFalse(perfilDAO.actualizar(new Perfil(99, "Perfil Nuevo", null, false, null)));
    }

    @Test
    void actualizar_conNombreDuplicado_lanzaClaveDuplicadaException() {
        assertThrows(ClaveDuplicadaException.class,
                () -> perfilDAO.actualizar(new Perfil(2, "Antonio", null, false, null)));
    }

    @Test
    void eliminar_conPerfilSinProgreso_devuelveVerdadero() {
        assertTrue(perfilDAO.eliminar(2));
        assertTrue(perfilDAO.buscarPorId(2).isEmpty());
    }

    @Test
    void eliminar_conPerfilConProgreso_borraSusRegistrosEnCascada() throws SQLException {
        assertEquals(3, contarProgresos(1));

        assertTrue(perfilDAO.eliminar(1));

        assertTrue(perfilDAO.buscarPorId(1).isEmpty());
        assertEquals(0, contarProgresos(1));
    }

    @Test
    void contarTodos_devuelveElNumeroDePerfiles() {
        assertEquals(3, perfilDAO.contarTodos());
    }

    private int contarProgresos(int perfilId) throws SQLException {
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(
                     "SELECT COUNT(*) FROM progreso WHERE perfil_id = ?")) {
            sentencia.setInt(1, perfilId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        }
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