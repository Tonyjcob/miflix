package com.antonio.miflix.exception;

import com.antonio.miflix.dto.ErrorDTO;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;

@Provider
public class RecursoNoEncontradoExceptionMapper implements ExceptionMapper<RecursoNoEncontradoException> {
    @Override
    public Response toResponse(RecursoNoEncontradoException excepcion) {
        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorDTO(404, "Not Found", excepcion.getMessage(), Instant.now()))
                .build();
    }
}