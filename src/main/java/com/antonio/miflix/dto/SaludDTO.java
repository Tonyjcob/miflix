package com.antonio.miflix.dto;

public class SaludDTO {
    private final String estado;
    private final String baseDatos;

    public SaludDTO(String estado, String baseDatos) {
        this.estado = estado;
        this.baseDatos = baseDatos;
    }

    public String getEstado() {
        return estado;
    }

    public String getBaseDatos() {
        return baseDatos;
    }
}