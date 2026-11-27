package com.antonio.miflix.dao;

import com.antonio.miflix.model.Genero;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/** Define el acceso JDBC a las asociaciones entre películas y géneros. */
public interface PeliculaGeneroDAO {
    void insertarGeneros(Connection conexion, int peliculaId, List<Integer> generoIds);

    void eliminarGenerosDePelicula(Connection conexion, int peliculaId);

    Map<Integer, List<Genero>> buscarGenerosPorPeliculas(List<Integer> peliculaIds);
}