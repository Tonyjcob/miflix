package com.antonio.miflix.dao;

import com.antonio.miflix.model.Perfil;

import java.util.List;
import java.util.Optional;

/** Define las operaciones JDBC disponibles para los perfiles. */
public interface PerfilDAO {
    List<Perfil> buscarTodos();

    Optional<Perfil> buscarPorId(int id);

    int insertar(Perfil perfil);

    boolean actualizar(Perfil perfil);

    boolean eliminar(int id);

    int contarTodos();
}