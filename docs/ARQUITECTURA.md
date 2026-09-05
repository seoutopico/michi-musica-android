# Arquitectura

Estado de producto, aprobaciones y punto de reanudación en [Continuidad](CONTINUIDAD.md).

La interfaz usa Jetpack Compose. `MainActivity` obtiene permiso persistente para una carpeta mediante Storage Access Framework y `MusicFolderReader` enumera únicamente sus audios directos. Los `content://` URI autorizados se entregan a Media3, sin convertirlos en rutas del sistema de archivos.

`PlaybackService` contiene `ExoPlayer` y `MediaSession` dentro de un `MediaSessionService`. Así la reproducción sobrevive a la actividad, publica controles multimedia y responde a auriculares o Bluetooth.

La actividad se conecta al servicio con `MediaController`; ningún audio sale del dispositivo. Carpeta, skin, colección de playlists y último punto de escucha se almacenan en `SharedPreferences` privados de la aplicación. Preparar una cola no se considera escucha activa ni provoca reproducción. El mini reproductor muestra la escucha activa o, si existe, la última canción pausada. Al reanudar cambia a la fuente guardada y espera a que los identificadores y el orden de la cola coincidan antes de hacer `seekTo` y aplicar la intención de reproducir o seguir en pausa. El escaneo inicial no sustituye una cola del servicio hasta que termina de leer la fuente correcta.

`LibraryHome.kt` contiene la portada, el mini reproductor y la navegación inferior compartida. `SongArtwork.kt` lee únicamente la imagen ya incrustada en el archivo mediante `MediaMetadataRetriever`: limita a dos lecturas simultáneas, reduce la imagen y mantiene una caché de 8 MB. Sin carátula muestra un fondo felino. No modifica los metadatos ni consulta imágenes externas. Las previews debug reutilizan estos componentes con canciones ficticias.

`MusicFolderReader` usa `MediaMetadataRetriever` y libera el recurso de forma compatible con API 26. La lista activa puede ser toda la biblioteca o una playlist Markdown. El parser normaliza Unicode, acepta enlaces, rutas relativas y nombres sin extensión únicos, conserva el orden y elimina duplicados. El buscador local normaliza acentos y compara título, artista, álbum y archivo, pero no altera la cola.

La lista visible y la cola de Media3 se comparan por sus identificadores `content://`. Cuando cambia el contenido o el orden, la cola se reconstruye; si la canción actual sigue presente, se conservan su índice, posición y estado de reproducción. Volver a elegir la misma carpeta incrementa una revisión interna y fuerza una nueva lectura, de modo que se incorporan archivos añadidos después del primer escaneo.

## Letras

Desde 1.8, `LyricsDialog.kt` crea una sesión ligada a canción y carpeta; `LyricsState.kt` gestiona carga, búsqueda, selección y escritura, y `LyricsHome.kt` separa lectura, búsqueda y vista previa. Cambiar de canción cancela la sesión de interfaz anterior. El lector permite seguir tiempos, detener el seguimiento al arrastrar y saltar a un verso. Detalles y límites en [Letra 1.8](LETRAS_1_8.md).

`LyricsRepository` consulta LRCLIB por título y artista con límites de entrada, tiempos de espera, identificación del cliente y máximo de respuesta. `LrcParser` interpreta marcas de tiempo sin depender de Android. `LyricsStorage` crea `Michi Letras` dentro de la carpeta autorizada y guarda una sola asociación por canción como `.lrc` o `.txt`; cambiar de modalidad elimina únicamente la letra anterior y **Quitar** nunca toca el audio. Por ello el permiso persistente de la carpeta incluye lectura y escritura.

## YouTube y MP3

La entrada acepta texto o enlaces de hosts cerrados de YouTube. `YouTubeService` ejecuta una búsqueda plana de hasta ocho resultados o resuelve un único enlace directo y devuelve identificador, título, canal, duración y miniatura. Desde 1.9, Ver vídeo usa el reproductor oficial en una WebView con HTML local, identidad de la app y eventos limitados de reproducción/posición. Pausa la música local y se pausa al salir de Buscar o pasar a segundo plano. `WebChromeClient` entrega la vista de pantalla completa; orientación/tamaño no recrean la actividad. Detalles en [Vídeo 1.9](VIDEO_1_9.md).

**MP3** descarga el identificador elegido mediante `youtubedl-android` 0.18.1 y FFmpeg; usa `--no-playlist`, mantiene el trabajo intermedio en la caché privada y copia únicamente el MP3 final mediante Storage Access Framework. No hay servidor intermedio ni credenciales propias. La APK se limita a `arm64-v8a`, correspondiente al Pixel 7, para contener el tamaño de los binarios nativos.

## Compatibilidad de compilación

El proyecto conserva `compileSdk` y `targetSdk` 36, Android Gradle Plugin 8.13.2 y Gradle 8.13. Las entregas actuales se verifican con JDK 21; el objetivo Java/Kotlin sigue siendo bytecode 17. El JBR 25 del entorno falló al iniciar Gradle. `androidx.core:core-ktx` queda fijado en 1.17.0 y `lifecycle-runtime-compose` en 2.9.4 porque las versiones 1.19.0 y 2.11.0 exigen API 37 y Android Gradle Plugin 9.1. La conexión asíncrona con `MediaController` usa `ContextCompat.getMainExecutor`, compatible con el mínimo API 26 declarado por la aplicación.
