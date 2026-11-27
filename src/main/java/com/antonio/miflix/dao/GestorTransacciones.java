package com.antonio.miflix.dao;

import com.antonio.miflix.exception.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;
import jakarta.inject.Inject;

/** Ejecuta operaciones JDBC con confirmación o reversión atómica. */
public class GestorTransacciones {
    private final DataSource dataSource;

    @Inject
    public GestorTransacciones(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T ejecutar(Function<Connection, T> operacion) {
        try (Connection conexion = dataSource.getConnection()) {
            boolean autoCommitOriginal = conexion.getAutoCommit();
            Throwable fallo = null;
            try {
                conexion.setAutoCommit(false);
                T resultado = operacion.apply(conexion);
                conexion.commit();
                return resultado;
            } catch (RuntimeException | Error excepcion) {
                fallo = excepcion;
                revertir(conexion, excepcion);
                throw excepcion;
            } catch (SQLException excepcion) {
                fallo = excepcion;
                revertir(conexion, excepcion);
                throw SQLExceptionTraductor.traducir(excepcion, "transacción");
            } finally {
                restaurarAutoCommit(conexion, autoCommitOriginal, fallo);
            }
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "transacción");
        }
    }

    private void revertir(Connection conexion, Throwable fallo) {
        try {
            conexion.rollback();
        } catch (SQLException excepcion) {
            fallo.addSuppressed(excepcion);
        }
    }

    private void restaurarAutoCommit(Connection conexion, boolean autoCommitOriginal, Throwable fallo) {
        try {
            conexion.setAutoCommit(autoCommitOriginal);
        } catch (SQLException excepcion) {
            if (fallo != null) {
                fallo.addSuppressed(excepcion);
            } else {
                throw new DataAccessException("No se pudo restaurar autoCommit tras la transacción.", excepcion);
            }
        }
    }
}