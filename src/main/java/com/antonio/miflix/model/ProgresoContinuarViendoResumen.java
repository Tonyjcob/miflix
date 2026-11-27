package com.antonio.miflix.model;

import java.time.LocalDateTime;

public class ProgresoContinuarViendoResumen {
    private final int peliculaId;
    private final String titulo;
    private final int anio;
    private final int duracionMin;
    private final String rutaPoster;
    private final int segundoActual;
    private final LocalDateTime ultimaVez;

    public ProgresoContinuarViendoResumen(int peliculaId, String titulo, int anio, int duracionMin,
                                          String rutaPoster, int segundoActual, LocalDateTime ultimaVez) {
        this.peliculaId = peliculaId;
        this.titulo = titulo;
        this.anio = anio;
        this.duracionMin = duracionMin;
        this.rutaPoster = rutaPoster;
        this.segundoActual = segundoActual;
        this.ultimaVez = ultimaVez;
    }

    public int getPeliculaId() {
        return peliculaId;
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

    public String getRutaPoster() {
        return rutaPoster;
    }

    public int getSegundoActual() {
        return segundoActual;
    }

    public LocalDateTime getUltimaVez() {
        return ultimaVez;
    }
}