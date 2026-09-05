# AGENTS.md — Michi Música para Android

## Producto

- Aplicación Android nativa, sencilla y privada para reproducir música local.
- Conservar la identidad de gato, la legibilidad y los mensajes comprensibles.
- La música nunca se sube a servicios externos.
- El modo Azar no repite pistas hasta completar una ronda.
- La reproducción debe continuar en segundo plano y responder a los controles del sistema.

## Desarrollo

- Antes de continuar trabajo de producto o interfaz, leer `docs/CONTINUIDAD.md`: dirección vigente, aprobaciones, mapa de código y siguiente comprobación. Los mockups históricos señalados como descartados no son requisitos actuales.
- Conservar la dirección visual y los recorridos aprobados en esa guía y en la ficha de cada pantalla. Reutilizar el tema y los componentes de producción; una mejora funcional no autoriza a rediseñar otras pantallas ni a reemplazar las decisiones vigentes por preferencias de quien continúa.
- Antes de editar, comprobar `git status` y conservar cambios existentes. Antes de entregar cambios visuales, compararlos con la ficha vigente y los estados afectados; registrar qué se observó realmente y qué sigue sin comprobar.
- Las pruebas de borrar o reclasificar deben usar audios creados para la validación. No desinstalar la app del usuario para resolver diferencias de firma ni borrar sus datos.

- Kotlin, Jetpack Compose y AndroidX Media3.
- `compileSdk` y `targetSdk` se actualizan únicamente después de revisar compatibilidad y probar.
- No escribir rutas de perfil ni secretos en el repositorio.
- El acceso a música se concede mediante el selector de carpetas de Android.
- Ejecutar `gradlew test lint assembleDebug` antes de entregar una APK.
- Registrar cambios de comportamiento en `docs/HISTORIAL_CAMBIOS.md` y trabajo pendiente en `docs/BACKLOG.md`.
