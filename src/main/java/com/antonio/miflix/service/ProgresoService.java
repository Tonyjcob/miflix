package com.antonio.miflix.service;

import com.antonio.miflix.dto.ContinuarViendoDTO;
import com.antonio.miflix.dto.ProgresoDTO;
import com.antonio.miflix.dto.ProgresoRequestDTO;

import java.util.List;

/** Expone las operaciones de negocio del progreso de visualización. */
public interface ProgresoService {
    ProgresoDTO guardar(int perfilId, int peliculaId, ProgresoRequestDTO solicitud);

    ProgresoDTO buscarPorPerfilYPelicula(int perfilId, int peliculaId);

    List<ContinuarViendoDTO> continuarViendo(int perfilId);
}