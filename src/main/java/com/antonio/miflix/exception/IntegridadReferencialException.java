package com.antonio.miflix.exception;

public class IntegridadReferencialException extends DataAccessException {
    public IntegridadReferencialException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}