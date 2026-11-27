package com.antonio.miflix.dao;

import com.antonio.miflix.model.Progreso;
import com.antonio.miflix.model.ProgresoContinuarViendoResumen;

import java.util.List;
import java.util.Optional;

/** Define las operaciones JDBC disponibles para el progreso de visualización. */
public interface ProgresoDAO {
    void guardar(Progreso progreso);

    Optional<Progreso> buscarPorPerfilYPelicula(int perfilId, int peliculaId);

    List<ProgresoContinuarViendoResumen> buscarNoCompletadosPorPerfil(int perfilId);
}