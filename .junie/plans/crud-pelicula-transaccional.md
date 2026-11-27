---
sessionId: session-261127-103957-g6f6
---

# Requirements

### Objetivo y alcance
Completar el CRUD de películas en `/api/peliculas`, respetando los patrones JAX-RS, JDBC, DTO y pruebas de Género/Director/Perfil. Las escrituras de película y sus asociaciones N:M deben ser atómicas; las consultas admiten filtros combinables y cargan los géneros en lote.

### Comportamiento
- `GET /api/peliculas` admite `q`, `generoId` y `directorId`, en cualquier combinación; ordena por título.
- `GET /api/peliculas/{id}` devuelve el detalle o 404. `POST` devuelve 201 y `Location`; `PUT` devuelve 200; `DELETE` devuelve 204 y conserva las cascadas definidas en la BD.
- La salida incluye los datos de película, director `{id,nombre}` o `null`, géneros `{id,nombre}` ordenados por nombre y `fechaAlta` ISO-8601. La entrada separa `directorId` y `generoIds`.
- Validar y normalizar título, año, duración, sinopsis, rutas, director y géneros como indica la tarea. Los errores de duplicado son 409; datos, referencias inexistentes y restricciones de integridad/CHECK son 400; película inexistente es 404.
- Los cambios de esquema, `init.sql`, `docker-compose.yml`, `Dockerfile` y `.env` quedan fuera de alcance. No se añadirán dependencias.

# Technical Design

### Implementación existente
`GeneroResource` y los recursos de Director/Perfil delegan a interfaces de servicio; `GeneroServiceImpl` aplica validaciones y `JdbcGeneroDAO` usa JDBC con `PreparedStatement`, try-with-resources y `SQLExceptionTraductor`. `MiflixApplication` registra recursos y bindings explícitamente. `AppConfig.getMediaPath()` ya expone `MEDIA_PATH`; `JacksonObjectMapperProvider` registra `JavaTimeModule` y desactiva timestamps. `db/init.sql` ya define las FK/cascadas y ocho películas semilla; no se modifica.

### Responsabilidades y decisiones
- **Servicio como frontera transaccional:** `PeliculaServiceImpl` valida los datos y coordina `GestorTransacciones`; al crear/actualizar comparte una conexión entre `PeliculaDAO` y `PeliculaGeneroDAO`. El gestor abre del `DataSource`, conserva/restaura `autoCommit`, confirma o revierte y cierra con try-with-resources.
- **DAO N:M separado:** `PeliculaGeneroDAO` aísla `pelicula_genero`. `insertarGeneros(Connection, peliculaId, generoIds)` ejecuta un batch; `eliminarGenerosDePelicula(Connection, peliculaId)` participa en la transacción; `buscarGenerosPorPeliculas(peliculaIds)` hace una única consulta y devuelve un mapa vacío sin ejecutar SQL si no recibe ids.
- **Validación de referencias antes de transacción:** el servicio reutiliza `DirectorDAO` y `GeneroDAO` para producir los mensajes de id inexistente. La traducción de `IntegridadReferencialException` a 400 se mantiene como salvaguarda frente a carreras.
- **Medios seguros:** la interfaz `AlmacenMedios` abstrae existencia y seguridad de rutas; su implementación resuelve bajo `AppConfig.getMediaPath()`, normaliza y rechaza rutas absolutas o que escapen de la raíz. El servicio verifica extensión y existencia del vídeo, y aplica protección equivalente al póster sin exigir que exista.
- Las lecturas de películas incluirán los datos del director mediante consulta de película y el lote de géneros en una consulta adicional. Filtros SQL dinámicos solo añadirán fragmentos constantes y enlazarán todos los valores mediante `?`; el filtro de género no duplicará resultados.
- Los errores de duplicidad identificarán si colisionó `titulo+anio` o `rutaArchivo`; errores FK y CHECK se convertirán en `ValidacionException`. El borrado usará la cascada existente hacia `pelicula_genero` y `progreso`.

### Archivos previstos
- **Nuevos modelos y DTOs:** `model/Pelicula.java`, `model/PeliculaResumen.java`, `dto/PeliculaDTO.java`, `dto/PeliculaRequestDTO.java`, `dto/DirectorPeliculaDTO.java`, `dto/GeneroPeliculaDTO.java`, `mapper/PeliculaMapper.java`.
- **Persistencia y medios:** `dao/GestorTransacciones.java`, `dao/PeliculaDAO.java`, `dao/JdbcPeliculaDAO.java`, `dao/PeliculaGeneroDAO.java`, `dao/JdbcPeliculaGeneroDAO.java`, `service/AlmacenMedios.java`, `config/AlmacenMediosSistemaArchivos.java`.
- **Capa API y binding:** `service/PeliculaService.java`, `service/PeliculaServiceImpl.java`, `controller/PeliculaResource.java`, modificación de `config/MiflixApplication.java`; ajuste acotado de `dao/SQLExceptionTraductor.java` para distinguir las dos restricciones únicas de Película.
- **Pruebas y solicitudes:** `src/test/java/com/antonio/miflix/dao/JdbcPeliculaDAOTest.java`, `src/test/java/com/antonio/miflix/dao/GestorTransaccionesTest.java`, `src/test/java/com/antonio/miflix/dao/PeliculaTransaccionTest.java`, `src/test/java/com/antonio/miflix/service/PeliculaServiceImplTest.java`, `src/test/java/com/antonio/miflix/config/AlmacenMediosSistemaArchivosTest.java`, `http/peliculas.http`.

