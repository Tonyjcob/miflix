package com.antonio.miflix.model;

public class GeneroResumen {
    private final int id;
    private final String nombre;
    private final int totalPeliculas;

    public GeneroResumen(int id, String nombre, int totalPeliculas) {
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