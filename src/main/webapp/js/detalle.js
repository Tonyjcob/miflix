import { obtenerPelicula, obtenerProgreso } from './api.js';
import {
    crearPosterFrame,
    mostrarErrorConReintento,
    mostrarSkeletons,
    mostrarToast
} from './ui.js';
import { exigirPerfilActivo } from './sesion.js';

const perfil = exigirPerfilActivo();
const contenido = document.querySelector('#detail-content');
const peliculaId = new URLSearchParams(window.location.search).get('id');

if (perfil) {
    cargarDetalle();
}

async function cargarDetalle() {
    if (!peliculaId || !Number.isInteger(Number(peliculaId)) || Number(peliculaId) < 1) {
        mostrarNoEncontrada();
        return;
    }

    mostrarSkeletons(contenido, 1, 'poster');
    try {
        const pelicula = await obtenerPelicula(peliculaId);
        let progreso = null;
        try {
            progreso = await obtenerProgreso(perfil.id, peliculaId);
        } catch (error) {
            mostrarToast(error.message);
        }
        renderizarDetalle(pelicula, progreso);
    } catch (error) {
        if (error.status === 404) {
            mostrarNoEncontrada();
        } else {
            mostrarErrorConReintento(contenido, error.message, cargarDetalle);
        }
    }
}

function renderizarDetalle(pelicula, progreso) {
    const disposicion = document.createElement('section');
    disposicion.className = 'detail-layout';
    disposicion.setAttribute('aria-labelledby', 'movie-title');

    const poster = crearPosterFrame(pelicula.id, pelicula.titulo);
    poster.classList.add('detail-poster');

    const texto = document.createElement('div');
    texto.className = 'detail-copy';
    const titulo = document.createElement('h1');
    titulo.id = 'movie-title';
    titulo.textContent = String(pelicula.titulo ?? 'Película');

    const datos = document.createElement('div');
    datos.className = 'detail-facts';
    const anio = document.createElement('span');
    anio.textContent = String(pelicula.anio ?? '');
    const duracion = document.createElement('span');
    duracion.textContent = formatearDuracion(pelicula.duracionMin);
    const director = document.createElement('span');
    director.textContent = String(pelicula.director?.nombre ?? 'Director no indicado');
    datos.append(anio, duracion, director);

    const listaGeneros = document.createElement('ul');
    listaGeneros.className = 'genre-tags';
    (Array.isArray(pelicula.generos) ? pelicula.generos : []).forEach(genero => {
        const etiqueta = document.createElement('li');
        etiqueta.className = 'genre-tag';
        etiqueta.textContent = String(genero.nombre ?? 'Género');
        listaGeneros.append(etiqueta);
    });

    const sinopsis = document.createElement('p');
    sinopsis.className = 'detail-synopsis';
    sinopsis.textContent = String(pelicula.sinopsis ?? 'Sin sinopsis disponible.');

    const accion = document.createElement('a');
    accion.className = 'primary-action';
    accion.href = `/reproductor.html?id=${encodeURIComponent(pelicula.id)}`;
    const segundoActual = Number(progreso?.segundoActual) || 0;
    accion.textContent = segundoActual > 0
        ? `Continuar (min ${Math.floor(segundoActual / 60)})`
        : 'Reproducir';

    texto.append(titulo, datos, listaGeneros, sinopsis, accion);
    disposicion.append(poster, texto);
    contenido.replaceChildren(disposicion);
    contenido.setAttribute('aria-busy', 'false');
    document.title = `${pelicula.titulo} — MiFlix`;
}

function formatearDuracion(duracionMin) {
    const minutos = Number(duracionMin) || 0;
    const horas = Math.floor(minutos / 60);
    const resto = minutos % 60;
    if (horas === 0) {
        return `${resto} min`;
    }
    return resto === 0 ? `${horas} h` : `${horas} h ${resto} min`;
}

function mostrarNoEncontrada() {
    const estado = document.createElement('div');
    estado.className = 'empty-state detail-error';
    const mensaje = document.createElement('p');
    mensaje.textContent = 'Película no encontrada';
    const enlace = document.createElement('a');
    enlace.className = 'text-link';
    enlace.href = '/catalogo.html';
    enlace.textContent = 'Volver al catálogo';
    estado.append(mensaje, enlace);
    contenido.replaceChildren(estado);
    contenido.setAttribute('aria-busy', 'false');
}