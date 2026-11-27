package com.antonio.miflix.controller;

import com.antonio.miflix.dto.ProgresoRequestDTO;
import com.antonio.miflix.service.ProgresoService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/perfiles")
@Produces(MediaType.APPLICATION_JSON)
public class ProgresoResource {
    private final ProgresoService progresoService;

    @Inject
    public ProgresoResource(ProgresoService progresoService) {
        this.progresoService = progresoService;
    }

    @PUT
    @Path("/{perfilId}/progreso/{peliculaId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response guardar(@PathParam("perfilId") int perfilId, @PathParam("peliculaId") int peliculaId,
                           ProgresoRequestDTO solicitud) {
        return Response.ok(progresoService.guardar(perfilId, peliculaId, solicitud)).build();
    }

    @GET
    @Path("/{perfilId}/progreso/{peliculaId}")
    public Response buscarPorPerfilYPelicula(@PathParam("perfilId") int perfilId,
                                             @PathParam("peliculaId") int peliculaId) {
        return Response.ok(progresoService.buscarPorPerfilYPelicula(perfilId, peliculaId)).build();
    }

    @GET
    @Path("/{perfilId}/continuar-viendo")
    public Response continuarViendo(@PathParam("perfilId") int perfilId) {
        return Response.ok(progresoService.continuarViendo(perfilId)).build();
    }
}