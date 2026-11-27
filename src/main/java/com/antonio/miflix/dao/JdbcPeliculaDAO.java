package com.antonio.miflix.dao;

import com.antonio.miflix.exception.DataAccessException;
import com.antonio.miflix.model.Pelicula;
import com.antonio.miflix.model.PeliculaResumen;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcPeliculaDAO implements PeliculaDAO {
    private static final String SELECT_PELICULAS = "SELECT p.id, p.titulo, p.anio, p.duracion_min, p.sinopsis, "
            + "p.ruta_archivo, p.ruta_poster, p.director_id, d.nombre AS nombre_director, p.fecha_alta "
            + "FROM pelicula p LEFT JOIN director d ON d.id = p.director_id";
    private final DataSource dataSource;

    @Inject
    public JdbcPeliculaDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<PeliculaResumen> buscarTodos(String q, Integer generoId, Integer directorId) {
        StringBuilder sql = new StringBuilder(SELECT_PELICULAS).append(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append(" AND LOWER(p.titulo) LIKE LOWER(?)");
            parametros.add("%" + q + "%");
        }
        if (generoId != null) {
            sql.append(" AND EXISTS (SELECT 1 FROM pelicula_genero pg WHERE pg.pelicula_id = p.id "
                    + "AND pg.genero_id = ?)");
            parametros.add(generoId);
        }
        if (directorId != null) {
            sql.append(" AND p.director_id = ?");
            parametros.add(directorId);
        }
        sql.append(" ORDER BY p.titulo");

        List<PeliculaResumen> peliculas = new ArrayList<>();
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql.toString())) {
            for (int indice = 0; indice < parametros.size(); indice++) {
                sentencia.setObject(indice + 1, parametros.get(indice));
            }
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    peliculas.add(mapearResumen(resultado));
                }
            }
            return peliculas;
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "película");
        }
    }

    @Override
    public Optional<PeliculaResumen> buscarPorId(int id) {
        String sql = SELECT_PELICULAS + " WHERE p.id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapearResumen(resultado));
            }
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "película");
        }
    }

    @Override
    public int insertar(Connection conexion, Pelicula pelicula) {
        String sql = "INSERT INTO pelicula (titulo, anio, duracion_min, sinopsis, ruta_archivo, ruta_poster, "
                + "director_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarParametros(sentencia, pelicula);
            sentencia.executeUpdate();
            try (ResultSet clavesGeneradas = sentencia.getGeneratedKeys()) {
                if (!clavesGeneradas.next()) {
                    throw new DataAccessException("La base de datos no devolvió el id de la película creada.", null);
                }
                return clavesGeneradas.getInt(1);
            }
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "película");
        }
    }

    @Override
    public boolean actualizar(Connection conexion, Pelicula pelicula) {
        String sql = "UPDATE pelicula SET titulo = ?, anio = ?, duracion_min = ?, sinopsis = ?, ruta_archivo = ?, "
                + "ruta_poster = ?, director_id = ? WHERE id = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            asignarParametros(sentencia, pelicula);
            sentencia.setInt(8, pelicula.getId());
            return sentencia.executeUpdate() > 0;
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "película");
        }
    }

    @Override
    public boolean eliminar(Connection conexion, int id) {
        String sql = "DELETE FROM pelicula WHERE id = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            return sentencia.executeUpdate() > 0;
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "película");
        }
    }

    private void asignarParametros(PreparedStatement sentencia, Pelicula pelicula) throws SQLException {
        sentencia.setString(1, pelicula.getTitulo());
        sentencia.setInt(2, pelicula.getAnio());
        sentencia.setInt(3, pelicula.getDuracionMin());
        sentencia.setString(4, pelicula.getSinopsis());
        sentencia.setString(5, pelicula.getRutaArchivo());
        sentencia.setString(6, pelicula.getRutaPoster());
        if (pelicula.getDirectorId() == null) {
            sentencia.setNull(7, Types.INTEGER);
        } else {
            sentencia.setInt(7, pelicula.getDirectorId());
        }
    }

    private PeliculaResumen mapearResumen(ResultSet resultado) throws SQLException {
        int directorIdLeido = resultado.getInt("director_id");
        Integer directorId = resultado.wasNull() ? null : directorIdLeido;
        LocalDateTime fechaAlta = resultado.getTimestamp("fecha_alta").toLocalDateTime();
        return new PeliculaResumen(resultado.getInt("id"), resultado.getString("titulo"),
                resultado.getInt("anio"), resultado.getInt("duracion_min"), resultado.getString("sinopsis"),
                resultado.getString("ruta_archivo"), resultado.getString("ruta_poster"), directorId,
                resultado.getString("nombre_director"), fechaAlta);
    }
}