# AI Workflow — MiFlix

Registro de ingeniería del desarrollo de MiFlix con el agente de codificación **Junie** (IntelliJ IDEA).
Para cada tarea se documenta el prompt dado, lo que generó el agente, la revisión realizada
y las correcciones manuales.

---

## Decisiones de arquitectura

| Decisión | Motivo |
|---|---|
| **JDBC puro + HikariCP en vez de JPA/Hibernate** | El módulo es Acceso a Datos: con JDBC y `PreparedStatement` se implementa explícitamente el patrón DAO, el control de transacciones (`commit`/`rollback`) y la gestión de recursos con try-with-resources. |
| **Películas en un bind mount de solo lectura, no en la BD** | Los vídeos ocupan GB. En la BD solo se guardan los metadatos y la ruta relativa; el backend los sirve por streaming con HTTP Range (206 Partial Content). |
| **Java 21 (Eclipse Temurin)** | Versión LTS. Es la misma distribución que usan las imágenes Docker (`maven:…-eclipse-temurin-21`, `tomcat:10.1-jre21`), así el entorno local y el contenedor son idénticos. |
| **Guidelines de proyecto para Junie (`.junie/guidelines.md`)** | Fijan stack, capas, reglas de acceso a datos, códigos HTTP y forma de trabajar, para que todas las tareas del agente sigan los mismos criterios. |
| **Red `backend` interna en Docker** | La base de datos no es accesible desde fuera de Docker; solo Adminer (y después la app) están en la red `frontend` para publicar su puerto. |

---

## Registro de tareas

### Fase 0 · Estructura inicial del proyecto — 26/11/2026

**Trabajo realizado:** creación manual del proyecto Maven en IntelliJ (packaging `war`, Java 21),
estructura de carpetas `src/main/java`, `src/main/webapp`, `src/test/java`, `.gitignore`,
`.env.example`, `README.md` y este archivo.

**Apoyo de IA:** el borrador de `.junie/guidelines.md` y de `db/init.sql` se preparó con ayuda
de un asistente de IA conversacional (Claude) a partir del enunciado. Ambos archivos se revisaron
manualmente antes del commit.

**Revisión del modelo de datos (`init.sql`):**
- 6 tablas: `director`, `pelicula`, `genero`, `pelicula_genero`, `perfil`, `progreso`.
- Relación 1:N `director → pelicula` con `ON DELETE SET NULL`.
- Relación N:M `pelicula ↔ genero` con `ON DELETE CASCADE` (película) y `ON DELETE RESTRICT` (género).
- Relación N:M con atributos `perfil ↔ pelicula` (`progreso`) con `ON DELETE CASCADE`.
- El límite superior del año no se pone en un `CHECK` porque MySQL no admite funciones no
  deterministas (`YEAR(CURDATE())`); se validará en la capa de servicio.
- Datos de prueba pensados para los tests: un género sin películas (Documental) y un director
  sin películas para probar borrados correctos, y géneros con películas para probar el RESTRICT.

**Correcciones manuales:**
- Renombrado del archivo de registro de `IA_WORKFLOW.MD` a `AI_WORKFLOW.md` (nombre exigido en el enunciado).
- Cambio del JDK del proyecto de OpenJDK 25 a Temurin 21.

**Commit:** `chore: estructura inicial, guidelines de Junie y script init.sql`

---

### Fase 2 · Infraestructura de base de datos con Docker Compose — 26/11/2026

**Prompt dado a Junie:**
> Tarea: crear la infraestructura Docker de la base de datos (Fase 2). Esta tarea SÍ te autoriza
> a crear docker-compose.yml. Lee .junie/guidelines.md, db/init.sql y .env.example antes de empezar.
> Crea un docker-compose.yml en la raíz con SOLO estos dos servicios: "db" (mysql:8.4, variables
> desde .env, volumen nombrado db_data, init.sql montado en /docker-entrypoint-initdb.d en solo
> lectura, healthcheck con mysqladmin ping escapando $ como $$, sin publicar el 3306,
> restart unless-stopped) y "adminer" (puerto ${ADMINER_PORT}:8080, ADMINER_DEFAULT_SERVER=db,
> depends_on db con condition: service_healthy). Redes: "backend" internal (db y adminer) y
> "frontend" (solo adminer). Sin la clave "version:", contenedores miflix-db y miflix-adminer,
> comentarios en español. No modifiques ningún otro archivo.

**Qué generó Junie:** `docker-compose.yml` con los servicios `db` y `adminer`, healthcheck,
volumen nombrado `db_data`, redes `backend` (interna) y `frontend`, sin credenciales en el archivo.

**Revisión:**
- ✅ Ninguna contraseña escrita; todo se inyecta desde `.env`.
- ✅ Healthcheck con `$$` (la variable se resuelve dentro del contenedor, no en el host).
- ✅ `db` no publica el puerto 3306.
- ✅ `depends_on` con `condition: service_healthy`.
- ✅ Volumen `db_data` declarado como volumen nombrado.
- 💡 **Acierto del agente:** el healthcheck usa `--host=127.0.0.1` en lugar de `localhost`.
  Durante la ejecución del `init.sql`, MySQL arranca un servidor temporal sin red; con
  `localhost` (socket) el healthcheck podría marcar "healthy" antes de terminar la inicialización
  y provocar una condición de carrera. Con `127.0.0.1` solo es "healthy" cuando el servidor
  definitivo está listo.

**Correcciones manuales:** sin correcciones en el código generado; el `docker-compose.yml`
cumplía todos los requisitos del prompt.

**Mejoras identificadas (pendientes):**
- Fijar la versión de la imagen de Adminer (ahora usa `latest`) para que el despliegue sea reproducible.
- Pasar la contraseña del healthcheck mediante `MYSQL_PWD` para evitar el aviso
  "Using a password on the command line interface can be insecure" en los logs.

**Verificación:**
- `docker compose ps` → `miflix-db` en estado **healthy**.
- Adminer (http://localhost:8081): 6 tablas creadas. Recuento de datos de prueba: 8 películas,
  8 géneros, 9 directores, 3 perfiles, 6 registros de progreso.
- Integridad referencial: `DELETE FROM genero WHERE id = 1` falla (RESTRICT);
  `DELETE FROM genero WHERE id = 8` funciona.
- Persistencia: tras `docker compose down` y `up -d` los cambios se mantienen (volumen nombrado).
  Con `docker compose down -v` se reinicia la BD y se vuelve a ejecutar `init.sql`.
- Capturas en `docs/img/`.

[Si hubo algún problema por el camino, añádelo aquí. Ejemplo: "El primer arranque falló porque
el .env estaba vacío; se rellenó y se limpió el volumen con `docker compose down -v`."]

**Commit:** `feat(docker): servicio db MySQL 8.4 y Adminer con healthcheck y volumen persistente`