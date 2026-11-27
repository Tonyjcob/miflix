package com.antonio.miflix.service;

import com.antonio.miflix.dao.DirectorDAO;
import com.antonio.miflix.dao.GeneroDAO;
import com.antonio.miflix.dao.GestorTransacciones;
import com.antonio.miflix.dao.PeliculaDAO;
import com.antonio.miflix.dao.PeliculaGeneroDAO;
import com.antonio.miflix.dto.GeneroPeliculaDTO;
import com.antonio.miflix.dto.PeliculaDTO;
import com.antonio.miflix.dto.PeliculaRequestDTO;
import com.antonio.miflix.exception.ClaveDuplicadaException;
import com.antonio.miflix.exception.ConflictoException;
import com.antonio.miflix.exception.IntegridadReferencialException;
import com.antonio.miflix.exception.RecursoNoEncontradoException;
import com.antonio.miflix.exception.ValidacionDatosException;
import com.antonio.miflix.exception.ValidacionException;
import com.antonio.miflix.mapper.PeliculaMapper;
import com.antonio.miflix.model.Director;
import com.antonio.miflix.model.Genero;
import com.antonio.miflix.model.Pelicula;
import com.antonio.miflix.model.PeliculaResumen;
import jakarta.inject.Inject;

import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class PeliculaServiceImpl implements PeliculaService {
    private final PeliculaDAO peliculaDAO;
    private final PeliculaGeneroDAO peliculaGeneroDAO;
    private final DirectorDAO directorDAO;
    private final GeneroDAO generoDAO;
    private final GestorTransacciones gestorTransacciones;
    private final AlmacenMedios almacenMedios;
    private final PeliculaMapper peliculaMapper;

    @Inject
    public PeliculaServiceImpl(PeliculaDAO peliculaDAO, PeliculaGeneroDAO peliculaGeneroDAO, DirectorDAO directorDAO,
                               GeneroDAO generoDAO, GestorTransacciones gestorTransacciones,
                               AlmacenMedios almacenMedios) {
        this.peliculaDAO = peliculaDAO;
        this.peliculaGeneroDAO = peliculaGeneroDAO;
        this.directorDAO = directorDAO;
        this.generoDAO = generoDAO;
        this.gestorTransacciones = gestorTransacciones;
        this.almacenMedios = almacenMedios;
        this.peliculaMapper = new PeliculaMapper();
    }

    @Override
    public List<PeliculaDTO> buscarTodos(String q, Integer generoId, Integer directorId) {
        List<PeliculaResumen> peliculas = peliculaDAO.buscarTodos(q, generoId, directorId);
        List<Integer> ids = peliculas.stream().map(PeliculaResumen::getId).toList();
        Map<Integer, List<Genero>> generosPorPelicula = peliculaGeneroDAO.buscarGenerosPorPeliculas(ids);
        return peliculas.stream()
                .map(pelicula -> peliculaMapper.aDTO(pelicula,
                        mapearGeneros(generosPorPelicula.getOrDefault(pelicula.getId(), List.of()))))
                .toList();
    }

    @Override
    public PeliculaDTO buscarPorId(int id) {
        PeliculaResumen pelicula = obtenerPorId(id);
        Map<Integer, List<Genero>> generosPorPelicula = peliculaGeneroDAO.buscarGenerosPorPeliculas(List.of(id));
        return peliculaMapper.aDTO(pelicula, mapearGeneros(generosPorPelicula.getOrDefault(id, List.of())));
    }

    @Override
    public PeliculaDTO crear(PeliculaRequestDTO solicitud) {
        DatosValidados datos = validarYNormalizar(solicitud);
        int id = ejecutarEscritura(() -> gestorTransacciones.ejecutar(conexion -> {
            int peliculaId = peliculaDAO.insertar(conexion, datos.pelicula());
            peliculaGeneroDAO.insertarGeneros(conexion, peliculaId, datos.generoIds());
            return peliculaId;
        }));
        return buscarPorId(id);
    }

    @Override
    public PeliculaDTO actualizar(int id, PeliculaRequestDTO solicitud) {
        obtenerPorId(id);
        DatosValidados datos = validarYNormalizar(solicitud);
        ejecutarEscritura(() -> gestorTransacciones.ejecutar(conexion -> {
            Pelicula pelicula = datos.pelicula();
            pelicula.setId(id);
            if (!peliculaDAO.actualizar(conexion, pelicula)) {
                throw recursoNoEncontrado(id);
            }
            peliculaGeneroDAO.eliminarGenerosDePelicula(conexion, id);
            peliculaGeneroDAO.insertarGeneros(conexion, id, datos.generoIds());
            return null;
        }));
        return buscarPorId(id);
    }

    @Override
    public void eliminar(int id) {
        obtenerPorId(id);
        boolean eliminado = ejecutarEscritura(() -> gestorTransacciones.ejecutar(
                conexion -> peliculaDAO.eliminar(conexion, id)));
        if (!eliminado) {
            throw recursoNoEncontrado(id);
        }
    }

    private PeliculaResumen obtenerPorId(int id) {
        return peliculaDAO.buscarPorId(id).orElseThrow(() -> recursoNoEncontrado(id));
    }

    private DatosValidados validarYNormalizar(PeliculaRequestDTO solicitud) {
        if (solicitud == null) {
            throw new ValidacionException("Los datos de la película son obligatorios.");
        }
        String titulo = normalizarTitulo(solicitud.getTitulo());
        int anio = validarAnio(solicitud.getAnio());
        int duracionMin = validarDuracion(solicitud.getDuracionMin());
        String sinopsis = normalizarSinopsis(solicitud.getSinopsis());
        String rutaArchivo = validarRutaArchivo(solicitud.getRutaArchivo());
        String rutaPoster = normalizarRutaPoster(solicitud.getRutaPoster());
        Integer directorId = solicitud.getDirectorId();
        List<Integer> generoIds = normalizarGeneroIds(solicitud.getGeneroIds());

        validarReferencias(directorId, generoIds);
        Pelicula pelicula = peliculaMapper.aEntidad(0, titulo, anio, duracionMin, sinopsis, rutaArchivo,
                rutaPoster, directorId);
        return new DatosValidados(pelicula, generoIds);
    }

    private String normalizarTitulo(String titulo) {
        if (titulo == null) {
            throw new ValidacionException("El título de la película es obligatorio.");
        }
        String normalizado = titulo.trim();
        if (normalizado.isEmpty() || normalizado.length() > 200) {
            throw new ValidacionException("El título debe tener entre 1 y 200 caracteres.");
        }
        return normalizado;
    }

    private int validarAnio(Integer anio) {
        if (anio == null || anio < 1888 || anio > Year.now().getValue()) {
            throw new ValidacionException("El año debe estar entre 1888 y el año actual.");
        }
        return anio;
    }

    private int validarDuracion(Integer duracionMin) {
        if (duracionMin == null || duracionMin < 1 || duracionMin > 999) {
            throw new ValidacionException("La duración debe estar entre 1 y 999 minutos.");
        }
        return duracionMin;
    }

    private String normalizarSinopsis(String sinopsis) {
        if (sinopsis == null || sinopsis.isBlank()) {
            return null;
        }
        String normalizada = sinopsis.trim();
        if (normalizada.length() > 2000) {
            throw new ValidacionException("La sinopsis no puede superar los 2000 caracteres.");
        }
        return normalizada;
    }

    private String validarRutaArchivo(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.isBlank()) {
            throw new ValidacionException("La ruta del archivo de vídeo es obligatoria.");
        }
        String normalizada = rutaArchivo.trim();
        String rutaMinusculas = normalizada.toLowerCase(Locale.ROOT);
        if (!rutaMinusculas.endsWith(".mp4") && !rutaMinusculas.endsWith(".webm")) {
            throw new ValidacionException("El archivo de vídeo debe tener extensión .mp4 o .webm.");
        }
        if (!almacenMedios.esRutaSegura(normalizada)) {
            throw new ValidacionException("La ruta del archivo de vídeo debe ser relativa y segura.");
        }
        if (!almacenMedios.existe(normalizada)) {
            throw new ValidacionException("El archivo de vídeo no existe dentro de MEDIA_PATH.");
        }
        return normalizada;
    }

    private String normalizarRutaPoster(String rutaPoster) {
        if (rutaPoster == null || rutaPoster.isBlank()) {
            return null;
        }
        String normalizada = rutaPoster.trim();
        if (normalizada.length() > 500) {
            throw new ValidacionException("La ruta del póster no puede superar los 500 caracteres.");
        }
        if (!almacenMedios.esRutaSegura(normalizada)) {
            throw new ValidacionException("La ruta del póster debe ser relativa y segura.");
        }
        return normalizada;
    }

    private List<Integer> normalizarGeneroIds(List<Integer> generoIds) {
        if (generoIds == null || generoIds.isEmpty()) {
            throw new ValidacionException("La película debe tener al menos un género.");
        }
        LinkedHashSet<Integer> idsUnicos = new LinkedHashSet<>(generoIds);
        if (idsUnicos.contains(null)) {
            throw new ValidacionException("El id de género es obligatorio.");
        }
        return new ArrayList<>(idsUnicos);
    }

    private void validarReferencias(Integer directorId, List<Integer> generoIds) {
        if (directorId != null && directorDAO.buscarPorId(directorId).isEmpty()) {
            throw new ValidacionException("El director con id " + directorId + " no existe.");
        }
        for (Integer generoId : generoIds) {
            Optional<Genero> genero = generoDAO.buscarPorId(generoId);
            if (genero.isEmpty()) {
                throw new ValidacionException("El género con id " + generoId + " no existe.");
            }
        }
    }

    private List<GeneroPeliculaDTO> mapearGeneros(List<Genero> generos) {
        return generos.stream().map(genero -> new GeneroPeliculaDTO(genero.getId(), genero.getNombre())).toList();
    }

    private <T> T ejecutarEscritura(Supplier<T> escritura) {
        try {
            return escritura.get();
        } catch (ClaveDuplicadaException excepcion) {
            throw new ConflictoException(excepcion.getMessage());
        } catch (IntegridadReferencialException | ValidacionDatosException excepcion) {
            throw new ValidacionException(excepcion.getMessage());
        }
    }

    private RecursoNoEncontradoException recursoNoEncontrado(int id) {
        return new RecursoNoEncontradoException("No se encontró la película con id " + id + ".");
    }

    private record DatosValidados(Pelicula pelicula, List<Integer> generoIds) {
    }
}