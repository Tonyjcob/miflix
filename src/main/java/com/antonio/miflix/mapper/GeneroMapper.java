package com.antonio.miflix.mapper;

import com.antonio.miflix.dto.GeneroDTO;
import com.antonio.miflix.model.Genero;
import com.antonio.miflix.model.GeneroResumen;

public class GeneroMapper {
    public GeneroDTO aDTO(Genero genero, int totalPeliculas) {
        return new GeneroDTO(genero.getId(), genero.getNombre(), totalPeliculas);
    }

    public GeneroDTO aDTO(GeneroResumen resumen) {
        return new GeneroDTO(resumen.getId(), resumen.getNombre(), resumen.getTotalPeliculas());
    }

    public Genero aEntidad(int id, String nombre) {
        return new Genero(id, nombre);
    }
}