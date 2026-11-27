package com.antonio.miflix.dao;

import com.antonio.miflix.exception.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GestorTransaccionesTest {
    private DataSource dataSource;
    private Connection conexion;
    private GestorTransacciones gestorTransacciones;

    @BeforeEach
    void prepararGestor() throws SQLException {
        dataSource = mock(DataSource.class);
        conexion = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(conexion);
        when(conexion.getAutoCommit()).thenReturn(true);
        gestorTransacciones = new GestorTransacciones(dataSource);
    }

    @Test
    void ejecutar_conOperacionCorrecta_confirmaYRestableceAutoCommit() throws SQLException {
        String resultado = gestorTransacciones.ejecutar(connection -> "confirmado");

        assertEquals("confirmado", resultado);
        verify(conexion).setAutoCommit(false);
        verify(conexion).commit();
        verify(conexion, never()).rollback();
        verify(conexion).setAutoCommit(true);
        verify(conexion).close();
    }

    @Test
    void ejecutar_conOperacionFallida_revierteYRestableceAutoCommit() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> gestorTransacciones.ejecutar(connection -> {
            throw new IllegalArgumentException("fallo de prueba");
        }));

        verify(conexion).setAutoCommit(false);
        verify(conexion, never()).commit();
        verify(conexion).rollback();
        verify(conexion).setAutoCommit(true);
        verify(conexion).close();
    }

    @Test
    void ejecutar_conFalloDeRollback_conservaExcepcionOriginalYcierraConexion() throws SQLException {
        IllegalStateException falloOriginal = new IllegalStateException("fallo de operación");
        SQLException falloRollback = new SQLException("fallo de rollback");
        doThrow(falloRollback).when(conexion).rollback();

        IllegalStateException excepcion = assertThrows(IllegalStateException.class,
                () -> gestorTransacciones.ejecutar(connection -> {
                    throw falloOriginal;
                }));

        assertSame(falloOriginal, excepcion);
        assertEquals(1, excepcion.getSuppressed().length);
        assertSame(falloRollback, excepcion.getSuppressed()[0]);
        verify(conexion).rollback();
        verify(conexion).setAutoCommit(true);
        verify(conexion).close();
    }

    @Test
    void ejecutar_conFalloDeConexion_alObtenerAutoCommit_traduceLaExcepcion() throws SQLException {
        when(conexion.getAutoCommit()).thenThrow(new SQLException("fallo de conexión"));

        assertThrows(DataAccessException.class, () -> gestorTransacciones.ejecutar(connection -> null));

        verify(conexion, never()).setAutoCommit(false);
        verify(conexion).close();
    }
}