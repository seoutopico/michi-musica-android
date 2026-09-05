# Historial de cambios

## 2026-09-05 — Validación final de 1.11.0 en Pixel y corrección de reanudación al borrar

- Pixel autorizado y desbloqueado: Música/Podcasts y clasificación reversible probadas con dos WAV propios. A conserva 0:52 y B 1:49 usando Anterior/Siguiente; B conserva 2:17 tras cerrar el proceso y abrir de nuevo, sin autoplay. Navegar por Música, Listas y una lista de 17 canciones mantiene la cola de podcasts. Azar de Música crea su propia cola y Siguiente cambia de pista estando en pausa.
- A termina en segundo plano, queda Escuchado y B recupera su posición; al seleccionar A finalizado empieza desde cero. Control de pausa del sistema comprobado. Cancelar borrado conserva el archivo; borrar B retira su referencia y conserva A seleccionado; borrar el actual pausa antes de cambiar de audio.
- Hallazgo nativo corregido: al borrar el episodio actual, Media3 seleccionaba el siguiente en cero aunque tuviera progreso. `episodeTransitionPosition` recupera su posición también en transiciones REMOVE, respetando posiciones explícitas y episodios finalizados. Dos pruebas nuevas. Verificado en APK final al borrar el primer y el último episodio de colas de dos: el audio restante queda pausado en su 12:41 guardado.
- Dos descargas MP3 reales del vídeo oficial de Blender «Caminandes 3: Llamigos» (SkVqJ1SGeL0), una a Música y otra a Podcasts; elección anterior recordada después de reinstalar debug. La segunda crea `(1)` y la primera mantiene exactamente su SHA-256. Categorías verificadas en el catálogo. Material de prueba libre: [publicación de Blender y licencia Creative Commons](https://vimeo.com/153608970); no se redistribuyen los MP3.
- `test lint assembleDebug assembleRelease --offline`: BUILD SUCCESSFUL en 5m 29s, JDK 21. 51 pruebas debug y 51 release sin fallos; lint app 0 errores/16 advertencias, iconpack 0 errores/3 advertencias. Firma release verificada. APK final SHA-256 `bbb58b8f448446fb0a44c3b70e8dbce3299a6f7de0c81992a4d02b2b911369ae`.
- Catálogo privado existente: lectura directa desde shell denegada por Android; no equivale a una prueba de intrusión de todos los componentes. Revisión de seguridad conserva sus límites de dependencias, actualizador, Bluetooth y proveedores.
- Eliminados solo WAV/MP3 de prueba mediante los diálogos de la app. Comparación del inventario original: mismos nombres, tamaños y fechas; sin datos de prueba en catálogo. Carpeta, listas, apariencia y última escucha originales conservadas. Pantalla restaurada a su configuración previa. Capturas reales nuevas de Música/Podcasts revisadas; instrucciones de continuidad reforzadas para otras personas o IA.


## 2026-09-05 — Michi Música 1.11.0: Música y Podcasts

- Aina aprueba la propuesta de separar podcasts. Selector Música/Podcasts en Biblioteca, sin añadir destinos inferiores ni cambiar el lugar del mini reproductor. Filas de episodios con estado Sin empezar/Continuar/Escuchado.
- Guardar MP3 pide Música o Podcasts y recuerda la última elección confirmada. Clasificación de la URI exacta creada, sin mover archivos ni editar sus etiquetas. Opciones Marcar como podcast / Marcar como música en los audios existentes.
- Las listas resuelven todos los archivos y después omiten podcasts sin contarlos como ausentes. Música, su búsqueda y Azar quedan separados de los episodios. Navegar entre secciones o listas no reemplaza la cola activa; comenzar un episodio crea su cola y desactiva Azar/Repetir.
- Posición por episodio en preferencias locales, guardada desde PlaybackService cada cinco segundos, al pausar y cambiar de audio. Siguiente/Anterior y avance automático recuperan posiciones. Episodios finalizados comienzan desde cero al volver a seleccionarlos. Borrar un audio limpia su clasificación/progreso.
- Si se reclasifica el audio actual, continúa con una cola de un solo audio para no mezclar categorías. La clasificación se conserva al actualizar y volver a la misma URI; desinstalar o borrar datos la elimina. Se mantiene lectura directa de carpeta, sin recursión. Especificación en `BORRADO_Y_PODCASTS.md`.
- Pruebas: 49 debug y 49 release sin fallos. Seis nuevas pruebas del catálogo cubren persistencia independiente, reclasificación reversible, borrado aislado, destino de descarga y límites de reanudación.
- `test lint assembleDebug assembleRelease --offline`, JDK 21: BUILD SUCCESSFUL en 6m 26s tras declarar el opt-in de `PositionInfo.mediaItem` exigido por lint. App: 0 errores/16 advertencias; iconpack: 0 errores/3 advertencias. Firma release v2 RSA 3072 verificada, mismo certificado oficial.
- Instalada debug 1.11.0 (código 20) final en Pixel conservando datos. La jerarquía nativa muestra Biblioteca con Música/Podcasts, las 110 canciones originales más dos WAV de prueba y mini reproductor pausado. El dispositivo se bloqueó antes de reclasificar o probar reproducción: esos recorridos NO están validados aún. Se retiraron únicamente los dos WAV propios y se restauró el ajuste de pantalla original. No se borraron audios del usuario.
- APK firmada preparada `Michi-Musica-1.11.0-arm64.apk`, SHA-256 `681a8d164752c2572426d61f4bb87295e6ea7c25eccdf13b335b489f195525a6`. Publicación preparada como borrador hasta completar la comprobación nativa; última release pública estable: 1.10.1.

## 2026-09-05 — Michi Música 1.10.1: borrar canciones

- Menú de tres puntos en las filas de Biblioteca, también dentro de una lista importada. Duración en la línea secundaria para reservar espacio al menú.
- Borrar canción pide confirmación con título, archivo y alcance. Cancelar conserva el audio; la operación muestra progreso y errores con reintento. Solo elimina el archivo de audio seleccionado directamente dentro de la carpeta SAF autorizada. No modifica los Markdown ni borra letras asociadas.
- Tras éxito, retira todas las referencias de la cola, actualiza biblioteca/listas y descarta la reanudación del audio eliminado. Si es la pista actual, pausa antes de retirarla; no inicia otra automáticamente.
- `test lint assembleDebug assembleRelease --offline`, JDK 21: BUILD SUCCESSFUL. 43 pruebas debug y 43 release sin fallos; lint app 0 errores/16 advertencias. Firma release verificada con apksigner.
- Instalada debug 1.10.1 (código 19) en Pixel 7 conservando datos. Prueba con WAV silencioso creado expresamente: Cancelar conserva el archivo; confirmar lo elimina y vuelve de 111 a 110 canciones. Segunda prueba con audio de tres minutos en reproducción: borrado confirmado, siguiente pista pausada en 0:00 y archivo ausente. Solo se borraron los archivos de prueba; se conservan las 110 canciones originales. Pendientes proveedores con errores/permisos y casos ampliados de cola: MMA-035.
- APK oficial `Michi-Musica-1.10.1-arm64.apk`, SHA-256 `2d68ef2beea05a17302bae668c0d4cd843a5d0ce7d126378bb5b017f996fb7fa`.
- Aina plantea separar podcasts de YouTube de la música normal. Propuesta registrada en `BORRADO_Y_PODCASTS.md`: Música/Podcasts dentro de Biblioteca, destino al guardar, clasificación de audios existentes, colas separadas y posición por episodio. No implementada en esta versión.


## 2026-09-05 — Aclaración de las funciones de YouTube en README

- Sustituida la frase ambigua «Ver no descarga un MP3» por dos funciones explícitas: Ver vídeo reproduce dentro de la app y Guardar MP3 descarga el audio a la carpeta de música para escucharlo sin conexión. Cambio documental; la descarga sigue disponible y no cambia la APK.

## 2026-09-05 — Michi Música 1.10.0: identidad y publicación comunitaria

- Publicados [repositorio público](https://github.com/seoutopico/michi-musica-android) y [release v1.10.0](https://github.com/seoutopico/michi-musica-android/releases/tag/v1.10.0): APK firmadas, sumas SHA-256, certificado público y archivo upstream. GitHub confirma reporte privado de vulnerabilidades habilitado.

- Petición de Aina: adaptar el icono al diseño aprobado, crear variantes para Niagara, abrir el código y distribuir una APK instalable, documentar todas las funciones y el desarrollo con IA, y revisar la seguridad.
- Icono adaptativo Medianoche con capa monocroma. SVG/PNG editables y generador en el repositorio. Nuevo módulo Michi Iconos 1.0.0 con tres variantes y sin permisos.
- Corregida lectura sin límite de letras/Markdown, sustitución destructiva de MP3 al descargar, temporales compartidos y acceso indiscriminado a la sesión. Copias de app desactivadas y HTTP explícitamente deshabilitado. Revisión y riesgos restantes en `REVISION_SEGURIDAD.md`.
- Firma privada de publicación separada de debug y excluida de Git. Manifiestos finales revisados: release no depurable, sin ShowcaseActivity; iconpack sin permisos. Ambas APK verificadas con apksigner (v2, RSA 3072).
- `test lint assembleDebug assembleRelease --offline` correcto con JDK 21: 40 pruebas debug y 40 release, sin fallos. Lint de app: 0 errores/16 advertencias; iconpack: 0 errores/3 advertencias.
- Instaladas debug 1.10 sobre la versión anterior e Iconos release 1.0.0 en Pixel 7. Biblioteca real de 110 canciones, carátulas, lista de 17 canciones, reproductor, seek pausado a 1:11 y cambio Rosa/Medianoche revisados. A petición posterior de Aina, las capturas públicas utilizan su biblioteca real; Letra conserva texto ficticio. Estas capturas no completan la batería de funciones del backlog.
- Niagara reconoce el paquete instalado y su asignación a Michi. Selector nativo con tres variantes comprobado; aplicada Medianoche a la app sin modificar los iconos de las demás aplicaciones. Búsqueda real de YouTube con ocho resultados y miniaturas confirmada en 1.10.
- README público con descarga, requisitos Android 8+/ARM64, funciones, capturas, licencia GPL-3.0, atribución de IA con dirección humana y guías de instalación/contribución. Añadidas privacidad, seguridad, licencias externas, guía Niagara, continuidad y plantilla CI con acciones fijadas a SHA. GitHub rechazó subir el workflow por falta de alcance OAuth `workflow`; se conserva en `docs/ci/` y no se da por ejecutado.
- APK oficial `Michi-Musica-1.10.0-arm64.apk`, código 18, SHA-256 `3ef74fd943a9e112f871a566d23973ad68da1c9549420c40bf8fa14e1c288404`. Iconos `Michi-Iconos-1.0.0.apk`, SHA-256 `6edabf8a97b249ec505af5fd9e80b27c586ef69c3740239541950dea6a288589`.

## 2026-09-05 — Michi Música 1.9.1: imagen del vídeo

- Diagnóstico en WebView: reproducción en marcha pero iframe y body con altura cero. Tamaño explícito con dimensiones numéricas del viewport; reajuste al rotar/cambiar tamaño y al regresar de pantalla completa, sin recrear el vídeo.
- `test lint assembleDebug --offline` correcto: 36 pruebas por variante sin fallos, lint sin errores. Instalación de APK 1.9.1 confirmada y vídeo visible dentro de Buscar. Pantalla completa y vuelta observadas durante el diagnóstico aplicando la corrección en vivo; la regresión completa final permanece en MMA-029.
- APK de desarrollo 1.9.1, código 17, 70.929.877 bytes. SHA-256 `449102102D1587A8C86652669FE27DDF609CDDCD6E12660031DEDD4289666F26`.

## 2026-09-05 — Michi Música 1.9.0: vídeo en Buscar y separación inferior

- Aina pide reproducir vídeos de YouTube dentro de Buscar, sin descargarlos, y abrirlos a pantalla completa. Ver vídeo sustituye a Escuchar en los resultados; Guardar MP3 conserva su acción independiente. Se retira la extracción de URL para la antigua preescucha de audio.
- Reproductor oficial integrado con WebView y HTML local: controles de YouTube, título, cerrar, posición de sesión, estados de carga/error, reintento y salida a YouTube si no se puede insertar. No se añaden SDK ni dependencias. Identidad Referer/origin derivada de la app, IDs validados, sin títulos/consultas interpolados en HTML; acceso a archivos/contenido desactivado y puente limitado a eventos.
- Pantalla completa mediante la vista entregada por WebChromeClient, salida con Atrás o botón, orientación horizontal temporal y restauración al salir. Orientación/tamaño gestionados sin recrear la actividad. Vídeo y música local se pausan mutuamente; vídeo pausado al salir de Buscar o pasar a segundo plano.
- Petición visual: HomeNavigation incorpora 8 dp de aire, divisor fino y 4 dp antes de las pestañas. Se aplica también en Ahora suena y cuando no hay mini reproductor.
- Documentación y continuidad actualizadas; especificación en `docs/VIDEO_1_9.md`. Letra y Siguiente en Azar conservan sus pendientes nativos anteriores.
- Validación final: `test lint assembleDebug --offline`, JDK 21, BUILD SUCCESSFUL en 1m 4s. 36 pruebas en debug y 36 en release, sin fallos/errores; lint 0 errores y 17 advertencias. Cuatro pruebas nuevas cubren validación de entradas del HTML, identidad/restauración sin autoplay, tiempos inválidos y mensajes de inserción restringida.
- APK final `app/build/outputs/apk/debug/Michi-Musica-1.9.0.apk`, versión 1.9.0, código 16, 70.931.205 bytes; SHA-256 `BEBD01EA74BDDB09E5EAD371972F95C1AE25CE62F97FF145E5F650660818398D`.
- ADB recuperó autorización y se instalaron/abrieron compilaciones de prueba 1.9 conservando datos. Se observó Buscar con Ver vídeo, separación del menú y apertura del contenedor integrado. Capturas locales `tmp/device-review/video-opening.png` y `video-loaded.png`; no prueban reproducción. El usuario navegaba durante la revisión; después el móvil se bloqueó y finalmente desapareció de ADB. La instalación de la APK final falló con `no devices found`; queda instalada una compilación de prueba anterior de 1.9. No se verificaron vídeo en reproducción, pantalla completa ni la interacción nativa completa: MMA-029.

## 2026-09-05 — Guía de continuidad para personas e IA

- Añadida `docs/CONTINUIDAD.md` y enlazada desde README y AGENTS: dirección aprobada, criterios de jerarquía/espaciado/navegación, alcance cancelado de metadatos, mapa de código, aprobaciones por pantalla, validación y orden de reanudación.
- Marcadas las propuestas y aperturas anteriores como históricas para evitar recuperar decisiones descartadas. Aclarado JDK 21 para ejecutar Gradle frente al objetivo de bytecode 17.
- Último estado observado del Pixel: detectado por ADB como `unauthorized`; sigue pendiente la prueba nativa de Letra y Siguiente 1.8. No se da por resuelta por el mensaje «listo».
- Cambio exclusivamente documental; no modifica la APK 1.8.0 ni sus resultados de pruebas.

## 2026-09-05 — Michi Música 1.8.0: Letra y Siguiente en Azar

- Aina aprueba el reproductor y pide Letra. Durante ese trabajo comunica que Siguiente queda apagado con Azar. Se incorpora la corrección a ambos controles de la app y a la reanudación con avance.
- El avance continúa el orden existente y, al agotar una ronda en Azar, vuelve al primer elemento de esa permutación. No repite dentro de la ronda ni vuelve a sortear cada pulsación. Con una sola pista no ofrece otra. Conserva reproducción/pausa y el fin secuencial normal.
- Nueva Letra: cabecera serif, versos abiertos y línea activa destacada. Seguimiento automático que se detiene al arrastrar, Seguir canción y salto a un verso con tiempos. Mini reproductor inferior conservado.
- Búsqueda en vista separada con campos de consulta, resultados informativos, vista previa y Guardar esta letra. Menú secundario para buscar otra letra o quitar la guardada con confirmación. Los campos no editan metadatos de audio.
- Estados de carga, búsqueda, escritura y error; sesión de operaciones ligada a canción/carpeta. Se verifica el borrado de letras y se escribe la nueva antes de retirar la versión del otro formato. Ningún audio se modifica.
- Seis previews reales con datos ficticios en `LyricsPreviews.kt`; detalles y límites en `docs/LETRAS_1_8.md`. SDK y dependencias conservados.
- Validación: `test lint assembleDebug --offline`, JDK 21, BUILD SUCCESSFUL; 32 pruebas en debug y 32 en release sin fallos. Incluye tres pruebas nuevas de avance y final de ronda. Lint: 0 errores, 17 advertencias.
- APK `app/build/outputs/apk/debug/Michi-Musica-1.8.0.apk`, 70.899.025 bytes; SHA-256 `7FEBA7C422DAF411C2651F1B65639F47AD84358A60926CA6A13B1967742E1F03`. Versión 1.8.0, código 15. `adb install -r` y arranque de actividad correctos en Pixel 7.
- El dispositivo se desconecta después de la instalación y antes de revisar Letra y probar Siguiente. No se da por verificado el renderizado ni la interacción nativa de esta entrega; pendiente MMA-028.

## 2026-09-05 — Michi Música 1.7.0: reproductor completo

- Aina aprueba Listas y pide continuar con el player. Ahora suena usa carátula real, título serif, márgenes de 24 dp y reproducción/pausa dominante. Azar, Repetir y Letra tienen iconos Material y etiquetas; se elimina Más opciones sin acción.
- Carátula adaptada al espacio disponible con lectura de hasta 1024 px, sin modificar imágenes ni metadatos. Marcador felino cuando no hay carátula. Contenido desplazable en poca altura o con texto aumentado.
- Progreso fino con control circular y área táctil nativa; posición de arrastre independiente del reloj y salto al soltar. Preparación y error de reproducción con reintento explícitos. El mismo MediaController y servicio conservan reproducción, cola y controles del sistema.
- Cinco previews en `PlayerPreviews.kt` y documentación en `docs/PLAYER_1_7.md`. SDK y dependencias sin cambios.
- Validación final: `test lint assembleDebug --offline`, JDK 21, BUILD SUCCESSFUL. 29 pruebas por variante debug/release sin fallos, incluidas tres pruebas nuevas de cálculo y límites de seek. Lint: 0 errores, 17 advertencias.
- APK `app/build/outputs/apk/debug/Michi-Musica-1.7.0.apk`, 71.027.069 bytes; SHA-256 `92BC33E28D1E09A41844A9269E1981F99F13FE7303BD66732D1D5EE25E2A2F71`. Versión 1.7.0, código 14. Instalación con `adb install -r` y arranque correctos en Pixel 7.
- Tras desbloquear el Pixel, revisada la APK final con Alegria de Cirque du Soleil: carátula real cargada, barra fina, tiempos, controles y tres destinos inferiores visibles sin solapamientos. Apertura desde el mini reproductor y regreso a Listas observados con escucha pausada en 0:06. Captura final en `tmp/device-review/player-final-loaded.png`. Aina está navegando durante la revisión; no se interrumpe esa interacción ni se da por verificado el arrastre o todos los controles en dispositivo. Validación ampliada en MMA-027.

## 2026-09-05 — Michi Música 1.6.0: Listas sigue el estilo aprobado

- Aina aprueba Buscar y pide continuar con Listas. Nueva pantalla con emblema felino, título serif, márgenes de 24 dp y acción Añadir lista. Toda tu música queda separada de Tus listas por un divisor discreto.
- Filas abiertas con composición de carátulas existentes, nombre, recuento y señal explícita de selección actual. Solo la selección lleva fondo tonal y línea lateral. Las referencias ausentes se muestran como información secundaria.
- Estados de carga y colección vacía, contenido desplazable y cabecera adaptable. El mini reproductor y la navegación inferior siguen siendo los componentes comunes. Tocar una lista abre sus canciones en Biblioteca; Toda tu música restaura la colección completa.
- Resúmenes de listas calculados en E/S sobre una copia de la biblioteca; se conservan las primeras cuatro canciones para las carátulas. Importador y archivos sin modificaciones.
- Cinco previews en `ListsPreviews.kt`, componentes en `ui/ListsHome.kt`; recorrido y límites en `docs/LISTAS_1_6.md`. SDK y dependencias sin cambios.
- Validación: `test lint assembleDebug --offline` con JDK 21, BUILD SUCCESSFUL; 26 pruebas en debug y 26 en release, sin fallos. Lint: 0 errores y 17 advertencias.
- APK `app/build/outputs/apk/debug/Michi-Musica-1.6.0.apk`, 70.826.677 bytes; SHA-256 `1526C2D9859E468B69BDB2F1738A60D24F1B679BAB9E28B7DA39DC8230463667`. Versión 1.6.0, código 13, instalada mediante `adb install -r` en Pixel 7.
- Tras desbloquear el Pixel, revisada la captura nativa de Listas: composición de cuatro carátulas reales para Electrónica: de menos a más, 17 canciones, acceso a Toda tu música con 105 canciones y reproductor inferior. Al abrir la lista se verifica Biblioteca con sus 17 canciones y Ver toda. Evidencias en `tmp/device-review/lists-new.png` y `list-open.xml`. Aina continúa probando otras pantallas; no se fuerza una pantalla final ni se considera completada la validación ampliada de MMA-025.

## 2026-09-05 — Michi Música 1.5.0: Buscar sigue la portada aprobada

- Aina aprueba Biblioteca en el Pixel y pide continuar con Buscar. Nueva pantalla con cabecera felina, título serif, márgenes de 24 dp, campo amplio y acción Buscar en YouTube. Filas sin tarjetas, miniaturas reales, título/canal/duración y acciones Escuchar / Guardar MP3.
- Estados de carga, falta de resultados, error con reintento, preparación, descarga y guardado junto a la acción correspondiente. El teclado permite enviar la consulta. El mini reproductor inferior conserva su posición y representa la música local.
- El estado y las operaciones de Buscar pertenecen al contenedor común: se conservan consulta y resultados al cambiar de pestaña. La preescucha se pausa al salir o iniciar audio local, y pausa el audio local al iniciarse o reanudarse. No se garantiza la continuidad de descargas tras destruir la actividad.
- Corregido un fallo observado durante la revisión: el filtro de Biblioteca retenía la lista vacía del primer escaneo porque su clave era una lista mutable. Ahora observa sus cambios y muestra las canciones al terminar la carga.
- Seis previews de producción en `SearchPreviews.kt`; recorrido, referencias y límites en `docs/BUSCAR_1_5.md`. SDK y dependencias sin cambios; iconos Material atribuidos en `docs/licenses`.
- Validación final: `test lint assembleDebug --offline` con JDK 21, BUILD SUCCESSFUL. 26 pruebas por variante debug/release sin fallos; lint 0 errores y 17 advertencias.
- APK `app/build/outputs/apk/debug/Michi-Musica-1.5.0.apk`, 70.912.505 bytes; SHA-256 `6850C9C2E21E1200657DF1F7423DECE14D8964EE79B9A87650257A89E9348D65`. Versión 1.5.0, código 12, instalada con `adb install -r` en Pixel 7.
- Capturas nativas: entrada Medianoche, carga y ocho resultados reales de Debussy con miniaturas. Verificado regreso Biblioteca → Buscar con consulta/resultados conservados y escucha local pausada en las mismas coordenadas. Verificada carga de 105 canciones en Biblioteca tras la instalación. Evidencias locales en `tmp/device-review/`.
- Preescucha real comprobada: Preparando → Escuchando/Pausar; al salir a Biblioteca y regresar a Buscar vuelve a Escuchar, con la música local aún pausada. Se deja Buscar abierto con resultados. No se ejecuta una descarga de prueba; validación ampliada en MMA-024.

## 2026-09-05 — Portada 1.4.0 instalada y revisada en Pixel 7

- Actualización mediante `adb install -r` correcta, conservando los datos. Confirmados `versionCode=11` y `versionName=1.4.0`; actividad abierta correctamente.
- Revisadas capturas nativas de Biblioteca en Rosa y Medianoche: 104 canciones, carátulas reales, cabecera, filas, mini reproductor pausado y tres destinos visibles, sin solapamientos en la captura.
- Comprobado el cambio a Listas y Buscar y el regreso a Biblioteca: la canción pausada y las coordenadas del mini reproductor se mantienen. Seleccionada Medianoche desde Ajustes para dejar visible la dirección aprobada.
- Evidencias locales en `tmp/device-review/`. La validación ampliada de reproducción, permisos, teclado, texto aumentado y TalkBack sigue pendiente en MMA-023.

## 2026-09-05 — Michi Música 1.4.0: portada aprobada

- Implementada la referencia aprobada `docs/mockups/reinicio/01-apertura-medianoche.png`: Biblioteca con cabecera felina, título serif, búsqueda local, filas sin tarjetas y carátulas existentes en los archivos. Sin carátula se muestra un marcador felino; no se editan metadatos ni se buscan imágenes externas.
- Mini reproductor inferior compartido con la última escucha pausada o la sesión activa. Abrir la aplicación no inicia audio. La restauración espera a la fuente y al orden correctos de la cola; la carga inicial no borra la sesión en segundo plano. Azar empieza por el primer elemento del orden aleatorio para recorrer la ronda.
- Avisos de permiso con acción, estados de carga y vacío, iconos Material, áreas táctiles de 48 dp, márgenes de contenido de 24 dp e insets del sistema y teclado. Medianoche por defecto sin sobrescribir una preferencia Rosa guardada.
- Previews actuales: `app/src/debug/java/com/ainalluna/michimusica/ui/LibraryHomePreviews.kt`. Reutilizan los componentes productivos; estados Medianoche, sin escucha previa, texto aumentado y Rosa. Detalles en `docs/PORTADA_1_4.md`.
- Versión `versionCode` 11, `versionName` 1.4.0; SDK y dependencias conservados.
- Validación final: `test lint assembleDebug --offline` con JDK 21, BUILD SUCCESSFUL. 26 pruebas en debug y 26 en release, sin fallos. Lint: 0 errores y 17 advertencias; informe en `app/build/reports/lint-results-debug.html`.
- Artefacto: `app/build/outputs/apk/debug/Michi-Musica-1.4.0.apk`. SHA-256 `2D2EEF900784FBA9937F6886C632F3F040D37E6D92AA29DD5E23C9A9F669A08D`.
- Sin dispositivo ni emulador conectado: instalación y revisión visual nativa pendientes en MMA-023. El mockup aprobado es una imagen conceptual, no una captura del resultado Compose. Las demás pantallas siguen pendientes de revisión individual en MMA-022.

## 2026-09-05 — Reinicio visual en formato de imagen

- A petición de Aina se descartan las direcciones visuales anteriores como referencia de trabajo. Los archivos permanecen únicamente como historial.
- Se cambia a exploración de una sola pantalla de apertura mediante generación de imagen de alta fidelidad. La imagen es un concepto visual, no una captura de la aplicación ni una implementación validada.
- Se conserva el alcance funcional: canciones accesibles al abrir, reproducción persistente y navegación Biblioteca · Buscar · Listas. Sin trabajo de metadatos.

## 2026-09-05 — Alternativa B de apertura para comparar estructura

- La revisión A de apertura no se considera validada: su composición se percibe igual al diseño previo.
- B propone una entrada a la colección con emblema felino, Ver canciones y Azar, conservando el reproductor inferior. Navega al listado en el prototipo y permite volver. Añade un toque para elegir una canción específica; no se declara superior sin evaluación.
- Dos previews B en `AperturaMichi.kt` y lámina `docs/mockups/apertura/coleccion-b.svg`; compilación debug Kotlin aprobada con JDK 21. Sin cambios productivos ni trabajo de metadatos.

## 2026-09-05 — Revisión pantalla a pantalla: apertura habitual

- Nueva propuesta independiente en `AperturaMichi.kt`: Biblioteca con jerarquía tipográfica, filas sin tarjetas, filtro local y reproductor inferior continuo. Cuatro previews para regreso pausado, reproducción, ausencia de escucha anterior y texto aumentado.
- Se toma provisionalmente la apertura habitual como escenario. Los destinos externos a esa pantalla están identificados como simulaciones de maqueta, sin diseñar nuevas capas en esta revisión.
- Lámina vectorial y PNG inspeccionada en `docs/mockups/apertura`; criterio, referencias, alcance y limitaciones en su README. El PNG es un render del SVG, no una captura Compose.
- `:app:compileDebugKotlin --offline` aprobado con JDK 21. Render de Compose y validación de interacción en dispositivo pendientes; app productiva y metadatos sin cambios.

## 2026-09-05 — Revisión de propuesta: continuidad del reproductor

- Se elimina la tarjeta superior Continuar escuchando de los mockups: la última canción pausada y la reproducción activa comparten el mini reproductor inferior en Biblioteca, Buscar y Listas.
- Se añade posición simulada compartida con Ahora suena y control compacto en Letra; seleccionar una canción nueva reinicia la posición. Primera apertura y biblioteca sin historial no inventan una selección.
- Catorce previews, incluidas Biblioteca sin escucha previa y Listas con escucha pausada. Se documentan referencias de Apple Music, Spotify y HIG, razonamiento de usuario y recorrido de revisión en `docs/PROPUESTA_MOCKUPS_UX.md`.
- Alcance: propuesta debug; la app productiva no se modifica. Compilación y revisión visual se registran en el documento de propuesta; MMA-021 y MMA-022 continúan pendientes.

## 2026-09-05 — Propuesta de mockups para revisión en Android Studio

- Doce previews Compose en `app/src/debug/java/com/ainalluna/michimusica/ui/proposal/MichiProposal.kt`: apertura, Biblioteca, Buscar, Listas, detalle, reproducción, letras, Medianoche, carpeta vacía y texto aumentado.
- Navegación interactiva simulada con datos de ejemplo; propuesta aislada en debug, sin alterar el comportamiento de la app instalada.
- Análisis y especificación en `docs/PROPUESTA_MOCKUPS_UX.md`; revisión visual e implementación pendientes en MMA-021 y MMA-022.
- Validación: `:app:compileDebugKotlin --offline` aprobado con JDK 21. El JBR 25 de Android Studio no inició correctamente este Gradle. No se entrega APK ni se da por verificado el renderizado de previews.

## 2026-09-05 — Michi Música 1.3.2: ancho correcto del menú

- Causa confirmada mediante captura del Pixel: al ocultar el mini reproductor, el `Column` del `bottomBar` medía la navegación por su ancho mínimo; las opciones se solapaban y Buscar quedaba invisible.
- Corrección: el contenedor de la barra inferior ocupa siempre el ancho completo, también en Ahora suena y Letra.
- Versión: `versionCode` 10 y `versionName` 1.3.2.
- Validación: `test lint assembleDebug` aprobado; instalada en el Pixel 7 y verificada mediante captura y jerarquía de UI con las tres opciones visibles a ancho completo.
- Artefacto: `app/build/outputs/apk/debug/Michi-Musica-1.3.2.apk`, 70.566.612 bytes, SHA-256 `662831A0EBED1254CB6270355CDFACB4EC042E86DBFFA1B9466908F2B9818B03`.

## 2026-09-05 — Michi Música 1.3.1: navegación persistente en reproducción

- Corrección: Ahora suena y Letra permanecen dentro del `Scaffold` principal, por lo que el menú **Música · Buscar · Listas** está visible y operativo también en ambas capas.
- Letra: el acceso **Ver letra** se sitúa inmediatamente bajo el título y el artista para que no pueda quedar desplazado fuera de pantalla.
- Previews: las variantes Rosa y Medianoche de Ahora suena muestran en Android Studio la misma navegación inferior que tendrá el teléfono.
- Versión: `versionCode` 9 y `versionName` 1.3.1.
- Validación: `test lint assembleDebug` aprobado; instalada y abierta correctamente en el Pixel 7 conservando los datos.
- Artefacto: `app/build/outputs/apk/debug/Michi-Musica-1.3.1.apk`, SHA-256 `6F2F76C3A8E446E5B98BB30AE2DE68637877799F6F76C95939DB1B2605408EE8`.

## 2026-09-04 — Michi Música 1.3.0: rediseño fiel al escritorio

- Navegación: la estructura definitiva pasa a **Música · Buscar · Listas**. Ahora suena y Letra son capas contextuales de la canción, no destinos principales.
- Inicio: preparar la cola ya no hace aparecer un reproductor falso. El mini reproductor solo se muestra después de elegir una pista, reanudarla o existir una sesión activa real.
- Continuidad: se guardan canción, posición y fuente de la cola. **Continuar** aparece en Música y recupera el punto exacto únicamente al tocarlo; abrir la app nunca inicia audio por sí solo.
- Listas: las playlists Markdown autorizadas se conservan como colección, muestran su resumen y pueden alternarse con toda la biblioteca sin copiar ni modificar canciones.
- Identidad: nueva traducción móvil del Michi de escritorio con superficies cálidas, ilustración felina, controles centrales, skins Rosa y Medianoche y lenguaje directo.
- Buscar: YouTube mantiene búsqueda interna, preescucha y guardado MP3, pero queda explicado como una fuente externa y separado del buscador local de Música.
- Regresión: se añadieron pruebas para los estados del mini reproductor y los límites de reanudación.
- Versión: `versionCode` 8 y `versionName` 1.3.0.
- Validación: `test lint assembleDebug` aprobado con JDK 21. La comprobación visual y funcional en el Pixel 7 continúa pendiente en `MMA-012`.
- Artefacto: `app/build/outputs/apk/debug/Michi-Musica-1.3.0.apk`, 70.566.904 bytes, SHA-256 `9D0980452D6290A31B4E9DF20F29A70ECB7CA086BD1E79B94465916CC6CB49C0`.

## 2026-09-04 — Reinicio del análisis UX Android

- Objetivo: descartar los mockups y decisiones visuales anteriores y reconstruir la propuesta desde las funciones reales de Michi Música.
- Limpieza: se eliminaron `docs/mockups/android`, el paquete `ui/mockups` y la propuesta UX anterior. La interfaz productiva se devolvió a su estado funcional previo; no queda un rediseño parcial.
- Análisis: se revisaron la aplicación principal de Windows, la implementación Android, la arquitectura Media3, playlists Markdown, YouTube, letras y guías actuales oficiales de Android y Apple.
- Hallazgo principal: el mini reproductor inicial aparece porque la cola preparada en el índice 0 se interpreta como una pista elegida; la futura UI debe separar ambos estados.
- Propuesta corregida tras validación con Aina: Música · Buscar · Listas como destinos; Ahora suena y Letra como capas contextuales. Se autoriza añadir Continuar escuchando, historial local y una colección real de playlists Markdown.
- Referencia: la interfaz de escritorio y sus mockups son la dirección visual; Android será una adaptación muy fiel, retirando solo controles propios de una ventana y delegando el volumen al sistema.
- Archivo: `docs/ANALISIS_UX_DESDE_CERO.md`.
- Verificación: `:app:compileDebugKotlin --offline` aprobado con JDK 21. No se generó una APK nueva porque esta fase no implementa comportamiento.

## 2026-09-04 — Michi Música 1.2.1: navegación siempre visible

- Corrección visual: se eliminan los mosaicos de carátula provisionales porque no representan portadas reales y ocupaban demasiado espacio.
- Ahora suena: la cabecera felina queda compacta y título, artista, progreso, controles y acceso a Letra permanecen dentro de la pantalla.
- Navegación: Biblioteca, Buscar y Letra son pantallas reales dentro del mismo `Scaffold`; dejan de abrir diálogos que ocultaban el menú inferior.
- Consistencia: la navegación inferior también permanece visible en Ahora suena.
- Versión: `versionCode` 7 y `versionName` 1.2.1.
- Validación: `test lint assembleDebug` aprobado; instalación pendiente porque el Pixel dejó de aparecer en ADB.

## 2026-09-04 — Michi Música 1.2.0: rediseño UX/UI móvil

- Arquitectura: la pantalla única se reorganiza en Biblioteca, Buscar y Letra mediante una navegación inferior estable.
- Reproducción: se añaden un mini reproductor persistente y una pantalla Ahora suena; ambas vistas comparten el mismo `MediaController` y no duplican la cola.
- Primera apertura: nuevo onboarding con una única acción para elegir carpeta, formatos compatibles y explicación de privacidad.
- Biblioteca: búsqueda, contador, fuente activa, relectura, carpeta y playlists quedan agrupados; las filas incorporan mosaicos de carátula provisionales, duración y estado de reproducción.
- Apariencia: Rosa Serena es el tema predeterminado y Medianoche Focus conserva la misma estructura desde Ajustes.
- Accesibilidad: controles frecuentes con áreas táctiles de 48 dp y descripciones semánticas para las acciones principales.
- Compatibilidad: se conserva Storage Access Framework, reproducción Media3 en segundo plano, Azar sin repeticiones, Repetir, letras, YouTube y descarga local.
- Versión: `versionCode` 6 y `versionName` 1.2.0.
- Validación: `test lint assembleDebug` aprobado; prueba visual, gestual y de funciones externas en Pixel 7 pendiente en `MMA-012`.

## 2026-09-04 — Michi Música 1.1.2: actualización de yt-dlp

- Causa: la APK inicializaba siempre el `yt-dlp` incluido en la dependencia 0.18.1, pero nunca ejecutaba el actualizador de la propia librería. Al superar 90 días, YouTube impedía resolver el audio para **Oír** y **MP3**.
- Resultado: antes de la primera preescucha o descarga se comprueba e instala el canal NIGHTLY de `yt-dlp`; las siguientes acciones reutilizan esa comprobación durante 24 horas.
- Robustez: un `Mutex` evita actualizaciones simultáneas y solo se guarda la fecha cuando la comprobación termina correctamente, por lo que un fallo de red permite reintentarlo.
- Regresión: `YouTubeRuntimeTest` cubre primera ejecución, ventana de 24 horas, vencimiento y cambio hacia atrás del reloj del dispositivo.
- Versión: `versionCode` 5 y `versionName` 1.1.2.
- Pruebas: `test lint assembleDebug --offline` aprobado; 20 pruebas debug sin fallos y lint sin errores bloqueantes.
- Artefacto: `app/build/outputs/apk/debug/Michi-Musica-1.1.2.apk`, 70.477.896 bytes, SHA-256 `0B59AC6318710B9EC496EEB51ABD268E1EBA7F9A723B4C3F0A6A169B4AFE14C3`.
- Verificación del manifiesto: paquete `com.ainalluna.michimusica`, versión 1.1.2 (código 5), mínimo API 26 y objetivo API 36.
- Validación pendiente: instalar en el Pixel y confirmar una preescucha y una descarga reales con conexión; se mantiene en `MMA-012`.

## 2026-09-04 — Michi Música 1.1.1: buscador de YouTube interno

- Objetivo: corregir la primera adaptación Android, que abría YouTube fuera de Michi y descargaba automáticamente el primer resultado en vez de reproducir el flujo del escritorio.
- Resultado: texto devuelve hasta ocho resultados dentro de la aplicación; un enlace directo devuelve solo su vídeo. Cada fila muestra título, canal y duración y ofrece **Oír/Pausa** y **MP3** para ese resultado concreto.
- Reproducción: la preescucha obtiene la URL temporal con `yt-dlp`, pausa la biblioteca local y usa un `ExoPlayer` independiente que se libera al cerrar el panel.
- Descarga: el botón pasa únicamente un identificador de vídeo validado al descargador; se mantiene conversión MP3, progreso y relectura automática de la biblioteca.
- Regresión: `YouTubeInputTest` cubre texto, enlaces cortos, Music, Shorts, dominio externo y rechazo de listas/canales.
- Versión: `versionCode` 4 y `versionName` 1.1.1.
- Pruebas: construcción limpia `clean test lint assembleDebug` aprobada, con 79 tareas ejecutadas y 0 errores de lint.
- Artefacto: `app/build/outputs/apk/debug/Michi-Musica-1.1.1.apk`, 70.475.708 bytes, SHA-256 `3EF1D801F909EE82BDD00E5C2F27C63853CB55DF0AE51F1A77C94D828D886BF8`.
- Entrega: copiada por MTP a `Download` del Pixel 7 y recuperada después para verificar exactamente tamaño y SHA-256 frente al artefacto original.
- Limitación: búsqueda, preescucha y descarga reales continúan pendientes de validación manual en el Pixel dentro de `MMA-012`.

## 2026-09-04 — Michi Música 1.1.0: paridad funcional móvil

- Objetivo: incorporar búsqueda en la lista y trasladar al Pixel las playlists, metadatos, letras y funciones de YouTube de la aplicación de escritorio.
- Biblioteca: filtro por título, artista, álbum o archivo sin distinguir mayúsculas ni acentos; lectura de metadatos y selector de playlists Markdown con vuelta a toda la música.
- Letras: búsqueda en LRCLIB, resultados sincronizados o de texto, guardado y retirada en la subcarpeta `Michi Letras`, seguimiento de la línea actual y límites de 300 KB.
- YouTube: búsqueda o enlace directo, apertura externa y descarga/conversión MP3 local con `youtubedl-android` 0.18.1, QuickJS y FFmpeg. El resultado se copia a la carpeta autorizada y fuerza un nuevo escaneo.
- Seguridad y privacidad: hosts de enlace cerrados a YouTube, `--no-playlist`, trabajo temporal en caché privada, URI de Storage Access Framework y ninguna credencial o ruta de perfil incorporada.
- Regresión: nuevas pruebas para playlists Markdown, parser LRC y entradas de YouTube; se mantienen las de formatos, azar y sincronización de cola.
- Versión: `versionCode` 3 y `versionName` 1.1.0; binarios nativos limitados a ARM64 para el Pixel 7.
- Pruebas: `clean test lint assembleDebug` terminó correctamente tras descargar dependencias y una segunda ejecución offline `test lint assembleDebug` también terminó correctamente. Lint: 0 errores, 12 avisos y 1 sugerencia no bloqueantes.
- Artefacto: `app/build/outputs/apk/debug/Michi-Musica-1.1.0.apk`, 70.451.624 bytes, SHA-256 `B84849F3493FB988B52E6B6D67489F91187F0F8EA28BC33A7C642663B6387B01`.
- Entrega al dispositivo: copiada por MTP a `Download` del Pixel 7 y recuperada después a una carpeta temporal; tamaño y SHA-256 coinciden exactamente con el artefacto original.
- Limitaciones reales: falta probar en el Pixel la interfaz, la consulta LRCLIB, la escritura SAF y una descarga MP3 autorizada; ADB continúa sin autorización. Las carátulas siguen pendientes en `MMA-005`.
- Archivos relevantes: `MainActivity.kt`, `library/*`, `lyrics/*`, `youtube/*`, `AndroidManifest.xml`, `app/build.gradle.kts`, `gradle.properties`, pruebas, README y documentación.

## 2026-09-04 — Michi Música 1.0.1: cola sincronizada al cambiar de lista

- Objetivo: corregir el bloqueo observado en el Pixel cuando se cambiaba de lista y Media3 conservaba la cola anterior.
- Causa: la cola solo se cargaba cuando `mediaItemCount` era cero; volver a seleccionar la misma carpeta tampoco repetía el escaneo porque el URI no cambiaba.
- Resultado: la biblioteca y la cola se comparan por ID y se resincronizan cuando cambia el contenido o el orden. Se conserva canción, posición y reproducción cuando la pista actual sigue presente; elegir de nuevo la misma carpeta fuerza una lectura nueva.
- Regresión: `PlaylistSyncTest` cubre canciones añadidas, cola ya sincronizada y cambio de orden.
- Versión: `versionCode` 2 y `versionName` 1.0.1.
- Pruebas: `clean test lint assembleDebug` terminó correctamente y sin red, con 79 tareas ejecutadas.
- Artefacto: `app/build/outputs/apk/debug/Michi-Musica-1.0.1.apk`, 15.024.395 bytes, SHA-256 `0F77B5683F8EE27035A65D7CCC3F64C399B48BBCF261C1EFA98505A08ACF9242`.
- Entrega al dispositivo: la carpeta local de música se copió a `Almacenamiento interno compartido/musica` con 112 archivos y 609.307.179 bytes; la APK 1.0.1 se copió a `Download` y se recuperó para verificar el mismo SHA-256.
- Limitación: ADB continúa sin autorización, por lo que la instalación de 1.0.1 y la prueba manual del cambio de lista deben realizarse desde el Pixel.
- Archivos: `app/build.gradle.kts`, `MainActivity.kt`, `PlaylistSyncTest.kt`, `README.md`, `docs/ARQUITECTURA.md`, `docs/BACKLOG.md` y este historial.

## 2026-09-03 — Primera APK de desarrollo validada

- Objetivo: completar `MMA-007` y convertir el MVP Android incompleto en un proyecto compilable.
- Compatibilidad: se fijaron `core-ktx` 1.17.0 y `lifecycle-runtime-compose` 2.9.4 para mantener SDK 36 y Android Gradle Plugin 8.13.2; las versiones anteriores exigían SDK 37 y el plugin 9.1.
- Código: se añadió el import ausente de `setContent` y se sustituyó `Context.mainExecutor`, disponible desde API 28, por `ContextCompat.getMainExecutor` para respetar el mínimo API 26.
- Pruebas: `clean test lint assembleDebug` terminó correctamente y sin red, con 79 tareas ejecutadas. Lint informó 0 errores y 7 advertencias no bloqueantes.
- Artefacto: `app/build/outputs/apk/debug/app-debug.apk`, 15.024.395 bytes, SHA-256 `296CB1522FD4D05D10912E2B66C18FA1465A515396B1BF8B43AB270E1D6550F1`.
- Inspección: paquete `com.ainalluna.michimusica`, versión `1.0.0`, mínimo API 26 y objetivo API 36.
- Limitación: Windows y `adb devices -l` todavía no detectaron el Pixel conectado, por lo que no se instaló ni se probó reproducción real, segundo plano, bloqueo o Bluetooth. Continúa pendiente `MMA-002`.
- Archivos: `app/build.gradle.kts`, `MainActivity.kt`, `README.md`, `docs/ARQUITECTURA.md`, `docs/BACKLOG.md` y este historial.

## 2026-09-03 — Primer intento de validación del MVP

- Objetivo: comprobar el proyecto existente con pruebas unitarias, lint y compilación de la APK de desarrollo.
- Entorno comprobado: Android SDK Platform 36 y Build Tools 35.0.0 disponibles; Gradle arrancó correctamente con JDK 17 y una caché temporal, sin introducir rutas de perfil en el proyecto.
- Resultado: la ejecución alcanzó `:app:checkDebugAarMetadata` durante la descarga inicial de dependencias y se interrumpió a petición de la usuaria por la lentitud de la red.
- Estado real: no se ejecutaron completamente `test` ni `lint`, no se compiló el código de la aplicación y no existe `app/build/outputs/apk/debug/app-debug.apk`.
- Continuación: completar `MMA-007`, registrar cualquier corrección necesaria y, después, realizar `MMA-002` en un Pixel 7 real.
- Archivos: `README.md`, `docs/BACKLOG.md` y este historial. No se modificó código fuente.

## 2026-09-03 — MVP Android nativo

- Objetivo: crear un repositorio Android independiente para usar Michi Música en un Pixel 7 actualizado.
- Resultado: proyecto Kotlin/Compose con selector persistente de carpeta, biblioteca local, Media3 en segundo plano, controles del sistema, progreso, navegación, Azar, Repetir y skins Rosa/Medianoche.
- Privacidad: se usan URI concedidos por Android; no hay subida de audio, analítica, credenciales ni permiso general de almacenamiento.
- Pruebas: se añadieron unitarias para formatos admitidos y barajado seguro. La compilación, lint y prueba en dispositivo deben registrarse al ejecutarse.
- Limitaciones: no incluye todavía playlists Markdown, letras, metadatos completos ni búsqueda/descarga desde YouTube.
