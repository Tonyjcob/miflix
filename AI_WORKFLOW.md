# AI Workflow — MiFlix

Registro del trabajo con Junie: prompts, decisiones y correcciones manuales.

## Decisiones de arquitectura

- **JDBC puro en vez de JPA**: el módulo es Acceso a Datos; con JDBC + PreparedStatement
  se ve explícitamente el patrón DAO, el control de transacciones y el manejo de recursos.
- **Vídeos en un bind mount de solo lectura, no en la BD**: los archivos ocupan GB;
  en la BD solo se guardan metadatos y la ruta relativa. Se sirven con HTTP Range (206).

## Registro de tareas

<!-- Formato: fecha · fase · prompt literal · qué generó · qué corregí y por qué -->