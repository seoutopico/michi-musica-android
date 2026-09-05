# Pantalla en revisión: apertura habitual

**Exploraciones históricas descartadas como dirección visual.** La referencia aprobada está en [reinicio](../reinicio/README.md). Para continuar el producto, leer [Continuidad](../../CONTINUIDAD.md); las alternativas y estados descritos a continuación pertenecen a la revisión anterior.

## Alternativa B: entrada a la colección

La primera revisión A se percibe demasiado parecida al diseño inicial: conservaba título, buscador y lista vertical como composición principal. Se mantiene como referencia comparativa, no como diseño validado.

Se añade B en el mismo `AperturaMichi.kt`, grupo **Alternativa B · estructura**, previews B1 y B2. Lámina en `coleccion-b.svg` y render en `coleccion-b.png`.

B abre con una portada de la colección: ilustración felina en un disco, título editorial, cantidad de canciones, botón principal Ver canciones y acción secundaria Escuchar al azar. La ilustración es un emblema de Michi, no una portada musical ni metadatos de una pista. El reproductor sigue abajo.

Ver canciones abre el listado con filtro dentro del prototipo; Biblioteca permite regresar a la portada. No se inventan recomendaciones, historial ni agrupaciones nuevas. B cambia tanto la composición como el recorrido y tiene un coste: elegir una canción específica necesita un toque adicional. Retomar y Azar siguen a un toque. Es una alternativa para evaluar, no una mejora de usabilidad demostrada ni una sustitución aprobada de A.

Compilación de B aprobada con `:app:compileDebugKotlin --offline`, JDK 21. Render e inspección de SVG; render de Compose aún pendiente. El resto del documento describe A.

## Escenario y alcance

Ya hay una carpeta autorizada con música. La persona abre Michi para elegir una canción o retomar su última escucha. Este escenario se ha elegido provisionalmente al no recibirse respuesta a la pregunta sobre primera apertura o apertura habitual. La primera instalación, que requiere elegir carpeta, será una pantalla diferente.

Esta revisión sustituye únicamente la dirección visual de la apertura de `MichiProposal.kt`. No presenta las otras pantallas como aprobadas. No implementa cambios de metadatos ni modifica la app productiva.

## Artefactos

- `AperturaMichi.kt`, en `app/src/debug/java/com/ainalluna/michimusica/ui/proposal/`: nueva composición Compose independiente. Localizar por nombre con Ctrl + Shift + N en Android Studio, variante debug, Split y Build & Refresh.
- `biblioteca.svg`: lámina de diseño vectorial con datos ficticios, barras del sistema ilustrativas y medidas aproximadas a la composición Compose.
- `biblioteca.png`: render de esa lámina para revisión inmediata. **No es una captura de Android ni una prueba de correspondencia exacta con Compose.** Usa Arial; el teléfono usará la tipografía Android.

## Decisiones de diseño

El título Biblioteca orienta. El contador y En tu dispositivo explican el alcance como información secundaria, sin tratarlo como alerta. La búsqueda local está cerca del título porque permite llegar directamente a una canción. El rótulo Canciones introduce la colección; Azar es una acción secundaria sobre ella.

Cada fila prioriza título, artista y duración. La canción seleccionada se distingue por texto rosa y un icono de estado. No hay una tarjeta por canción ni portadas falsas. Todas las filas comparten márgenes, alturas mínimas y alineaciones para facilitar el barrido visual. Los títulos largos se truncan a una línea; el comportamiento con nombres reales y texto grande requiere revisión en el dispositivo.

El reproductor inferior es una superficie continua, reservada para controlar la escucha. Su color lo separa del contenido desplazable; Play es el único control circular relleno, para que reanudar sea inmediato. Pausa ocupa el mismo sitio. Título y estado abren Ahora suena; Siguiente actúa sobre la selección. La barra de progreso es informativa, no un slider diminuto. Biblioteca, Buscar y Listas permanecen debajo, con icono y texto, sin cápsulas de selección.

El rosa y los tonos cálidos conservan continuidad de marca. Esta pantalla prioriza el contenido; la identidad felina deberá desarrollarse en la apertura inicial y otras superficies de marca, sin anteponer una ilustración a las canciones en cada visita.

## Función de los mensajes

No se añade un aviso de éxito o privacidad permanente a una biblioteca que funciona con normalidad. Si el filtro no encuentra canciones, la respuesta aparece en el área de resultados con Borrar búsqueda. Los datos sobre carpeta y actualización se consultan en Opciones de biblioteca.

Para una futura variante con permiso perdido: explicación persistente en el área de contenido y una acción Volver a dar acceso; no mostrar un reproductor que parezca operativo si no puede leer la pista. Ese estado no está dibujado en esta revisión. Confirmaciones transitorias, errores de descarga y acciones de otras pantallas no forman parte de esta apertura.

## Referencias y criterio

- [Apple HIG: jerarquía](https://developer.apple.com/design/human-interface-guidelines): distinguir contenido y controles. Aplicación propia: espacio, alineación y peso tipográfico antes de añadir contenedores.
- [Apple Music](https://support.apple.com/en-gb/guide/iphone/iph676daac9b/ios) y [Spotify](https://support.spotify.com/ws/article/now-playing/): referencia funcional de control compacto inferior y acceso a Ahora suena.
- [Android: objetivos táctiles](https://support.google.com/accessibility/android/answer/7101858?hl=en): mínimo 48 dp para controles individuales, independientemente del tamaño visible del icono.
- [Apple: onboarding](https://developer.apple.com/design/human-interface-guidelines/onboarding): si existe una preparación imprescindible, mantenerla breve. Por eso elegir carpeta pertenece a la primera apertura, sin repetirse en cada visita.

Se trata de una propuesta razonada, no de investigación con usuarios ni de reproducción literal de Apple Music. La estética y la comodidad deben contrastarse con Aina sobre la pantalla real.

## Recorrido de evaluación

En Interactive Mode se puede buscar por título/artista sin distinguir acentos, borrar la búsqueda, seleccionar una canción, pausar, continuar y pasar a la siguiente. Al elegir una canción nueva, la posición simulada vuelve a 0:00; al pausar y continuar se conserva. No hay audio ni reloj real. Azar selecciona una pista diferente a la actual para demostrar la acción; no implementa ni valida las rondas del producto.

Las pestañas Buscar/Listas, el acceso Ahora suena y las acciones de carpeta muestran un diálogo identificado como explicación de maqueta. Estos diálogos **no forman parte del diseño del producto**: permiten identificar el destino mientras se revisa una sola pantalla.

Las cuatro previews muestran la misma pantalla al volver en pausa, reproduciendo, sin escucha previa y a 360 dp con texto al 130 %. Verificar que Play/Pausa no desplaza las canciones, que la navegación permanece legible y que no se corta el acceso a la última fila.

Validación realizada: `:app:compileDebugKotlin --offline` con JDK 21; renderizado e inspección visual de la lámina SVG mediante Chrome. Pendiente: renderizado de las previews Compose en Android Studio y validación táctil, de TalkBack y teclado en el Pixel. No se entrega APK.
