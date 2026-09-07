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

Validación nativa completada en Pixel 7, repetida tras los ajustes finales: los tres plazos pausan y guardan posición en el límite exacto del reloj inyectado, permiten sustitución/desactivación y conservan el plazo al buscar, cambiar velocidad o pausar. El servicio real se detiene antes del siguiente episodio y antes de repetir el actual, cancela al cambiar de episodio/música y pausa con la actividad en segundo plano. Informe local en `tmp/sleep-timer/report.txt`.

Revisión visual en Medianoche y Rosa al 130 %, con todas las opciones visibles y seleccionables. Se corrigió la apertura parcial de la hoja, que acercaba 45 minutos al borde inferior. También se corrigió el reconocimiento de un podcast con temporizador activo y pausado en 0:00 tras recrear la actividad: mantiene controles operativos y el plazo anterior. Comprobados 45 minutos, Fin del episodio y Desactivado desde la UI; el contador no se reinicia al cambiar de tema o escala. Capturas reales revisadas localmente en `tmp/rss-device-review/timer-*.png`; no son mockups.

**1.14.0 debug instalada, código 24, conservando datos:** siete archivos privados idénticos byte a byte inmediatamente tras `adb install -r`; última escucha original en pausa y posición conservadas. Variante aislada y audios sintéticos retirados; escala 1.0 y apagado de 60 segundos restaurados. No se añadieron ni modificaron audios personales.

APK oficial firmada: 56.910.615 bytes, SHA-256 `12a9b2e4b5770dd0c0cfcead732aec5fbef0060aa3231032bfa53291f950a950`; misma firma oficial de las entregas anteriores. Debug instalada: `cea1dd2e824893618459979a2eb43a1dba9d4529fe5520e0b1580ccdd9478ca4`.

Límites de la comprobación: los plazos de minutos se aceleraron con un reloj inyectado sobre ExoPlayer real; no se esperaron 45 minutos ni se certificó Doze/pantalla bloqueada. El fin de episodio sí se recorrió en tiempo real con el servicio en segundo plano. No confundir esta verificación con pruebas en otros móviles.

Referencia de implementación: [pausa al final del elemento en ExoPlayer](https://developer.android.com/reference/androidx/media3/exoplayer/ExoPlayer#setPauseAtEndOfMediaItems(boolean)).
