package com.antonio.miflix.mapper;

import com.antonio.miflix.dto.PerfilDTO;
import com.antonio.miflix.model.Perfil;

public class PerfilMapper {
    public PerfilDTO aDTO(Perfil perfil) {
        return new PerfilDTO(perfil.getId(), perfil.getNombre(), perfil.getAvatar(), perfil.isEsInfantil(),
                perfil.getFechaCreacion());
    }

    public Perfil aEntidad(int id, String nombre, String avatar, boolean esInfantil) {
        return new Perfil(id, nombre, avatar, esInfantil, null);
    }
}