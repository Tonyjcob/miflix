# Guidelines para Junie — Proyecto MiFlix

> Este archivo define cómo debe trabajar Junie en este repositorio.
> Léelo completo antes de cada tarea. Si una petición contradice estas reglas, avísame antes de continuar.

## 1. Contexto del proyecto

MiFlix es una plataforma de streaming personal (tipo Netflix) para ver películas almacenadas en local.
Es la práctica "Entrega 1 con IA" del módulo **Acceso a Datos** (2.º DAM). El alumno actúa como
arquitecto y revisor; Junie implementa bajo sus indicaciones.

Funcionalidades:
- Selección de perfil (perfiles normales e infantiles).
- Catálogo de películas con filtro por género y búsqueda por título.
- Detalle de película y reproducción en el navegador (streaming con HTTP Range).
- "Continuar viendo": guardar y recuperar el progreso de cada perfil.
- Panel de administración con CRUD de películas, géneros, directores y perfiles.

## 2. Stack (no cambiar sin preguntar)

- Java 21, Maven, empaquetado `.war`.
- Tomcat 10.1 (Jakarta EE 10, paquetes `jakarta.*`, nunca `javax.*`).
- API REST con JAX-RS (Jersey) + Jackson para JSON.
- Acceso a datos con **JDBC puro** + **HikariCP**. **No usar JPA/Hibernate.**
- MySQL 8.4.
- Frontend: HTML5 semántico + CSS3 + JavaScript vanilla (ES2020+) con `fetch`. Sin frameworks.
- Tests: JUnit 5, Mockito, Testcontainers (MySQL).
- **No añadir dependencias nuevas al `pom.xml` sin pedirme permiso y justificarlo.**

## 3. Arquitectura por capas (obligatorio)

Paquete base: `com.antonio.miflix`

```
controller/  → Recursos JAX-RS. Solo: recibir petición, llamar al servicio, devolver Response.
service/     → Interfaces + implementaciones. Reglas de negocio, validaciones y transacciones.
dao/         → Interfaces + implementaciones JDBC. Solo SQL y mapeo ResultSet → entidad.
model/       → Entidades que reflejan las tablas. Nunca salen de la capa de servicio.
dto/         → Objetos de transferencia (entrada y salida de la API).
mapper/      → Conversión entidad ↔ DTO.
exception/   → Excepciones propias y ExceptionMappers de JAX-RS.
config/      → DataSourceProvider y configuración leída de variables de entorno.
```

Reglas:
- Flujo estricto: `Controller → Service → DAO → BD`. Ninguna capa se salta a otra.
- Los controladores **no** contienen lógica de negocio, SQL ni `try/catch` de negocio.
- Los controladores reciben y devuelven **solo DTOs**, nunca entidades de `model/`.
- DTOs de entrada separados de los de salida cuando tengan campos distintos
  (ej. `PeliculaCreateDTO` vs `PeliculaDTO`).
- Programar contra interfaces (`PeliculaService`, `PeliculaDAO`) y no contra implementaciones.
- Sin dependencias circulares entre paquetes.

## 4. Acceso a datos

- **Siempre `PreparedStatement`.** Prohibido concatenar valores en sentencias SQL.
- **Siempre try-with-resources** para `Connection`, `PreparedStatement` y `ResultSet`.
- Las conexiones salen del `DataSource` de HikariCP (`DataSourceProvider`), nunca de `DriverManager`.
- Las `SQLException` se envuelven en una excepción propia (`DataAccessException`, unchecked).
- Detectar clave duplicada (SQLState `23000` / error 1062) y convertirla en una excepción de dominio.
- Operaciones que tocan varias tablas (ej. crear película + sus géneros) van en **una transacción**:
  `setAutoCommit(false)`, `commit()` y `rollback()` en caso de error.
- Los nombres de tablas y columnas son los de `db/init.sql`. **No modificar `init.sql` sin preguntarme.**

## 5. Configuración externalizada

- **Prohibido escribir credenciales, URLs de conexión o rutas absolutas en el código.**
- Leer con `System.getenv()`: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `MEDIA_PATH` (dentro del contenedor: `/media`).
- Si falta una variable obligatoria, fallar al arrancar con un mensaje claro.
- Cualquier variable nueva debe añadirse también a `.env.example` y al README.

