package com.antonio.miflix.controller;

import com.antonio.miflix.dto.PosterDTO;
import com.antonio.miflix.service.PosterService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Path("/posters")
public class PosterResource {
    private final PosterService posterService;

    @Inject
    public PosterResource(PosterService posterService) {
        this.posterService = posterService;
    }

    @GET
    @Path("/{peliculaId}")
    public Response obtenerPoster(@PathParam("peliculaId") int peliculaId) {
        PosterDTO poster = posterService.obtenerPoster(peliculaId);
        return Response.ok(poster.getContenido(), poster.getTipoContenido())
                .header("Content-Length", poster.getLongitud())
                .header("Cache-Control", "max-age=86400")
                .build();
    }
}