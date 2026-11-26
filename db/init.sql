-- =====================================================================
--  MiFlix — Script de inicialización de la base de datos (MySQL 8.4)
-- ---------------------------------------------------------------------
--  Se ejecuta automáticamente la PRIMERA vez que arranca el contenedor
--  `db` (montado en /docker-entrypoint-initdb.d/). La base de datos la
--  crea la imagen oficial de MySQL a partir de MYSQL_DATABASE (.env),
--  y este script se ejecuta dentro de ella.
--
--  Si cambias este archivo y quieres que se vuelva a ejecutar:
--      docker compose down -v   (borra el volumen con los datos)
--      docker compose up --build -d
--
--  Modelo:
--    director 1 ──── N pelicula                       (1:N)
--    pelicula N ──── M genero    vía pelicula_genero  (N:M)
--    perfil   N ──── M pelicula  vía progreso         (N:M con atributos)
-- =====================================================================

SET NAMES utf8mb4;
SET time_zone = '+01:00';

-- Por si se ejecuta a mano sobre una BD con datos (orden inverso a las FK)
DROP TABLE IF EXISTS progreso;
DROP TABLE IF EXISTS pelicula_genero;
DROP TABLE IF EXISTS pelicula;
DROP TABLE IF EXISTS perfil;
DROP TABLE IF EXISTS genero;
DROP TABLE IF EXISTS director;

-- =====================================================================
--  DDL
-- =====================================================================

