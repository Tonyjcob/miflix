package com.antonio.miflix.controller;

import com.antonio.miflix.dto.PerfilDTO;
import com.antonio.miflix.dto.PerfilRequestDTO;
import com.antonio.miflix.service.PerfilService;
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

@Path("/perfiles")
@Produces(MediaType.APPLICATION_JSON)
public class PerfilResource {
    private final PerfilService perfilService;

    @Inject
    public PerfilResource(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GET
    public Response buscarTodos() {
        List<PerfilDTO> perfiles = perfilService.buscarTodos();
        return Response.ok(perfiles).build();
    }

    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") int id) {
        return Response.ok(perfilService.buscarPorId(id)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(PerfilRequestDTO solicitud, @Context UriInfo uriInfo) {
        PerfilDTO perfil = perfilService.crear(solicitud);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(perfil.getId())).build())
                .entity(perfil)
                .build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizar(@PathParam("id") int id, PerfilRequestDTO solicitud) {
        return Response.ok(perfilService.actualizar(id, solicitud)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") int id) {
        perfilService.eliminar(id);
        return Response.noContent().build();
    }
}