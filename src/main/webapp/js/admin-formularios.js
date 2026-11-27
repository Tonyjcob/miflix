export function crearCamposFormulario(tipo, registro, opciones = {}) {
    const campos = document.createElement('div');
    campos.className = 'admin-form-grid';

    if (tipo === 'peliculas') {
        campos.append(
            crearCampo({ nombre: 'titulo', etiqueta: 'Título', valor: registro?.titulo, required: true,
                minlength: 1, maxlength: 200, anchoCompleto: true }),
            crearCampo({ nombre: 'anio', etiqueta: 'Año', tipo: 'number', valor: registro?.anio,
                required: true, min: 1888, max: new Date().getFullYear() }),
            crearCampo({ nombre: 'duracionMin', etiqueta: 'Duración (minutos)', tipo: 'number',
                valor: registro?.duracionMin, required: true, min: 1, max: 999 }),
            crearCampo({ nombre: 'sinopsis', etiqueta: 'Sinopsis', tipo: 'textarea', valor: registro?.sinopsis,
                maxlength: 2000, anchoCompleto: true }),
            crearSelectorVideo(opciones.videos ?? [], registro?.rutaArchivo),
            crearCampo({ nombre: 'rutaPoster', etiqueta: 'Ruta del póster (opcional)', valor: registro?.rutaPoster,
                maxlength: 500, anchoCompleto: true }),
            crearSelectorDirector(opciones.directores ?? [], registro?.director?.id),
            crearSelectorGeneros(opciones.generos ?? [], registro?.generos ?? [])
        );
    } else if (tipo === 'generos') {
        campos.append(crearCampo({ nombre: 'nombre', etiqueta: 'Nombre del género', valor: registro?.nombre,
            required: true, minlength: 2, maxlength: 50, anchoCompleto: true }));
    } else if (tipo === 'directores') {
        campos.append(
            crearCampo({ nombre: 'nombre', etiqueta: 'Nombre del director', valor: registro?.nombre,
                required: true, minlength: 2, maxlength: 120, anchoCompleto: true }),
            crearCampo({ nombre: 'nacionalidad', etiqueta: 'Nacionalidad (opcional)',
                valor: registro?.nacionalidad, maxlength: 60, anchoCompleto: true })
        );
    } else if (tipo === 'perfiles') {
        campos.append(
            crearCampo({ nombre: 'nombre', etiqueta: 'Nombre del perfil', valor: registro?.nombre,
                required: true, minlength: 2, maxlength: 40, anchoCompleto: true }),
            crearCampo({ nombre: 'avatar', etiqueta: 'Avatar (opcional)', valor: registro?.avatar,
                maxlength: 255, anchoCompleto: true }),
            crearCampoBooleano('esInfantil', 'Perfil infantil', Boolean(registro?.esInfantil))
        );
    }

    return campos;
}

export function validarFormulario(tipo, formulario) {
    if (tipo === 'peliculas') {
        const generos = formulario.querySelectorAll('input[name="generoIds"]:checked');
        if (generos.length === 0) {
            const primero = formulario.querySelector('input[name="generoIds"]');
            primero?.setCustomValidity('Selecciona al menos un género.');
            primero?.reportValidity();
            return { valido: false, mensaje: 'Selecciona al menos un género.' };
        }
        formulario.querySelector('input[name="generoIds"]')?.setCustomValidity('');
    }

    return { valido: formulario.reportValidity(), mensaje: '' };
}

export function leerDatosFormulario(tipo, formulario) {
    const datos = new FormData(formulario);
    if (tipo === 'peliculas') {
        const directorId = datos.get('directorId');
        return {
            titulo: String(datos.get('titulo')).trim(),
            anio: Number(datos.get('anio')),
            duracionMin: Number(datos.get('duracionMin')),
            sinopsis: datos.get('sinopsis'),
            rutaArchivo: datos.get('rutaArchivo'),
            rutaPoster: datos.get('rutaPoster'),
            directorId: directorId ? Number(directorId) : null,
            generoIds: Array.from(formulario.querySelectorAll('input[name="generoIds"]:checked'),
                genero => Number(genero.value))
        };
    }
    if (tipo === 'generos') {
        return { nombre: String(datos.get('nombre')).trim() };
    }
    if (tipo === 'directores') {
        return {
            nombre: String(datos.get('nombre')).trim(),
            nacionalidad: datos.get('nacionalidad')
        };
    }
    return {
        nombre: String(datos.get('nombre')).trim(),
        avatar: datos.get('avatar'),
        esInfantil: formulario.elements.esInfantil.checked
    };
}

