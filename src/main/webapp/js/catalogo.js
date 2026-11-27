import {
    buscarPeliculas,
    obtenerContinuarViendo,
    obtenerGeneros
} from './api.js';
import {
    crearAvatar,
    crearPosterFrame,
    mostrarErrorConReintento,
    mostrarEstadoVacio,
    mostrarSkeletons,
    mostrarToast
} from './ui.js';
import { exigirPerfilActivo } from './sesion.js';

const perfil = exigirPerfilActivo();
const contenido = document.querySelector('#catalog-content');
const selectorGenero = document.querySelector('#genre-select');
const formulario = document.querySelector('#search-form');
const busqueda = document.querySelector('#search-input');
let generosCatalogo = [];
let versionSolicitud = 0;
let temporizadorBusqueda;

if (perfil) {
    prepararCabeceraPerfil();
    conectarFiltros();
    void iniciarCatalogo();
}

function prepararCabeceraPerfil() {
    const nombrePerfil = document.querySelector('#profile-name');
    const avatarActual = document.querySelector('#profile-avatar');
    nombrePerfil.textContent = String(perfil.nombre ?? 'Perfil');
    const avatar = crearAvatar(perfil, 'avatar-small');
    avatar.id = 'profile-avatar';
    avatarActual.replaceWith(avatar);
}

function conectarFiltros() {
    busqueda.addEventListener('input', () => {
        window.clearTimeout(temporizadorBusqueda);
        temporizadorBusqueda = window.setTimeout(() => cargarCatalogo(), 300);
    });
    formulario.addEventListener('submit', evento => {
        evento.preventDefault();
        window.clearTimeout(temporizadorBusqueda);
        void cargarCatalogo();
    });
    selectorGenero.addEventListener('change', () => void cargarCatalogo());
}

async function iniciarCatalogo() {
    mostrarSkeletons(contenido, 8, 'poster');
    try {
        const generos = await obtenerGeneros();
        rellenarGeneros(selectorGenero, generos);
        void cargarContinuarViendo();
        await cargarCatalogo();
    } catch (error) {
        mostrarErrorConReintento(contenido, error.message, iniciarCatalogo);
    }
}

function rellenarGeneros(selector, generos) {
    generosCatalogo = Array.isArray(generos) ? generos : [];
    const fragmento = document.createDocumentFragment();
    const opcionTodos = document.createElement('option');
    opcionTodos.value = '';
    opcionTodos.textContent = 'Todos los géneros';
    fragmento.append(opcionTodos);

    generosCatalogo.forEach(genero => {
        const opcion = document.createElement('option');
        opcion.value = String(genero.id);
        opcion.textContent = String(genero.nombre ?? 'Género');
        fragmento.append(opcion);
    });
    selector.replaceChildren(fragmento);
}

async function cargarCatalogo(generos = generosCatalogo) {
    const versionActual = ++versionSolicitud;
    const textoBusqueda = busqueda.value.trim();
    const generoId = selectorGenero.value;
    mostrarSkeletons(contenido, 8, 'poster');

    try {
        const peliculas = await buscarPeliculas({ q: textoBusqueda, generoId });
        if (versionActual !== versionSolicitud) {
            return;
        }
        renderizarCatalogo(contenido, Array.isArray(peliculas) ? peliculas : [], generos, {
            busqueda: textoBusqueda,
            generoId
        });
    } catch (error) {
        if (versionActual === versionSolicitud) {
            mostrarErrorConReintento(contenido, error.message, () => cargarCatalogo(generos));
        }
    }
}

