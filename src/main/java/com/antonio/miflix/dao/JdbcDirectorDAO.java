package com.antonio.miflix.dao;

import com.antonio.miflix.exception.DataAccessException;
import com.antonio.miflix.model.Director;
import com.antonio.miflix.model.DirectorResumen;
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

public class JdbcDirectorDAO implements DirectorDAO {
    private final DataSource dataSource;

    @Inject
    public JdbcDirectorDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<DirectorResumen> buscarTodos() {
        String sql = "SELECT d.id, d.nombre, d.nacionalidad, COUNT(p.id) AS total_peliculas "
                + "FROM director d LEFT JOIN pelicula p ON p.director_id = d.id "
                + "GROUP BY d.id, d.nombre, d.nacionalidad ORDER BY d.nombre";
        List<DirectorResumen> directores = new ArrayList<>();
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                directores.add(new DirectorResumen(
                        resultado.getInt("id"),
                        resultado.getString("nombre"),
                        resultado.getString("nacionalidad"),
                        resultado.getInt("total_peliculas")));
            }
            return directores;
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "director");
        }
    }

    @Override
    public Optional<Director> buscarPorId(int id) {
        String sql = "SELECT id, nombre, nacionalidad FROM director WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Director(
                        resultado.getInt("id"),
                        resultado.getString("nombre"),
                        resultado.getString("nacionalidad")));
            }
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "director");
        }
    }

    @Override
    public int insertar(Director director) {
        String sql = "INSERT INTO director (nombre, nacionalidad) VALUES (?, ?)";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, director.getNombre());
            sentencia.setString(2, director.getNacionalidad());
            sentencia.executeUpdate();
            try (ResultSet clavesGeneradas = sentencia.getGeneratedKeys()) {
                if (!clavesGeneradas.next()) {
                    throw new DataAccessException("La base de datos no devolvió el id del director creado.", null);
                }
                return clavesGeneradas.getInt(1);
            }
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "director");
        }
    }

    @Override
    public boolean actualizar(Director director) {
        String sql = "UPDATE director SET nombre = ?, nacionalidad = ? WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, director.getNombre());
            sentencia.setString(2, director.getNacionalidad());
            sentencia.setInt(3, director.getId());
            return sentencia.executeUpdate() > 0;
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "director");
        }
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM director WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            return sentencia.executeUpdate() > 0;
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "director");
        }
    }

    @Override
    public int contarPeliculas(int id) {
        String sql = "SELECT COUNT(*) FROM pelicula WHERE director_id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "director");
        }
    }
}