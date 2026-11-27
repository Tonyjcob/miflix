package com.antonio.miflix.dto;

public class DirectorDTO {
    private final int id;
    private final String nombre;
    private final String nacionalidad;
    private final int totalPeliculas;

    public DirectorDTO(int id, String nombre, String nacionalidad, int totalPeliculas) {
        this.id = id;
        this.nombre = nombre;
        this.nacionalidad = nacionalidad;
        this.totalPeliculas = totalPeliculas;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public int getTotalPeliculas() {
        return totalPeliculas;
    }
}