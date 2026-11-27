package com.antonio.miflix.controller;

import com.antonio.miflix.dto.GeneroDTO;
import com.antonio.miflix.dto.GeneroRequestDTO;
import com.antonio.miflix.service.GeneroService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/generos")
@Produces(MediaType.APPLICATION_JSON)
public class GeneroResource {
    private final GeneroService generoService;

    @Inject
    public GeneroResource(GeneroService generoService) {
        this.generoService = generoService;
    }

    @GET
    public Response buscarTodos() {
        return Response.ok(generoService.buscarTodos()).build();
    }

    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") int id) {
        return Response.ok(generoService.buscarPorId(id)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(GeneroRequestDTO solicitud, @Context UriInfo uriInfo) {
        GeneroDTO genero = generoService.crear(solicitud);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(genero.getId())).build())
                .entity(genero)
                .build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizar(@PathParam("id") int id, GeneroRequestDTO solicitud) {
        return Response.ok(generoService.actualizar(id, solicitud)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") int id) {
        generoService.eliminar(id);
        return Response.noContent().build();
    }
}