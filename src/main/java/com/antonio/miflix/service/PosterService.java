package com.antonio.miflix.service;

import com.antonio.miflix.dto.PosterDTO;

/** Expone la lectura segura de pósteres de películas. */
public interface PosterService {
    PosterDTO obtenerPoster(int peliculaId);
}