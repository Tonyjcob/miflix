import {
    actualizarDirector,
    actualizarGenero,
    actualizarPelicula,
    actualizarPerfil,
    crearDirector,
    crearGenero,
    crearPelicula,
    crearPerfil,
    eliminarDirector,
    eliminarGenero,
    eliminarPelicula,
    eliminarPerfil,
    obtenerDirectores,
    obtenerGeneros,
    obtenerPeliculas,
    obtenerPerfiles,
    obtenerVideosDisponibles
} from './api.js';
import { crearCamposFormulario, leerDatosFormulario, validarFormulario } from './admin-formularios.js';
import {
    mostrarCarga,
    mostrarEstadoVacio,
    mostrarErrorConReintento,
    mostrarToast
} from './ui.js';

const CONFIGURACIONES = {
    peliculas: {
        titulo: 'Películas',
        singular: 'película',
        nuevo: 'Nueva película',
        descripcion: 'Las historias de tu colección.',
        listar: obtenerPeliculas,
        crear: crearPelicula,
        actualizar: actualizarPelicula,
        eliminar: eliminarPelicula,
        columnas: [
            { nombre: 'Película', valor: registro => registro.titulo },
            { nombre: 'Año', valor: registro => registro.anio },
            { nombre: 'Duración', valor: registro => `${registro.duracionMin} min` },
            { nombre: 'Director', valor: registro => registro.director?.nombre ?? 'Sin director' },
            { nombre: 'Géneros', valor: registro => (registro.generos ?? []).map(genero => genero.nombre).join(', ') }
        ]
    },
    generos: {
        titulo: 'Géneros',
        singular: 'género',
        nuevo: 'Nuevo género',
        descripcion: 'Las categorías de tu colección.',
        listar: obtenerGeneros,
        crear: crearGenero,
        actualizar: actualizarGenero,
        eliminar: eliminarGenero,
        columnas: [
            { nombre: 'Género', valor: registro => registro.nombre },
            { nombre: 'Películas', valor: registro => registro.totalPeliculas }
        ]
    },
    directores: {
        titulo: 'Directores',
        singular: 'director',
        nuevo: 'Nuevo director',
        descripcion: 'Las personas detrás de tus películas.',
        listar: obtenerDirectores,
        crear: crearDirector,
        actualizar: actualizarDirector,
        eliminar: eliminarDirector,
        columnas: [
            { nombre: 'Director', valor: registro => registro.nombre },
            { nombre: 'Nacionalidad', valor: registro => registro.nacionalidad ?? '—' },
            { nombre: 'Películas', valor: registro => registro.totalPeliculas }
        ]
    },
    perfiles: {
        titulo: 'Perfiles',
        singular: 'perfil',
        nuevo: 'Nuevo perfil',
        descripcion: 'Las personas que disfrutan la videoteca.',
        listar: obtenerPerfiles,
        crear: crearPerfil,
        actualizar: actualizarPerfil,
        eliminar: eliminarPerfil,
        columnas: [
            { nombre: 'Perfil', valor: registro => registro.nombre },
            { nombre: 'Avatar', valor: registro => registro.avatar ?? 'Inicial automática' },
            { nombre: 'Infantil', valor: registro => registro.esInfantil ? 'Sí' : 'No' }
        ]
    }
};

const elementos = {
    pestanas: Array.from(document.querySelectorAll('[data-tab]')),
    panel: document.querySelector('#admin-panel'),
    tituloSeccion: document.querySelector('#section-title'),
    descripcionSeccion: document.querySelector('#section-description'),
    botonNuevo: document.querySelector('#new-record'),
    tabla: document.querySelector('#table-container'),
    dialogoEdicion: document.querySelector('#edit-dialog'),
    formulario: document.querySelector('#entity-form'),
    tituloDialogo: document.querySelector('#dialog-title'),
    camposFormulario: document.querySelector('#form-fields'),
    errorFormulario: document.querySelector('#form-error'),
    botonGuardar: document.querySelector('#save-record'),
    dialogoBorrado: document.querySelector('#delete-dialog'),
    descripcionBorrado: document.querySelector('#delete-description'),
    errorBorrado: document.querySelector('#delete-error'),
    botonConfirmarBorrado: document.querySelector('#confirm-delete')
};

