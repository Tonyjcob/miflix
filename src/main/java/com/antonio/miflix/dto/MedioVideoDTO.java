package com.antonio.miflix.dto;

public class MedioVideoDTO {
    private final String rutaArchivo;
    private final boolean registrado;

    public MedioVideoDTO(String rutaArchivo, boolean registrado) {
        this.rutaArchivo = rutaArchivo;
        this.registrado = registrado;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public boolean isRegistrado() {
        return registrado;
    }
}