package com.antonio.miflix.model;

import java.time.LocalDateTime;

public class Perfil {
    private int id;
    private String nombre;
    private String avatar;
    private boolean esInfantil;
    private LocalDateTime fechaCreacion;

    public Perfil() {
    }

    public Perfil(int id, String nombre, String avatar, boolean esInfantil, LocalDateTime fechaCreacion) {
        this.id = id;
        this.nombre = nombre;
        this.avatar = avatar;
        this.esInfantil = esInfantil;
        this.fechaCreacion = fechaCreacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isEsInfantil() {
        return esInfantil;
    }

    public void setEsInfantil(boolean esInfantil) {
        this.esInfantil = esInfantil;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}