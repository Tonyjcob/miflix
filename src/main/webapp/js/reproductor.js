import {
    guardarProgreso as enviarProgreso,
    obtenerPelicula,
    obtenerProgreso,
    urlPoster,
    urlStream
} from './api.js';
import { mostrarToast } from './ui.js';
import { exigirPerfilActivo } from './sesion.js';

const perfil = exigirPerfilActivo();
const video = document.querySelector('#movie-video');
const mensajeError = document.querySelector('#player-error');
const titulo = document.querySelector('#player-title');
const enlaceDetalle = document.querySelector('#back-to-detail');
const peliculaId = new URLSearchParams(window.location.search).get('id');
let ultimoSegundoGuardado = -1;
let temporizadorProgreso;

if (perfil) {
    iniciarReproductor();
}

video.addEventListener('error', () => mostrarErrorVideo());
video.addEventListener('play', () => {
    window.clearInterval(temporizadorProgreso);
    temporizadorProgreso = window.setInterval(() => guardarSegundoActual(), 10_000);
});
video.addEventListener('pause', () => {
    window.clearInterval(temporizadorProgreso);
    guardarSegundoActual(true);
});
video.addEventListener('ended', () => guardarSegundoActual(true));
window.addEventListener('pagehide', () => guardarSegundoActual(true, true));

async function iniciarReproductor() {
    if (!peliculaId || !Number.isInteger(Number(peliculaId)) || Number(peliculaId) < 1) {
        mostrarErrorVideo('Película no encontrada');
        return;
    }

    const urlDetalle = `/detalle.html?id=${encodeURIComponent(peliculaId)}`;
    enlaceDetalle.addEventListener('click', () => window.location.assign(urlDetalle));
    try {
        const pelicula = await obtenerPelicula(peliculaId);
        titulo.textContent = String(pelicula.titulo ?? 'Reproduciendo');
        document.title = `${pelicula.titulo} — MiFlix`;
        video.poster = urlPoster(peliculaId);

        let progreso = null;
        try {
            progreso = await obtenerProgreso(perfil.id, peliculaId);
        } catch (error) {
            mostrarToast(error.message);
        }
        ultimoSegundoGuardado = Number(progreso?.segundoActual) || 0;
        const segundoReanudacion = ultimoSegundoGuardado;
        video.addEventListener('loadedmetadata', () => {
            if (segundoReanudacion > 0 && segundoReanudacion < video.duration) {
                video.currentTime = segundoReanudacion;
            }
        }, { once: true });
        video.src = urlStream(peliculaId);
        video.load();
    } catch (error) {
        if (error.status === 404) {
            mostrarErrorVideo('Película no encontrada');
        } else {
            mostrarErrorVideo(error.message);
        }
    }
}

function guardarSegundoActual(forzar = false, keepalive = false) {
    if (!perfil || !peliculaId || !Number.isFinite(video.currentTime)) {
        return Promise.resolve();
    }
    const segundoActual = Math.max(0, Math.floor(video.currentTime));
    if (!forzar && segundoActual === ultimoSegundoGuardado) {
        return Promise.resolve();
    }

    const anterior = ultimoSegundoGuardado;
    ultimoSegundoGuardado = segundoActual;
    return enviarProgreso(perfil.id, peliculaId, segundoActual, { keepalive })
        .catch(error => {
            if (ultimoSegundoGuardado === segundoActual) {
                ultimoSegundoGuardado = anterior;
            }
            if (!keepalive) {
                mostrarToast(error.message);
            }
        });
}

function mostrarErrorVideo(mensaje = 'No se encuentra el archivo de vídeo') {
    video.hidden = true;
    mensajeError.hidden = false;
    mensajeError.textContent = mensaje;
}