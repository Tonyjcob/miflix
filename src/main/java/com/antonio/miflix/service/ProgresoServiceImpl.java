package com.antonio.miflix.service;

import com.antonio.miflix.dao.PeliculaDAO;
import com.antonio.miflix.dao.PerfilDAO;
import com.antonio.miflix.dao.ProgresoDAO;
import com.antonio.miflix.dto.ContinuarViendoDTO;
import com.antonio.miflix.dto.ProgresoDTO;
import com.antonio.miflix.dto.ProgresoRequestDTO;
import com.antonio.miflix.exception.RecursoNoEncontradoException;
import com.antonio.miflix.exception.ValidacionException;
import com.antonio.miflix.mapper.ProgresoMapper;
import com.antonio.miflix.model.PeliculaResumen;
import com.antonio.miflix.model.Progreso;
import com.antonio.miflix.model.ProgresoContinuarViendoResumen;
import jakarta.inject.Inject;

import java.util.List;

public class ProgresoServiceImpl implements ProgresoService {
    private final ProgresoDAO progresoDAO;
    private final PerfilDAO perfilDAO;
    private final PeliculaDAO peliculaDAO;
    private final ProgresoMapper progresoMapper;

    @Inject
    public ProgresoServiceImpl(ProgresoDAO progresoDAO, PerfilDAO perfilDAO, PeliculaDAO peliculaDAO) {
        this.progresoDAO = progresoDAO;
        this.perfilDAO = perfilDAO;
        this.peliculaDAO = peliculaDAO;
        this.progresoMapper = new ProgresoMapper();
    }

    @Override
    public ProgresoDTO guardar(int perfilId, int peliculaId, ProgresoRequestDTO solicitud) {
        PeliculaResumen pelicula = validarRecursos(perfilId, peliculaId);
        if (solicitud == null || solicitud.getSegundoActual() == null) {
            throw new ValidacionException("El segundo actual es obligatorio.");
        }

        int segundoActual = solicitud.getSegundoActual();
        int duracionSegundos = pelicula.getDuracionMin() * 60;
        if (segundoActual < 0 || segundoActual > duracionSegundos) {
            throw new ValidacionException("El segundo actual debe estar entre 0 y " + duracionSegundos + ".");
        }

        boolean completada = segundoActual * 10L >= duracionSegundos * 9L;
        progresoDAO.guardar(new Progreso(perfilId, peliculaId, segundoActual, completada, null));
        Progreso progreso = progresoDAO.buscarPorPerfilYPelicula(perfilId, peliculaId)
                .orElseThrow(() -> new IllegalStateException("No se encontró el progreso guardado."));
        return progresoMapper.aDTO(progreso, pelicula.getDuracionMin());
    }

    @Override
    public ProgresoDTO buscarPorPerfilYPelicula(int perfilId, int peliculaId) {
        PeliculaResumen pelicula = validarRecursos(perfilId, peliculaId);
        Progreso progreso = progresoDAO.buscarPorPerfilYPelicula(perfilId, peliculaId)
                .orElseGet(() -> new Progreso(perfilId, peliculaId, 0, false, null));
        return progresoMapper.aDTO(progreso, pelicula.getDuracionMin());
    }

    @Override
    public List<ContinuarViendoDTO> continuarViendo(int perfilId) {
        verificarPerfil(perfilId);
        return progresoDAO.buscarNoCompletadosPorPerfil(perfilId).stream()
                .map(progresoMapper::aContinuarViendoDTO)
                .toList();
    }

    private PeliculaResumen validarRecursos(int perfilId, int peliculaId) {
        verificarPerfil(perfilId);
        return peliculaDAO.buscarPorId(peliculaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la película con id " + peliculaId + "."));
    }

    private void verificarPerfil(int perfilId) {
        if (perfilDAO.buscarPorId(perfilId).isEmpty()) {
            throw new RecursoNoEncontradoException("No se encontró el perfil con id " + perfilId + ".");
        }
    }
}