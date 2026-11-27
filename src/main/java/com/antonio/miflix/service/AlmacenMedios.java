package com.antonio.miflix.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/** Comprueba rutas relativas dentro del almacenamiento configurado de medios. */
public interface AlmacenMedios {
    boolean esRutaSegura(String rutaRelativa);

    boolean existe(String rutaRelativa);

    Optional<Path> resolverRutaSegura(String rutaRelativa);

    List<String> listarVideos();
}