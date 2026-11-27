package com.antonio.miflix.service;

/** Comprueba rutas relativas dentro del almacenamiento configurado de medios. */
public interface AlmacenMedios {
    boolean esRutaSegura(String rutaRelativa);

    boolean existe(String rutaRelativa);
}