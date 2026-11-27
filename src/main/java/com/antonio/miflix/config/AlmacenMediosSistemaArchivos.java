package com.antonio.miflix.config;

import com.antonio.miflix.service.AlmacenMedios;
import jakarta.inject.Inject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Locale;
import java.util.List;
import java.util.stream.Stream;

public class AlmacenMediosSistemaArchivos implements AlmacenMedios {
    private final Path raizMedios;

    @Inject
    public AlmacenMediosSistemaArchivos() {
        this(Path.of(new AppConfig().getMediaPath()));
    }

    AlmacenMediosSistemaArchivos(Path raizMedios) {
        this.raizMedios = raizMedios.toAbsolutePath().normalize();
    }

    @Override
    public boolean esRutaSegura(String rutaRelativa) {
        Path ruta = resolverRuta(rutaRelativa);
        return ruta != null && permaneceDentroDeLaRaiz(ruta);
    }

    @Override
    public boolean existe(String rutaRelativa) {
        return resolverRutaSegura(rutaRelativa).isPresent();
    }

    @Override
    public Optional<Path> resolverRutaSegura(String rutaRelativa) {
        Path ruta = resolverRuta(rutaRelativa);
        if (ruta == null) {
            return Optional.empty();
        }
        try {
            Path raizReal = raizMedios.toRealPath();
            Path rutaReal = ruta.toRealPath();
            if (!rutaReal.startsWith(raizReal) || !Files.isRegularFile(rutaReal)) {
                return Optional.empty();
            }
            return Optional.of(rutaReal);
        } catch (IOException excepcion) {
            return Optional.empty();
        }
    }

    @Override
    public List<String> listarVideos() {
        if (!Files.isDirectory(raizMedios)) {
            return List.of();
        }
        try (Stream<Path> rutas = Files.walk(raizMedios)) {
            return rutas
                    .filter(Files::isRegularFile)
                    .map(raizMedios::relativize)
                    .map(Path::toString)
                    .map(ruta -> ruta.replace('\\', '/'))
                    .filter(this::esVideo)
                    .filter(ruta -> resolverRutaSegura(ruta).isPresent())
                    .sorted()
                    .toList();
        } catch (IOException excepcion) {
            throw new UncheckedIOException("No se pudieron listar los vídeos de MEDIA_PATH.", excepcion);
        }
    }

    private boolean esVideo(String ruta) {
        String rutaMinusculas = ruta.toLowerCase(Locale.ROOT);
        return rutaMinusculas.endsWith(".mp4") || rutaMinusculas.endsWith(".webm");
    }

    private Path resolverRuta(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.isBlank() || tieneSegmentoTraversal(rutaRelativa)
                || esRutaAbsoluta(rutaRelativa)) {
            return null;
        }
        try {
            Path relativa = Path.of(rutaRelativa);
            if (relativa.isAbsolute()) {
                return null;
            }
            Path ruta = raizMedios.resolve(relativa).normalize();
            return ruta.startsWith(raizMedios) ? ruta : null;
        } catch (InvalidPathException excepcion) {
            return null;
        }
    }

    private boolean permaneceDentroDeLaRaiz(Path ruta) {
        try {
            Path raizReal = raizMedios.toRealPath();
            Path existente = ruta;
            while (existente != null && existente.startsWith(raizMedios)) {
                if (Files.exists(existente)) {
                    return existente.toRealPath().startsWith(raizReal);
                }
                existente = existente.getParent();
            }
            return !Files.exists(raizMedios);
        } catch (IOException excepcion) {
            return false;
        }
    }

    private boolean esRutaAbsoluta(String ruta) {
        return ruta.startsWith("/") || ruta.startsWith("\\") || ruta.matches("(?i)^[a-z]:.*");
    }

    private boolean tieneSegmentoTraversal(String ruta) {
        for (String segmento : ruta.split("[\\\\/]+")) {
            if (segmento.equals("..")) {
                return true;
            }
        }
        return false;
    }
}