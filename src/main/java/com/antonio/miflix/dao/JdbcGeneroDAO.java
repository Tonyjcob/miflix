package com.antonio.miflix.dao;

import com.antonio.miflix.exception.ClaveDuplicadaException;
import com.antonio.miflix.exception.DataAccessException;
import com.antonio.miflix.exception.IntegridadReferencialException;
import com.antonio.miflix.model.Genero;
import com.antonio.miflix.model.GeneroResumen;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcGeneroDAO implements GeneroDAO {
    private static final String MENSAJE_ERROR = "No se pudo completar la operación de géneros.";

    private final DataSource dataSource;

    @Inject
    public JdbcGeneroDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<GeneroResumen> buscarTodos() {
        String sql = "SELECT g.id, g.nombre, COUNT(pg.pelicula_id) AS total_peliculas "
                + "FROM genero g LEFT JOIN pelicula_genero pg ON pg.genero_id = g.id "
                + "GROUP BY g.id, g.nombre ORDER BY g.nombre";
        List<GeneroResumen> generos = new ArrayList<>();
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                generos.add(new GeneroResumen(
                        resultado.getInt("id"),
                        resultado.getString("nombre"),
                        resultado.getInt("total_peliculas")));
            }
            return generos;
        } catch (SQLException excepcion) {
            throw traducirSQLException(excepcion);
        }
    }

    @Override
    public Optional<Genero> buscarPorId(int id) {
        String sql = "SELECT id, nombre FROM genero WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Genero(resultado.getInt("id"), resultado.getString("nombre")));
            }
        } catch (SQLException excepcion) {
            throw traducirSQLException(excepcion);
        }
    }

    @Override
    public int insertar(Genero genero) {
        String sql = "INSERT INTO genero (nombre) VALUES (?)";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, genero.getNombre());
            sentencia.executeUpdate();
            try (ResultSet clavesGeneradas = sentencia.getGeneratedKeys()) {
                if (!clavesGeneradas.next()) {
                    throw new DataAccessException("La base de datos no devolvió el id del género creado.",
                            new SQLException("No se generó una clave para el género."));
                }
                return clavesGeneradas.getInt(1);
            }
        } catch (SQLException excepcion) {
            throw traducirSQLException(excepcion);
        }
    }

    @Override
    public boolean actualizar(Genero genero) {
        String sql = "UPDATE genero SET nombre = ? WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, genero.getNombre());
            sentencia.setInt(2, genero.getId());
            return sentencia.executeUpdate() > 0;
        } catch (SQLException excepcion) {
            throw traducirSQLException(excepcion);
        }
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM genero WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            return sentencia.executeUpdate() > 0;
        } catch (SQLException excepcion) {
            throw traducirSQLException(excepcion);
        }
    }

    @Override
    public boolean tienePeliculas(int id) {
        return contarPeliculas(id) > 0;
    }

    @Override
    public int contarPeliculas(int id) {
        String sql = "SELECT COUNT(*) FROM pelicula_genero WHERE genero_id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        } catch (SQLException excepcion) {
            throw traducirSQLException(excepcion);
        }
    }

    private DataAccessException traducirSQLException(SQLException excepcion) {
        if (excepcion.getErrorCode() == 1062) {
            return new ClaveDuplicadaException("Ya existe un género con ese nombre.", excepcion);
        }
        if (excepcion.getErrorCode() == 1451) {
            return new IntegridadReferencialException("El género tiene películas asociadas.", excepcion);
        }
        return new DataAccessException(MENSAJE_ERROR, excepcion);
    }
}