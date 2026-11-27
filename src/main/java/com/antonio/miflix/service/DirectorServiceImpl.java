package com.antonio.miflix.service;

import com.antonio.miflix.dao.DirectorDAO;
import com.antonio.miflix.dto.DirectorDTO;
import com.antonio.miflix.dto.DirectorRequestDTO;
import com.antonio.miflix.exception.ClaveDuplicadaException;
import com.antonio.miflix.exception.ConflictoException;
import com.antonio.miflix.exception.RecursoNoEncontradoException;
import com.antonio.miflix.exception.ValidacionException;
import com.antonio.miflix.mapper.DirectorMapper;
import com.antonio.miflix.model.Director;
import jakarta.inject.Inject;

import java.util.List;

public class DirectorServiceImpl implements DirectorService {
    private final DirectorDAO directorDAO;
    private final DirectorMapper directorMapper;

    @Inject
    public DirectorServiceImpl(DirectorDAO directorDAO) {
        this.directorDAO = directorDAO;
        this.directorMapper = new DirectorMapper();
    }

    @Override
    public List<DirectorDTO> buscarTodos() {
        return directorDAO.buscarTodos().stream()
                .map(directorMapper::aDTO)
                .toList();
    }

    @Override
    public DirectorDTO buscarPorId(int id) {
        Director director = obtenerPorId(id);
        return directorMapper.aDTO(director, directorDAO.contarPeliculas(id));
    }

    @Override
    public DirectorDTO crear(DirectorRequestDTO solicitud) {
        Director datos = validarYNormalizar(solicitud);
        try {
            int id = directorDAO.insertar(datos);
            return directorMapper.aDTO(directorMapper.aEntidad(id, datos.getNombre(), datos.getNacionalidad()), 0);
        } catch (ClaveDuplicadaException excepcion) {
            throw conflictoPorNombre(datos.getNombre());
        }
    }

    @Override
    public DirectorDTO actualizar(int id, DirectorRequestDTO solicitud) {
        Director datos = validarYNormalizar(solicitud);
        obtenerPorId(id);
        try {
            directorDAO.actualizar(directorMapper.aEntidad(id, datos.getNombre(), datos.getNacionalidad()));
        } catch (ClaveDuplicadaException excepcion) {
            throw conflictoPorNombre(datos.getNombre());
        }
        Director directorActualizado = obtenerPorId(id);
        return directorMapper.aDTO(directorActualizado, directorDAO.contarPeliculas(id));
    }

    @Override
    public void eliminar(int id) {
        Director director = obtenerPorId(id);
        // La FK aplica ON DELETE SET NULL: se permite borrar el director y conservar sus películas.
        if (!directorDAO.eliminar(id)) {
            throw recursoNoEncontrado(id);
        }
    }

    private Director obtenerPorId(int id) {
        return directorDAO.buscarPorId(id).orElseThrow(() -> recursoNoEncontrado(id));
    }

    private Director validarYNormalizar(DirectorRequestDTO solicitud) {
        if (solicitud == null || solicitud.getNombre() == null) {
            throw new ValidacionException("El nombre del director es obligatorio.");
        }
        String nombre = solicitud.getNombre().trim();
        if (nombre.length() < 2 || nombre.length() > 120) {
            throw new ValidacionException("El nombre del director debe tener entre 2 y 120 caracteres.");
        }
        String nacionalidad = normalizarNacionalidad(solicitud.getNacionalidad());
        return directorMapper.aEntidad(0, nombre, nacionalidad);
    }

    private String normalizarNacionalidad(String nacionalidad) {
        if (nacionalidad == null || nacionalidad.isBlank()) {
            return null;
        }
        String nacionalidadNormalizada = nacionalidad.trim();
        if (nacionalidadNormalizada.length() > 60) {
            throw new ValidacionException("La nacionalidad no puede superar los 60 caracteres.");
        }
        return nacionalidadNormalizada;
    }

    private ConflictoException conflictoPorNombre(String nombre) {
        return new ConflictoException("Ya existe un director con el nombre '" + nombre + "'.");
    }

    private RecursoNoEncontradoException recursoNoEncontrado(int id) {
        return new RecursoNoEncontradoException("No se encontró el director con id " + id + ".");
    }
}