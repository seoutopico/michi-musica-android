# Podcasts de YouTube y orden por publicación — 1.13

[1.13.0 estable publicada](https://github.com/seoutopico/michi-musica-android/releases/tag/v1.13.0), PR #2 integrado en `main`. Seis adjuntos comprobados por tamaño y SHA-256 remoto. Instalación final y verificación descritas abajo.

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

Validación nativa completada en Pixel 7: el enlace exacto de Aina resuelve 15 publicaciones con fecha, rechaza el duplicado canónico y actualiza sin inventar novedades. Cancelación real y reintento completos; MP3 de 3.645 segundos guardado mediante SAF, clasificado como Podcast y reproducido desde Descargados. La primera ejecución detectó que el JSON completo de yt-dlp superaba 9 MB; se corrigió solicitando solo identidad, disponibilidad, título, descripción, estado de directo y duración. La segunda ejecución completa pasó. La batería RSS también pasó: cancelación activa/en cola/durante commit/tardía, fallos HTTP/SAF controlados, interrupción real del proceso, recuperación y avisos sin duplicados.

Revisión visual real: Medianoche y Rosa al 130 %, texto y controles legibles, filas de episodios desplazables y mini reproductor común conservados. Capturas revisadas localmente en `tmp/rss-device-review/youtube-*.png`. En la app personal se añadió el canal solicitado desde el diálogo; los ocho programas se observaron en orden de publicación descendente y Novedades mostró ocho entradas de las últimas 72 horas. Última escucha anterior conservada en pausa; no se descargó audio de prueba en su carpeta.

1.13.0 debug instalada con `adb install -r`, código 23: siete archivos privados (preferencias, catálogo y snapshots) idénticos byte a byte antes y después de instalar, antes de añadir el canal. La variante aislada y sus audios se retiraron; escala 1.0 y apagado a 60 segundos restaurados. APK oficial verificada con la firma existente, SHA-256 `f5c970efe295e1360be731bcab5bba43650b2e04144d9ef8c32843af8c7c7151`, 56.877.486 bytes. Debug instalada: `5576cbfed233db8244d872d5929a64a62790dec97e50497ac5520c91090258ee`.

La prueba real demuestra este canal y ese audio; no garantiza que YouTube permita todas las descargas futuras. Los fallos HTTP/SAF de regresión son simulados. Se conservan los límites de actualización bajo Doze y las comprobaciones generales del proyecto, sin atribuirles una validación nueva.
