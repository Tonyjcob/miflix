package com.antonio.miflix.dto;

public class GeneroDTO {
    private final int id;
    private final String nombre;
    private final int totalPeliculas;

    public GeneroDTO(int id, String nombre, int totalPeliculas) {
        this.id = id;
        this.nombre = nombre;
        this.totalPeliculas = totalPeliculas;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getTotalPeliculas() {
        return totalPeliculas;
    }
}