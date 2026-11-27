package com.antonio.miflix.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlmacenMediosSistemaArchivosTest {
    @TempDir
    Path mediaRoot;

    @Test
    void existe_conArchivoRegularDentroDeLaRaiz_devuelveTrue() throws IOException {
        Files.createFile(mediaRoot.resolve("pelicula.mp4"));
        AlmacenMediosSistemaArchivos almacen = new AlmacenMediosSistemaArchivos(mediaRoot);

        assertTrue(almacen.esRutaSegura("pelicula.mp4"));
        assertTrue(almacen.existe("pelicula.mp4"));
    }

    @Test
    void existe_conArchivoInexistente_devuelveFalse() {
        AlmacenMediosSistemaArchivos almacen = new AlmacenMediosSistemaArchivos(mediaRoot);

        assertFalse(almacen.existe("no-existe.mp4"));
    }

    @Test
    void esRutaSegura_conRutaTraversal_devuelveFalse() {
        AlmacenMediosSistemaArchivos almacen = new AlmacenMediosSistemaArchivos(mediaRoot);

        assertFalse(almacen.esRutaSegura("../secreto.mp4"));
        assertFalse(almacen.esRutaSegura("subcarpeta/../pelicula.mp4"));
    }

    @Test
    void esRutaSegura_conRutaAbsoluta_devuelveFalse() {
        AlmacenMediosSistemaArchivos almacen = new AlmacenMediosSistemaArchivos(mediaRoot);

        assertFalse(almacen.esRutaSegura(mediaRoot.resolve("pelicula.mp4").toString()));
        assertFalse(almacen.esRutaSegura("C:\\videos\\pelicula.mp4"));
    }
}