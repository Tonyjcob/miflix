package com.antonio.miflix.dto;

public class PerfilRequestDTO {
    private String nombre;
    private String avatar;
    private Boolean esInfantil;

    public PerfilRequestDTO() {
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

    public Boolean getEsInfantil() {
        return esInfantil;
    }

    public void setEsInfantil(Boolean esInfantil) {
        this.esInfantil = esInfantil;
    }
}