package com.antonio.miflix.dto;

import java.time.LocalDateTime;

public class PerfilDTO {
    private final int id;
    private final String nombre;
    private final String avatar;
    private final boolean esInfantil;
    private final LocalDateTime fechaCreacion;

    public PerfilDTO(int id, String nombre, String avatar, boolean esInfantil, LocalDateTime fechaCreacion) {
        this.id = id;
        this.nombre = nombre;
        this.avatar = avatar;
        this.esInfantil = esInfantil;
        this.fechaCreacion = fechaCreacion;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getAvatar() {
        return avatar;
    }

    public boolean isEsInfantil() {
        return esInfantil;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}