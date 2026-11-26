package com.antonio.miflix.exception;

import com.antonio.miflix.dto.ErrorDTO;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ErrorExceptionMapperTest {
    private final ErrorExceptionMapper mapper = new ErrorExceptionMapper();

    @Test
    void toResponse_conNotFoundException_conserva404() {
        Response respuesta = mapper.toResponse(new NotFoundException());

        assertErrorHttp(respuesta, 404, "Not Found", "El recurso solicitado no existe");
    }

    @Test
    void toResponse_conNotAllowedException_conserva405() {
        Response respuesta = mapper.toResponse(new NotAllowedException(
                Response.status(Response.Status.METHOD_NOT_ALLOWED).build()));

        assertErrorHttp(respuesta, 405, "Method Not Allowed",
                "Método HTTP no permitido para este recurso");
    }

    @Test
    void toResponse_conUnsupportedMediaTypeException_conserva415() {
        Response respuesta = mapper.toResponse(new NotSupportedException());

        assertErrorHttp(respuesta, 415, "Unsupported Media Type", "Tipo de contenido no soportado");
    }

    @Test
    void toResponse_conRuntimeException_devuelve500SinDetallesInternos() {
        Response respuesta = mapper.toResponse(new RuntimeException("detalle interno"));

        assertEquals(500, respuesta.getStatus());
        ErrorDTO error = assertInstanceOf(ErrorDTO.class, respuesta.getEntity());
        assertEquals(500, error.getStatus());
        assertEquals("Internal Server Error", error.getError());
        assertEquals("Ha ocurrido un error interno.", error.getMensaje());
        assertFalse(error.getMensaje().contains("detalle interno"));
    }

    private void assertErrorHttp(Response respuesta, int status, String nombreEstado, String mensaje) {
        assertEquals(status, respuesta.getStatus());
        ErrorDTO error = assertInstanceOf(ErrorDTO.class, respuesta.getEntity());
        assertEquals(status, error.getStatus());
        assertEquals(nombreEstado, error.getError());
        assertEquals(mensaje, error.getMensaje());
    }
}