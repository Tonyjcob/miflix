package com.antonio.miflix.dao;

import com.antonio.miflix.model.Director;
import com.antonio.miflix.model.DirectorResumen;

import java.util.List;
import java.util.Optional;

/** Define las operaciones JDBC disponibles para los directores. */
public interface DirectorDAO {
    List<DirectorResumen> buscarTodos();

    Optional<Director> buscarPorId(int id);

    int insertar(Director director);

    boolean actualizar(Director director);

    boolean eliminar(int id);

    int contarPeliculas(int id);
}