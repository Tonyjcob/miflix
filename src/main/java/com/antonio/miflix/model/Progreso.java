package com.antonio.miflix.model;

import java.time.LocalDateTime;

public class Progreso {
    private int perfilId;
    private int peliculaId;
    private int segundoActual;
    private boolean completada;
    private LocalDateTime ultimaVez;

    public Progreso() {
    }

    public Progreso(int perfilId, int peliculaId, int segundoActual, boolean completada, LocalDateTime ultimaVez) {
        this.perfilId = perfilId;
        this.peliculaId = peliculaId;
        this.segundoActual = segundoActual;
        this.completada = completada;
        this.ultimaVez = ultimaVez;
    }

    public int getPerfilId() {
        return perfilId;
    }

    public void setPerfilId(int perfilId) {
        this.perfilId = perfilId;
    }

    public int getPeliculaId() {
        return peliculaId;
    }

    public void setPeliculaId(int peliculaId) {
        this.peliculaId = peliculaId;
    }

    public int getSegundoActual() {
        return segundoActual;
    }

    public void setSegundoActual(int segundoActual) {
        this.segundoActual = segundoActual;
    }

    public boolean isCompletada() {
        return completada;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }

    public LocalDateTime getUltimaVez() {
        return ultimaVez;
    }

    public void setUltimaVez(LocalDateTime ultimaVez) {
        this.ultimaVez = ultimaVez;
    }
}