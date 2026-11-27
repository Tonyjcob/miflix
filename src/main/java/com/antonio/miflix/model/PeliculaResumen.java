package com.antonio.miflix.model;

import java.time.LocalDateTime;

public class PeliculaResumen {
    private final int id;
    private final String titulo;
    private final int anio;
    private final int duracionMin;
    private final String sinopsis;
    private final String rutaArchivo;
    private final String rutaPoster;
    private final Integer directorId;
    private final String nombreDirector;
    private final LocalDateTime fechaAlta;

    public PeliculaResumen(int id, String titulo, int anio, int duracionMin, String sinopsis, String rutaArchivo,
                           String rutaPoster, Integer directorId, String nombreDirector, LocalDateTime fechaAlta) {
        this.id = id;
        this.titulo = titulo;
        this.anio = anio;
        this.duracionMin = duracionMin;
        this.sinopsis = sinopsis;
        this.rutaArchivo = rutaArchivo;
        this.rutaPoster = rutaPoster;
        this.directorId = directorId;
        this.nombreDirector = nombreDirector;
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

    public Integer getDirectorId() {
        return directorId;
    }

    public String getNombreDirector() {
        return nombreDirector;
    }

    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }
}