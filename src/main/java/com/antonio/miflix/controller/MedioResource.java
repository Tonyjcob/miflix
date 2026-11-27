package com.antonio.miflix.controller;

import com.antonio.miflix.service.MedioService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/medios")
@Produces(MediaType.APPLICATION_JSON)
public class MedioResource {
    private final MedioService medioService;

    @Inject
    public MedioResource(MedioService medioService) {
        this.medioService = medioService;
    }

    @GET
    @Path("/videos")
    public Response listarVideos() {
        return Response.ok(medioService.listarVideos()).build();
    }
}