-- ---------------------------------------------------------------------
--  director
-- ---------------------------------------------------------------------
CREATE TABLE director (
    id            INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(120)  NOT NULL,
    nacionalidad  VARCHAR(60)   NULL,
    CONSTRAINT pk_director        PRIMARY KEY (id),
    CONSTRAINT uq_director_nombre UNIQUE (nombre)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
--  genero
-- ---------------------------------------------------------------------
CREATE TABLE genero (
    id      INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre  VARCHAR(50)  NOT NULL,
    CONSTRAINT pk_genero        PRIMARY KEY (id),
    CONSTRAINT uq_genero_nombre UNIQUE (nombre)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
--  pelicula
--  - ruta_archivo y ruta_poster son RELATIVAS a MEDIA_PATH (/media).
--  - El límite superior del año (año actual) se valida en la capa de
--    servicio: MySQL no permite funciones no deterministas en un CHECK.
-- ---------------------------------------------------------------------
CREATE TABLE pelicula (
    id            INT UNSIGNED      NOT NULL AUTO_INCREMENT,
    titulo        VARCHAR(200)      NOT NULL,
    anio          SMALLINT UNSIGNED NOT NULL,
    duracion_min  SMALLINT UNSIGNED NOT NULL,
    sinopsis      TEXT              NULL,
    ruta_archivo  VARCHAR(500)      NOT NULL,
    ruta_poster   VARCHAR(500)      NULL,
    director_id   INT UNSIGNED      NULL,
    fecha_alta    DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_pelicula              PRIMARY KEY (id),
    CONSTRAINT uq_pelicula_titulo_anio  UNIQUE (titulo, anio),
    CONSTRAINT uq_pelicula_ruta_archivo UNIQUE (ruta_archivo),
    CONSTRAINT ck_pelicula_anio         CHECK (anio >= 1888),
    CONSTRAINT ck_pelicula_duracion     CHECK (duracion_min > 0),
    CONSTRAINT fk_pelicula_director
        FOREIGN KEY (director_id) REFERENCES director (id)
        ON DELETE SET NULL      -- si se borra el director, la película se queda sin director
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_pelicula_director ON pelicula (director_id);
CREATE INDEX idx_pelicula_titulo   ON pelicula (titulo);

-- ---------------------------------------------------------------------
--  pelicula_genero  (tabla intermedia N:M)
-- ---------------------------------------------------------------------
CREATE TABLE pelicula_genero (
    pelicula_id  INT UNSIGNED NOT NULL,
    genero_id    INT UNSIGNED NOT NULL,
    CONSTRAINT pk_pelicula_genero PRIMARY KEY (pelicula_id, genero_id),
    CONSTRAINT fk_pg_pelicula
        FOREIGN KEY (pelicula_id) REFERENCES pelicula (id)
        ON DELETE CASCADE       -- al borrar la película se borran sus asociaciones
        ON UPDATE CASCADE,
    CONSTRAINT fk_pg_genero
        FOREIGN KEY (genero_id) REFERENCES genero (id)
        ON DELETE RESTRICT      -- no se puede borrar un género con películas
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_pg_genero ON pelicula_genero (genero_id);

-- ---------------------------------------------------------------------
--  perfil
-- ---------------------------------------------------------------------
CREATE TABLE perfil (
    id              INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(40)  NOT NULL,
    avatar          VARCHAR(255) NULL,
    es_infantil     BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_creacion  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_perfil        PRIMARY KEY (id),
    CONSTRAINT uq_perfil_nombre UNIQUE (nombre)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
--  progreso  (N:M perfil–pelicula con atributos: "Continuar viendo")
-- ---------------------------------------------------------------------
CREATE TABLE progreso (
    perfil_id       INT UNSIGNED NOT NULL,
    pelicula_id     INT UNSIGNED NOT NULL,
    segundo_actual  INT UNSIGNED NOT NULL DEFAULT 0,
    completada      BOOLEAN      NOT NULL DEFAULT FALSE,
    ultima_vez      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                 ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_progreso PRIMARY KEY (perfil_id, pelicula_id),
    CONSTRAINT fk_progreso_perfil
        FOREIGN KEY (perfil_id) REFERENCES perfil (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_progreso_pelicula
        FOREIGN KEY (pelicula_id) REFERENCES pelicula (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Acelera la consulta "continuar viendo" (por perfil, más recientes primero)
CREATE INDEX idx_progreso_perfil_fecha ON progreso (perfil_id, ultima_vez);
CREATE INDEX idx_progreso_pelicula     ON progreso (pelicula_id);

-- =====================================================================
--  DML — Datos de prueba
--  Películas: cortometrajes libres de la Blender Foundation (licencia
--  Creative Commons), descargables legalmente para la demo.
--  Los archivos deben estar en MEDIA_PATH con estos mismos nombres.
--  Las duraciones son aproximadas.
-- =====================================================================

INSERT INTO director (id, nombre, nacionalidad) VALUES
    (1, 'Sacha Goedegebure', 'Países Bajos'),
    (2, 'Bassam Kurdali',    'Estados Unidos'),
    (3, 'Colin Levy',        'Estados Unidos'),
    (4, 'Ian Hubert',        'Estados Unidos'),
    (5, 'Mathieu Auvray',    'Francia'),
    (6, 'Andy Goralczyk',    'Alemania'),
    (7, 'Hjalti Hjálmarsson','Islandia'),
    (8, 'Matthew Luhn',      'Estados Unidos'),
    (9, 'Director Sin Películas', NULL);   -- para probar borrado sin dependencias

INSERT INTO genero (id, nombre) VALUES
    (1, 'Animación'),
    (2, 'Comedia'),
    (3, 'Fantasía'),
    (4, 'Ciencia ficción'),
    (5, 'Acción'),
    (6, 'Drama'),
    (7, 'Terror'),        -- solo lo usa Sprite Fright
    (8, 'Documental');    -- sin películas: se puede borrar (prueba de DELETE correcto)

INSERT INTO pelicula (id, titulo, anio, duracion_min, sinopsis, ruta_archivo, ruta_poster, director_id) VALUES
    (1, 'Big Buck Bunny', 2008, 10,
        'Un conejo enorme y bonachón decide darles una lección a los tres roedores que no paran de molestar a los animales del bosque.',
        'big-buck-bunny.mp4', 'posters/big-buck-bunny.jpg', 1),
    (2, 'Elephants Dream', 2006, 11,
        'Emo y Proog recorren una máquina infinita y surrealista que solo uno de ellos parece comprender.',
        'elephants-dream.mp4', 'posters/elephants-dream.jpg', 2),
    (3, 'Sintel', 2010, 15,
        'Una joven busca por tierras hostiles al pequeño dragón al que cuidó y que le fue arrebatado.',
        'sintel.mp4', 'posters/sintel.jpg', 3),
    (4, 'Tears of Steel', 2012, 12,
        'En una Ámsterdam futurista, un grupo de científicos intenta salvar el mundo de unos robots reviviendo un viejo desamor.',
        'tears-of-steel.mp4', 'posters/tears-of-steel.jpg', 4),
    (5, 'Cosmos Laundromat', 2015, 12,
        'Una oveja deprimida en una isla desierta recibe una oferta extraña de un vendedor muy peculiar: vivir otras vidas.',
        'cosmos-laundromat.mp4', 'posters/cosmos-laundromat.jpg', 5),
    (6, 'Spring', 2019, 8,
        'Una pastora y su perro se enfrentan a antiguos espíritus para que el ciclo de la vida continúe.',
        'spring.mp4', 'posters/spring.jpg', 6),
    (7, 'Agent 327: Operation Barbershop', 2017, 4,
        'El agente secreto 327 investiga una barbería que esconde mucho más que tijeras y navajas.',
        'agent-327.mp4', 'posters/agent-327.jpg', 7),
    (8, 'Sprite Fright', 2021, 10,
        'Un grupo de adolescentes de excursión por un bosque británico descubre que sus diminutos habitantes no son tan adorables.',
        'sprite-fright.mp4', 'posters/sprite-fright.jpg', 8);

INSERT INTO pelicula_genero (pelicula_id, genero_id) VALUES
    (1, 1), (1, 2),            -- Big Buck Bunny: Animación, Comedia
    (2, 1), (2, 4),            -- Elephants Dream: Animación, Ciencia ficción
    (3, 1), (3, 3), (3, 6),    -- Sintel: Animación, Fantasía, Drama
    (4, 4), (4, 5),            -- Tears of Steel: Ciencia ficción, Acción
    (5, 1), (5, 2), (5, 3),    -- Cosmos Laundromat: Animación, Comedia, Fantasía
    (6, 1), (6, 3),            -- Spring: Animación, Fantasía
    (7, 1), (7, 5), (7, 2),    -- Agent 327: Animación, Acción, Comedia
    (8, 1), (8, 2), (8, 7);    -- Sprite Fright: Animación, Comedia, Terror

INSERT INTO perfil (id, nombre, avatar, es_infantil) VALUES
    (1, 'Antonio',  'avatares/rojo.png',     FALSE),
    (2, 'Invitado', 'avatares/azul.png',     FALSE),
    (3, 'Peques',   'avatares/amarillo.png', TRUE);

-- segundo_actual en segundos; completada = TRUE si se ha visto ≥ 90 %
INSERT INTO progreso (perfil_id, pelicula_id, segundo_actual, completada, ultima_vez) VALUES
    (1, 3,  420, FALSE, NOW() - INTERVAL 1 DAY),   -- Antonio va por el min 7 de Sintel
    (1, 4,  700, TRUE,  NOW() - INTERVAL 3 DAY),   -- Antonio terminó Tears of Steel
    (1, 5,  150, FALSE, NOW() - INTERVAL 2 HOUR),  -- Antonio empezó Cosmos Laundromat
    (2, 1,  300, FALSE, NOW() - INTERVAL 5 DAY),   -- Invitado a mitad de Big Buck Bunny
    (3, 6,  480, TRUE,  NOW() - INTERVAL 1 DAY),   -- Peques terminó Spring
    (3, 1,   90, FALSE, NOW() - INTERVAL 6 HOUR);  -- Peques empezó Big Buck Bunny
