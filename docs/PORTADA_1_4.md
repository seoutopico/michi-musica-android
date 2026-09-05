# Portada aprobada — Michi Música 1.4

Referencia aprobada por Aina: `docs/mockups/reinicio/01-apertura-medianoche.png`. La implementación se centra en la apertura habitual de Biblioteca y sus controles persistentes. Buscar, Listas, Ahora suena y Letra conservan sus pantallas funcionales para revisarlas por separado.

## Verla en Android Studio

Usa Ctrl + Shift + N y abre **LibraryHomePreviews.kt**, dentro de `app/src/debug/java/com/ainalluna/michimusica/ui/`. Selecciona debug, Split y Build & Refresh. Las cuatro previews usan los componentes de producción: Medianoche, sin escucha previa, texto al 130 % en 360 dp y Rosa. Los datos son ficticios; las previews muestran el marcador de ausencia de carátula. Los accesos a otras pantallas solo están conectados en la aplicación real.

El código de la portada está en `app/src/main/java/com/ainalluna/michimusica/ui/LibraryHome.kt`. El `MainActivity` conecta sus acciones al mismo `MediaController` que usa el resto de la app.

## Diseño implementado

- Fondo Medianoche, acento rosa, marca felina, título Biblioteca con tipografía serif y búsqueda local.
- Márgenes de 24 dp para contenido; superficies de selección y reproductor a 16 dp para dar espacio al indicador lateral y al contorno.
- Filas con carátula de 72 dp, título, artista, duración y señal de reproducción solo cuando realmente está sonando.
- Mini reproductor con carátula, estado, posición, Play/Pausa, Siguiente y progreso. Permanece abajo en Biblioteca, Buscar, Listas y Letra; Ahora suena presenta sus controles completos.
- Iconos Material de Google, con objetivos táctiles de al menos 48 dp; atribución en `docs/licenses`.
- Barras del sistema ajustadas al tema, insets y espacio del teclado. El contenido es desplazable, también con texto aumentado.

Medianoche es el valor por defecto cuando no existe una apariencia guardada. Si ya se eligió Rosa, se conserva esa preferencia. Puede cambiarse con el engranaje → Apariencia.

## Comportamiento

Preparar una cola no crea una selección ficticia. Al volver se muestra la última canción disponible, pausada y con la posición guardada, incluso si se había detenido antes de cinco segundos. Abrir la portada no inicia audio. Play reanuda; abrir Ahora suena restaura sin reproducir. La restauración espera a que la fuente y el orden de la cola coincidan con los del reproductor.

La lectura de la carpeta se hace en un hilo de E/S. Hasta que finaliza no se reemplaza la cola de una sesión que ya esté funcionando en segundo plano. Azar comienza por el primer elemento del orden aleatorio de Media3 para recorrer la cola sin repetir dentro de la ronda; las canciones se siguen reproduciendo mediante el servicio existente.

Los avisos se distinguen de los datos normales. Un permiso perdido muestra Elegir carpeta; un filtro sin coincidencias muestra Borrar búsqueda; los errores de lista permiten reconocer el problema. Contadores y estados correctos no generan alertas permanentes.

## Carátulas y privacidad

Se leen las imágenes que ya estén incrustadas en los archivos mediante `MediaMetadataRetriever`. La lectura es asíncrona, con un máximo de dos lecturas simultáneas, imágenes muestreadas y caché de 8 MB. Releer la carpeta invalida las claves de caché para esa revisión. Si no hay imagen, no se puede leer o supera el límite admitido, se muestra un marcador felino.

Las fotos del mockup son muestras de diseño: no se asignan a los audios reales. No se modifica ningún metadato, no se buscan imágenes externas y no se sube música.

## Validación y límites

La validación automatizada de la entrega y el hash de la APK se registran en `docs/HISTORIAL_CAMBIOS.md`. Las pruebas cubren conservación de la cola durante carga, correspondencia de fuente/cola y límites de la posición restaurada, además de la batería existente.

Instalada la versión 1.4.0 en el Pixel 7 el 5 de septiembre de 2026. Revisadas capturas nativas en Rosa y Medianoche con carátulas reales y 104 canciones; comprobado el cambio Biblioteca → Listas → Buscar → Biblioteca con la misma escucha pausada y el mini reproductor en las mismas coordenadas. Se deja abierta Biblioteca en Medianoche. Evidencias locales en `tmp/device-review/`.

Quedan pendientes: revisar las cuatro previews en el IDE, Play/Pausa/Siguiente al reabrir, teclado, permiso revocado, fuente Markdown, texto aumentado, TalkBack y reproducción con pantalla bloqueada. El PNG aprobado no sustituye esas comprobaciones.
