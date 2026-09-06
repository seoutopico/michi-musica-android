# Podcasts de YouTube y orden por publicación — 1.13

Aina señala que el seguimiento prometido también debía aceptar canales de YouTube y pide ordenar Tus programas por su episodio más reciente. Se amplía la sección existente conservando tema, filas, navegación, colas independientes y ventana de novedades de 72 horas.

## Uso

- Añadir podcast acepta RSS públicos HTTPS y canales de YouTube: `@nombre`, `/channel/UC…`, `/c/…`, `/user/…`, con pestañas como `/videos`. También admite el feed público de YouTube con `channel_id`. Un vídeo individual se sigue gestionando desde Buscar; una lista de reproducción no se interpreta como canal.
- El enlace se resuelve al identificador estable del canal. Añadir el mismo canal mediante otra dirección no crea un programa duplicado. Las actualizaciones posteriores consultan su feed público Atom, sin volver a resolver el nombre.
- YouTube comparte sus publicaciones recientes, normalmente las últimas 15; no se importa el archivo completo. El catálogo se renueva con ese listado. Los audios ya guardados y sus posiciones permanecen en Descargados aunque dejen de aparecer en el feed.
- Tus programas se ordena por la publicación más reciente conocida, de mayor a menor. Ignora fechas futuras y ausentes; los programas sin fecha válida quedan al final. En empate se ordena por nombre y URL. La fecha Comprobado y el momento de seguimiento no alteran este orden.
- Novedades conserva exactamente las últimas 72 horas desde la publicación, no desde la actualización del vídeo ni desde el alta del canal. La primera importación no marca todo como Nuevo.
- Descargar guarda un MP3 en la carpeta elegida y lo clasifica como Podcasts. Se vuelve a comprobar la identidad del vídeo/canal y su disponibilidad pública; no se utilizan cuentas, cookies de acceso ni pagos. Se rechazan entradas identificadas como exclusivas/adelantos y directos aún sin grabación terminada. Un vídeo público puede ser un extracto editorial del programa: Michi descarga ese vídeo, no promete recuperar una versión completa que el canal no publique.
- YouTube puede bloquear una descarga o exigir acceso. Se muestra un error con Reintentar, sin crear un audio incompleto. La cancelación detiene el proceso de extracción; la escritura definitiva mantiene el diario y la finalización atómica de Podcasts RSS. Los temporales privados abandonados por un cierre del proceso se limpian al iniciar.

## Implementación y verificación

`YouTubePodcasts.kt` resuelve canales y lee Atom con límites y XML sin entidades externas. `podcastSource` selecciona RSS o YouTube para seguimiento y actualización. `YouTubePodcastAudio.kt` reutiliza el motor yt-dlp/FFmpeg ya incluido; prepara el audio privado y entrega a `PodcastDownloadService` la validación de duración y el guardado SAF. No cambia el formato del catálogo privado existente.

Pruebas unitarias de alias/duplicados, fechas de publicación frente a actualización, fuentes de audio verificadas, contenido restringido, XML inseguro y orden entre proveedores, incluyendo programas sin fecha y actualizaciones. La fase `youtube` del arnés aislado usa el canal real proporcionado por Aina y red real: seguimiento, actualización, cancelación y reintento de MP3 con clasificación y audio decodificable. Ejecutar en un proceso nuevo sin la fábrica HTTP simulada de la batería RSS.

Verificación de compilación completada: `test lint assembleDebug :app:assembleValidation assembleRelease --offline`, 89 pruebas por variante (debug/release/validation), cero fallos. Lint de la app sin errores y con las 16 advertencias previas. APK debug y oficial compiladas; firma oficial comprobada. `scripts/validate_youtube_podcasts.ps1` prepara únicamente la variante aislada y deja el informe real en `tmp/youtube-podcasts/native-report.txt`.

Estado de esta ampliación: pendiente validar la descarga real y las vistas en el Pixel desbloqueado, instalar conservando datos y publicar. No confundir este estado con la entrega RSS 1.12.0, ya publicada. La app personal no se ha sustituido por una compilación sin esta comprobación.
