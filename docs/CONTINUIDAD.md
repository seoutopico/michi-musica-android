# Continuidad de Michi Música Android

Actualizado: 2026-09-05. Punto de entrada para continuar sin depender del historial del chat.

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

**1.11.0, código 20:** código implementado, 49 pruebas por variante y lint sin errores, APK oficial firmada y debug final instalada conservando datos. Publicación 1.11.0 preparada en borrador; 1.10.1 sigue siendo la última estable pública. El Pixel volvió a bloquearse durante la revisión: solo se observó Biblioteca con el selector y el mini en pausa. Falta probar reclasificación reversible, colas al navegar, posición independiente de dos episodios, reinicio y destino de descarga. No dar estas comprobaciones por hechas.

Al recibir «listo» o desbloqueo, crear otra vez dos audios de prueba (los `Michi prueba podcast A/B 621b.wav` ya se retiraron; las copias locales están en `tmp/device-review/`), comprobar los recorridos anteriores y limpiar solo esos archivos. El ajuste `stay_on_while_plugged_in` se restauró a su valor original. Si hace falta mantener pantalla encendida, considerar que ADB indica alimentación AC, no USB: guardar/restaurar valor y usar `svc power stayon true` temporalmente. No sortear el bloqueo seguro. Actualizar capturas/documentación y publicar el borrador tras las comprobaciones.

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
