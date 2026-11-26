package com.antonio.miflix.controller;

import com.antonio.miflix.dto.SaludDTO;
import com.antonio.miflix.service.SaludService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class SaludResource {
    private final SaludService saludService;

    @Inject
    public SaludResource(SaludService saludService) {
        this.saludService = saludService;
    }

    @GET
    public Response obtenerEstado() {
        boolean baseDatosDisponible = saludService.estaDisponible();
        SaludDTO salud = new SaludDTO(
                baseDatosDisponible ? "UP" : "DOWN",
                baseDatosDisponible ? "UP" : "DOWN");
        return Response.status(baseDatosDisponible
                        ? Response.Status.OK
                        : Response.Status.SERVICE_UNAVAILABLE)
                .entity(salud)
                .build();
    }
}