## 6. Reglas de negocio (capa de servicio)

- Película: título obligatorio (máx. 200), año entre 1888 y el año actual, duración > 0,
  archivo existente dentro de `MEDIA_PATH` con extensión `.mp4` o `.webm`.
- No puede haber dos películas con mismo título y año.
- No se puede borrar un género que tenga películas asociadas.
- Máximo 5 perfiles. Nombre de perfil único.
- Progreso: si `segundo_actual` ≥ 90 % de la duración → `completada = true`.
- Los perfiles infantiles solo ven películas de géneros permitidos (Animación, Comedia, Fantasía).

## 7. Excepciones y códigos HTTP

| Situación | Excepción | HTTP |
|---|---|---|
| Lectura correcta / actualización | — | 200 |
| Creación | — | 201 + cabecera `Location` |
| Borrado correcto | — | 204 |
| Datos de entrada inválidos | `ValidacionException` | 400 |
| Recurso inexistente | `RecursoNoEncontradoException` | 404 |
| Duplicado o conflicto de integridad | `ConflictoException` | 409 |
| Error no controlado | cualquier otra | 500 |

- Todas las respuestas de error con el mismo JSON: `{ "status": 400, "error": "...", "mensaje": "...", "timestamp": "..." }`.
- Nunca devolver stacktraces al cliente. Loguear el error en el servidor.

## 8. Streaming de vídeo

- Endpoint `GET /api/stream/{peliculaId}` con soporte de cabecera `Range`:
  respuesta 206, `Content-Range`, `Accept-Ranges: bytes`, `Content-Length` correcto.
- Leer el archivo por bloques (`StreamingOutput`); **nunca cargarlo entero en memoria**.
- Proteger contra *path traversal*: normalizar la ruta y comprobar que está dentro de `MEDIA_PATH`.
- La carpeta de medios es de solo lectura; la app nunca escribe en ella.

## 9. Frontend

- Sin Java ni SQL en las vistas. Toda la comunicación con la API va por `fetch` (async/await).
- Un módulo JS `api.js` centraliza las llamadas y el manejo de errores.
- Mostrar estados de carga y mensajes de error claros (usar el campo `mensaje` del JSON de error).
- Validación también en el formulario, pero la válida es siempre la del servidor.
- Diseño responsive (CSS Grid/Flexbox), estética oscura tipo plataforma de streaming.
- La consola del navegador debe quedar sin errores.

## 10. Tests

- Todo código nuevo de DAO o servicio va acompañado de sus tests.
- DAO: tests de integración con Testcontainers (MySQL 8.4) cargando `db/init.sql`.
- Servicio: tests unitarios con Mockito (DAO mockeado).
- Cubrir siempre: camino feliz, registro inexistente, clave duplicada y datos inválidos.
- Nombres descriptivos: `crear_conTituloYAnioDuplicados_lanzaConflictoException()`.
- `mvn test` debe pasar antes de dar una tarea por terminada.

## 11. Estilo de código

- Nombres de clases, métodos y variables en español, salvo términos técnicos (DAO, DTO, Resource…).
- Javadoc breve en interfaces públicas.
- Métodos cortos y con una sola responsabilidad.
- Nada de código comentado ni `System.out.println` (usar `java.util.logging` o SLF4J si ya está).

## 12. Cómo trabajar conmigo

- Antes de cambios grandes (más de 3 archivos o algo de arquitectura), **explica el plan y espera mi OK**.
- Haz tareas pequeñas y acotadas; no implementes funcionalidades que no he pedido.
- Al terminar cada tarea, indica:
  1. Lista de archivos creados o modificados.
  2. Cómo probarlo (comando o petición HTTP).
  3. Un mensaje de commit propuesto en formato Conventional Commits (`feat:`, `fix:`, `test:`, `docs:`, `chore:`, `refactor:`).
  4. Un resumen de 2-3 líneas para que yo lo añada a `AI_WORKFLOW.md` (qué se pidió, qué se hizo, decisiones tomadas).
- Si no estás seguro de algo, pregunta en vez de suponer.
- No toques `.env`, `.env.example`, `docker-compose.yml`, `Dockerfile` ni `init.sql` salvo que la tarea lo pida.
