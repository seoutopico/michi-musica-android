# Borrar canciones y separar podcasts

## Borrado — implementación 1.10.1

Petición de Aina: falta una acción para borrar canciones. Se añade al menú de tres puntos de cada fila de Biblioteca, también cuando se muestra una lista importada. La acción se llama **Borrar canción**. La duración se coloca en la línea secundaria para reservar espacio al menú sin estrechar más el título.

Antes de modificar archivos se muestra el título y nombre del archivo, el alcance y las acciones Cancelar/Borrar canción. El mensaje aclara que se elimina el archivo de la carpeta y deja de estar disponible en las listas, y que Michi no ofrece deshacer. Durante la operación se evita confirmar dos veces y se muestra progreso. Un error permanece en ese diálogo con Reintentar y Cancelar.

El proveedor SAF elimina únicamente el audio seleccionado dentro de la carpeta autorizada. La función verifica pertenencia directa a esa carpeta, que sea un archivo y que su extensión sea de audio compatible. No borra directorios ni letras asociadas ni edita los Markdown. Si falla el proveedor, no se retira la canción de la biblioteca ni se cambia la cola. Las referencias Markdown que apuntaban al audio borrado dejan de resolverse; la recuperación de listas sin ninguna canción disponible sigue en MMA-026.

Tras éxito se retiran todas sus referencias de la cola y la biblioteca y se elimina su reanudación guardada. Si era la pista actual, se pausa antes de retirarla, sin empezar otra automáticamente. Si sonaba otra pista, se conserva su reproducción. Se actualizan los resúmenes de listas. Una operación confirmada termina su conciliación de estado aunque se cierre esa pantalla; Android todavía puede terminar el proceso y la próxima lectura de carpeta reflejará los archivos existentes.

Fuentes de implementación: [DocumentsContract](https://developer.android.com/reference/android/provider/DocumentsContract) y [modificar colas Media3](https://developer.android.com/media/media3/exoplayer/playlists). Pruebas y resultados efectivos: historial y backlog.

## Podcasts — propuesta, todavía sin implementar

Aina escucha podcasts de YouTube y no quiere encontrarlos en la música normal. Una playlist llamada Podcasts no basta: los episodios seguirían apareciendo en la biblioteca completa y en Azar.

Propuesta de producto:

1. Conservar Biblioteca · Buscar · Listas en la navegación inferior. Dentro de Biblioteca, un selector sencillo **Música / Podcasts**. La app abre en Música; cambiar de sección no inicia ni interrumpe por sí solo la reproducción.
2. En Guardar MP3, elegir **Guardar en Música** o **Guardar en Podcasts**. Mantener la última elección visible; no deducir la categoría por duración, título o canal.
3. Para audios ya descargados, menú **Marcar como podcast** y la acción inversa **Marcar como música**. Es una clasificación local del catálogo, sin editar etiquetas ni mover archivos que las listas existentes referencian.
4. Música, su búsqueda, sus listas y Azar excluyen los podcasts. Podcasts construye su propia cola al iniciar un episodio. El mini reproductor mantiene la escucha en curso aunque se consulte la otra sección.
5. Guardar la posición de cada episodio para continuar donde se dejó. Más adelante, valorar velocidad y saltos de 15 segundos como controles propios de podcasts.

Antes de implementar: concretar restauración de la clasificación si se cambia de carpeta o se reinstala, comportamiento de listas importadas que mezclen ambos tipos y estados de episodios nuevos/en curso/finalizados. Los audios todavía sin clasificar permanecen en Música para no ocultar contenido existente. La propuesta no añade suscripciones RSS ni guarda vídeos en esta entrega.