function crearCampo({ nombre, etiqueta, tipo = 'text', valor = '', required = false, minlength,
    maxlength, min, max, anchoCompleto = false }) {
    const envoltorio = document.createElement('div');
    envoltorio.className = `admin-field${anchoCompleto ? ' admin-field-wide' : ''}`;

    const label = document.createElement('label');
    label.htmlFor = `admin-${nombre}`;
    label.textContent = etiqueta;

    const entrada = tipo === 'textarea' ? document.createElement('textarea') : document.createElement('input');
    entrada.id = `admin-${nombre}`;
    entrada.name = nombre;
    if (tipo !== 'textarea') {
        entrada.type = tipo;
    }
    if (required) {
        entrada.required = true;
    }
    if (minlength !== undefined) {
        entrada.minLength = minlength;
    }
    if (maxlength !== undefined) {
        entrada.maxLength = maxlength;
    }
    if (min !== undefined) {
        entrada.min = String(min);
    }
    if (max !== undefined) {
        entrada.max = String(max);
    }
    if (valor !== null && valor !== undefined) {
        entrada.value = String(valor);
    }
    envoltorio.append(label, entrada);
    return envoltorio;
}

function crearSelectorVideo(videos, rutaActual) {
    const envoltorio = document.createElement('div');
    envoltorio.className = 'admin-field admin-field-wide';
    const label = document.createElement('label');
    label.htmlFor = 'admin-rutaArchivo';
    label.textContent = 'Archivo de vídeo';
    const selector = document.createElement('select');
    selector.id = 'admin-rutaArchivo';
    selector.name = 'rutaArchivo';
    selector.required = true;
    agregarOpcion(selector, '', 'Selecciona un archivo de vídeo');

    videos.forEach(video => {
        const esActual = video.rutaArchivo === rutaActual;
        const sufijo = video.registrado ? ' — ya registrada' : '';
        const opcion = agregarOpcion(selector, video.rutaArchivo, `${video.rutaArchivo}${sufijo}`);
        opcion.disabled = video.registrado && !esActual;
        opcion.selected = esActual;
    });
    if (rutaActual && !videos.some(video => video.rutaArchivo === rutaActual)) {
        agregarOpcion(selector, rutaActual, `${rutaActual} — ruta actual`, true);
    }
    envoltorio.append(label, selector);
    return envoltorio;
}

function crearSelectorDirector(directores, directorActual) {
    const envoltorio = document.createElement('div');
    envoltorio.className = 'admin-field admin-field-wide';
    const label = document.createElement('label');
    label.htmlFor = 'admin-directorId';
    label.textContent = 'Director';
    const selector = document.createElement('select');
    selector.id = 'admin-directorId';
    selector.name = 'directorId';
    agregarOpcion(selector, '', 'Sin director');
    directores.forEach(director => {
        const opcion = agregarOpcion(selector, director.id, director.nombre);
        opcion.selected = Number(director.id) === Number(directorActual);
    });
    envoltorio.append(label, selector);
    return envoltorio;
}

function crearSelectorGeneros(generos, generosActuales) {
    const fieldset = document.createElement('fieldset');
    fieldset.className = 'admin-field admin-field-wide admin-genre-fieldset';
    const legend = document.createElement('legend');
    legend.textContent = 'Géneros (elige al menos uno)';
    const lista = document.createElement('div');
    lista.className = 'admin-checkbox-grid';
    const idsActuales = new Set(generosActuales.map(genero => Number(genero.id)));

    generos.forEach(genero => {
        const etiqueta = document.createElement('label');
        etiqueta.className = 'admin-checkbox-label';
        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.name = 'generoIds';
        checkbox.value = String(genero.id);
        checkbox.checked = idsActuales.has(Number(genero.id));
        checkbox.addEventListener('change', () => {
            fieldset.querySelector('input[name="generoIds"]')?.setCustomValidity('');
        });
        const texto = document.createElement('span');
        texto.textContent = String(genero.nombre ?? 'Género');
        etiqueta.append(checkbox, texto);
        lista.append(etiqueta);
    });

    fieldset.append(legend, lista);
    return fieldset;
}

function crearCampoBooleano(nombre, etiqueta, marcado) {
    const envoltorio = document.createElement('div');
    envoltorio.className = 'admin-field admin-field-wide';
    const label = document.createElement('label');
    label.className = 'admin-checkbox-label';
    const checkbox = document.createElement('input');
    checkbox.type = 'checkbox';
    checkbox.id = `admin-${nombre}`;
    checkbox.name = nombre;
    checkbox.checked = marcado;
    const texto = document.createElement('span');
    texto.textContent = etiqueta;
    label.append(checkbox, texto);
    envoltorio.append(label);
    return envoltorio;
}

function agregarOpcion(selector, valor, texto, seleccionada = false) {
    const opcion = document.createElement('option');
    opcion.value = String(valor ?? '');
    opcion.textContent = String(texto ?? '');
    opcion.selected = seleccionada;
    selector.append(opcion);
    return opcion;
}