# Podcasts RSS — 1.12.0

Petición aprobada por Aina el 6 de septiembre de 2026: seguir programas, conocer sus publicaciones y descargar episodios, **sin contenido de pago**, conservando el diseño completo de Michi. Implementación en código; la validación nativa depende de volver a conectar el Pixel. No confundir compilación o previews con instalación o prueba de audio.

## Recorrido y diseño

- Biblioteca conserva la cabecera felina, título serif de 36 sp, selector Música/Podcasts y tema Medianoche/Rosa. La música, Buscar, Listas y el mini reproductor conservan sus componentes y posición.
- Dentro de Podcasts: **Siguiendo · Novedades · Descargados**. Selector horizontal desplazable para texto grande; acciones Añadir podcast, Actualizar y opciones propias del pódcast. El engranaje superior sigue siendo el de la biblioteca.
- **Añadir podcast** admite la dirección RSS pública HTTPS. No es el enlace de cualquier página web: si se pega una página, se indica copiar Fuente RSS. Se valida antes de seguir, se impiden duplicados y no se descarga el archivo histórico. No se añade ningún programa sin una acción del usuario.
- **Siguiendo** muestra portada publicada por el programa (gato si falta/falla), nombre, número de episodios del feed, novedades y fecha del último. Tocar abre sus episodios. Dejar de seguir requiere confirmar y conserva los audios descargados y las posiciones.
- **Novedades** muestra los episodios de los programas seguidos por fecha descendente. La primera importación muestra el catálogo disponible, sin marcarlo entero como nuevo. Solo los identificadores incorporados después llevan Nuevo. Marcar novedades como vistas es independiente de Escuchado y de la posición de reproducción.
- **Descargados** conserva las filas de audio y el reproductor de producción. Añade estado de las descargas en curso/fallidas, progreso, Cancelar y Reintentar. Los audios descargados desde RSS quedan clasificados como Podcasts. El título y nombre del programa se guardan en el catálogo de Michi, sin editar etiquetas del archivo.
- Filas abiertas, márgenes de 24 dp, acento del tema, buscadores con superficie y forma iguales a Biblioteca, controles con objetivos de 48 dp. Descripciones en un diálogo desplazable, mensajes de error junto a su acción y contenidos bajo la navegación persistente.

## Actualización y descargas

- Actualización manual y tarea periódica de Android cada seis horas aproximadamente; requiere red, admite Wi-Fi o datos y está sujeta a batería/cuotas del sistema. Se puede desactivar. La tarea persiste al reiniciar el móvil. Forzar la detención de Michi puede detener los trabajos hasta volver a abrirla.
- Avisos de novedades desactivados inicialmente. Activarlos pide el permiso de notificaciones de Android cuando corresponde. Tocar el aviso abre Podcasts/Novedades. No se anuncian fechas futuras que el autor no haya publicado.
- Descargas siempre manuales. Un servicio propio en primer plano mantiene la cola durante navegación, segundo plano y recreación de la actividad, con notificación de progreso. Cancelar descarta el temporal. Un fallo permite reintentar; una terminación del proceso conserva un registro de interrupción y requiere reintento al abrir. No se promete continuación por bytes tras reiniciar.
- Antes de descargar se vuelve a leer el RSS público y se comprueba que el episodio siga admitido. Se guarda primero un temporal privado, se comprueba que tenga audio reproducible y se compara su duración con la anunciada, si existe. Se admite una diferencia de hasta el mayor de 30 segundos o un 10 %, para pequeñas diferencias de codificación/anuncios; un recorte mayor se rechaza.
- Copia a la carpeta autorizada mediante un documento `.part`, que no lee la biblioteca. Solo se cambia a extensión de audio al terminar. Nombre disponible, sin sobrescribir. Registro persistente de la URI creada; la limpieza se limita a archivos creados por esa descarga. Límite de 1 GB por episodio.
- Borrar un audio permite volver a descargarlo después. Marcarlo como música se refleja como Guardado en Música; no provoca otra copia ni se revierte la clasificación del usuario. Cambiar de carpeta conserva los registros asociados a cada carpeta.
- Formatos: MP3, M4A, OGG, OPUS, AAC, FLAC y WAV según el MIME de audio del RSS. No convierte audio ni descarga vídeo.

## Solo contenido público: alcance y límites comprobados

No hay inicio de sesión, pagos, cookies de cuenta ni configuración de RSS privados. Se rechazan direcciones con credenciales, rutas privadas y parámetros de autenticación reconocibles. El parser excluye marcas de episodios para mecenas/fans/suscriptores, adelantos/tráilers identificados y `isAccessibleForFree=false`. La disponibilidad real depende del servidor; un RSS no tiene una marca universal de pago o de recorte. No se afirma que todos los enlaces de Substack/iVoox sean compatibles.

