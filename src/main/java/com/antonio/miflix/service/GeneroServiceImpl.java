package com.antonio.miflix.service;

import com.antonio.miflix.dao.GeneroDAO;
import com.antonio.miflix.dto.GeneroDTO;
import com.antonio.miflix.dto.GeneroRequestDTO;
import com.antonio.miflix.exception.ClaveDuplicadaException;
import com.antonio.miflix.exception.ConflictoException;
import com.antonio.miflix.exception.IntegridadReferencialException;
import com.antonio.miflix.exception.RecursoNoEncontradoException;
import com.antonio.miflix.exception.ValidacionException;
import com.antonio.miflix.mapper.GeneroMapper;
import com.antonio.miflix.model.Genero;
import jakarta.inject.Inject;

import java.util.List;

public class GeneroServiceImpl implements GeneroService {
    private final GeneroDAO generoDAO;
    private final GeneroMapper generoMapper;

    @Inject
    public GeneroServiceImpl(GeneroDAO generoDAO) {
        this.generoDAO = generoDAO;
        this.generoMapper = new GeneroMapper();
    }

    @Override
    public List<GeneroDTO> buscarTodos() {
        return generoDAO.buscarTodos().stream()
                .map(generoMapper::aDTO)
                .toList();
    }

    @Override
    public GeneroDTO buscarPorId(int id) {
        Genero genero = obtenerPorId(id);
        return generoMapper.aDTO(genero, generoDAO.contarPeliculas(id));
    }

    @Override
    public GeneroDTO crear(GeneroRequestDTO solicitud) {
        String nombre = validarYNormalizar(solicitud);
        Genero genero = generoMapper.aEntidad(0, nombre);
        try {
            int id = generoDAO.insertar(genero);
            return generoMapper.aDTO(generoMapper.aEntidad(id, nombre), 0);
        } catch (ClaveDuplicadaException excepcion) {
            throw conflictoPorNombre(nombre);
        }
    }

    @Override
    public GeneroDTO actualizar(int id, GeneroRequestDTO solicitud) {
        String nombre = validarYNormalizar(solicitud);
        obtenerPorId(id);
        try {
            generoDAO.actualizar(generoMapper.aEntidad(id, nombre));
        } catch (ClaveDuplicadaException excepcion) {
            throw conflictoPorNombre(nombre);
        }
        Genero generoActualizado = obtenerPorId(id);
        return generoMapper.aDTO(generoActualizado, generoDAO.contarPeliculas(id));
    }

    @Override
    public void eliminar(int id) {
        Genero genero = obtenerPorId(id);
        int totalPeliculas = generoDAO.contarPeliculas(id);
        if (totalPeliculas > 0) {
            throw new ConflictoException("No se puede eliminar el género " + genero.getNombre()
                    + " porque tiene " + totalPeliculas + " películas asociadas.");
        }
        try {
            if (!generoDAO.eliminar(id)) {
                throw recursoNoEncontrado(id);
            }
        } catch (IntegridadReferencialException excepcion) {
            throw new ConflictoException("No se puede eliminar el género " + genero.getNombre()
                    + " porque tiene películas asociadas.");
        }
    }

    private Genero obtenerPorId(int id) {
        return generoDAO.buscarPorId(id).orElseThrow(() -> recursoNoEncontrado(id));
    }

    private String validarYNormalizar(GeneroRequestDTO solicitud) {
        if (solicitud == null || solicitud.getNombre() == null) {
            throw new ValidacionException("El nombre del género es obligatorio.");
        }
        String nombre = solicitud.getNombre().trim();
        if (nombre.length() < 2 || nombre.length() > 50) {
            throw new ValidacionException("El nombre del género debe tener entre 2 y 50 caracteres.");
        }
        return nombre;
    }

    private ConflictoException conflictoPorNombre(String nombre) {
        return new ConflictoException("Ya existe un género con el nombre '" + nombre + "'.");
    }

    private RecursoNoEncontradoException recursoNoEncontrado(int id) {
        return new RecursoNoEncontradoException("No se encontró el género con id " + id + ".");
    }
}