import { urlPoster } from './api.js';

const COLORES_AVATAR = ['#49685d', '#745d53', '#50617d', '#80654b', '#645b78'];

export function mostrarToast(mensaje, tipo = 'error') {
    let region = document.querySelector('#toasts');
    if (!region) {
        region = document.createElement('div');
        region.id = 'toasts';
        region.className = 'toast-region';
        region.setAttribute('aria-live', 'polite');
        region.setAttribute('aria-atomic', 'true');
        document.body.append(region);
    }

    const toast = document.createElement('p');
    toast.className = `toast toast-${tipo === 'success' ? 'success' : 'error'}`;
    toast.textContent = String(mensaje ?? 'Ha ocurrido un error');
    region.append(toast);
    window.setTimeout(() => toast.remove(), 5000);
}

export function mostrarCarga(contenedor, mensaje = 'Cargando…') {
    contenedor.replaceChildren();
    const estado = document.createElement('p');
    estado.className = 'loading-state';
    estado.setAttribute('role', 'status');
    estado.textContent = mensaje;
    contenedor.append(estado);
    contenedor.setAttribute('aria-busy', 'true');
}

export function mostrarSkeletons(contenedor, cantidad = 6, tipo = 'poster') {
    const fragmento = document.createDocumentFragment();
    const destino = tipo === 'poster' ? document.createElement('div') : fragmento;
    if (tipo === 'poster') {
        destino.className = 'catalog-grid';
    }

    for (let indice = 0; indice < cantidad; indice += 1) {
        const esPerfil = tipo === 'perfil';
        const tarjeta = document.createElement('div');
        tarjeta.className = esPerfil ? 'profile-skeleton' : 'skeleton-card';
        tarjeta.setAttribute('aria-hidden', 'true');

        const imagen = document.createElement('div');
        imagen.className = esPerfil ? 'skeleton-avatar' : 'skeleton-poster';
        tarjeta.append(imagen);

        const titulo = document.createElement('div');
        titulo.className = 'skeleton-line';
        tarjeta.append(titulo);
        if (!esPerfil) {
            const subtitulo = document.createElement('div');
            subtitulo.className = 'skeleton-line short';
            tarjeta.append(subtitulo);
        }
        destino.append(tarjeta);
    }

    contenedor.replaceChildren(fragmento);
    if (tipo === 'poster') {
        contenedor.append(destino);
    }
    contenedor.setAttribute('aria-busy', 'true');
}

export function mostrarEstadoVacio(contenedor, mensaje = 'No se encontraron películas') {
    const estado = document.createElement('div');
    estado.className = 'empty-state';
    const texto = document.createElement('p');
    texto.textContent = mensaje;
    estado.append(texto);
    contenedor.replaceChildren(estado);
    contenedor.setAttribute('aria-busy', 'false');
}

export function mostrarErrorConReintento(contenedor, mensaje, reintentar) {
    const estado = document.createElement('div');
    estado.className = 'error-state';
    estado.setAttribute('role', 'alert');

    const texto = document.createElement('p');
    texto.textContent = mensaje;
    const boton = document.createElement('button');
    boton.className = 'retry-button';
    boton.type = 'button';
    boton.textContent = 'Reintentar';
    boton.addEventListener('click', reintentar);

    estado.append(texto, boton);
    contenedor.replaceChildren(estado);
    contenedor.setAttribute('aria-busy', 'false');
}

export function escaparHTML(valor) {
    const entidades = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    };
    return String(valor ?? '').replace(/[&<>"']/g, caracter => entidades[caracter]);
}

export function crearAvatar(perfil, tamano = 'avatar-large') {
    const nombre = String(perfil?.nombre ?? 'Perfil');
    const contenedor = document.createElement('span');
    contenedor.className = `avatar ${tamano}`;
    contenedor.style.setProperty('--avatar-color', colorParaNombre(nombre));
    const inicial = Array.from(nombre.trim())[0]?.toLocaleUpperCase('es') || '?';
    contenedor.textContent = inicial;

    const url = urlLocal(perfil?.avatar);
    if (url) {
        const imagen = document.createElement('img');
        imagen.src = url;
        imagen.alt = `Avatar de ${nombre}`;
        imagen.loading = 'lazy';
        imagen.addEventListener('error', () => {
            imagen.remove();
            contenedor.textContent = inicial;
        }, { once: true });
        contenedor.replaceChildren(imagen);
    }
    return contenedor;
}

export function crearPosterFrame(peliculaId, titulo) {
    const marco = document.createElement('div');
    marco.className = 'poster-frame';
    const imagen = document.createElement('img');
    imagen.src = urlPoster(peliculaId);
    imagen.alt = `Póster de ${titulo}`;
    imagen.loading = 'lazy';

    const alternativa = document.createElement('span');
    alternativa.className = 'poster-fallback';
    alternativa.textContent = String(titulo ?? 'Póster no disponible');
    alternativa.hidden = true;
    imagen.addEventListener('error', () => {
        imagen.hidden = true;
        alternativa.hidden = false;
    }, { once: true });

    marco.append(imagen, alternativa);
    return marco;
}

function colorParaNombre(nombre) {
    const suma = Array.from(nombre).reduce((total, caracter) => total + caracter.codePointAt(0), 0);
    return COLORES_AVATAR[suma % COLORES_AVATAR.length];
}

function urlLocal(valor) {
    if (typeof valor !== 'string' || !valor.trim()) {
        return null;
    }
    try {
        const url = new URL(valor, window.location.origin);
        return url.origin === window.location.origin && ['http:', 'https:'].includes(url.protocol)
            ? url.href
            : null;
    } catch {
        return null;
    }
}