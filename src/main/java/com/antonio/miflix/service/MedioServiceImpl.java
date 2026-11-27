package com.antonio.miflix.service;

import com.antonio.miflix.dao.PeliculaDAO;
import com.antonio.miflix.dto.MedioVideoDTO;
import com.antonio.miflix.model.PeliculaResumen;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MedioServiceImpl implements MedioService {
    private final AlmacenMedios almacenMedios;
    private final PeliculaDAO peliculaDAO;

    @Inject
    public MedioServiceImpl(AlmacenMedios almacenMedios, PeliculaDAO peliculaDAO) {
        this.almacenMedios = almacenMedios;
        this.peliculaDAO = peliculaDAO;
    }

    @Override
    public List<MedioVideoDTO> listarVideos() {
        Set<String> rutasRegistradas = peliculaDAO.buscarTodos(null, null, null).stream()
                .map(PeliculaResumen::getRutaArchivo)
                .map(this::normalizarRuta)
                .collect(Collectors.toSet());

        return almacenMedios.listarVideos().stream()
                .map(this::normalizarRuta)
                .map(ruta -> new MedioVideoDTO(ruta, rutasRegistradas.contains(ruta)))
                .toList();
    }

    private String normalizarRuta(String ruta) {
        return ruta.replace('\\', '/');
    }
}