package com.antonio.miflix.mapper;

import com.antonio.miflix.dto.DirectorPeliculaDTO;
import com.antonio.miflix.dto.GeneroPeliculaDTO;
import com.antonio.miflix.dto.PeliculaDTO;
import com.antonio.miflix.model.Pelicula;
import com.antonio.miflix.model.PeliculaResumen;

import java.util.List;

public class PeliculaMapper {
    public PeliculaDTO aDTO(Pelicula pelicula, DirectorPeliculaDTO director, List<GeneroPeliculaDTO> generos) {
        return new PeliculaDTO(pelicula.getId(), pelicula.getTitulo(), pelicula.getAnio(), pelicula.getDuracionMin(),
                pelicula.getSinopsis(), pelicula.getRutaArchivo(), pelicula.getRutaPoster(), director, generos,
                pelicula.getFechaAlta());
    }

    public PeliculaDTO aDTO(PeliculaResumen resumen, List<GeneroPeliculaDTO> generos) {
        DirectorPeliculaDTO director = resumen.getDirectorId() == null ? null
                : new DirectorPeliculaDTO(resumen.getDirectorId(), resumen.getNombreDirector());
        return new PeliculaDTO(resumen.getId(), resumen.getTitulo(), resumen.getAnio(), resumen.getDuracionMin(),
                resumen.getSinopsis(), resumen.getRutaArchivo(), resumen.getRutaPoster(), director, generos,
                resumen.getFechaAlta());
    }

    public Pelicula aEntidad(int id, String titulo, int anio, int duracionMin, String sinopsis, String rutaArchivo,
                             String rutaPoster, Integer directorId) {
        return new Pelicula(id, titulo, anio, duracionMin, sinopsis, rutaArchivo, rutaPoster, directorId, null);
    }
}