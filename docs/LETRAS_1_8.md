# Letra y Siguiente en Azar — Michi Música 1.8

El reproductor 1.7 está aprobado por Aina. Esta entrega continúa con Letra y corrige su aviso de que Siguiente quedaba desactivado en Azar.

## Lectura

Cabecera serif, canción y artista, márgenes de 24 dp y versos abiertos sin superficies rectangulares. La línea sincronizada actual se destaca en rosa y negrita. Las letras sin tiempos conservan estrofas y se leen manualmente.

La lectura sincronizada mantiene la línea actual a la vista. Arrastrar la lista desactiva el seguimiento; Seguir canción lo recupera. Tocar un verso sincronizado lleva la reproducción a su tiempo sin cambiar el estado de pausa. El mini reproductor permanece abajo con la navegación común.

## Buscar, revisar y guardar

Sin letra guardada se muestra Buscar letra. Con letra, el menú Opciones de letra ofrece Buscar otra letra y Quitar letra guardada. Quitar pide confirmación dentro de la app y no borra el audio.

La búsqueda tiene campos separados de título y artista; son datos de consulta, no editan los metadatos del audio. Las coincidencias muestran título, artista, álbum, duración y si incluyen tiempos. Ver letra abre una vista previa; Guardar esta letra confirma la selección. Se conservan LRCLIB y el almacenamiento local en Michi Letras.

Carga, búsqueda, guardado, errores y permisos tienen estados propios. Las operaciones quedan asociadas a una canción y carpeta: cambiar de canción cancela su sesión de interfaz para que una respuesta anterior no cambie la letra visible de la nueva canción. Se comprueba el resultado de quitar archivos; la versión de otro formato se elimina después de escribir la nueva.

## Siguiente en Azar

El botón usaba únicamente la existencia de un siguiente elemento de Media3 y se apagaba en el último del orden aleatorio. Ambos controles de la app ahora usan `NextTrack.kt`: avanzan al siguiente elemento del orden actual y, al agotarlo, vuelven al primero de ese orden si Azar está activo. No se rehace la permutación con cada pulsación ni se repite el último elemento inmediatamente. Una cola de una sola canción no ofrece otra pista.

El avance manual conserva el estado de reproducción/pausa. Este ajuste afecta al mini reproductor y al completo; no modifica los controles de notificación ni introduce repetición automática al acabar el audio.

## Código y validación

`lyrics/LyricsHome.kt`: lectura, búsqueda, vista previa y confirmación. `LyricsState.kt`: operaciones y estados. `LyricsDialog.kt`: sesión por canción. `playback/NextTrack.kt`: avance compartido. Referencia: [listas y control de desplazamiento en Compose](https://developer.android.com/develop/ui/compose/lists).

Android Studio: `LyricsPreviews.kt`, variante debug, Split. Seis previews de componentes reales con versos ficticios originales: sincronizada, vacío, búsqueda, vista previa, texto aumentado y Rosa.

`NextTrackTest` cubre continuidad del orden, final de ronda, fin secuencial, cola vacía y una sola canción. Validación de APK y dispositivo registrada en `HISTORIAL_CAMBIOS.md`. Pendientes ampliados: TalkBack y desplazamiento accesible, errores de red/escritura, quitar y reemplazar letras con archivos reales.
