package com.antonio.miflix.dao;

import com.antonio.miflix.exception.ClaveDuplicadaException;
import com.antonio.miflix.exception.DataAccessException;
import com.antonio.miflix.exception.IntegridadReferencialException;
import com.antonio.miflix.exception.ValidacionDatosException;

import java.sql.SQLException;

public final class SQLExceptionTraductor {
    private static final String MENSAJE_ERROR = "No se pudo completar la operación de géneros.";

    private SQLExceptionTraductor() {
    }

    public static DataAccessException traducir(SQLException excepcion, String entidad) {
        return switch (excepcion.getErrorCode()) {
            case 1062 -> new ClaveDuplicadaException(
                    "Ya existe un/a " + entidad + " con esos datos", excepcion);
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
}