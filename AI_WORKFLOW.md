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

---

### Fase 3A · Configuración base, pool de conexiones, excepciones y endpoint de salud — 26/11/2026

**Prompt 1 dado a Junie:**

<details>
<summary>Ver prompt completo</summary>

> Tarea (Fase 3A): configurar el proyecto Java y la base de la arquitectura. Lee .junie/guidelines.md antes de empezar.
>
> 1. pom.xml — añade las dependencias, con las versiones en `<properties>` y usando BOMs donde existan:
     >    jakarta.servlet-api 6.0 (scope provided; Tomcat 10.1 ya la incluye); Jersey 3.1.x (compatible con
     >    Jakarta EE 10 / Tomcat 10.1; NO Jersey 4): jersey-container-servlet, jersey-hk2, jersey-media-json-jackson;
     >    jackson-datatype-jsr310; HikariCP; mysql-connector-j; un binding de SLF4J; y para tests JUnit 5, Mockito y
     >    Testcontainers (MySQL + JUnit 5). Usa versiones estables actuales y explícame para qué sirve cada dependencia.
> 2. Paquete config: AppConfig (lee DB_URL, DB_USER, DB_PASSWORD y MEDIA_PATH con System.getenv() y falla con un
     >    mensaje claro si falta alguna), DataSourceProvider (un único HikariDataSource perezoso y thread-safe, máx. 10
     >    conexiones, nombre "miflix-pool") y AppContextListener (cierra el pool al parar la app).
> 3. Paquete exception: DataAccessException, ValidacionException, RecursoNoEncontradoException, ConflictoException,
     >    ErrorDTO (status, error, mensaje, timestamp), un ExceptionMapper por excepción (400, 404, 409) y un mapper
     >    genérico que devuelva 500 sin exponer el stacktrace y registre el error en el log.
> 4. MiflixApplication con @ApplicationPath("/api") y un ContextResolver<ObjectMapper> con JavaTimeModule y
     >    fechas ISO-8601.
> 5. Endpoint de salud respetando Controller → Service → DAO: SaludDAO/JdbcSaludDAO ("SELECT 1" con
     >    PreparedStatement y try-with-resources), SaludService/SaludServiceImpl y SaludResource
     >    (GET /api/health → 200 UP / 503 DOWN).
> 6. Tests: SaludServiceImplTest con Mockito (BD disponible y caída) y AppConfigTest (variable ausente).
> 7. Borra los .gitkeep de las carpetas donde crees archivos.
>
> No crees Dockerfile ni modifiques docker-compose.yml, init.sql ni .env. Ejecuta mvn clean package y dame el
> resultado, la lista de archivos, el mensaje de commit y el resumen para AI_WORKFLOW.md.

</details>

**Qué generó Junie:**
- `pom.xml` con versiones en propiedades y BOMs: Servlet 6.0.0 (provided), Jersey 3.1.12, Jackson,
  HikariCP, MySQL Connector/J, SLF4J (slf4j-simple), JUnit 5, Mockito y Testcontainers.
- `config/`: `AppConfig`, `DataSourceProvider`, `DataSourceFactory`, `AppContextListener`,
  `JacksonObjectMapperProvider` y `MiflixApplication`.
- `exception/`: 4 excepciones propias y sus `ExceptionMapper` (400, 404, 409 y 500 genérico).
- `dto/`: `ErrorDTO` y `SaludDTO`.
- Endpoint `GET /api/health` por capas: `SaludResource → SaludService → SaludDAO`.
- Tests: `AppConfigTest`, `SaludServiceImplTest` (Mockito) y `JdbcSaludDAOTest` (Testcontainers).

**Revisión:**
- ✅ Jersey 3.1.x, compatible con Tomcat 10.1 / Jakarta EE 10. Sin imports `javax.*`.
- ✅ Sin credenciales ni URLs en el código: todo pasa por `AppConfig`.
- ✅ DAO con `PreparedStatement` y try-with-resources.
- ✅ Versiones verificadas: Maven las resolvió todas desde Maven Central.
- 💡 **Aciertos del agente:**
  - Añadió por iniciativa propia `JdbcSaludDAOTest` con Testcontainers, que además valida que
    `init.sql` se ejecuta correctamente sobre un MySQL 8.4 real.
  - Creó la interfaz `DataSourceFactory` para poder testear `DataSourceProvider` sin una base de datos.
  - Refactorizó `AppConfig` para recibir un `Map<String,String>` y poder testear la validación
    de variables sin depender de `System.getenv()`.
- ⚠️ **Error detectado en revisión:** `ErrorExceptionMapper` implementaba `ExceptionMapper<Exception>`
  y capturaba también las `WebApplicationException` del propio JAX-RS. Consecuencia: una ruta
  inexistente (404), un método no permitido (405) o un tipo de contenido no soportado (415)
  devolvían **500** y se registraban en el log como error grave (`SEVERE`). Esto incumplía el
  requisito de códigos HTTP semánticos.

**Prompt 2 dado a Junie (corrección):**
> El ErrorExceptionMapper implementa ExceptionMapper<Exception> y convierte en 500 las
> WebApplicationException de JAX-RS (404 de ruta inexistente, 405 método no permitido, 415 tipo no
> soportado...). Corrígelo: si la excepción es una WebApplicationException, respeta su código HTTP
> original devolviendo un ErrorDTO con ese status y un mensaje en español según el código, sin
> registrarla como SEVERE. Cualquier otra excepción sigue devolviendo 500 genérico con log SEVERE.
> Añade ErrorExceptionMapperTest con NotFoundException → 404, NotAllowedException → 405 y
> RuntimeException → 500 sin detalles internos. Ejecuta mvn test.

