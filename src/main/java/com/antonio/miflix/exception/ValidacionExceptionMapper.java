package com.antonio.miflix.exception;

import com.antonio.miflix.dto.ErrorDTO;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;

@Provider
public class ValidacionExceptionMapper implements ExceptionMapper<ValidacionException> {
    @Override
    public Response toResponse(ValidacionException excepcion) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorDTO(400, "Bad Request", excepcion.getMessage(), Instant.now()))
                .build();
    }
}