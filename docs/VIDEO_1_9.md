# Vídeo en Buscar y separación del menú — Michi Música 1.9

Petición de Aina: ver vídeos de YouTube dentro de Buscar, sin descargarlos, con pantalla completa; separar visualmente el contenido del menú inferior cuando no aparece el mini reproductor.

## Recorrido

La acción principal de los resultados pasa de Escuchar a **Ver vídeo**. Abre el reproductor integrado encima del contenido desplazable de Buscar; permanece visible mientras se consultan resultados. Muestra el título y Cerrar vídeo. Los controles de YouTube permiten reproducir, pausar, avanzar y entrar en pantalla completa. Guardar MP3 sigue siendo una acción independiente y explícita; ver no guarda un archivo de música en la biblioteca.

Pantalla completa usa la vista de vídeo que entrega WebView, con fondo negro, orientación horizontal y salida explícita. Atrás sale del modo completo. Al salir se restaura la orientación anterior. Los cambios de orientación/tamaño se gestionan sin recrear la actividad para conservar el reproductor y la búsqueda.

Abrir el vídeo pausa la música local. Si se vuelve a reproducir música local, el vídeo se pausa; los controles del vídeo vuelven a pausar la música local cuando se pulsa Play. Al salir de Buscar o pasar la app a segundo plano se pausa el vídeo. Al volver a Buscar se recupera la selección y la última posición comunicada, sin reproducir automáticamente. Consulta y resultados siguen en el controlador común durante la sesión. No se garantiza restaurar esta sesión tras cerrar el proceso.

## Integración

`youtube/YouTubeVideoPlayer.kt` aloja el reproductor oficial en WebView y gestiona ciclo de vida, posición, errores y pantalla completa. `YouTubeEmbed.kt` genera el HTML local solo con identificadores validados y valores numéricos. No interpola títulos ni consultas en HTML. La identidad Referer/origin se obtiene del identificador de la app instalada.

El puente JavaScript solo informa de disponibilidad, reproducción, posición y error; no expone archivos ni credenciales. WebView desactiva acceso a archivos/contenido y contenido mixto. Los enlaces de navegación principal no sustituyen el HTML local. Vídeos privados, retirados o con inserción restringida tienen un mensaje y acceso a Abrir en YouTube. No se eluden esas restricciones.

La reproducción conecta directamente con YouTube y requiere Internet. Puede generar caché de reproducción de WebView; no es una descarga MP3 ni un modo sin conexión. Los audios locales no se envían. La reproducción de vídeo sustituye a la preescucha de audio extraída en esta pantalla; el descargador MP3 conserva su implementación independiente.

## Separación inferior

`HomeNavigation` añade 8 dp de aire, un divisor fino usando el color de contorno del tema y 4 dp antes de las pestañas. Se aplica de forma común en Biblioteca, Buscar, Listas, Ahora suena y Letra. Los objetivos táctiles y el padding de navegación del sistema se conservan. El separador se ve aunque no exista el mini reproductor.

## Referencias técnicas consultadas

- [IFrame Player API](https://developers.google.com/youtube/iframe_api_reference): controles, eventos y viewport mínimo de 200 × 200.
- [Parámetros de inserción](https://developers.google.com/youtube/player_parameters): controles, reproducción en línea y pantalla completa.
- [Identificación del reproductor integrado](https://developers.google.com/youtube/terms/required-minimum-functionality): Referer de aplicaciones WebView y formato del identificador.
- [WebChromeClient](https://developer.android.com/reference/android/webkit/WebChromeClient): entrada y salida de la vista de pantalla completa.

La compilación, las pruebas y los recorridos realmente comprobados se registran en `HISTORIAL_CAMBIOS.md`; los estados pendientes, en `BACKLOG.md`.

## Corrección 1.9.1: se oía el vídeo sin imagen

Aina comunica audio sin imagen. Diagnóstico directo en la WebView del Pixel: viewport de 363 × 204 píxeles CSS y estado de YouTube 1 (reproduciendo), pero `body` e `iframe` medían 0 de alto. También se comprobó que cambiar a unidades `vh` dejaba esa altura en 0 en la sesión afectada. No era un fallo de descarga ni una razón para sustituir el decodificador.

`YouTubeEmbed.kt` pasa a asignar las dimensiones numéricas del viewport al crear el reproductor y tamaños explícitos en píxeles CSS a documento, cuerpo e iframe. Actualiza el tamaño al estar listo, cambiar el viewport y salir de pantalla completa. Conserva el mismo iframe y su posición; no recarga el vídeo para redimensionarlo. Durante pantalla completa deja que el reproductor gestione sus dimensiones.

La modificación en vivo recuperó una imagen visible y se observó vídeo en pantalla completa. La prueba de regresión de la APK definitiva debe comprobar dimensiones positivas e imagen cambiante desde una apertura nueva, entrada/salida de pantalla completa y continuidad del tiempo; no basta con estado «reproduciendo» o sonido. Resultados concretos en el historial.
