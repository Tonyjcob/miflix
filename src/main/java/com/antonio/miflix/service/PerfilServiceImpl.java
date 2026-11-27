package com.antonio.miflix.service;

import com.antonio.miflix.dao.PerfilDAO;
import com.antonio.miflix.dto.PerfilDTO;
import com.antonio.miflix.dto.PerfilRequestDTO;
import com.antonio.miflix.exception.ClaveDuplicadaException;
import com.antonio.miflix.exception.ConflictoException;
import com.antonio.miflix.exception.RecursoNoEncontradoException;
import com.antonio.miflix.exception.ValidacionException;
import com.antonio.miflix.mapper.PerfilMapper;
import com.antonio.miflix.model.Perfil;
import jakarta.inject.Inject;

import java.util.List;

public class PerfilServiceImpl implements PerfilService {
    private static final int MAXIMO_PERFILES = 5;
    private static final String MENSAJE_MAXIMO_PERFILES = "Se ha alcanzado el máximo de 5 perfiles";

    private final PerfilDAO perfilDAO;
    private final PerfilMapper perfilMapper;

    @Inject
    public PerfilServiceImpl(PerfilDAO perfilDAO) {
        this.perfilDAO = perfilDAO;
        this.perfilMapper = new PerfilMapper();
    }

    @Override
    public List<PerfilDTO> buscarTodos() {
        return perfilDAO.buscarTodos().stream()
                .map(perfilMapper::aDTO)
                .toList();
    }

    @Override
    public PerfilDTO buscarPorId(int id) {
        return perfilMapper.aDTO(obtenerPorId(id));
    }

    @Override
    public PerfilDTO crear(PerfilRequestDTO solicitud) {
        Perfil datos = validarYNormalizar(solicitud);
        if (perfilDAO.contarTodos() >= MAXIMO_PERFILES) {
            throw new ConflictoException(MENSAJE_MAXIMO_PERFILES);
        }
        try {
            int id = perfilDAO.insertar(datos);
            return perfilMapper.aDTO(obtenerPorId(id));
        } catch (ClaveDuplicadaException excepcion) {
            throw conflictoPorNombre(datos.getNombre());
        }
    }

    @Override
    public PerfilDTO actualizar(int id, PerfilRequestDTO solicitud) {
        Perfil datos = validarYNormalizar(solicitud);
        obtenerPorId(id);
        try {
            perfilDAO.actualizar(perfilMapper.aEntidad(id, datos.getNombre(), datos.getAvatar(),
                    datos.isEsInfantil()));
        } catch (ClaveDuplicadaException excepcion) {
            throw conflictoPorNombre(datos.getNombre());
        }
        return perfilMapper.aDTO(obtenerPorId(id));
    }

    @Override
    public void eliminar(int id) {
        obtenerPorId(id);
        if (!perfilDAO.eliminar(id)) {
            throw recursoNoEncontrado(id);
        }
    }

    private Perfil obtenerPorId(int id) {
        return perfilDAO.buscarPorId(id).orElseThrow(() -> recursoNoEncontrado(id));
    }

    private Perfil validarYNormalizar(PerfilRequestDTO solicitud) {
        if (solicitud == null || solicitud.getNombre() == null) {
            throw new ValidacionException("El nombre del perfil es obligatorio.");
        }
        String nombre = solicitud.getNombre().trim();
        if (nombre.length() < 2 || nombre.length() > 40) {
            throw new ValidacionException("El nombre del perfil debe tener entre 2 y 40 caracteres.");
        }
        String avatar = normalizarAvatar(solicitud.getAvatar());
        boolean esInfantil = solicitud.getEsInfantil() != null && solicitud.getEsInfantil();
        return perfilMapper.aEntidad(0, nombre, avatar, esInfantil);
    }

    private String normalizarAvatar(String avatar) {
        if (avatar == null || avatar.isBlank()) {
            return null;
        }
        String avatarNormalizado = avatar.trim();
        if (avatarNormalizado.length() > 255) {
            throw new ValidacionException("El avatar no puede superar los 255 caracteres.");
        }
        return avatarNormalizado;
    }

    private ConflictoException conflictoPorNombre(String nombre) {
        return new ConflictoException("Ya existe un perfil con el nombre '" + nombre + "'.");
    }

    private RecursoNoEncontradoException recursoNoEncontrado(int id) {
        return new RecursoNoEncontradoException("No se encontró el perfil con id " + id + ".");
    }
}