package com.antonio.miflix.exception;

public class ValidacionDatosException extends DataAccessException {
    public ValidacionDatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}