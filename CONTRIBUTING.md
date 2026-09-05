# Contribuir a Michi Música

Puedes estudiar, modificar y compartir el proyecto bajo GPL-3.0. Empieza por [AGENTS.md](AGENTS.md) y [Continuidad](docs/CONTINUIDAD.md), también si trabajas con otra IA. Ahí están las decisiones aprobadas y los recorridos que deben conservarse.

## Preparar el proyecto

1. Clona este repositorio y ábrelo en Android Studio.
2. Instala Android SDK Platform 36 y selecciona JDK 21 para Gradle. El bytecode Java/Kotlin sigue siendo 17.
3. Ejecuta `./gradlew test lint assembleDebug`; en Windows, `./gradlew.bat`. La primera compilación necesita Internet; después puede usarse `--offline` si la caché está completa.
4. La app debug se genera en `app/build/outputs/apk/debug/app-debug.apk`; el paquete de iconos, en `iconpack/build/outputs/apk/debug/iconpack-debug.apk`.

Las APK que compiles con tu clave no pueden actualizar una instalación firmada por otra clave. Para un fork instalado junto a la original, cambia `applicationId` y la referencia del paquete en `iconpack/src/main/res/xml/appfilter.xml`; conserva atribuciones y licencia.

## Cambios y pruebas

- Un PR por problema o mejora concreta. Describe el comportamiento anterior y el nuevo, con capturas si cambia la interfaz y las comprobaciones realizadas.
- Reutiliza componentes de producción y el sistema visual aprobado. No recuperes los mockups históricos descartados.
- Mantén música y datos personales fuera de Git. No uses tu biblioteca real para fixtures públicos.
- Añade pruebas para lógica o fallos relevantes; no des por probados audio, red o permisos porque una preview se dibuje.
- Ejecuta las tareas requeridas por AGENTS y actualiza `docs/HISTORIAL_CAMBIOS.md` y `docs/BACKLOG.md`.
- Si utilizas IA, indícalo en el PR y revisa su resultado. Aporta datos reproducibles; no inventes pruebas o referencias.

## Diseño y capturas

Las previews están en `app/src/debug/java/com/ainalluna/michimusica/ui/`. `ShowcaseActivity` muestra esos componentes con datos ficticios para capturas reproducibles y solo existe en debug. Por ejemplo:

```sh
adb shell am start -S -n com.ainalluna.michimusica/.ui.ShowcaseActivity --es screen lyrics
```

Opciones: `library`, `search`, `lists`, `player`, `lyrics`, `rose`. No reproduce audio ni consulta servicios. Los iconos SVG, PNG y vectores Android se regeneran con `python scripts/generate_icons.py` (requiere Pillow).

## Publicar un fork

Usa una clave propia y guárdala fuera de Git. `MICHI_SIGNING_PROPERTIES` puede apuntar a un archivo privado con `storeFile`, `storePassword`, `keyAlias` y `keyPassword`; `storeFile` se resuelve respecto a ese archivo. No subas el archivo, la clave o sus contraseñas. El script de preparación incluido está pensado para el mantenedor en Windows; no es necesario para contribuir ni para compilar debug.

La [plantilla CI](docs/ci/README.md) se entrega preparada, pendiente de activar en GitHub con permisos de escritura de workflows. Mientras tanto, adjunta el resultado de las comprobaciones locales a cada PR.
