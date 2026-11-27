package com.antonio.miflix.dto;

public class DirectorPeliculaDTO {
    private final int id;
    private final String nombre;

    public DirectorPeliculaDTO(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}