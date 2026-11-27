package com.antonio.miflix.mapper;

import com.antonio.miflix.dto.DirectorDTO;
import com.antonio.miflix.model.Director;
import com.antonio.miflix.model.DirectorResumen;

public class DirectorMapper {
    public DirectorDTO aDTO(Director director, int totalPeliculas) {
        return new DirectorDTO(director.getId(), director.getNombre(), director.getNacionalidad(), totalPeliculas);
    }

    public DirectorDTO aDTO(DirectorResumen resumen) {
        return new DirectorDTO(resumen.getId(), resumen.getNombre(), resumen.getNacionalidad(),
                resumen.getTotalPeliculas());
    }

    public Director aEntidad(int id, String nombre, String nacionalidad) {
        return new Director(id, nombre, nacionalidad);
    }
}