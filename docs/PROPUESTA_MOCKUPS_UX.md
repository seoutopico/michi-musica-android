# Propuesta UX/UI — Michi Música

**Documento histórico; no es la especificación vigente.** Empezar por [Continuidad](CONTINUIDAD.md). Biblioteca, Buscar, Listas y reproductor ya están aprobados; Letra 1.8 está implementada y pendiente de revisión. Las menciones a «actual», «vigente» o «pendiente» que siguen describen el momento de esta propuesta, no el estado de la entrega actual.

**Direcciones visuales anteriores descartadas.** A petición de Aina, se reinicia la exploración visual con un mockup raster independiente, sin tomar como referencia las pantallas de este documento, `MichiProposal.kt`, `AperturaMichi.kt` ni sus láminas. Se conservan los archivos como historial, no como propuesta vigente. La exploración se centra en una sola apertura habitual, con acceso directo a canciones y control de escucha persistente; no implica trabajo de metadatos ni una nueva implementación Compose.

5 de septiembre de 2026 · Propuesta para revisión, no implementada en la interfaz productiva.

**Revisión actual, pantalla a pantalla:** la apertura habitual se está reconstruyendo en `AperturaMichi.kt`. Ver [composición y criterio de apertura](mockups/apertura/README.md). La dirección visual del conjunto anterior queda pendiente de replantear; no se considera aprobada por compilar. El resto de este documento conserva la especificación de recorridos y estados del prototipo anterior.

## Ver y recorrer los mockups

Abre `app/src/debug/java/com/ainalluna/michimusica/ui/proposal/MichiProposal.kt` en Android Studio y selecciona **Split** o **Design**. Usa la variante **debug**. El grupo **Propuesta UX** contiene trece pantallas; **Accesibilidad** añade una variante de 360 dp con texto al 130 %. Si el panel lo solicita, pulsa **Build & Refresh**. Para encontrar el archivo, usa **Ctrl + Shift + N** y escribe `MichiProposal.kt`; también puedes cambiar la vista del árbol de Android a Project y recorrer `app/src/debug`.

Activa **Interactive Mode** en una preview para recorrer Biblioteca, Buscar y Listas, abrir una lista, seleccionar una canción, abrir el mini reproductor, ver Letra y cambiar la apariencia desde el menú superior. La primera apertura simula el resultado de elegir una carpeta. Los archivos, consultas, audio y descargas son datos de demostración; no se ejecutan servicios ni se solicitan permisos.

La compilación se verifica con JDK 21, disponible en el entorno local. El JBR 25 incluido en Android Studio falló al iniciar esta versión de Gradle; en los ajustes de Gradle del IDE se debe usar el JDK 21 instalado, resuelto mediante el selector del IDE, sin guardar rutas de perfil en el repositorio.

## Lectura del producto actual

Se revisaron `MainActivity.kt`, tema, iconos, búsqueda/descarga de YouTube, pantalla de letras, arquitectura, README, backlog, historial y dirección UX previa. Es una revisión del código y la documentación; no una sesión de uso observada en el teléfono.

La base tiene valor: permiso de carpeta acotado, reproducción persistente, continuidad voluntaria, playlists externas sin duplicar música, dos apariencias y navegación inferior estable. Deben conservarse.

| Hallazgo actual | Implicación | Propuesta |
| --- | --- | --- |
| El destino inicial se llama Música y mezcla fuente activa y biblioteca | Cuesta distinguir colección y cola | Llamarlo Biblioteca, como se solicita ahora; las listas tienen detalle propio |
| Cara de gato, título, subtítulo, reanudación, campo, contador y mensaje preceden a las canciones | Exceso de elementos antes del contenido | Una cabecera; continuidad en el mini reproductor inferior; mensajes solo cuando necesitan atención |
| Buscar corresponde a YouTube; también existe búsqueda local | Dos búsquedas con alcance diferente | Explicar YouTube bajo el título y dentro del botón; conservar el filtro local en Biblioteca |
| Seleccionar una lista cambia la cola y devuelve a Música | Explorar una lista afecta al contexto musical | Abrir su detalle; cambiar la cola al tocar Reproducir, Azar o una canción |
| Cada lista usa borde, fondo y mosaico | Mucho peso visual repetido | Filas separadas por espacio; reservar el fondo para contenido destacado |
| Hay acciones rotuladas Abrir .md y estados técnicos largos | La interfaz exige entender el formato demasiado pronto | Importar lista; explicar Markdown al importar; errores breves con acción de recuperación |
| El reproductor ya conserva la barra inferior | Permite explorar mientras suena música | Mantenerlo en todas las capas musicales |

