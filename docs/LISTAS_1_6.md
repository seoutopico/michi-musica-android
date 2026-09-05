# Listas — Michi Música 1.6

Continúa el estilo de Biblioteca y Buscar, aprobados por Aina. Cabecera felina, título serif de 36 sp, contenido alineado a 24 dp, fondo Medianoche y acento rosa. Rosa usa la misma composición.

## Recorrido y jerarquía

- **Añadir lista**, junto al título, abre el selector de documentos de Android. La ayuda explica que se importan archivos Markdown (.md) con canciones de la carpeta autorizada.
- **Toda tu música** permite volver a la colección completa. La marca de selección aparece cuando esa es la fuente activa.
- **Tus listas** muestra nombre, número de canciones disponibles y composición de cuatro carátulas reales. Si tiene menos de cuatro canciones, usa la primera; si falta una carátula se conserva el marcador felino.
- Tocar una lista la activa y abre sus canciones en Biblioteca. Al regresar a Listas, la selección actual tiene un fondo tonal suave, una línea lateral y una etiqueta explícita. Las demás filas no tienen tarjetas ni contornos.
- Las canciones que no están en la carpeta se cuentan bajo la lista, como información secundaria. No se confunden con la selección ni con una alerta de reproducción.

Los estados de carga y de colección sin listas tienen contenido propio. Toda la pantalla se desplaza; el título y Añadir lista pueden pasar a dos líneas si falta anchura. El mini reproductor y la navegación siguen perteneciendo al contenedor común y conservan su posición.

## Implementación y alcance

`ui/ListsHome.kt` contiene la pantalla y las composiciones de carátulas. Se leen las imágenes ya incrustadas mediante `SongArtwork`; no hay imágenes inventadas ni búsquedas externas. Los resúmenes Markdown se calculan en E/S sobre una copia de la biblioteca y aportan las primeras cuatro canciones para las carátulas.

Se conserva el comportamiento del importador: recuerda las listas que puede leer y resolver contra la carpeta, sin duplicar audios ni editar archivos. No se añade un editor de listas. La recuperación de documentos inaccesibles o sin canciones resolubles sigue pendiente y no se presenta como implementada.

## Revisión

Android Studio: Ctrl+Shift+N → `ListsPreviews.kt` → debug → Split. Cinco previews con componentes reales: colección, sin listas, texto al 130 % en 360 dp, Rosa y carga. Los datos son ficticios y los controles de importación no abren documentos desde la preview.

Las pruebas y las comprobaciones nativas de la APK se registran en `HISTORIAL_CAMBIOS.md`. Quedan como comprobaciones ampliadas TalkBack, texto aumentado en el dispositivo e importación de documentos nuevos y con referencias ausentes.
