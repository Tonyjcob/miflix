import { mostrarToast } from './ui.js';

const CLAVE_PERFIL = 'miflix.perfilActivo';

export function obtenerPerfilActivo() {
    try {
        const guardado = localStorage.getItem(CLAVE_PERFIL);
        if (!guardado) {
            return null;
        }
        const perfil = JSON.parse(guardado);
        return perfil && Number.isInteger(Number(perfil.id)) && Number(perfil.id) > 0
            ? perfil
            : null;
    } catch {
        return null;
    }
}

export function guardarPerfilActivo(perfil) {
    try {
        localStorage.setItem(CLAVE_PERFIL, JSON.stringify({
            id: Number(perfil.id),
            nombre: String(perfil.nombre ?? ''),
            avatar: perfil.avatar ?? null,
            esInfantil: Boolean(perfil.esInfantil)
        }));
        return true;
    } catch {
        mostrarToast('No se pudo guardar el perfil en este navegador.');
        return false;
    }
}

export function exigirPerfilActivo() {
    const perfil = obtenerPerfilActivo();
    if (!perfil) {
        window.location.replace('/index.html');
        return null;
    }
    return perfil;
}