## Dirección visual

Calidad basada en jerarquía, espacio y consistencia: fondo marfil, tinta ciruela, rosa como acento y superficies de poca elevación. Títulos de 34 sp, texto principal de 16–17 sp y apoyo de 14 sp; márgenes de 24 dp y controles de al menos 48 dp. El rosa de acciones en la propuesta es más oscuro (`#A33764`) para mejorar la lectura sobre claro; Medianoche conserva el rosa luminoso existente.

El gato identifica la bienvenida y Ahora suena. Las filas de canciones priorizan título y artista; no inventamos portadas. La ilustración felina del reproductor es un recurso de marca. Se conserva el comportamiento nativo de Android, el selector de archivos y la navegación del sistema; la inspiración Apple se traduce en orden y cuidado visual.

## Criterio de navegación y referencias

Decisión vigente: un único mini reproductor encima de Biblioteca · Buscar · Listas, tanto para la última escucha pausada como para la reproducción activa. Se elimina la tarjeta superior Continuar escuchando. Esta decisión sustituye ese punto de la propuesta inicial y de `ANALISIS_UX_DESDE_CERO.md`; no cambia todavía el comportamiento productivo.

Apple Music documenta un MiniPlayer cerca del borde inferior que abre Ahora suena; Spotify sitúa su barra de reproducción justo encima del menú inferior. Son patrones de producto contrastados, no una obligación universal de Apple para cualquier app. Referencias: [controles de Apple Music](https://support.apple.com/en-gb/guide/iphone/iph676daac9b/ios) y [Ahora suena de Spotify](https://support.spotify.com/ws/article/now-playing/).

Las [guías de barras de pestañas de Apple](https://developer.apple.com/design/human-interface-guidelines/tab-bars) recomiendan mantener disponible la navegación para que la interfaz sea predecible. Para Michi aplicamos ese criterio a los tres destinos, además de mantener estable el control musical. La ubicación y los estados de restauración que proponemos son una decisión propia de diseño; no se atribuyen a una regla de Apple.

El razonamiento parte de tres tareas: retomar la canción al abrir, explorar sin perder el control de la escucha y pausar con rapidez. La hipótesis de usabilidad es que una ubicación persistente reduce la búsqueda visual y evita reaprender dónde está Play cuando cambia el estado. Una tarjeta superior podría servir para elegir entre varias escuchas anteriores; no aporta suficiente valor para retomar una sola pista. No se ha realizado investigación con participantes ni una prueba comparativa: estas hipótesis deberán contrastarse en el Pixel.

| Contexto del usuario | Comportamiento propuesto |
| --- | --- |
| Primera apertura o biblioteca cargada sin selección previa | Sin reproductor; elegir carpeta no selecciona automáticamente la canción 0 |
| Vuelve con una última canción válida | Mini reproductor inferior pausado, con título y posición guardada; sin inicio automático |
| Pulsa Play o Pausa | Cambia el estado dentro del mismo componente; cabecera, canciones y controles conservan su distribución |
| Cambia entre Biblioteca, Buscar y Listas | Misma canción, posición y ubicación del mini reproductor |
| Explora el detalle de una lista | Conserva la escucha; solo cambia al elegir una canción, Reproducir o Azar |
| Abre Ahora suena | El reproductor completo asume los controles; se oculta el compacto para evitar duplicación |
| Abre Letra | Mini reproductor inferior accesible para pausar; tocarlo vuelve a Ahora suena |
| La canción guardada ya no está disponible | No mostrar un control inoperante; explicar la ausencia y permitir elegir otra canción |

El modo pausado debe distinguirse mediante texto e icono, además del color. El botón Play mantiene su área táctil al cambiar a Pausa. La posición guardada se comparte con Ahora suena. El mockup mantiene este estado mientras se navega; no simula el avance temporal del audio.

## Pantallas y recorrido

| Preview | Qué debe resolver |
| --- | --- |
| 01 Primera apertura | Explicar el valor y ofrecer una única acción: Elegir mi carpeta. Sin música automática ni mini reproductor |
| 02 Biblioteca al volver | Mostrar la última canción pausada a 1:24 en el mini reproductor inferior; sin tarjeta superior |
| 03 Biblioteca reproduciendo | Misma estructura y ubicación que 02, con Pausa y estado Reproduciendo |
| 04 Buscar inicio | Explicar alcance externo y permitir nombre o enlace; sin resultados inventados antes de consultar |
| 05 Buscar resultados | Distinguir Escuchar y Guardar MP3; confirmación junto al resultado |
| 06 Listas | Importar, acceder a toda la biblioteca y explorar selecciones guardadas |
| 07 Detalle de lista | Revisar canciones antes de cambiar la reproducción; Reproducir y Azar visibles |
| 08 Ahora suena | Título, artista, Letra y control central prioritarios; navegación inferior presente |
| 09 Letra | Lectura protagonista, mini reproductor accesible y regreso a Ahora suena |
| 10 Medianoche | Misma estructura, adaptada a fondo oscuro |
| 11 Carpeta vacía | Explicar qué falta y ofrecer Elegir otra carpeta |
| 12 Pantalla pequeña | Inspeccionar distribución con texto aumentado y contenido desplazable |
| 13 Biblioteca sin escucha previa | Biblioteca cargada sin selección artificial ni mini reproductor |
| 14 Listas con escucha pausada | La misma última escucha sigue accesible al explorar las listas |

Flujo principal: apertura → selector de carpeta de Android → Biblioteca → canción → mini reproductor → Ahora suena → Letra. La barra inferior conserva Biblioteca · Buscar · Listas. En Listas: colección → detalle → selección explícita para reproducir. Buscar permite preescuchar y guardar; la pista guardada deberá incorporarse a Biblioteca.

Al volver, mostrar el mini reproductor si hay una selección anterior válida, pausada o activa. Su presencia depende de una canción elegida o restaurada, no de que el audio esté sonando. Sin historial, mostrar directamente las canciones. El botón Atrás del sistema deberá cerrar primero la capa contextual y restaurar pestaña, filtro y desplazamiento. Explorar una lista no debería detener la música.

## Estados para la implementación

| Situación | Respuesta propuesta |
| --- | --- |
| Se cancela el selector | Permanecer en bienvenida, sin tratarlo como error |
| Carpeta cargando | Indicador y texto Leyendo tu música; impedir dobles solicitudes |
| Permiso revocado | Volver a dar acceso a la carpeta, conservando las referencias recuperables |
| Filtro sin coincidencias | Texto breve y posibilidad de borrar el filtro |
| Sin listas | Importar tu primera lista, con explicación del formato en ese contexto |
| Lista con ausencias | Mostrar disponibles y ausentes; permitir reproducir las disponibles |
| Búsqueda sin conexión o sin resultados | Mensaje específico y Reintentar o cambiar consulta |
| Preescucha externa activa | Pausar audio local; identificar la muestra y permitir detenerla |
| Descarga en curso | Progreso persistente entre pestañas; evitar pulsaciones repetidas |
| Letra no disponible | Buscar letra o importar archivo; reproducción accesible |

Los estados de conexión, descarga en curso y letras reales se especifican aquí, pero no están conectados en el prototipo. Las previews simulan guardado inmediato. El slider comparte la posición simulada con el mini reproductor; las duraciones son ilustrativas y no hay reloj de reproducción. Las listas reutilizan seis pistas de ejemplo. Anterior/Siguiente simulan cambios; no representan la cola ni prueban Azar sin repetición. El botón Atrás del sistema y la restauración independiente del desplazamiento por pantalla siguen pendientes de implementar; los botones Volver del mockup permiten recorrer las capas.

## Alcance y validación

Los mockups viven únicamente en `src/debug`; no sustituyen las pantallas actuales ni cambian Media3, permisos, versiones o SDK. Esta propuesta actualiza el nombre del primer destino y el recorrido de listas respecto al documento anterior; queda pendiente trasladar esas decisiones al producto.

Comprobación de compilación: `:app:compileDebugKotlin --offline` con JDK 21. No se entrega una APK. Las previews requieren revisión visual en Android Studio: no se ha verificado su renderizado en esta sesión. Antes de implementar, revisar texto grande, barras del sistema, teclado, TalkBack, contraste y uso con una mano en el Pixel. Antes de entregar una APK se ejecutarán `test lint assembleDebug` y las pruebas funcionales pendientes del backlog.

Recorrido concreto de revisión: comparar 02 y 03; en 02 pulsar Play/Pausa y cambiar a Listas y Buscar; abrir Ahora suena y Letra; volver a Biblioteca; en 01 elegir carpeta y comprobar que no aparece reproductor hasta seleccionar una canción. Repetir con Medianoche y texto grande. Criterios: ubicación estable entre pausa y reproducción, ninguna reproducción automática al abrir, una sola selección compartida y controles accesibles sin solapamientos. Compilación no equivale a validación visual o de usabilidad.