Prueba real del [RSS público de Días Extraños](https://feeds.ivoox.com/feed_fg_f1413939_filtro_1.xml), el 6 de septiembre: 250 entradas de origen; el parser Kotlin conserva **209 entradas** y excluye **41 exclusivas**. Son entradas públicas, no 209 descargas completas comprobadas.

Hallazgo adicional: «Sectas 3.0: la nueva era de la captación digital» anuncia 9:00 y 8.654.680 bytes, pero el servidor entrega audio/mpeg de 2.023.160 bytes y **126,354 segundos**. Comprobados HTTP y tipo con el cliente Kotlin y duración con ffprobe sobre una copia temporal local. La comprobación de duración rechaza ese adelanto aunque el RSS diga `episodeType=full`. La interfaz explica Audio completo no disponible. **Seguir este programa para novedades es viable; el RSS no garantiza que pueda descargarse cada episodio completo fuera de iVoox.** No se extrae el audio restringido de su app ni se presenta ese recorte como una descarga completa.

El RSS de Sergio Parra devolvió HTTP 403 en la consulta desde este equipo; no se ha validado su audio ni afirmado que sea de pago. El soporte de RSS público es genérico, sin un extractor específico para Substack.

## Persistencia, red y privacidad

`podcasts.bin` es un snapshot privado, versionado y atómico: programas, identidades vistas, preferencias y registros de descarga. Hasta 100 programas, 1.000 episodios por feed y 20.000 identificadores vistos por programa. Desinstalar o borrar datos pierde ese catálogo; los audios finales de la carpeta permanecen. No se sube música ni se crea una cuenta o servidor de Michi.

HTTPS, validación de redirecciones, rechazo de hosts locales y direcciones privadas resueltas, tiempos de espera y lecturas limitadas a 6 MB por feed. XML sin DTD/entidades externas, profundidad y número de entradas acotados. Portadas muestreadas a 320 px, 3 MB de entrada máximo y caché de hasta 150 imágenes. La reproducción de un audio local solo usa la portada ya guardada en caché, sin iniciar una descarga de imagen. Los proveedores reciben la IP y solicitudes del RSS, portadas o audio; actualizar automáticamente causa conexiones periódicas. Consultar PRIVACY.md.

## Código y verificación

`podcasts/PodcastFeed.kt`: parser y reglas de disponibilidad/identidad. `PodcastNetwork.kt`: red. `PodcastStore.kt` / `PodcastRepository.kt`: persistencia. `PodcastDownloadService.kt`: cola y guardado. `PodcastRefreshService.kt`: actualización y avisos. `PodcastHome.kt` / `PodcastController.kt`: interfaz y acciones. `LibraryHome.kt` incorpora las vistas sin sustituir cabecera, filas locales ni navegación.

Previews de producción en `PodcastPreviews.kt`: Siguiendo, Novedades, Rosa con texto al 130 %/360 dp y vacío. Rutas debug de Showcase: `podcasts`, `podcast-news`, `podcast-empty`, `podcast-rose`. Datos ficticios sin red; no forman parte de la release.

Verificación automatizada: parser, filtros de pago/adelantos, entidades externas/UTF-16, URLs privadas/inseguras, límites, identidad sin GUID, duplicados, novedades vistas, episodio que cambia a pago, duración recortada, nombres sin colisión y snapshot Unicode/truncado/versionado. Resultados de compilación definitivos en HISTORIAL_CAMBIOS.md.

Referencia visual anterior inspeccionada: `docs/screenshots/podcasts.png`. Se ha contrastado la integración en el código con PORTADA_1_4 y BORRADO_Y_PODCASTS. **Todavía no hay captura nativa de 1.12 ni comprobación táctil/visual de estas previews.** ADB no detecta un dispositivo y no hay emulador configurado. Pendientes: instalar conservando datos; revisar ambas apariencias y texto grande; añadir/quitar un programa; descargar un audio público completo; cancelar/reintentar; rechazo nativo del recorte de iVoox; permisos/espacio; avisos; cola y reproducción con pantalla bloqueada. No borrar ni reclasificar audios personales durante esas pruebas.

Referencias técnicas: [RSS 2.0](https://www.rssboard.org/rss-specification), [JobScheduler](https://developer.android.com/reference/android/app/job/JobScheduler), [servicio dataSync](https://developer.android.com/develop/background-work/services/fgs/service-types#data-sync) y [permiso de notificaciones](https://developer.android.com/develop/ui/compose/notifications/notification-permission).
