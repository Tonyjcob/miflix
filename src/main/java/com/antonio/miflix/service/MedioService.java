package com.antonio.miflix.service;

import com.antonio.miflix.dto.MedioVideoDTO;

import java.util.List;

/** Expone las rutas de vídeo disponibles para su selección en administración. */
public interface MedioService {
    List<MedioVideoDTO> listarVideos();
}