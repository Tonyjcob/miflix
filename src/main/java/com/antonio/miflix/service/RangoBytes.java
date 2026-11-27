package com.antonio.miflix.service;

public class RangoBytes {
    public enum Estado {
        COMPLETO,
        PARCIAL,
        NO_SATISFACIBLE
    }

    private final Estado estado;
    private final long inicio;
    private final long fin;

    public RangoBytes(Estado estado, long inicio, long fin) {
        this.estado = estado;
        this.inicio = inicio;
        this.fin = fin;
    }

    public Estado getEstado() {
        return estado;
    }

    public long getInicio() {
        return inicio;
    }

    public long getFin() {
        return fin;
    }
}