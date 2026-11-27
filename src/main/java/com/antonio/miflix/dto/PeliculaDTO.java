package com.antonio.miflix.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PeliculaDTO {
    private final int id;
    private final String titulo;
    private final int anio;
    private final int duracionMin;
    private final String sinopsis;
    private final String rutaArchivo;
    private final String rutaPoster;
    private final DirectorPeliculaDTO director;
    private final List<GeneroPeliculaDTO> generos;
    private final LocalDateTime fechaAlta;

    public PeliculaDTO(int id, String titulo, int anio, int duracionMin, String sinopsis, String rutaArchivo,
                       String rutaPoster, DirectorPeliculaDTO director, List<GeneroPeliculaDTO> generos,
                       LocalDateTime fechaAlta) {
        this.id = id;
        this.titulo = titulo;
        this.anio = anio;
        this.duracionMin = duracionMin;
        this.sinopsis = sinopsis;
        this.rutaArchivo = rutaArchivo;
        this.rutaPoster = rutaPoster;
        this.director = director;
        this.generos = List.copyOf(generos);
        this.fechaAlta = fechaAlta;
    }

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getAnio() {
        return anio;
    }

    public int getDuracionMin() {
        return duracionMin;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public String getRutaPoster() {
        return rutaPoster;
    }

    public DirectorPeliculaDTO getDirector() {
        return director;
    }

    public List<GeneroPeliculaDTO> getGeneros() {
        return generos;
    }

    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }
}