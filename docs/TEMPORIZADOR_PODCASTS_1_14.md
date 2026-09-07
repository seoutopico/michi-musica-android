# Temporizador de podcasts — 1.14

Petición de Aina del 7 de septiembre de 2026: temporizador de fin de episodio, 15, 30 o 45 minutos, cuidando la estética vigente.

## Recorrido y diseño

En Ahora suena, cuando el audio actual pertenece a Podcasts, aparece un botón de texto con un reloj dibujado como vector: **Temporizador**. Mantiene los controles de reproducción, Azar, Repetir y Letra. La carátula reserva el espacio adicional del botón sin cambiar el tamaño del reproductor de música. La pantalla sigue siendo desplazable para texto ampliado o poca altura.

El botón abre una hoja inferior del tema, con título serif, filas abiertas de al menos 56 dp y selección accesible: Desactivado, Fin del episodio, 15 minutos, 30 minutos, 45 minutos. Elegir una opción aplica el cambio y cierra la hoja. Activo, el botón usa el acento del tema y muestra la cuenta atrás o Fin del episodio. No se inicia la reproducción al configurar el temporizador.

## Comportamiento

- Minutos desde el momento de activación, con reloj monotónico: continúan al pausar, buscar una posición o cambiar la velocidad. Se mantienen entre episodios de la cola de podcasts. Elegir otro plazo lo cuenta desde ese momento; Desactivado lo cancela.
- Fin del episodio se vincula al audio seleccionado y usa la pausa nativa de Media3 al final del elemento, antes de avanzar o repetir. Elegir manualmente otro episodio cancela esa opción.
- Cambiar a música, reclasificar el audio actual como música o vaciar la cola cancela el temporizador. Finalizar la cola también lo desactiva.
- Al cumplirse, se pausa y se guarda la posición mediante el servicio existente. El temporizador se consume; no queda preparado para detener una escucha posterior.
- El servicio posee el reloj y los cambios del reproductor. Minimizar, navegar o recrear la actividad no reinicia la cuenta. El reproductor conserva la CPU activa mientras reproduce (`WAKE_LOCK`, permiso normal sin diálogo), para que el temporizador pueda ejecutarse con la pantalla apagada. No se usa permiso de alarmas exactas.
- Al destruir el servicio o terminar el proceso se cancela. Reabrir mantiene la regla de la app: última escucha en pausa, sin reactivar un temporizador antiguo. Si el proceso continúa pausado durante suspensión del dispositivo, el plazo se comprueba al volver a ejecutarse y al cambiar la reproducción.

## Código y verificación

`PodcastSleepTimer.kt` contiene el estado y los cálculos de plazos. `PlaybackSleepTimer.kt` controla el ExoPlayer del servicio, sus transiciones y la publicación de estado a la UI del mismo proceso. `PodcastTimerControl.kt` utiliza los componentes reales del tema. No se añaden conexiones externas ni almacenamiento de preferencias del temporizador.

Pruebas de lógica: plazos de los tres presets, límite exacto, sustitución/cancelación, fin vinculado a episodio, cambio a música y redondeo del contador. Arnés aislado `scripts/validate_sleep_timer.ps1`: audio sintético, ExoPlayer real y reloj inyectado para recorrer 15/30/45 minutos sin esperar 90 minutos; después comprueba fin de episodio con el servicio real y repetición activada/desactivada. Esta aceleración no equivale a medir 45 minutos reales bajo Doze.

Compilación final `test lint assembleDebug :app:assembleValidation assembleRelease --offline` correcta: 94 pruebas por variante (debug/release/validation), lint sin errores y 16 advertencias previas. APK debug y oficial compiladas.

Estado: pendiente ejecutar el arnés nativo, revisar las vistas y comprobar la pausa en segundo plano. El Pixel está conectado pero requiere desbloqueo. No presentar como instalada ni publicada hasta cerrar esas comprobaciones.

Referencia de implementación: [pausa al final del elemento en ExoPlayer](https://developer.android.com/reference/androidx/media3/exoplayer/ExoPlayer#setPauseAtEndOfMediaItems(boolean)).
