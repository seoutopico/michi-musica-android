# Continuidad de Michi Música Android

Actualizado: 2026-09-06. Punto de entrada para continuar sin depender del historial del chat.

## YouTube y orden de programas 1.13: instalada y publicada

**Entrega completada:** [1.13.0 estable](https://github.com/seoutopico/michi-musica-android/releases/tag/v1.13.0) publicada como latest, sin borrador ni prerelease. [PR #2](https://github.com/seoutopico/michi-musica-android/pull/2) integrado en `main` (`14cf9d743581f0d717b3bdf705690654550cc56e`). Los seis adjuntos remotos coinciden en tamaño y SHA-256 con los locales. El Pixel conserva la edición debug final para mantener sus datos; no desinstalar para sustituir su firma.

Aina pide que Podcasts también siga canales de YouTube, incluido `@COASTTOCOASTAMOFFICIAL/videos`, y que Tus programas se ordene por el episodio más reciente. [Podcasts YouTube 1.13](PODCASTS_YOUTUBE_1_13.md) implementa resolución del canal, Atom público y descarga con el motor existente, sin cuentas/pagos. Diseño y ventana de 72 horas conservados. `test lint assembleDebug :app:assembleValidation assembleRelease --offline` correcto: 89 pruebas por variante, lint sin errores/16 advertencias previas. Prueba real de YouTube completada: seguimiento, duplicados, actualización, cancelación, reintento y MP3 reproducible de 60:45. Corregido el exceso de metadatos encontrado en la primera ejecución; la repetición y toda la regresión RSS pasaron. Vistas Medianoche/Rosa al 130 % revisadas. **1.13.0 debug instalada conservando datos**, siete archivos privados idénticos tras instalar. Canal solicitado añadido desde la app personal; ocho programas ordenados por publicación y Novedades limitada a 72 horas observados. Variante aislada retirada, escala/tiempo de apagado restaurados y última escucha conservada en pausa. APK oficial firmada publicada; cierre completado.

## Ampliación vigente: podcasts RSS 1.12.0

Aina autoriza seguir programas, novedades y descargas manuales **sin contenido de pago**, y exige conservar el diseño. Implementación en `podcasts/`, integrada en Biblioteca/Podcasts con Siguiendo · Novedades · Descargados. Leer [Podcasts RSS 1.12](PODCASTS_RSS_1_12.md) antes de continuar. No hay cuentas, pagos ni soporte de feeds privados. Actualización periódica configurable y avisos opcionales. Aina precisa después que una novedad tiene un máximo de 3 días desde su publicación: ventana móvil de 72 horas para Novedades, contadores y avisos; el catálogo completo permanece en Siguiendo.

El RSS real de Días Extraños contiene episodios exclusivos y otros audios recortados aunque anuncie duración completa: 209 entradas admitidas y 41 excluidas por el parser; una muestra pública de 9 minutos devuelve 126,354 segundos. No prometer descarga completa universal de iVoox. El guardado verifica duración y rechaza esos recortes. Sergio Parra: RSS devolvió 403, sin audio validado.

**1.12.0 instalada en Pixel 7 conservando datos.** Verificados seguimiento real de Días Extraños, descarga pública completa de NASA a través de SAF, rechazo nativo del recorte de iVoox, reproducción en segundo plano, posición tras reiniciar en pausa y vistas Medianoche/Rosa con texto al 130 %. Se corrigió la autocorrección del campo RSS. Días Extraños queda seguido; el programa y audio de NASA usados para validar se retiraron. Restauradas última escucha, apariencia, escala de texto y tiempo de apagado; los 112 archivos originales inventariados conservan ruta, tamaño y fecha. La ampliación final está publicada como [1.12.0](https://github.com/seoutopico/michi-musica-android/releases/tag/v1.12.0). MMA-039 está completado; los límites generales se distinguen en la ficha.

**Cierre nativo completado y debug final instalada conservando datos.** Regla de 72 horas, cancelación tardía y avisos comprobados. `scripts/validate_podcasts.ps1` completó toda la batería en Pixel desbloqueado: cancelación activa/en cola/durante commit, reintento, corte HTTP simulado, fallos SAF simulados, cierre del proceso y recuperación, notificación de una nueva entrada, apertura de Novedades y ausencia de duplicados. La variante aislada se retiró al terminar. En la app personal, Novedades muestra únicamente tres episodios del 4, 5 y 6 de septiembre; última escucha conservada. Preferencias, catálogo y podcasts seguidos idénticos inmediatamente después de instalar.

`test lint assembleDebug :app:assembleValidation assembleRelease --offline` correcto: 82 pruebas por variante, lint app sin errores/16 advertencias. APK oficial verificada con la firma de 1.11.1 en `dist/1.12.0/`; debug final en `dist/Michi-Musica-1.12.0-debug-arm64.apk`. Hashes y evidencia en el historial. Los fallos de red/proveedor de la batería son controlados; la frecuencia del trabajo sigue sujeta a Android/Doze. [Método y límites](VALIDACION_PODCASTS.md). **Entrega completada:** PR #1 integrado en `main` y [release estable 1.12.0](https://github.com/seoutopico/michi-musica-android/releases/tag/v1.12.0) publicada. Los seis adjuntos se verificaron por nombre, tamaño y SHA-256 remoto frente a los archivos locales. La edición debug final permanece instalada en el Pixel para conservar sus datos; no sustituirla por la firma oficial mediante desinstalación.

Capturas públicas de 1.12.0 añadidas al README a petición de Aina: `docs/screenshots/podcasts-following.png`, `podcasts-following-rose.png` y `podcasts-episodes-rose.png`. Son imágenes nativas seleccionadas de la validación; la guía de capturas explica cada estado. `podcasts.png` conserva la pantalla anterior y ya no representa la sección RSS en la galería principal.

## Qué leer y qué prevalece

Leer primero `AGENTS.md` en la raíz y esta guía. Después, la ficha de la pantalla que se vaya a tocar, [Historial de cambios](HISTORIAL_CAMBIOS.md) y [Backlog](BACKLOG.md). Las instrucciones posteriores de Aina prevalecen; registrar aquí los cambios de dirección que acepte.

La referencia visual aprobada es [apertura Medianoche](mockups/reinicio/01-apertura-medianoche.png). Es un concepto raster, no una captura de la aplicación. Los componentes de producción y las fichas 1.4–1.9 describen lo implementado. Una discrepancia entre imagen, código y documentación debe revisarse y explicarse antes de considerarla una decisión nueva.

`PROPUESTA_MOCKUPS_UX.md`, `ANALISIS_UX_DESDE_CERO.md`, `mockups/apertura/` y los prototipos debug `ui/proposal/` son históricos. Contienen recorridos sustituidos, como la tarjeta superior Continuar y alternativas de apertura. No reutilizarlos como especificación vigente.

## Método de trabajo acordado

Aina pide trabajar pantalla a pantalla. Antes de proponer un cambio, revisar la función real, el recorrido del usuario, las referencias relevantes y los estados de la pantalla. Explicar una propuesta concreta y su razón. Mantener la dirección ya aprobada entre pantallas y registrar cualquier revisión del criterio. Las instrucciones y aprobaciones existentes permiten continuar el trabajo autorizado; no pedirlas de nuevo para cada paso técnico.

La intención es una app cuidada, cómoda y musical, con el cuidado visual que Aina asocia a Apple. Las referencias de navegación consultadas —Apple Music, Spotify y guías de Apple— están enlazadas en la sección «Criterio de navegación y referencias» de la propuesta histórica. El uso nativo de Compose, sus controles y accesibilidad se referencia en las fichas actuales. Son referencias de diseño, no una certificación de conformidad ni una investigación con participantes: no se ha realizado esa investigación. Si se vuelve a comparar el comportamiento actual de esos productos, comprobar las fuentes de nuevo.

## Criterios que deben conservarse

- **Jerarquía:** música y acciones claras. Filas abiertas; separar con espacio, tipografía y divisores discretos. Reservar fondos y contornos para selección, controles o agrupaciones que los necesiten. Evitar convertir cada dato en una tarjeta redondeada.
- **Paleta:** Medianoche por defecto solo si no hay preferencia guardada; conservar Rosa si ya está elegida. En `MichiTheme.kt`: fondo Medianoche `#090A1B`, acento `#FF88AC`, texto principal `#F8F5FA`, secundario `#BEB0C2`. Usar los roles del tema, también para Rosa.
- **Tipografía y espacio:** títulos editoriales serif; textos y controles sans serif. Biblioteca, Buscar y Listas usan título de 36 sp; Letra usa 30 sp y el título de canción del reproductor 27 sp. Contenido alineado a 24 dp; superficies del mini reproductor/selección a 16 dp. Adaptar con insets, teclado, ancho y texto aumentado; no copiar coordenadas del Pixel como medidas de diseño.
- **Iconos:** sistema Material atribuido en `licenses/`, etiquetas cuando ayudan a entender la acción, objetivos táctiles mínimos de 48 dp y estados accesibles. Ningún botón decorativo sin función.
- **Mensajes:** información normal y contadores en segundo plano. Carga junto al proceso; error junto a la acción que falló y recuperación concreta. Permiso perdido → Elegir carpeta. No usar alertas permanentes para estados correctos.
- **Imágenes:** carátulas existentes en los archivos; marcador felino si faltan. Las portadas del mockup son ejemplos y no se asignan a canciones reales. La búsqueda de YouTube usa las miniaturas de sus resultados.
- **Continuidad:** Biblioteca · Buscar · Listas son los destinos principales. Mini reproductor común abajo en esas pantallas y Letra; Ahora suena usa controles completos y vuelve al origen al minimizar. No mover Play entre cabecera y pie según esté pausado o sonando.
- **Separación del menú:** petición posterior de Aina en 1.9: mantener una línea fina y aire antes de las pestañas, también sin mini reproductor y en Ahora suena. Se resuelve en `HomeNavigation`, compartido por todas las pantallas.
- **Apertura:** mostrar la última escucha disponible en pausa y con su posición; abrir no inicia música. Preparar una cola tampoco inventa una canción seleccionada. Respetar la reproducción que siga activa en el servicio.
- **Alcance:** Aina canceló la actualización/edición de metadatos. Leer etiquetas y carátulas existentes sigue permitido; no retomar su edición sin una nueva petición. Las letras son archivos asociados, no cambios del audio.

## Corrección 1.11.1: Niagara y apertura

La tarjeta de Niagara necesitaba visibilidad del servicio de escucha de notificaciones para que Media3 verificase su identidad. Conservar `<queries>` del manifiesto y el filtro de controladores de confianza; no resolverlo aceptando cualquier controlador. Play/Pausa/Anterior/Siguiente se probaron tocando Niagara.

`MusicFolderReader` usa `LibrarySnapshot` privado, versionado y atómico. `MainActivity` carga la caché en IO antes de la primera composición; luego comprueba cambios en la carpeta. No mostrar biblioteca vacía mientras se hidrata, ni borrar elementos de la cola hasta terminar una lectura válida. Releer fuerza etiquetas; borrado invalida su entrada. Mantener el diseño y las colas independientes. Consultar la última entrada del historial para compilación, instalación y publicación.

## Pantallas y aprobaciones

| Pantalla | Estado al cierre | Especificación y preview debug |
| --- | --- | --- |
| Biblioteca | Diseño aprobado; instalada y revisada, validación ampliada pendiente | [Portada 1.4](PORTADA_1_4.md), `LibraryHomePreviews.kt` |
| Buscar | Base 1.5 aprobada; Aina pide añadir vídeo integrado/pantalla completa en 1.9; revisión de esa ampliación pendiente | [Buscar 1.5](BUSCAR_1_5.md), [Vídeo 1.9](VIDEO_1_9.md), `SearchPreviews.kt` |
| Listas | Diseño aprobado; colección y apertura de lista revisadas | [Listas 1.6](LISTAS_1_6.md), `ListsPreviews.kt` |
| Ahora suena | Diseño aprobado; composición nativa revisada, batería de controles pendiente | [Player 1.7](PLAYER_1_7.md), `PlayerPreviews.kt` |
| Letra | Implementada en 1.8; pendiente de revisión nativa y aprobación de Aina | [Letras 1.8](LETRAS_1_8.md), `LyricsPreviews.kt` |

Una aprobación visual no equivale a haber probado todos los estados. Los pendientes concretos están en MMA-023 a MMA-028 del backlog.

## Mapa de implementación

Rutas relativas a `app/src/main/java/com/ainalluna/michimusica/`:

| Responsabilidad | Archivos |
| --- | --- |
| Navegación, permisos, conexión al controlador y restauración | `MainActivity.kt` |
| Tema, Biblioteca, mini reproductor y pestañas comunes | `ui/MichiTheme.kt`, `ui/LibraryHome.kt` |
| Carátulas locales | `ui/SongArtwork.kt` |
| Colección de listas y reproductor completo | `ui/ListsHome.kt`, `ui/PlayerHome.kt` |
| Reproducción de fondo y avance manual | `playback/PlaybackService.kt`, `playback/NextTrack.kt` |
| Clasificación local y posición por episodio | `library/AudioCatalog.kt`; guardado de fondo en `playback/PlaybackService.kt` |
| Lectura de biblioteca e importación Markdown | `library/MusicFolderReader.kt`, `library/MarkdownPlaylist.kt` |
| Buscar y sus operaciones | `youtube/SearchContent.kt`, `youtube/YouTubeDialog.kt`, servicios de `youtube/` |
| Vídeo integrado, identidad y pantalla completa | `youtube/YouTubeVideoPlayer.kt`, `youtube/YouTubeEmbed.kt`; orientación/tamaño en `AndroidManifest.xml` |
| Sesión de Letra, operaciones y vistas | `lyrics/LyricsDialog.kt`, `lyrics/LyricsState.kt`, `lyrics/LyricsHome.kt` |
| Consulta, almacenamiento y tiempos de letras | `lyrics/LyricsRepository.kt`, `lyrics/LyricsStorage.kt`, `lyrics/LrcParser.kt` |

Previews en `app/src/debug/java/com/ainalluna/michimusica/ui/`: abrir por nombre con Ctrl+Shift+N en Android Studio, variante debug, Split, Build & Refresh. Usan componentes reales con datos ficticios; no sustituyen la prueba de permisos, red, audio o archivos. Arquitectura ampliada en [ARQUITECTURA.md](ARQUITECTURA.md).

## Trabajo posterior: borrado y podcasts

Aina pide borrar canciones y plantea separar podcasts de YouTube de su música. Borrado implementado en 1.10.1 (código 19), instalado en Pixel y validado con archivos de prueba; Aina aprueba después la separación de podcasts, implementada en 1.11.0 (código 20). Música/Podcasts comparte Biblioteca y mantiene las colas separadas, con clasificación local y posición por episodio. Especificación y decisiones en [Borrado y podcasts](BORRADO_Y_PODCASTS.md). No borrar audios reales durante pruebas; usar archivos creados para la validación.

## Podcasts 1.11: reglas de continuidad

La sección consultada y la cola de reproducción son independientes. No reintroducir sincronización automática de `songs` hacia Media3 al navegar. La cola se crea al tocar un audio, Azar o Continuar; el servicio conserva su cola mientras se consultan otras secciones/listas. `AudioCatalog` usa URI como clave, sin mover audios ni modificar etiquetas. Las listas se resuelven antes de filtrar podcasts para no marcarlos falsamente como archivos ausentes. Posiciones periódicas y transiciones se guardan en el servicio; la actividad no es su único propietario. Leer `BORRADO_Y_PODCASTS.md` para los estados y límites de persistencia.

## Última entrega y punto exacto de reanudación

**1.11.0, código 20, validación final:** depuración autorizada y Pixel desbloqueado. Comprobados clasificación reversible, colas independientes al navegar entre Música/Podcasts/Listas, posiciones distintas de dos episodios, Anterior/Siguiente, avance automático, finalizado y reinicio del proceso sin autoplay. Descargas reales a Música y Podcasts, última elección recordada y segunda copia sin sobrescribir la primera (SHA-256 idéntico). Corregida una transición detectada al borrar el episodio actual: el siguiente conserva su posición y queda en pausa. Comprobado tanto al borrar el primero como el último de una cola de dos episodios.

`test lint assembleDebug assembleRelease --offline` final: 51 pruebas por variante, lint sin errores (16 advertencias app/3 iconpack), firma oficial verificada. Debug final instalada conservando datos. APK oficial 1.11.0 y sumas actualizadas para [la release](https://github.com/seoutopico/michi-musica-android/releases/tag/v1.11.0). No volver a subir la APK preliminar cuyo hash empieza por `681a8d`.

Todos los audios creados/descargados para las pruebas se eliminaron mediante la app. Inventario original conservado (nombres, tamaños y fechas), catálogo sin referencias de prueba, carpeta/listas/apariencia y última escucha conservadas. El Pixel quedó en Música, con su podcast original pausado en 12:41; ajuste de pantalla restaurado a 0. Capturas reales revisadas: `docs/screenshots/library.png` y `podcasts.png`. No repetir estas pruebas por una indicación antigua de «Pixel bloqueado»: los pendientes ampliados vigentes están en MMA-035/037, y los límites de seguridad en `REVISION_SEGURIDAD.md`.

Actualización de borrado: **1.10.1**, [release](https://github.com/seoutopico/michi-musica-android/releases/tag/v1.10.1). 43 pruebas por variante, lint sin errores y firma release verificada. El Pixel conserva la firma debug para mantener sus datos. Esta entrega queda sucedida por Podcasts 1.11.0; consultar la entrada más reciente del historial para validación y publicación.

**1.10.0, código 18**, publicada: [repositorio público](https://github.com/seoutopico/michi-musica-android) y [APK firmadas de la release](https://github.com/seoutopico/michi-musica-android/releases/tag/v1.10.0), nuevo icono adaptativo y módulo `iconpack` para Niagara. Aina autoriza crear el repositorio público, distribuir la APK, explicar el uso de IA y facilitar contribuciones. Leer README, CONTRIBUTING, PRIVACY, SECURITY y THIRD_PARTY antes de publicar o cambiar servicios externos. CI aún no está activado: plantilla y explicación en `docs/ci/`; no hay una ejecución remota aprobada. Los informes privados de vulnerabilidades de GitHub sí están habilitados.

La 1.9.1 corrigió el vídeo que se oía sin verse: el iframe tenía altura cero. `YouTubeEmbed.kt` fija dimensiones numéricas del viewport y responde a cambios de tamaño. Instalada 1.9.1 y confirmada imagen dentro de Buscar. Pantalla completa y regreso se observaron durante el diagnóstico con la corrección aplicada en vivo; no equivalen a una regresión completa sobre la APK final. Ver MMA-029.

La 1.10 incorpora lectura acotada de letras/listas, descargas que conservan archivos existentes y usan temporales independientes, controladores de sesión de confianza, copias de app desactivadas y HTTP explícitamente deshabilitado. [Revisión de seguridad](REVISION_SEGURIDAD.md) describe alcance y límites. Se conserva la actualización NIGHTLY solicitada; falta fortalecer su verificación criptográfica.

La firma oficial vive fuera del repositorio y se selecciona con `MICHI_SIGNING_PROPERTIES`. Nunca copiar claves o contraseñas a GitHub. La instalación de desarrollo y la release oficial tienen firmas diferentes: no desinstalar para migrar sin tener en cuenta preferencias y permisos. Se ha instalado debug 1.10 encima de la edición anterior, conservando sus datos; el paquete de iconos se instala con firma de publicación.

Capturas públicas en `docs/screenshots/`: Aina pide expresamente usar su biblioteca y carátulas reales. Se revisan las capturas seleccionadas antes de publicarlas; no subir rutas ni notificaciones personales. Letra conserva texto ficticio mediante `ShowcaseActivity`, exclusiva de debug. No publicar indiscriminadamente las evidencias de `tmp/device-review/`.

Continuar así:

1. Consultar el historial para la última APK, pruebas e instalación confirmadas. Comprobar ADB antes de actuar; «listo» no sustituye la autorización técnica. [Guía ADB](INSTALACION_ADB.md).
2. Completar Buscar → Ver vídeo → controles → pantalla completa → Atrás en la compilación final. Revisar posición al volver, pausa por pestaña/segundo plano y exclusión con música local. No hace falta descargar para probar vídeo.
3. Revisar Siguiente en mini y completo con Azar, incluida una ronda corta. `NextTrack.kt` recorre la permutación actual sin repetir dentro de la ronda y conserva pausa/reproducción; al agotarla vuelve al inicio. No implica una nueva mezcla ni repetición automática al final del audio.
4. Revisar Letra vacía y guardada, seguimiento, arrastre, Seguir canción y salto por verso. Probar escritura y quitar con archivos de prueba; conservar las letras del usuario.
5. Ampliar controles del sistema, bloqueo/Bluetooth, TalkBack, texto grande y fallos de red/permisos/escritura. No prometer descargas persistentes al destruir la actividad.
6. Registrar resultados efectivos en historial/backlog. Las capturas de demostración no prueban red, audio ni permisos; no cerrar pendientes por haber compilado.

## Compilar y entregar sin perder el contexto

API 36, mínimo 26, ARM64; Java/Kotlin generan bytecode 17. Entorno probado de Gradle: **JDK 21**; JBR 25 falló al iniciar Gradle. Elegir JDK 21 en Android Studio. En este equipo se ha usado este comando portable; en otro, seleccionar su instalación equivalente:

```powershell
$env:JAVA_HOME = Join-Path $env:USERPROFILE '.jdks/jbr-21.0.11'
.\gradlew.bat test lint assembleDebug --offline
```

`--offline` requiere dependencias ya descargadas; una máquina nueva necesita resolverlas primero. No cambiar SDK/dependencias para solucionar una selección incorrecta de JDK. La APK se genera en `app/build/outputs/apk/debug/app-debug.apk`. Antes de cada nueva entrega ejecutar las tres tareas, anotar versión, pruebas, lint e instalación y actualizar pendientes.

Los artefactos de `app/build/` no forman parte de una copia de fuentes habitual. Las capturas de `tmp/device-review/` son evidencias locales temporales y pueden no viajar al compartir el proyecto. Las verificaciones duraderas deben quedar descritas en el historial; si se comparten capturas, revisar que no expongan contenido personal.

No guardar rutas de perfil, claves ADB ni secretos en el repositorio. Resolver rutas locales con variables de entorno. La guía ADB no debe incluir identificadores del dispositivo. Si hay que abrir una URL en Windows para Aina, usar Chrome explícitamente.

## Entrega a otra persona o IA

El repositorio es la fuente de continuidad; no hace falta recuperar este chat. Las aprobaciones, limitaciones y pendientes están descritos aquí y en las fichas enlazadas. La documentación orienta y permite revisar el cumplimiento; no garantiza por sí sola que otra herramienta siga las instrucciones.

1. Abrir la raíz del proyecto, leer `AGENTS.md`, esta guía y la ficha de la pantalla afectada. Comprobar `git status` antes de editar y conservar trabajo existente.
2. Identificar el problema concreto y los estados que cambia. Reutilizar `MichiTheme`, filas, mini reproductor y navegación existentes. Conservar paleta, tipografía, márgenes, jerarquía de mensajes y posición de controles documentados. Si la petición ya autoriza un cambio, aplicarlo sin pedir otra vez la misma aprobación.
3. Implementar dentro de ese alcance y comparar el resultado con la especificación vigente y una captura anterior pertinente. Una nueva función debe mantener los recorridos de las demás pantallas. Los mockups antiguos descartados no son alternativas disponibles.
4. Ejecutar las comprobaciones requeridas y anotar su resultado real. Distinguir diseño aprobado, prueba unitaria, preview, prueba en dispositivo, APK instalada y release publicada. Si Android impide una prueba, dejarla pendiente y describir el bloqueo concreto.
5. Actualizar historial, backlog y esta guía cuando cambie el punto de reanudación. Guardar y subir el trabajo autorizado a Git. Conservar fuera del repositorio claves, preferencias del teléfono y audios de prueba privados; un clon puede compilar debug sin la firma oficial ni acceso al Pixel.

Texto para iniciar otra sesión:

> Continúa Michi Música desde este repositorio. Lee AGENTS.md, docs/CONTINUIDAD.md y la ficha de la pantalla que vayas a modificar. Conserva los diseños y recorridos aprobados y los cambios existentes; no recuperes propuestas descartadas. Retoma el apartado «Última entrega y punto exacto de reanudación», verifica el estado real y registra pruebas y pendientes sin darlos por completados por inferencia.