let tipoActivo = 'peliculas';
let tipoFormulario = null;
let registroEditando = null;
let registroEliminando = null;
let enviando = false;
let secuenciaCargaTabla = 0;
let secuenciaFormulario = 0;

elementos.pestanas.forEach(pestana => {
    pestana.addEventListener('click', () => activarPestana(pestana.dataset.tab));
    pestana.addEventListener('keydown', manejarTecladoPestanas);
});

elementos.botonNuevo.addEventListener('click', () => abrirFormulario(tipoActivo));
elementos.formulario.addEventListener('submit', guardarRegistro);
document.querySelector('#close-edit').addEventListener('click', () => elementos.dialogoEdicion.close());
document.querySelector('#cancel-edit').addEventListener('click', () => elementos.dialogoEdicion.close());
document.querySelector('#close-delete').addEventListener('click', cerrarConfirmacionBorrado);
document.querySelector('#cancel-delete').addEventListener('click', cerrarConfirmacionBorrado);
elementos.botonConfirmarBorrado.addEventListener('click', confirmarBorrado);
elementos.dialogoEdicion.addEventListener('cancel', impedirCierreDuranteEnvio);
elementos.dialogoBorrado.addEventListener('cancel', impedirCierreDuranteEnvio);

activarPestana(tipoActivo);

async function activarPestana(tipo) {
    if (!CONFIGURACIONES[tipo]) {
        return;
    }
    tipoActivo = tipo;
    const configuracion = CONFIGURACIONES[tipo];
    elementos.pestanas.forEach(pestana => {
        const seleccionada = pestana.dataset.tab === tipo;
        pestana.setAttribute('aria-selected', String(seleccionada));
        pestana.tabIndex = seleccionada ? 0 : -1;
    });
    elementos.panel.setAttribute('aria-labelledby', `tab-${tipo}`);
    elementos.tituloSeccion.textContent = configuracion.titulo;
    elementos.descripcionSeccion.textContent = configuracion.descripcion;
    elementos.botonNuevo.textContent = configuracion.nuevo;
    await cargarTabla(tipo);
}

function manejarTecladoPestanas(evento) {
    const indice = elementos.pestanas.indexOf(evento.currentTarget);
    let siguiente = indice;
    if (evento.key === 'ArrowRight') {
        siguiente = (indice + 1) % elementos.pestanas.length;
    } else if (evento.key === 'ArrowLeft') {
        siguiente = (indice - 1 + elementos.pestanas.length) % elementos.pestanas.length;
    } else if (evento.key === 'Home') {
        siguiente = 0;
    } else if (evento.key === 'End') {
        siguiente = elementos.pestanas.length - 1;
    } else {
        return;
    }
    evento.preventDefault();
    elementos.pestanas[siguiente].focus();
    activarPestana(elementos.pestanas[siguiente].dataset.tab);
}

async function cargarTabla(tipo) {
    const secuencia = ++secuenciaCargaTabla;
    const configuracion = CONFIGURACIONES[tipo];
    mostrarCarga(elementos.tabla, `Cargando ${configuracion.titulo.toLocaleLowerCase('es')}…`);
    elementos.panel.setAttribute('aria-busy', 'true');
    try {
        const registros = await configuracion.listar();
        if (secuencia !== secuenciaCargaTabla || tipo !== tipoActivo) {
            return;
        }
        if (!Array.isArray(registros) || registros.length === 0) {
            mostrarEstadoVacio(elementos.tabla, `Todavía no hay ${configuracion.titulo.toLocaleLowerCase('es')}.`
                + ` Usa «${configuracion.nuevo}» para añadir el primero.`);
            return;
        }
        elementos.tabla.replaceChildren(crearTabla(tipo, registros));
        elementos.tabla.setAttribute('aria-busy', 'false');
    } catch (error) {
        if (secuencia === secuenciaCargaTabla && tipo === tipoActivo) {
            mostrarErrorConReintento(elementos.tabla, error.message, () => cargarTabla(tipo));
        }
    } finally {
        if (secuencia === secuenciaCargaTabla) {
            elementos.panel.setAttribute('aria-busy', 'false');
        }
    }
}

