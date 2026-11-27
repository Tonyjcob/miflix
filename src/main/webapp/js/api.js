const API_BASE = '/api';

async function solicitar(ruta, opciones = {}) {
    const cabeceras = new Headers(opciones.headers ?? {});
    cabeceras.set('Accept', 'application/json');

    const configuracion = { ...opciones, headers: cabeceras };
    if (opciones.body !== undefined && typeof opciones.body !== 'string') {
        cabeceras.set('Content-Type', 'application/json');
        configuracion.body = JSON.stringify(opciones.body);
    }

    let respuesta;
    try {
        respuesta = await fetch(`${API_BASE}${ruta}`, configuracion);
    } catch (causa) {
        const error = new Error('No se puede conectar con el servidor');
        error.cause = causa;
        throw error;
    }

    if (!respuesta.ok) {
        let errorDTO = null;
        try {
            errorDTO = await respuesta.json();
        } catch {
            errorDTO = null;
        }
        const error = new Error(errorDTO?.mensaje || 'No se pudo completar la solicitud');
        error.status = respuesta.status;
        throw error;
    }

    if (respuesta.status === 204) {
        return null;
    }
    return respuesta.json();
}

export function obtenerPerfiles() {
    return solicitar('/perfiles');
}

export function obtenerGeneros() {
    return solicitar('/generos');
}

export function obtenerDirectores() {
    return solicitar('/directores');
}

export function obtenerPeliculas() {
    return solicitar('/peliculas');
}

export function obtenerVideosDisponibles() {
    return solicitar('/medios/videos');
}

export function crearPelicula(datos) {
    return solicitar('/peliculas', { method: 'POST', body: datos });
}

export function actualizarPelicula(id, datos) {
    return solicitar(`/peliculas/${encodeURIComponent(id)}`, { method: 'PUT', body: datos });
}

export function eliminarPelicula(id) {
    return solicitar(`/peliculas/${encodeURIComponent(id)}`, { method: 'DELETE' });
}

export function crearGenero(datos) {
    return solicitar('/generos', { method: 'POST', body: datos });
}

export function actualizarGenero(id, datos) {
    return solicitar(`/generos/${encodeURIComponent(id)}`, { method: 'PUT', body: datos });
}

export function eliminarGenero(id) {
    return solicitar(`/generos/${encodeURIComponent(id)}`, { method: 'DELETE' });
}

export function crearDirector(datos) {
    return solicitar('/directores', { method: 'POST', body: datos });
}

export function actualizarDirector(id, datos) {
    return solicitar(`/directores/${encodeURIComponent(id)}`, { method: 'PUT', body: datos });
}

export function eliminarDirector(id) {
    return solicitar(`/directores/${encodeURIComponent(id)}`, { method: 'DELETE' });
}

export function crearPerfil(datos) {
    return solicitar('/perfiles', { method: 'POST', body: datos });
}

export function actualizarPerfil(id, datos) {
    return solicitar(`/perfiles/${encodeURIComponent(id)}`, { method: 'PUT', body: datos });
}

export function eliminarPerfil(id) {
    return solicitar(`/perfiles/${encodeURIComponent(id)}`, { method: 'DELETE' });
}

export function buscarPeliculas({ q, generoId, directorId } = {}) {
    const parametros = new URLSearchParams();
    if (q?.trim()) {
        parametros.set('q', q.trim());
    }
    if (generoId !== undefined && generoId !== null && generoId !== '') {
        parametros.set('generoId', String(generoId));
    }
    if (directorId !== undefined && directorId !== null && directorId !== '') {
        parametros.set('directorId', String(directorId));
    }
    const cadenaParametros = parametros.toString();
    const consulta = cadenaParametros ? `?${cadenaParametros}` : '';
    return solicitar(`/peliculas${consulta}`);
}

export function obtenerPelicula(peliculaId) {
    return solicitar(`/peliculas/${encodeURIComponent(peliculaId)}`);
}

export function obtenerProgreso(perfilId, peliculaId) {
    return solicitar(`/perfiles/${encodeURIComponent(perfilId)}/progreso/${encodeURIComponent(peliculaId)}`);
}

export function guardarProgreso(perfilId, peliculaId, segundoActual, { keepalive = false } = {}) {
    return solicitar(`/perfiles/${encodeURIComponent(perfilId)}/progreso/${encodeURIComponent(peliculaId)}`, {
        method: 'PUT',
        body: { segundoActual },
        keepalive
    });
}

export function obtenerContinuarViendo(perfilId) {
    return solicitar(`/perfiles/${encodeURIComponent(perfilId)}/continuar-viendo`);
}

export function urlPoster(peliculaId) {
    return `${API_BASE}/posters/${encodeURIComponent(peliculaId)}`;
}

export function urlStream(peliculaId) {
    return `${API_BASE}/stream/${encodeURIComponent(peliculaId)}`;
}