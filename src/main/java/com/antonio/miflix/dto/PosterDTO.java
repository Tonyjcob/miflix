package com.antonio.miflix.dto;

import jakarta.ws.rs.core.StreamingOutput;

public class PosterDTO {
    private final long longitud;
    private final String tipoContenido;
    private final StreamingOutput contenido;

    public PosterDTO(long longitud, String tipoContenido, StreamingOutput contenido) {
        this.longitud = longitud;
        this.tipoContenido = tipoContenido;
        this.contenido = contenido;
    }

    public long getLongitud() {
        return longitud;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public StreamingOutput getContenido() {
        return contenido;
    }
}