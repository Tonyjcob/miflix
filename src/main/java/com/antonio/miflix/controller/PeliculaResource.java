package com.antonio.miflix.controller;

import com.antonio.miflix.dto.PeliculaDTO;
import com.antonio.miflix.dto.PeliculaRequestDTO;
import com.antonio.miflix.service.PeliculaService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/peliculas")
@Produces(MediaType.APPLICATION_JSON)
public class PeliculaResource {
    private final PeliculaService peliculaService;

    @Inject
    public PeliculaResource(PeliculaService peliculaService) {
        this.peliculaService = peliculaService;
    }

    @GET
    public Response buscarTodos(@QueryParam("q") String q, @QueryParam("generoId") Integer generoId,
                                @QueryParam("directorId") Integer directorId) {
        return Response.ok(peliculaService.buscarTodos(q, generoId, directorId)).build();
    }

    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") int id) {
        return Response.ok(peliculaService.buscarPorId(id)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(PeliculaRequestDTO solicitud, @Context UriInfo uriInfo) {
        PeliculaDTO pelicula = peliculaService.crear(solicitud);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(pelicula.getId())).build())
                .entity(pelicula)
                .build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizar(@PathParam("id") int id, PeliculaRequestDTO solicitud) {
        return Response.ok(peliculaService.actualizar(id, solicitud)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") int id) {
        peliculaService.eliminar(id);
        return Response.noContent().build();
    }
}