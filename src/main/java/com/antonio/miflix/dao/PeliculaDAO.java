package com.antonio.miflix.dao;

import com.antonio.miflix.model.Pelicula;
import com.antonio.miflix.model.PeliculaResumen;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/** Define las operaciones JDBC disponibles para las películas. */
public interface PeliculaDAO {
    List<PeliculaResumen> buscarTodos(String q, Integer generoId, Integer directorId);

    Optional<PeliculaResumen> buscarPorId(int id);

    int insertar(Connection conexion, Pelicula pelicula);

    boolean actualizar(Connection conexion, Pelicula pelicula);

    boolean eliminar(Connection conexion, int id);
}