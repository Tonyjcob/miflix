package com.antonio.miflix.dao;

import com.antonio.miflix.model.Genero;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class JdbcPeliculaGeneroDAO implements PeliculaGeneroDAO {
    private final DataSource dataSource;

    @Inject
    public JdbcPeliculaGeneroDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insertarGeneros(Connection conexion, int peliculaId, List<Integer> generoIds) {
        if (generoIds.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO pelicula_genero (pelicula_id, genero_id) VALUES (?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            for (Integer generoId : generoIds) {
                sentencia.setInt(1, peliculaId);
                sentencia.setInt(2, generoId);
                sentencia.addBatch();
            }
            sentencia.executeBatch();
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "película y género");
        }
    }

    @Override
    public void eliminarGenerosDePelicula(Connection conexion, int peliculaId) {
        String sql = "DELETE FROM pelicula_genero WHERE pelicula_id = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, peliculaId);
            sentencia.executeUpdate();
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "película y género");
        }
    }

    @Override
    public Map<Integer, List<Genero>> buscarGenerosPorPeliculas(List<Integer> peliculaIds) {
        if (peliculaIds.isEmpty()) {
            return Map.of();
        }
        Map<Integer, List<Genero>> generosPorPelicula = new LinkedHashMap<>();
        peliculaIds.forEach(id -> generosPorPelicula.putIfAbsent(id, new ArrayList<>()));
        StringJoiner parametros = new StringJoiner(", ");
        peliculaIds.forEach(id -> parametros.add("?"));
        String sql = "SELECT pg.pelicula_id, g.id, g.nombre FROM pelicula_genero pg "
                + "JOIN genero g ON g.id = pg.genero_id WHERE pg.pelicula_id IN (" + parametros + ") "
                + "ORDER BY g.nombre";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            for (int indice = 0; indice < peliculaIds.size(); indice++) {
                sentencia.setInt(indice + 1, peliculaIds.get(indice));
            }
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    generosPorPelicula.get(resultado.getInt("pelicula_id")).add(
                            new Genero(resultado.getInt("id"), resultado.getString("nombre")));
                }
            }
            return generosPorPelicula;
        } catch (SQLException excepcion) {
            throw SQLExceptionTraductor.traducir(excepcion, "película y género");
        }
    }
}