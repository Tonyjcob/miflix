package com.antonio.miflix.dao;

import com.antonio.miflix.model.Genero;
import com.antonio.miflix.model.GeneroResumen;

import java.util.List;
import java.util.Optional;

/** Define las operaciones JDBC disponibles para los géneros. */
public interface GeneroDAO {
    List<GeneroResumen> buscarTodos();

    Optional<Genero> buscarPorId(int id);

    int insertar(Genero genero);

    boolean actualizar(Genero genero);

    boolean eliminar(int id);

    boolean tienePeliculas(int id);

    int contarPeliculas(int id);
}