package com.antonio.miflix.mapper;

import com.antonio.miflix.dto.ContinuarViendoDTO;
import com.antonio.miflix.dto.ProgresoDTO;
import com.antonio.miflix.model.Progreso;
import com.antonio.miflix.model.ProgresoContinuarViendoResumen;

public class ProgresoMapper {
    public ProgresoDTO aDTO(Progreso progreso, int duracionMin) {
        return new ProgresoDTO(progreso.getPeliculaId(), progreso.getSegundoActual(), progreso.isCompletada(),
                calcularPorcentaje(progreso.getSegundoActual(), duracionMin), progreso.getUltimaVez());
    }

    public ContinuarViendoDTO aContinuarViendoDTO(ProgresoContinuarViendoResumen resumen) {
        return new ContinuarViendoDTO(resumen.getPeliculaId(), resumen.getTitulo(), resumen.getAnio(),
                resumen.getDuracionMin(), resumen.getRutaPoster(), resumen.getSegundoActual(),
                calcularPorcentaje(resumen.getSegundoActual(), resumen.getDuracionMin()), resumen.getUltimaVez());
    }

    private int calcularPorcentaje(int segundoActual, int duracionMin) {
        if (duracionMin <= 0) {
            return 0;
        }
        long duracionSegundos = (long) duracionMin * 60;
        return (int) Math.min(100, segundoActual * 100L / duracionSegundos);
    }
}