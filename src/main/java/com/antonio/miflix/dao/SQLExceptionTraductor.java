package com.antonio.miflix.dao;

import com.antonio.miflix.exception.ClaveDuplicadaException;
import com.antonio.miflix.exception.DataAccessException;
import com.antonio.miflix.exception.IntegridadReferencialException;
import com.antonio.miflix.exception.ValidacionDatosException;

import java.sql.SQLException;

public final class SQLExceptionTraductor {
    private static final String RESTRICCION_TITULO_ANIO = "uq_pelicula_titulo_anio";
    private static final String RESTRICCION_RUTA_ARCHIVO = "uq_pelicula_ruta_archivo";

    private SQLExceptionTraductor() {
    }

    public static DataAccessException traducir(SQLException excepcion, String entidad) {
        return switch (excepcion.getErrorCode()) {
            case 1062 -> traducirDuplicado(excepcion, entidad);
            case 1451 -> new IntegridadReferencialException(
                    "El registro tiene dependencias.", excepcion);
            case 1452 -> new IntegridadReferencialException(
                    "La referencia apunta a un registro que no existe.", excepcion);
            case 3819 -> new ValidacionDatosException(
                    "Los datos incumplen una restricción CHECK.", excepcion);
            default -> new DataAccessException(
                    "No se pudo completar la operación sobre " + entidad + ".", excepcion);
        };
    }

    private static ClaveDuplicadaException traducirDuplicado(SQLException excepcion, String entidad) {
        String mensajeSql = excepcion.getMessage();
        if (mensajeSql != null && mensajeSql.contains(RESTRICCION_TITULO_ANIO)) {
            return new ClaveDuplicadaException("Ya existe una película con el mismo título y año.", excepcion);
        }
        if (mensajeSql != null && mensajeSql.contains(RESTRICCION_RUTA_ARCHIVO)) {
            return new ClaveDuplicadaException("Ya existe una película con esa ruta de archivo.", excepcion);
        }
        return new ClaveDuplicadaException("Ya existe un/a " + entidad + " con esos datos", excepcion);
    }
}