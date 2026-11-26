package com.antonio.miflix.exception;

import com.antonio.miflix.dto.ErrorDTO;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;

@Provider
public class ConflictoExceptionMapper implements ExceptionMapper<ConflictoException> {
    @Override
    public Response toResponse(ConflictoException excepcion) {
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorDTO(409, "Conflict", excepcion.getMessage(), Instant.now()))
                .build();
    }
}