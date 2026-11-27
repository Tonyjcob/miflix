package com.antonio.miflix.service;

import com.antonio.miflix.dto.DirectorDTO;
import com.antonio.miflix.dto.DirectorRequestDTO;

import java.util.List;

/** Expone las operaciones de negocio de los directores. */
public interface DirectorService {
    List<DirectorDTO> buscarTodos();

    DirectorDTO buscarPorId(int id);

    DirectorDTO crear(DirectorRequestDTO solicitud);

    DirectorDTO actualizar(int id, DirectorRequestDTO solicitud);

    void eliminar(int id);
}