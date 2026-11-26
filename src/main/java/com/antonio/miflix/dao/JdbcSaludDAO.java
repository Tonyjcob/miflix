package com.antonio.miflix.dao;

import com.antonio.miflix.exception.DataAccessException;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcSaludDAO implements SaludDAO {
    private final DataSource dataSource;

    @Inject
    public JdbcSaludDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean comprobarConexion() {
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement("SELECT 1");
             ResultSet resultado = sentencia.executeQuery()) {
            return resultado.next() && resultado.getInt(1) == 1;
        } catch (SQLException excepcion) {
            throw new DataAccessException("No se pudo comprobar la conexión con la base de datos.", excepcion);
        }
    }
}