import { obtenerPerfiles } from './api.js';
import { crearAvatar, mostrarErrorConReintento, mostrarEstadoVacio, mostrarSkeletons } from './ui.js';
import { guardarPerfilActivo } from './sesion.js';

const listaPerfiles = document.querySelector('#profile-list');

async function cargarPerfiles() {
    mostrarSkeletons(listaPerfiles, 4, 'perfil');
    try {
        const perfiles = await obtenerPerfiles();
        if (!Array.isArray(perfiles) || perfiles.length === 0) {
            mostrarEstadoVacio(listaPerfiles, 'Todavía no hay perfiles. Crea uno para empezar a ver.');
            return;
        }

        const fragmento = document.createDocumentFragment();
        perfiles.forEach(perfil => fragmento.append(crearOpcionPerfil(perfil)));
        listaPerfiles.replaceChildren(fragmento);
        listaPerfiles.setAttribute('aria-busy', 'false');
    } catch (error) {
        mostrarErrorConReintento(listaPerfiles, error.message, cargarPerfiles);
    }
}

function crearOpcionPerfil(perfil) {
    const boton = document.createElement('button');
    boton.className = 'profile-choice';
    boton.type = 'button';
    boton.setAttribute('aria-label', `Entrar con el perfil ${perfil.nombre}`);

    const avatar = crearAvatar(perfil);
    const nombre = document.createElement('span');
    nombre.className = 'profile-choice-name';
    nombre.textContent = String(perfil.nombre ?? 'Perfil');
    boton.append(avatar, nombre);

    boton.addEventListener('click', () => {
        if (guardarPerfilActivo(perfil)) {
            window.location.assign('/catalogo.html');
        }
    });
    return boton;
}

cargarPerfiles();