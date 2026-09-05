# Buscar — Michi Música 1.5

Esta ficha conserva el diseño aprobado de 1.5. Desde [Vídeo 1.9](VIDEO_1_9.md), **Ver vídeo** sustituye a Escuchar: reproductor integrado, pantalla completa y pausa al salir. La descarga permanece independiente. Consultar esa ficha para el comportamiento actual de reproducción.

Continuación de la portada aprobada por Aina. Se mantiene el fondo Medianoche, el acento rosa, el emblema felino, el título serif de 36 sp y el margen lateral de 24 dp. Rosa conserva la misma estructura.

## Recorrido

1. Entrada: campo para canción, artista o enlace y botón Buscar en YouTube; también funciona la acción Buscar del teclado. El texto introductorio explica qué se puede hacer sin mostrar sugerencias ficticias.
2. Resultados: miniaturas de YouTube, título de hasta dos líneas, canal y duración. Filas abiertas con separadores sutiles; Escuchar y Guardar MP3 son acciones con icono y etiqueta.
3. Preescucha: Preparando, Escuchando y Pausar en el propio resultado. Al salir de Buscar se pausa. Al iniciar música local también se pausa; al iniciar o reanudar la preescucha se pausa la música local.
4. Descarga: progreso junto al resultado; al terminar, Guardado y Ya está en tu biblioteca. Un error permanece junto a la acción que falló. La carpeta y el motor de descarga existentes se conservan.

El mini reproductor local y las tres pestañas pertenecen al contenedor común. La consulta y los resultados se conservan al cambiar de pestaña durante la misma sesión. Una búsqueda o descarga en curso puede terminar mientras se consulta otra pestaña. Esto no introduce un servicio de descargas: no se garantiza su continuidad tras recrear la actividad o cerrar el proceso.

## Estados y accesibilidad

Carga, error de búsqueda con reintento, ausencia de resultados, falta de carpeta, preparación de audio, error de preescucha, descarga y guardado tienen mensajes propios. No se muestran registros técnicos de yt-dlp en la interfaz. Las acciones tienen una altura mínima de 48 dp; las acciones de resultado pueden pasar a otra línea. Toda la pantalla es desplazable con teclado o texto aumentado.

Se utilizan componentes de texto y semántica de Compose: [entrada y teclado](https://developer.android.com/develop/ui/compose/text/user-input) y [encabezados y anuncios de estado](https://developer.android.com/develop/ui/compose/accessibility/semantics). No se cambian SDK ni dependencias.

Las miniaturas se descargan únicamente para los identificadores de resultados válidos desde el CDN público de YouTube: hasta tres lecturas simultáneas, límite de 1 MB por respuesta, muestreo y caché en memoria de 4 MB. Sin imagen se muestra el emblema felino. No se envían audios locales ni se modifica su metadatado.

## Revisar el diseño

En Android Studio, abrir `SearchPreviews.kt` con Ctrl+Shift+N y seleccionar debug → Split. Seis vistas con componentes reales: entrada, resultados, descarga, error con texto aumentado, Rosa y resultados con texto aumentado. Los ejemplos son ficticios y no ejecutan red, reproducción ni descargas.

Código: `youtube/SearchContent.kt` (interfaz), `youtube/YouTubeDialog.kt` (estado y acciones), `youtube/SearchThumbnail.kt` (miniaturas). La validación de la APK y las comprobaciones en dispositivo se registran en `HISTORIAL_CAMBIOS.md`.
