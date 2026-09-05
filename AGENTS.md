# AGENTS.md — Michi Música para Android

## Producto

- Aplicación Android nativa, sencilla y privada para reproducir música local.
- Conservar la identidad de gato, la legibilidad y los mensajes comprensibles.
- La música nunca se sube a servicios externos.
- El modo Azar no repite pistas hasta completar una ronda.
- La reproducción debe continuar en segundo plano y responder a los controles del sistema.

## Desarrollo

- Antes de continuar trabajo de producto o interfaz, leer `docs/CONTINUIDAD.md`: dirección vigente, aprobaciones, mapa de código y siguiente comprobación. Los mockups históricos señalados como descartados no son requisitos actuales.

- Kotlin, Jetpack Compose y AndroidX Media3.
- `compileSdk` y `targetSdk` se actualizan únicamente después de revisar compatibilidad y probar.
- No escribir rutas de perfil ni secretos en el repositorio.
- El acceso a música se concede mediante el selector de carpetas de Android.
- Ejecutar `gradlew test lint assembleDebug` antes de entregar una APK.
- Registrar cambios de comportamiento en `docs/HISTORIAL_CAMBIOS.md` y trabajo pendiente en `docs/BACKLOG.md`.