function renderizarCatalogo(contenido, peliculas, generos, filtros) {
    if (peliculas.length === 0) {
        mostrarEstadoVacio(contenido, 'No se encontraron películas');
        return;
    }

    if (filtros.busqueda || filtros.generoId) {
        const seccion = document.createElement('section');
        seccion.className = 'catalog-grid-section';
        const titulo = document.createElement('h2');
        titulo.textContent = filtros.busqueda ? 'Resultados de búsqueda' : 'Películas del género';
        const cuadricula = document.createElement('div');
        cuadricula.className = 'catalog-grid';
        peliculas.forEach(pelicula => cuadricula.append(crearTarjeta(pelicula)));
        seccion.append(titulo, cuadricula);
        contenido.replaceChildren(seccion);
        contenido.setAttribute('aria-busy', 'false');
        return;
    }

    const fragmento = document.createDocumentFragment();
    let cantidadSecciones = 0;
    generos.forEach(genero => {
        const peliculasGenero = peliculas.filter(pelicula =>
            Array.isArray(pelicula.generos)
            && pelicula.generos.some(referencia => Number(referencia.id) === Number(genero.id))
        );
        if (peliculasGenero.length === 0) {
            return;
        }

        const seccion = document.createElement('section');
        seccion.className = 'catalog-row';
        const titulo = document.createElement('h2');
        titulo.textContent = String(genero.nombre ?? 'Género');
        const fila = document.createElement('div');
        fila.className = 'poster-row';
        peliculasGenero.forEach(pelicula => fila.append(crearTarjeta(pelicula)));
        seccion.append(titulo, fila);
        fragmento.append(seccion);
        cantidadSecciones += 1;
    });

    if (cantidadSecciones === 0) {
        const seccion = document.createElement('section');
        seccion.className = 'catalog-grid-section';
        const cuadricula = document.createElement('div');
        cuadricula.className = 'catalog-grid';
        peliculas.forEach(pelicula => cuadricula.append(crearTarjeta(pelicula)));
        seccion.append(cuadricula);
        fragmento.append(seccion);
    }

    contenido.replaceChildren(fragmento);
    contenido.setAttribute('aria-busy', 'false');
}

function crearTarjeta(pelicula, { continuar = false, porcentaje = 0 } = {}) {
    const tarjeta = document.createElement('a');
    tarjeta.className = 'poster-card';
    tarjeta.href = continuar
        ? `/reproductor.html?id=${encodeURIComponent(pelicula.peliculaId)}`
        : `/detalle.html?id=${encodeURIComponent(pelicula.id)}`;
    tarjeta.setAttribute('aria-label', `${continuar ? 'Continuar viendo' : 'Ver detalle de'} ${pelicula.titulo}`);

    const id = continuar ? pelicula.peliculaId : pelicula.id;
    tarjeta.append(crearPosterFrame(id, pelicula.titulo));
    const titulo = document.createElement('span');
    titulo.className = 'poster-title';
    titulo.textContent = String(pelicula.titulo ?? 'Película');
    tarjeta.append(titulo);

    const metadatos = document.createElement('span');
    metadatos.className = 'poster-meta';
    metadatos.textContent = continuar
        ? `${pelicula.porcentaje ?? 0}% visto`
        : String(pelicula.anio ?? '');
    tarjeta.append(metadatos);

    if (continuar) {
        const progreso = document.createElement('span');
        progreso.className = 'poster-progress';
        progreso.setAttribute('role', 'progressbar');
        progreso.setAttribute('aria-label', `Avance de ${pelicula.titulo}`);
        progreso.setAttribute('aria-valuemin', '0');
        progreso.setAttribute('aria-valuemax', '100');
        progreso.setAttribute('aria-valuenow', String(porcentaje));
        const avance = document.createElement('span');
        avance.style.width = `${porcentaje}%`;
        progreso.append(avance);
        tarjeta.append(progreso);
    }
    return tarjeta;
}

async function cargarContinuarViendo() {
    const seccion = document.querySelector('#continue-section');
    const lista = document.querySelector('#continue-list');
    try {
        const peliculas = await obtenerContinuarViendo(perfil.id);
        if (!Array.isArray(peliculas) || peliculas.length === 0) {
            seccion.hidden = true;
            return;
        }
        const fragmento = document.createDocumentFragment();
        peliculas.forEach(pelicula => {
            const porcentaje = Math.max(0, Math.min(100, Number(pelicula.porcentaje) || 0));
            fragmento.append(crearTarjeta(pelicula, { continuar: true, porcentaje }));
        });
        lista.replaceChildren(fragmento);
        seccion.hidden = false;
    } catch (error) {
        seccion.hidden = true;
        mostrarToast(error.message);
    }
}