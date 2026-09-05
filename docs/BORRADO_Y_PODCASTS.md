# Borrar canciones y separar podcasts

## Borrado — implementación 1.10.1

Petición de Aina: falta una acción para borrar canciones. Se añade al menú de tres puntos de cada fila de Biblioteca, también cuando se muestra una lista importada. La acción se llama **Borrar canción**. La duración se coloca en la línea secundaria para reservar espacio al menú sin estrechar más el título.

Antes de modificar archivos se muestra el título y nombre del archivo, el alcance y las acciones Cancelar/Borrar canción. El mensaje aclara que se elimina el archivo de la carpeta y deja de estar disponible en las listas, y que Michi no ofrece deshacer. Durante la operación se evita confirmar dos veces y se muestra progreso. Un error permanece en ese diálogo con Reintentar y Cancelar.

El proveedor SAF elimina únicamente el audio seleccionado dentro de la carpeta autorizada. La función verifica pertenencia directa a esa carpeta, que sea un archivo y que su extensión sea de audio compatible. No borra directorios ni letras asociadas ni edita los Markdown. Si falla el proveedor, no se retira la canción de la biblioteca ni se cambia la cola. Las referencias Markdown que apuntaban al audio borrado dejan de resolverse; la recuperación de listas sin ninguna canción disponible sigue en MMA-026.

Tras éxito se retiran todas sus referencias de la cola y la biblioteca y se elimina su reanudación guardada. Si era la pista actual, se pausa antes de retirarla, sin empezar otra automáticamente. Si sonaba otra pista, se conserva su reproducción. Se actualizan los resúmenes de listas. Una operación confirmada termina su conciliación de estado aunque se cierre esa pantalla; Android todavía puede terminar el proceso y la próxima lectura de carpeta reflejará los archivos existentes.

Fuentes de implementación: [DocumentsContract](https://developer.android.com/reference/android/provider/DocumentsContract) y [modificar colas Media3](https://developer.android.com/media/media3/exoplayer/playlists). Pruebas y resultados efectivos: historial y backlog.

## Podcasts — aprobados por Aina, implementación 1.11.0

Aina escucha podcasts de YouTube y no quiere encontrarlos en la música normal. Una playlist llamada Podcasts no basta: los episodios seguirían apareciendo en la biblioteca completa y en Azar.

Comportamiento:

1. Conservar Biblioteca · Buscar · Listas en la navegación inferior. Dentro de Biblioteca, un selector sencillo **Música / Podcasts**. La app abre en Música; cambiar de sección no inicia ni interrumpe por sí solo la reproducción.
2. En Guardar MP3, elegir **Guardar en Música** o **Guardar en Podcasts**. Mantener la última elección visible; no deducir la categoría por duración, título o canal.
3. Para audios ya descargados, menú **Marcar como podcast** y la acción inversa **Marcar como música**. Es una clasificación local del catálogo, sin editar etiquetas ni mover archivos que las listas existentes referencian.
4. Música, su búsqueda, sus listas y Azar excluyen los podcasts. Podcasts construye su propia cola al iniciar un episodio. El mini reproductor mantiene la escucha en curso aunque se consulte la otra sección.
5. Guardar la posición de cada episodio para continuar donde se dejó. Más adelante, valorar velocidad y saltos de 15 segundos como controles propios de podcasts.

La clasificación y las posiciones viven en las preferencias privadas de Michi, identificadas por la URI del audio. Se conservan al actualizar la APK y al cambiar de carpeta y volver a la misma URI. Borrar los datos de la app o desinstalar elimina este catálogo; los archivos de audio permanecen. Renombrar o mover un archivo puede cambiar su URI y requerir clasificarlo otra vez. No se usa copia automática de Android. Los audios sin clasificar permanecen en Música.

Las listas Markdown se resuelven contra todos los archivos y después se omiten los podcasts, sin contarlos como archivos ausentes. Una lista que solo contenga podcasts no aparece en la colección de música. No se modifica el Markdown. Se conserva el límite existente: lectura de audios directamente dentro de la carpeta elegida, sin recorrer subcarpetas.

Los episodios muestran Sin empezar, Continuar con el tiempo guardado o Escuchado al alcanzar su duración. Tocar un episodio finalizado lo inicia desde cero. La posición se guarda cada cinco segundos en el servicio, al pausar y al cambiar de audio; una terminación abrupta del proceso puede perder los últimos segundos. Siguiente/Anterior del sistema y el avance automático también recuperan la posición de cada episodio.

Al iniciar un episodio se crea una cola de podcasts en el orden de Biblioteca y se desactivan Azar y Repetir. Cambiar la sección o consultar una lista no sustituye la cola en curso. Si se reclasifica el audio que está sonando, continúa y su cola se reduce a ese audio para evitar saltar después a la categoría anterior. Reclasificar otro audio lo retira de la cola actual si estaba incluido.

Guardar MP3 abre un diálogo con Música y Podcasts y recuerda la última elección confirmada. La clasificación se asigna a la URI exacta creada por Android, inmediatamente después de copiar el archivo, incluso si el proveedor cambia el nombre. No hay inferencia por título, duración o canal. Esta entrega no añade suscripciones RSS, velocidad de reproducción ni guardado de vídeos.

Validación y límites efectivos: `HISTORIAL_CAMBIOS.md` y MMA-036/037 del backlog.
