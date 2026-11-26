# MiFlix
Servidor de streaming personal desplegado con Docker Compose.

## Configuración y arranque

1. Copia `.env.example` a `.env` y cambia las contraseñas de ejemplo. Ajusta `MEDIA_PATH` a la carpeta del host que contiene los vídeos.
2. Construye la aplicación y arranca los servicios:

   ```powershell
   docker compose up --build -d
   ```

3. Comprueba el estado de los contenedores y la API:

   ```powershell
   docker compose ps
   Invoke-RestMethod -Uri http://localhost:8080/api/health
   ```

La aplicación recibe `MEDIA_PATH=/media` dentro del contenedor; el valor de `.env` solo identifica la carpeta del host montada en solo lectura. Adminer queda disponible en `http://localhost:8081` y usa `db` como servidor.
