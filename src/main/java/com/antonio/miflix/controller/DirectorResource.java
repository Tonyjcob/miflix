package com.antonio.miflix.controller;

import com.antonio.miflix.dto.DirectorDTO;
import com.antonio.miflix.dto.DirectorRequestDTO;
import com.antonio.miflix.service.DirectorService;
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

import java.util.List;

@Path("/directores")
@Produces(MediaType.APPLICATION_JSON)
public class DirectorResource {
    private final DirectorService directorService;

    @Inject
    public DirectorResource(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GET
    public Response buscarTodos() {
        List<DirectorDTO> directores = directorService.buscarTodos();
        return Response.ok(directores).build();
    }

    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") int id) {
        return Response.ok(directorService.buscarPorId(id)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(DirectorRequestDTO solicitud, @Context UriInfo uriInfo) {
        DirectorDTO director = directorService.crear(solicitud);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(director.getId())).build())
                .entity(director)
                .build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizar(@PathParam("id") int id, DirectorRequestDTO solicitud) {
        return Response.ok(directorService.actualizar(id, solicitud)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") int id) {
        directorService.eliminar(id);
        return Response.noContent().build();
    }
}