**Resultado de la corrección:** el mapper distingue `WebApplicationException` (conserva su status,
mensaje en español y log `FINE`) del resto de excepciones (500 genérico y log `SEVERE`). Se añadió
`ErrorExceptionMapperTest` con pruebas de regresión para 404, 405, 415 y 500.

**Correcciones manuales:**
- Los mensajes de commit propuestos por el agente estaban en inglés; se tradujeron al español
  para mantener la coherencia del historial.

**Verificación:**
- `mvn test` → **11 tests, 0 fallos, 0 errores**.
- `mvn clean package` → BUILD SUCCESS, genera `target/miflix.war`.

**Lección aprendida:** un `ExceptionMapper<Exception>` genérico es útil como red de seguridad, pero
en JAX-RS hay que excluir explícitamente las `WebApplicationException`, porque el propio framework
las usa para comunicar errores HTTP legítimos.

**Commit:** `feat: configuración base, pool de conexiones, excepciones y endpoint de salud`

---

### Fase 3B · Contenerización de la aplicación (Dockerfile multi-stage + servicio app) — 27/11/2026

**Prompt dado a Junie:**

<details>
<summary>Ver prompt completo</summary>

> Tarea (Fase 3B): contenerizar la aplicación. Esta tarea SÍ te autoriza a crear Dockerfile y
> .dockerignore y a modificar docker-compose.yml. Lee .junie/guidelines.md antes de empezar.
>
> 1. Dockerfile multi-stage: etapa "build" con maven:3.9-eclipse-temurin-21 (copiar primero solo
     >    el pom.xml y ejecutar mvn dependency:go-offline para aprovechar la caché; después copiar src y
     >    mvn package -DskipTests). Etapa final con Tomcat 10.1 y JRE 21 (Temurin), eliminando las
     >    webapps por defecto y copiando el WAR como webapps/ROOT.war. EXPOSE 8080.
> 2. .dockerignore excluyendo target/, .git/, .idea/, .junie/, media-demo/, docs/, .env y vídeos.
> 3. docker-compose.yml: añadir el servicio "app" sin modificar db ni adminer: build local,
     >    variables DB_* desde .env, MEDIA_PATH fijo a /media dentro del contenedor, bind mount
     >    ${MEDIA_PATH}:/media en solo lectura, puerto ${APP_PORT}:8080, depends_on db con
     >    condition: service_healthy, redes backend y frontend, restart unless-stopped.
>
> No modifiques código Java, init.sql ni .env.

</details>

**Qué generó Junie:**
- `Dockerfile` multi-stage:
    - Etapa `build` (`maven:3.9-eclipse-temurin-21`): copia el `pom.xml`, ejecuta
      `mvn -B dependency:go-offline`, copia `src` y empaqueta con `mvn -B package -DskipTests`.
    - Etapa final (`tomcat:10.1-jre21-temurin`): borra las webapps por defecto y despliega el WAR
      como `ROOT.war`.
- `.dockerignore` excluyendo artefactos de compilación, configuración del IDE, secretos y vídeos.
- Servicio `app` en `docker-compose.yml`, con dependencia de `db` en estado healthy.

**Comportamiento del agente:** al relanzar el prompt, Junie informó de que los archivos ya
existían y cumplían los requisitos, por lo que no hizo cambios. Además, no pudo verificar la
construcción de la imagen porque Docker Desktop no estaba arrancado: solo validó la sintaxis con
`docker compose config --quiet`. La verificación real se hizo manualmente después.

**Revisión:**
- ✅ Multi-stage real: la imagen final solo contiene Tomcat y el WAR, sin Maven ni código fuente.
- ✅ Caché de capas: `pom.xml` y dependencias se copian antes que `src`, así que un cambio de
  código no vuelve a descargar dependencias.
- ✅ Maven en modo batch (`-B`), con logs limpios.
- ✅ WAR desplegado como `ROOT.war`: la API queda en `/api`.
- ✅ Los tests se omiten en la build (`-DskipTests`) porque los de integración usan Testcontainers
  y necesitan Docker; se ejecutan en local con `mvn test`.
- [✅ En el servicio `app`, `MEDIA_PATH` es `/media` (ruta del contenedor) y el volumen de medios es de solo lectura (`:ro`).]

**Correcciones manuales:**
- El commit se creó con un mensaje equivocado (repetía el de la Fase 3A). Como aún no estaba
  subido, se corrigió con `git commit --amend`.

**Mejoras identificadas (pendientes):**
- Ejecutar Tomcat con un usuario sin privilegios (`USER tomcat`) en lugar de root.
- Fijar la versión de la imagen de Adminer y usar `MYSQL_PWD` en el healthcheck de `db`.

**Verificación:**
- `mvn test` → 42 tests, 0 fallos (DAO con MySQL real vía Testcontainers, servicio con Mockito y
  traductor).
- Verificación en Docker ejecutada por Junie con tabla esperado/obtenido: listar 200, buscar 200/404,
  crear 201/400/409, actualizar 200/409, eliminar Documental 204, eliminar Animación 409, ruta
  inexistente 404 en JSON, error de Tomcat sin stacktrace → todo OK.
- Caso límite: crear "animacion" devuelve 409, porque la intercalación `utf8mb4_0900_ai_ci` no
  distingue tildes ni mayúsculas.
- ⚠️ La verificación detectó un fallo: `DELETE /api/generos/7` (Terror) devolvía "tiene 1 películas
  asociadas". La corrección de concordancia, identificada en la revisión de código, no se había llegado
  a aplicar. El agente lo reportó sin modificar código, como pedía el prompt; se corrigió manualmente y
  se volvió a verificar.

- `fix(config): driver JDBC explícito para Tomcat, pool sin conexión forzada y errores de Tomcat ocultos`

