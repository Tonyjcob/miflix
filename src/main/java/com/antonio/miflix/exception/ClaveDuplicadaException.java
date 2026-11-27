package com.antonio.miflix.exception;

public class ClaveDuplicadaException extends DataAccessException {
    public ClaveDuplicadaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}