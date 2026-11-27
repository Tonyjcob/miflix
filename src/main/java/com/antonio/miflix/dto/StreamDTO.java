package com.antonio.miflix.dto;

import jakarta.ws.rs.core.StreamingOutput;

public class StreamDTO {
    private final int estado;
    private final long longitud;
    private final String rangoContenido;
    private final String tipoContenido;
    private final StreamingOutput contenido;

    public StreamDTO(int estado, long longitud, String rangoContenido, String tipoContenido,
                     StreamingOutput contenido) {
        this.estado = estado;
        this.longitud = longitud;
        this.rangoContenido = rangoContenido;
        this.tipoContenido = tipoContenido;
        this.contenido = contenido;
    }

    public int getEstado() {
        return estado;
    }

    public long getLongitud() {
        return longitud;
    }

    public String getRangoContenido() {
        return rangoContenido;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public StreamingOutput getContenido() {
        return contenido;
    }
}