# Testing

### Validación automatizada
- `JdbcPeliculaDAOTest` con Testcontainers MySQL 8.4 recargará `db/init.sql` antes de cada test; comprobará las ocho películas, filtros individuales/combinados, detalle con director y géneros, escrituras con `Connection`, CHECK de año y FK de director inexistente.
- `GestorTransaccionesTest` comprobará commit y rollback. `PeliculaTransaccionTest` verificará con MySQL real que crear con género inexistente no deja película y que actualizar con género inexistente conserva película y asociaciones anteriores.
- `PeliculaServiceImplTest` usará Mockito para validar límites y normalización de campos, extensión/rutas/archivo, género vacío y duplicado, referencias inexistentes, duplicados, 404 y mensajes/códigos de error. La prueba del almacen verificará normalización y contención de rutas.
- Ejecutar `mvn test` y registrar total, fallos, errores y omitidas.

### Verificación HTTP obligatoria
Crear temporalmente `media-demo/prueba-junie.mp4`, levantar `docker compose up --build -d`, esperar `/api/health` con `UP` y ejecutar cada caso de `http/peliculas.http`. Verificar además: creación con `generoIds [1,999]` devuelve 400 y no aumenta el total; `?q=bunny` devuelve 1 y `?generoId=1` devuelve 7; año 1800 y `../x.mp4` devuelven 400; eliminar película devuelve 204 y `GET /api/generos` confirma el descenso del total asociado. Presentar tabla petición/código esperado/obtenido/resultado; si hay fallos, describirlos antes de corregir. Borrar el archivo temporal y finalizar con `docker compose down -v` seguido de `docker compose up -d`.

# Delivery Steps

### ✓ Step 1: Definir contratos de Película y acceso seguro a medios
Los modelos, DTOs y la abstracción de medios describen el contrato del CRUD y bloquean rutas fuera de `MEDIA_PATH`.
- Añadir `Pelicula`, `PeliculaResumen`, `PeliculaDTO`, `PeliculaRequestDTO`, DTOs de referencia para director/géneros y `PeliculaMapper`.
- Añadir `AlmacenMedios` y `AlmacenMediosSistemaArchivos`, reutilizando `AppConfig.getMediaPath()` para normalizar y validar rutas de vídeo y póster.
- Añadir pruebas del almacen para ruta segura, ruta absoluta/traversal, extensión/existencia según aplique.
- No cambiar el esquema ni añadir configuración externa.

### ✓ Step 2: Implementar consultas JDBC y operaciones transaccionales
Los DAOs consultan catálogo y asociaciones sin N+1, y las escrituras pueden compartir conexión para commit/rollback atómico.
- Añadir `PeliculaDAO`/`JdbcPeliculaDAO` para lectura, filtros seguros y escrituras con `Connection`; mapear la referencia al director en la consulta.
- Añadir `PeliculaGeneroDAO`/`JdbcPeliculaGeneroDAO`: batch `insertarGeneros`, eliminación con conexión y carga agregada `buscarGenerosPorPeliculas`; ids vacíos retornan mapa vacío sin SQL.
- Añadir `GestorTransacciones` genérico con adquisición de conexión desde `DataSource`, desactivación/restauración de `autoCommit`, commit, rollback y cierre.
- Ajustar `SQLExceptionTraductor` para distinguir duplicidad de título/año y ruta de archivo sin alterar las traducciones existentes.
- Añadir pruebas Testcontainers de `JdbcPeliculaDAO` y commit/rollback de `GestorTransacciones`, reiniciando `db/init.sql` por prueba DAO.

### ✓ Step 3: Completar servicio, recurso y verificación de extremo a extremo
`/api/peliculas` ofrece el CRUD y sus filtros con validación completa, relaciones transaccionales y respuestas conforme al contrato.
- Añadir `PeliculaService`/`PeliculaServiceImpl`; validar campos y existencia del archivo, deduplicar `generoIds` y verificar director/géneros con los DAOs existentes antes de abrir transacción.
- Coordinar creación y actualización con `GestorTransacciones`; mapear duplicados a 409 y referencias/CHECK a 400. Mantener el borrado 404/204 y las cascadas existentes.
- Añadir `PeliculaResource` con GET y filtros `q`, `generoId`, `directorId`, GET por id, POST con `Location`, PUT y DELETE; registrar recurso y bindings en `MiflixApplication`.
- Añadir `PeliculaServiceImplTest`, prueba MySQL de rollback crear/actualizar y `http/peliculas.http` con los casos requeridos.
- Ejecutar `mvn test`, pruebas Docker/HTTP y rollback solicitados; informar tabla de estados, eliminar `media-demo/prueba-junie.mp4` y dejar Compose en el estado limpio especificado.