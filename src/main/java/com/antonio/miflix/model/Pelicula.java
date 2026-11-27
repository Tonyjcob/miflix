package com.antonio.miflix.model;

import java.time.LocalDateTime;

public class Pelicula {
    private int id;
    private String titulo;
    private int anio;
    private int duracionMin;
    private String sinopsis;
    private String rutaArchivo;
    private String rutaPoster;
    private Integer directorId;
    private LocalDateTime fechaAlta;

    public Pelicula() {
    }

    public Pelicula(int id, String titulo, int anio, int duracionMin, String sinopsis, String rutaArchivo,
                    String rutaPoster, Integer directorId, LocalDateTime fechaAlta) {
        this.id = id;
        this.titulo = titulo;
        this.anio = anio;
        this.duracionMin = duracionMin;
        this.sinopsis = sinopsis;
        this.rutaArchivo = rutaArchivo;
        this.rutaPoster = rutaPoster;
        this.directorId = directorId;
        this.fechaAlta = fechaAlta;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public int getDuracionMin() {
        return duracionMin;
    }

    public void setDuracionMin(int duracionMin) {
        this.duracionMin = duracionMin;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public void setSinopsis(String sinopsis) {
        this.sinopsis = sinopsis;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public String getRutaPoster() {
        return rutaPoster;
    }

    public void setRutaPoster(String rutaPoster) {
        this.rutaPoster = rutaPoster;
    }

    public Integer getDirectorId() {
        return directorId;
    }

    public void setDirectorId(Integer directorId) {
        this.directorId = directorId;
    }

    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(LocalDateTime fechaAlta) {
        this.fechaAlta = fechaAlta;
    }
}