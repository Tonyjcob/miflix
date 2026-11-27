package com.antonio.miflix.service;

import com.antonio.miflix.dto.GeneroDTO;
import com.antonio.miflix.dto.GeneroRequestDTO;

import java.util.List;

/** Expone las operaciones de negocio de los géneros. */
public interface GeneroService {
    List<GeneroDTO> buscarTodos();

    GeneroDTO buscarPorId(int id);

    GeneroDTO crear(GeneroRequestDTO solicitud);

    GeneroDTO actualizar(int id, GeneroRequestDTO solicitud);

    void eliminar(int id);
}