# Reproductor — Michi Música 1.7

Continúa la dirección de Biblioteca, Buscar y Listas aprobada por Aina. Carátula real como protagonista, título serif, márgenes de 24 dp y controles con la paleta Medianoche/Rosa.

## Recorrido

- Minimizar reproductor vuelve a la pantalla de origen. La cabecera muestra Ahora suena y la fuente de la colección.
- Carátula cuadrada ajustada al espacio disponible, con lectura de hasta 1024 px. Las filas siguen usando versiones pequeñas. Sin imagen incrustada aparece el marcador felino.
- Título de hasta dos líneas y artista. Barra fina de progreso con control circular, tiempo transcurrido y restante. El área táctil es la del Slider nativo; el salto se aplica al soltar.
- Anterior, reproducción/pausa dominante y Siguiente. Los controles reflejan la disponibilidad de la cola real.
- Azar y Repetir con icono, etiqueta y estado de interruptor accesible. Repetir alterna entre desactivado y repetir una canción, como antes. Letra abre la pantalla existente.

La pantalla completa puede desplazarse con texto aumentado o poca altura. Preparación y error con Reintentar tienen mensajes propios. Se elimina el botón Más opciones que no tenía acción. El servicio de audio no cambia y el mini reproductor reaparece en su posición habitual al minimizar.

## Código y revisión

Componentes en `ui/PlayerHome.kt`, integración en `MainActivity.kt` y carátulas compartidas en `ui/SongArtwork.kt`. Referencia de interacción: [Slider de Compose](https://developer.android.com/develop/ui/compose/components/slider). Iconos Material atribuidos en `docs/licenses`.

Android Studio: Ctrl+Shift+N → `PlayerPreviews.kt` → debug → Split. Cinco previews: Medianoche, texto al 130 % con título largo, Rosa, error y horizontal. Datos y acciones simulados; componente productivo.

`PlayerSeekTest` comprueba fracción a milisegundos, límites, duración no disponible y valores no finitos. Pruebas y revisión nativa registradas en `HISTORIAL_CAMBIOS.md`. Validación ampliada pendiente en MMA-027: TalkBack, fuentes grandes en dispositivo, errores de audio, bloqueo y Bluetooth.
