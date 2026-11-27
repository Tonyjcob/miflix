package com.antonio.miflix.service;

import com.antonio.miflix.dto.PeliculaDTO;
import com.antonio.miflix.dto.PeliculaRequestDTO;

import java.util.List;

/** Expone las operaciones de negocio del catálogo de películas. */
public interface PeliculaService {
    List<PeliculaDTO> buscarTodos(String q, Integer generoId, Integer directorId);

    PeliculaDTO buscarPorId(int id);

    PeliculaDTO crear(PeliculaRequestDTO solicitud);

    PeliculaDTO actualizar(int id, PeliculaRequestDTO solicitud);

    void eliminar(int id);
}