package com.antonio.miflix.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserRango {
    private static final Pattern FORMATO_RANGO = Pattern.compile("(?i)^bytes=(\\d*)-(\\d*)$");

    public RangoBytes parsear(String encabezado, long tamano) {
        if (tamano < 0) {
            throw new IllegalArgumentException("El tamaño del recurso no puede ser negativo.");
        }
        if (encabezado == null) {
            return completo(tamano);
        }

        Matcher coincidencia = FORMATO_RANGO.matcher(encabezado.trim());
        if (!coincidencia.matches()) {
            return completo(tamano);
        }

        String inicioTexto = coincidencia.group(1);
        String finTexto = coincidencia.group(2);
        if (inicioTexto.isEmpty() && finTexto.isEmpty()) {
            return completo(tamano);
        }

        try {
            if (inicioTexto.isEmpty()) {
                return rangoSufijo(finTexto, tamano);
            }
            long inicio = Long.parseLong(inicioTexto);
            long fin = finTexto.isEmpty() ? tamano - 1 : Long.parseLong(finTexto);
            if (tamano == 0 || inicio >= tamano || fin < inicio) {
                return noSatisfacible();
            }
            return new RangoBytes(RangoBytes.Estado.PARCIAL, inicio, Math.min(fin, tamano - 1));
        } catch (NumberFormatException excepcion) {
            return completo(tamano);
        }
    }

    private RangoBytes rangoSufijo(String finTexto, long tamano) {
        long sufijo = Long.parseLong(finTexto);
        if (sufijo == 0 || tamano == 0) {
            return noSatisfacible();
        }
        return new RangoBytes(RangoBytes.Estado.PARCIAL, Math.max(0, tamano - sufijo), tamano - 1);
    }

    private RangoBytes completo(long tamano) {
        return new RangoBytes(RangoBytes.Estado.COMPLETO, 0, tamano - 1);
    }

    private RangoBytes noSatisfacible() {
        return new RangoBytes(RangoBytes.Estado.NO_SATISFACIBLE, -1, -1);
    }
}