function crearTabla(tipo, registros) {
    const configuracion = CONFIGURACIONES[tipo];
    const tabla = document.createElement('table');
    tabla.className = 'admin-table';
    const caption = document.createElement('caption');
    caption.className = 'visually-hidden';
    caption.textContent = configuracion.titulo;
    const cabecera = document.createElement('thead');
    const filaCabecera = document.createElement('tr');
    configuracion.columnas.forEach(columna => {
        const celda = document.createElement('th');
        celda.scope = 'col';
        celda.textContent = columna.nombre;
        filaCabecera.append(celda);
    });
    const encabezadoAcciones = document.createElement('th');
    encabezadoAcciones.scope = 'col';
    encabezadoAcciones.textContent = 'Acciones';
    filaCabecera.append(encabezadoAcciones);
    cabecera.append(filaCabecera);

    const cuerpo = document.createElement('tbody');
    registros.forEach(registro => cuerpo.append(crearFila(tipo, registro)));
    tabla.append(caption, cabecera, cuerpo);
    return tabla;
}

function crearFila(tipo, registro) {
    const fila = document.createElement('tr');
    CONFIGURACIONES[tipo].columnas.forEach(columna => {
        const celda = document.createElement('td');
        celda.dataset.label = columna.nombre;
        const valor = columna.valor(registro);
        celda.textContent = String(valor ?? '—');
        fila.append(celda);
    });

    const acciones = document.createElement('td');
    acciones.dataset.label = 'Acciones';
    acciones.className = 'admin-actions-cell';
    const grupo = document.createElement('div');
    grupo.className = 'admin-row-actions';
    const nombre = obtenerNombreRegistro(tipo, registro);
    grupo.append(
        crearBotonAccion('Editar', `Editar ${nombre}`, () => abrirFormulario(tipo, registro)),
        crearBotonAccion('Eliminar', `Eliminar ${nombre}`, () => abrirConfirmacionBorrado(tipo, registro), true)
    );
    acciones.append(grupo);
    fila.append(acciones);
    return fila;
}

function crearBotonAccion(texto, etiqueta, accion, peligro = false) {
    const boton = document.createElement('button');
    boton.className = `table-action${peligro ? ' table-action-danger' : ''}`;
    boton.type = 'button';
    boton.textContent = texto;
    boton.setAttribute('aria-label', etiqueta);
    boton.addEventListener('click', accion);
    return boton;
}

async function abrirFormulario(tipo, registro = null) {
    const secuencia = ++secuenciaFormulario;
    tipoFormulario = tipo;
    registroEditando = registro;
    const configuracion = CONFIGURACIONES[tipo];
    elementos.tituloDialogo.textContent = registro
        ? `Editar ${configuracion.singular}`
        : `Nuevo ${configuracion.singular}`;
    elementos.errorFormulario.hidden = true;
    elementos.errorFormulario.textContent = '';
    elementos.formulario.reset();
    elementos.camposFormulario.replaceChildren();
    elementos.botonGuardar.disabled = false;
    elementos.botonGuardar.textContent = registro ? 'Guardar cambios' : 'Crear';
    if (!elementos.dialogoEdicion.open) {
        elementos.dialogoEdicion.showModal();
    }

    if (tipo !== 'peliculas') {
        renderizarCampos(tipo, registro);
        return;
    }

    elementos.botonGuardar.disabled = true;
    mostrarCarga(elementos.camposFormulario, 'Cargando vídeos, directores y géneros…');
    try {
        const [videos, directores, generos] = await Promise.all([
            obtenerVideosDisponibles(),
            obtenerDirectores(),
            obtenerGeneros()
        ]);
        if (secuencia !== secuenciaFormulario || !elementos.dialogoEdicion.open) {
            return;
        }
        renderizarCampos(tipo, registro, { videos, directores, generos });
    } catch (error) {
        if (secuencia === secuenciaFormulario && elementos.dialogoEdicion.open) {
            mostrarErrorConReintento(elementos.camposFormulario, error.message,
                () => abrirFormulario(tipo, registro));
        }
    }
}

function renderizarCampos(tipo, registro, opciones = {}) {
    elementos.camposFormulario.replaceChildren(crearCamposFormulario(tipo, registro, opciones));
    elementos.camposFormulario.setAttribute('aria-busy', 'false');
    elementos.botonGuardar.disabled = false;
}

