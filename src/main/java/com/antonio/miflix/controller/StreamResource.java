package com.antonio.miflix.controller;

import com.antonio.miflix.dto.StreamDTO;
import com.antonio.miflix.service.StreamService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Path("/stream")
public class StreamResource {
    private final StreamService streamService;

    @Inject
    public StreamResource(StreamService streamService) {
        this.streamService = streamService;
    }

    @GET
    @Path("/{peliculaId}")
    public Response obtenerVideo(@PathParam("peliculaId") int peliculaId,
                                 @HeaderParam("Range") String encabezadoRange) {
        StreamDTO resultado = streamService.obtenerVideo(peliculaId, encabezadoRange);
        Response.ResponseBuilder respuesta = Response.status(resultado.getEstado())
                .type(resultado.getTipoContenido())
                .header("Accept-Ranges", "bytes")
                .header("Content-Length", resultado.getLongitud());
        if (resultado.getRangoContenido() != null) {
            respuesta.header("Content-Range", resultado.getRangoContenido());
        }
        if (resultado.getContenido() != null) {
            respuesta.entity(resultado.getContenido());
        }
        return respuesta.build();
    }
}