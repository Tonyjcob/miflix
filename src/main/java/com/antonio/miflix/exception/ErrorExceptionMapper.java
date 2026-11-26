package com.antonio.miflix.exception;

import com.antonio.miflix.dto.ErrorDTO;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class ErrorExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger LOGGER = Logger.getLogger(ErrorExceptionMapper.class.getName());

    @Override
    public Response toResponse(Exception excepcion) {
        if (excepcion instanceof WebApplicationException excepcionHttp) {
            return respuestaHttp(excepcionHttp);
        }

        LOGGER.log(Level.SEVERE, "Error interno al procesar la petición", excepcion);
        ErrorDTO error = new ErrorDTO(500, "Internal Server Error",
                "Ha ocurrido un error interno.", Instant.now());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(error)
                .build();
    }

    private Response respuestaHttp(WebApplicationException excepcion) {
        Response respuestaOriginal = excepcion.getResponse();
        int status = respuestaOriginal.getStatus();
        String nombreEstado = respuestaOriginal.getStatusInfo().getReasonPhrase();
        LOGGER.log(Level.FINE, "Excepción HTTP de JAX-RS: {0}", status);
        ErrorDTO error = new ErrorDTO(status, nombreEstado, mensajeHttp(status), Instant.now());
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(error)
                .build();
    }

    private String mensajeHttp(int status) {
        return switch (status) {
            case 404 -> "El recurso solicitado no existe";
            case 405 -> "Método HTTP no permitido para este recurso";
            case 415 -> "Tipo de contenido no soportado";
            default -> "La petición no pudo completarse.";
        };
    }
}