async function guardarRegistro(evento) {
    evento.preventDefault();
    if (enviando) {
        return;
    }
    elementos.errorFormulario.hidden = true;
    elementos.errorFormulario.textContent = '';
    const validacion = validarFormulario(tipoFormulario, elementos.formulario);
    if (!validacion.valido) {
        if (validacion.mensaje) {
            mostrarErrorFormulario(validacion.mensaje);
        }
        return;
    }

    const configuracion = CONFIGURACIONES[tipoFormulario];
    const datos = leerDatosFormulario(tipoFormulario, elementos.formulario);
    enviando = true;
    elementos.botonGuardar.disabled = true;
    elementos.botonGuardar.textContent = 'Guardando…';
    document.querySelector('#close-edit').disabled = true;
    document.querySelector('#cancel-edit').disabled = true;
    try {
        if (registroEditando) {
            await configuracion.actualizar(registroEditando.id, datos);
        } else {
            await configuracion.crear(datos);
        }
        const mensaje = registroEditando
            ? `Se han guardado los cambios de ${configuracion.singular}.`
            : `Se ha creado ${configuracion.singular}.`;
        elementos.dialogoEdicion.close();
        mostrarToast(mensaje, 'success');
        await cargarTabla(tipoFormulario);
    } catch (error) {
        mostrarErrorFormulario(error.message);
    } finally {
        enviando = false;
        elementos.botonGuardar.disabled = false;
        elementos.botonGuardar.textContent = registroEditando ? 'Guardar cambios' : 'Crear';
        document.querySelector('#close-edit').disabled = false;
        document.querySelector('#cancel-edit').disabled = false;
    }
}

function mostrarErrorFormulario(mensaje) {
    elementos.errorFormulario.textContent = String(mensaje ?? 'No se pudo guardar el registro.');
    elementos.errorFormulario.hidden = false;
}

function abrirConfirmacionBorrado(tipo, registro) {
    registroEliminando = { tipo, registro };
    const nombre = obtenerNombreRegistro(tipo, registro);
    elementos.errorBorrado.hidden = true;
    elementos.errorBorrado.textContent = '';
    elementos.botonConfirmarBorrado.disabled = false;
    elementos.botonConfirmarBorrado.textContent = 'Eliminar';
    elementos.descripcionBorrado.textContent = obtenerConsecuenciaBorrado(tipo, nombre);
    elementos.dialogoBorrado.showModal();
}

function obtenerConsecuenciaBorrado(tipo, nombre) {
    if (tipo === 'peliculas') {
        return `Se eliminará «${nombre}» junto con sus asociaciones de género y el progreso guardado.`;
    }
    if (tipo === 'generos') {
        return `Se eliminará «${nombre}». Si tiene películas asociadas, el servidor impedirá el borrado.`;
    }
    if (tipo === 'directores') {
        return `Se eliminará «${nombre}». Las películas de este director quedarán sin director.`;
    }
    return `Se eliminará el perfil «${nombre}» y también su progreso de visualización.`;
}

async function confirmarBorrado() {
    if (!registroEliminando || enviando) {
        return;
    }
    const { tipo, registro } = registroEliminando;
    const configuracion = CONFIGURACIONES[tipo];
    enviando = true;
    elementos.botonConfirmarBorrado.disabled = true;
    elementos.botonConfirmarBorrado.textContent = 'Eliminando…';
    document.querySelector('#close-delete').disabled = true;
    document.querySelector('#cancel-delete').disabled = true;
    elementos.errorBorrado.hidden = true;
    try {
        await configuracion.eliminar(registro.id);
        elementos.dialogoBorrado.close();
        mostrarToast(`Se ha eliminado ${configuracion.singular}.`, 'success');
        await cargarTabla(tipo);
    } catch (error) {
        elementos.errorBorrado.textContent = String(error.message ?? 'No se pudo eliminar el registro.');
        elementos.errorBorrado.hidden = false;
    } finally {
        enviando = false;
        elementos.botonConfirmarBorrado.disabled = false;
        elementos.botonConfirmarBorrado.textContent = 'Eliminar';
        document.querySelector('#close-delete').disabled = false;
        document.querySelector('#cancel-delete').disabled = false;
    }
}

function cerrarConfirmacionBorrado() {
    if (!enviando) {
        elementos.dialogoBorrado.close();
        registroEliminando = null;
    }
}

function impedirCierreDuranteEnvio(evento) {
    if (enviando) {
        evento.preventDefault();
    }
}

function obtenerNombreRegistro(tipo, registro) {
    if (tipo === 'peliculas') {
        return String(registro.titulo ?? 'Película');
    }
    return String(registro.nombre ?? CONFIGURACIONES[tipo].singular);
}