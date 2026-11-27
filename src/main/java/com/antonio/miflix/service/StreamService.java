package com.antonio.miflix.service;

import com.antonio.miflix.dto.StreamDTO;

/** Expone la reproducción de vídeo con soporte para rangos HTTP. */
public interface StreamService {
    StreamDTO obtenerVideo(int peliculaId, String encabezadoRange);
}