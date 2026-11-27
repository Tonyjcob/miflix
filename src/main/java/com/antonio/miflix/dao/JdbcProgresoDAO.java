package com.antonio.miflix.dao;

import com.antonio.miflix.model.Progreso;
import com.antonio.miflix.model.ProgresoContinuarViendoResumen;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcProgresoDAO implements ProgresoDAO {
    private final DataSource dataSource;

    @Inject
    public JdbcProgresoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void guardar(Progreso progreso) {
        String sql = "INSERT INTO progreso (perfil_id, pelicula_id, segundo_actual, completada) "
                + "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE segundo_actual = VALUES(segundo_actual), "
                + "completada = VALUES(completada), ultima_vez = CURRENT_TIMESTAMP";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, progreso.getPerfilId());
            sentencia.setInt(2, progreso.getPeliculaId());
            sentencia.setInt(3, progreso.getSegundoActual());
            sentencia.setBoolean(4, progreso.isCompletada());
            sentencia.executeUpdate();
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "progreso");
        }
    }

    @Override
    public Optional<Progreso> buscarPorPerfilYPelicula(int perfilId, int peliculaId) {
        String sql = "SELECT perfil_id, pelicula_id, segundo_actual, completada, ultima_vez "
                + "FROM progreso WHERE perfil_id = ? AND pelicula_id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, perfilId);
            sentencia.setInt(2, peliculaId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapearProgreso(resultado));
            }
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "progreso");
        }
    }

    @Override
    public List<ProgresoContinuarViendoResumen> buscarNoCompletadosPorPerfil(int perfilId) {
        String sql = "SELECT p.id AS pelicula_id, p.titulo, p.anio, p.duracion_min, p.ruta_poster, "
                + "pr.segundo_actual, pr.ultima_vez FROM progreso pr "
                + "JOIN pelicula p ON p.id = pr.pelicula_id "
                + "WHERE pr.perfil_id = ? AND pr.completada = FALSE ORDER BY pr.ultima_vez DESC";
        List<ProgresoContinuarViendoResumen> progresos = new ArrayList<>();
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, perfilId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    progresos.add(mapearResumen(resultado));
                }
            }
            return progresos;
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "progreso");
        }
    }

    private Progreso mapearProgreso(ResultSet resultado) throws SQLException {
        Timestamp ultimaVez = resultado.getTimestamp("ultima_vez");
        return new Progreso(resultado.getInt("perfil_id"), resultado.getInt("pelicula_id"),
                resultado.getInt("segundo_actual"), resultado.getBoolean("completada"),
                ultimaVez.toLocalDateTime());
    }

    private ProgresoContinuarViendoResumen mapearResumen(ResultSet resultado) throws SQLException {
        return new ProgresoContinuarViendoResumen(resultado.getInt("pelicula_id"), resultado.getString("titulo"),
                resultado.getInt("anio"), resultado.getInt("duracion_min"), resultado.getString("ruta_poster"),
                resultado.getInt("segundo_actual"), resultado.getTimestamp("ultima_vez").toLocalDateTime());
    }
}