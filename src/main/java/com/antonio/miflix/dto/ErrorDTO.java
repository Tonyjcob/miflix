package com.antonio.miflix.dto;

import java.time.Instant;

public class ErrorDTO {
    private final int status;
    private final String error;
    private final String mensaje;
    private final Instant timestamp;

    public ErrorDTO(int status, String error, String mensaje, Instant timestamp) {
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}