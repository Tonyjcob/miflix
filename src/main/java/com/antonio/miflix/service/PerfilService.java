package com.antonio.miflix.service;

import com.antonio.miflix.dto.PerfilDTO;
import com.antonio.miflix.dto.PerfilRequestDTO;

import java.util.List;

/** Expone las operaciones de negocio de los perfiles. */
public interface PerfilService {
    List<PerfilDTO> buscarTodos();

    PerfilDTO buscarPorId(int id);

    PerfilDTO crear(PerfilRequestDTO solicitud);

    PerfilDTO actualizar(int id, PerfilRequestDTO solicitud);

    void eliminar(int id);
}