# Licencias y componentes externos

El código propio de Michi Música y Michi Iconos se publica bajo **GPL-3.0**, en [LICENSE](LICENSE). Los componentes externos conservan sus licencias y atribuciones. No se distribuyen archivos de canciones ni vídeos. Las capturas contienen carátulas de la biblioteca autorizada, como se explica al final de este documento; esas imágenes conservan los derechos de sus respectivos titulares.

| Componente | Uso | Licencia / fuente |
| --- | --- | --- |
| AndroidX, Compose y Media3 | Interfaz, sesión y reproducción | Apache-2.0; [AndroidX](https://android.googlesource.com/platform/frameworks/support/), [Media3](https://github.com/androidx/media) |
| Kotlin y coroutines | Lenguaje y concurrencia | Apache-2.0; [Kotlin](https://github.com/JetBrains/kotlin), [coroutines](https://github.com/Kotlin/kotlinx.coroutines) |
| youtubedl-android 0.18.1, common y ffmpeg | Búsqueda y descarga | GPL-3.0; [fuente exacta](https://github.com/yausername/youtubedl-android/tree/d725d5c9a18c3a99a13ee0308bf78275dc310760) |
| yt-dlp | Motor de extracción | [Licencia y código upstream](https://github.com/yt-dlp/yt-dlp); su actualización NIGHTLY se descarga en el dispositivo |
| FFmpeg, Python, QuickJS y bibliotecas nativas | Componentes empaquetados por youtubedl-android | [FFmpeg](https://ffmpeg.org/legal.html), [Python](https://docs.python.org/3/license.html), [QuickJS](https://bellard.org/quickjs/); aplicar las licencias de cada componente |
| Iconos Material | Iconos funcionales de la interfaz | Apache-2.0; [atribuciones](docs/licenses/README.md) |
| Gato Michi y variantes de icono | Identidad del proyecto | Código/arte vectorial propio, incluido bajo GPL-3.0 |

## Fuentes para modificar y reconstruir

La release incluye el archivo fuente de Michi generado por GitHub y un archivo de la revisión exacta de youtubedl-android 0.18.1, con sus fuentes de integración y los archivos que distribuye upstream. `scripts/collect_sources.py` reproduce esa descarga y calcula su SHA-256. Las dependencias JVM/Android se resuelven desde los repositorios públicos indicados en Gradle, con versiones declaradas en los archivos de construcción.

El archivo de youtubedl-android incluye binarios nativos precompilados; no confundirlo con una reconstrucción independiente de esos binarios. Las instrucciones upstream se conservan en [BUILD_FFMPEG](docs/licenses/YOUTUBEDL_BUILD_FFMPEG.md) y [BUILD_PYTHON](docs/licenses/YOUTUBEDL_BUILD_PYTHON.md). Para modificar la pila nativa, consultar también las fuentes y recetas de [Termux](https://github.com/termux/termux-packages). La revisión de seguridad de Michi no certifica la reproducibilidad bit a bit de esos binarios.

YouTube, LRCLIB y Niagara son servicios/productos externos con sus propias condiciones. Michi no está afiliada a ellos. Las capturas públicas de Biblioteca, Listas y reproductor muestran la biblioteca real de Aina-Lluna por petición expresa. Sus carátulas y títulos ilustran el uso de la app y pertenecen a sus respectivos titulares; no se distribuyen los audios. Letra usa texto ficticio. Las demás evidencias temporales del dispositivo no se publican.
