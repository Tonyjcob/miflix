package com.antonio.miflix.dao;

import com.antonio.miflix.exception.DataAccessException;
import com.antonio.miflix.model.Perfil;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcPerfilDAO implements PerfilDAO {
    private final DataSource dataSource;

    @Inject
    public JdbcPerfilDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Perfil> buscarTodos() {
        String sql = "SELECT id, nombre, avatar, es_infantil, fecha_creacion FROM perfil ORDER BY nombre";
        List<Perfil> perfiles = new ArrayList<>();
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                perfiles.add(mapearPerfil(resultado));
            }
            return perfiles;
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "perfil");
        }
    }

    @Override
    public Optional<Perfil> buscarPorId(int id) {
        String sql = "SELECT id, nombre, avatar, es_infantil, fecha_creacion FROM perfil WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapearPerfil(resultado));
            }
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "perfil");
        }
    }

    @Override
    public int insertar(Perfil perfil) {
        String sql = "INSERT INTO perfil (nombre, avatar, es_infantil) VALUES (?, ?, ?)";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, perfil.getNombre());
            sentencia.setString(2, perfil.getAvatar());
            sentencia.setBoolean(3, perfil.isEsInfantil());
            sentencia.executeUpdate();
            try (ResultSet clavesGeneradas = sentencia.getGeneratedKeys()) {
                if (!clavesGeneradas.next()) {
                    throw new DataAccessException("La base de datos no devolvió el id del perfil creado.", null);
                }
                return clavesGeneradas.getInt(1);
            }
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "perfil");
        }
    }

    @Override
    public boolean actualizar(Perfil perfil) {
        String sql = "UPDATE perfil SET nombre = ?, avatar = ?, es_infantil = ? WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, perfil.getNombre());
            sentencia.setString(2, perfil.getAvatar());
            sentencia.setBoolean(3, perfil.isEsInfantil());
            sentencia.setInt(4, perfil.getId());
            return sentencia.executeUpdate() > 0;
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "perfil");
        }
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM perfil WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            return sentencia.executeUpdate() > 0;
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "perfil");
        }
    }

    @Override
    public int contarTodos() {
        String sql = "SELECT COUNT(*) FROM perfil";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            resultado.next();
            return resultado.getInt(1);
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "perfil");
        }
    }

    private Perfil mapearPerfil(ResultSet resultado) throws SQLException {
        Timestamp fechaCreacion = resultado.getTimestamp("fecha_creacion");
        return new Perfil(
                resultado.getInt("id"),
                resultado.getString("nombre"),
                resultado.getString("avatar"),
                resultado.getBoolean("es_infantil"),
                fechaCreacion.toLocalDateTime());
    }
}