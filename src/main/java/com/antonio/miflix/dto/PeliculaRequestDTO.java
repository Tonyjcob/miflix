package com.antonio.miflix.dto;

import java.util.List;

public class PeliculaRequestDTO {
    private String titulo;
    private Integer anio;
    private Integer duracionMin;
    private String sinopsis;
    private String rutaArchivo;
    private String rutaPoster;
    private Integer directorId;
    private List<Integer> generoIds;

    public PeliculaRequestDTO() {
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public Integer getDuracionMin() {
        return duracionMin;
    }

    public void setDuracionMin(Integer duracionMin) {
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

    public List<Integer> getGeneroIds() {
        return generoIds;
    }

    public void setGeneroIds(List<Integer> generoIds) {
        this.generoIds = generoIds;
    }
}