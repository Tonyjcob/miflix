package com.antonio.miflix.dto;

import java.time.LocalDateTime;

public class ProgresoDTO {
    private final int peliculaId;
    private final int segundoActual;
    private final boolean completada;
    private final int porcentaje;
    private final LocalDateTime ultimaVez;

    public ProgresoDTO(int peliculaId, int segundoActual, boolean completada, int porcentaje,
                       LocalDateTime ultimaVez) {
        this.peliculaId = peliculaId;
        this.segundoActual = segundoActual;
        this.completada = completada;
        this.porcentaje = porcentaje;
        this.ultimaVez = ultimaVez;
    }

    public int getPeliculaId() {
        return peliculaId;
    }

    public int getSegundoActual() {
        return segundoActual;
    }

    public boolean isCompletada() {
        return completada;
    }

    public int getPorcentaje() {
        return porcentaje;
    }

    public LocalDateTime getUltimaVez() {
        return ultimaVez;
    }
}