# Backlog

Última revisión: **2026-09-06**.

## Pendiente

| ID | Prioridad | Trabajo | Resultado esperado |
| --- | --- | --- | --- |
| MMA-002 | P1 | Probar en un Pixel 7 real | Confirmar selector de carpeta, formatos, segundo plano, bloqueo y Bluetooth. |
| MMA-005 | P2 | Verificar las carátulas existentes en el Pixel | Lectura asíncrona, muestreo, caché y marcador felino implementados; comprobar archivos con y sin imagen incrustada. No incluye edición de metadatos. |
| MMA-006 | P3 | Valorar publicación en Play Store | La publicación solicitada es GitHub con APK directa. Play Console, sus requisitos y pruebas quedan fuera de esta entrega. |
| MMA-012 | P1 | Validar las funciones 1.1.1 en Pixel 7 | Probar búsqueda local, playlist real, LRCLIB, resultados/preescucha de YouTube, escritura/quitar letra y una descarga MP3 autorizada. |
| MMA-020 | P2 | Ampliar Continuar con historial reciente | Mantener hasta 20 pistas escuchadas con su última posición, además de la reanudación principal ya implementada. |
| MMA-022 | P2 | Revisar las siguientes pantallas individualmente | Biblioteca, Buscar, Listas y reproductor aprobados por Aina. Letra rediseñada en 1.8.0 para revisión; no dar por aprobada hasta que Aina la revise. |
| MMA-023 | P1 | Completar la validación de portada 1.4.0 en Android Studio y Pixel | Instalada en Pixel 7: capturas Rosa/Medianoche, carátulas reales y navegación con mini reproductor pausado estable comprobadas. Pendientes `LibraryHomePreviews.kt`, reanudación y controles de audio, fuente Markdown, Azar, teclado, texto grande, TalkBack y pantalla bloqueada. |
| MMA-024 | P2 | Completar validación de Buscar 1.5.0 | Diseño aprobado por Aina. Entrada, resultados y preescucha revisados en Pixel; consulta conservada al cambiar pestaña. Pendientes TalkBack, texto aumentado y pruebas de fallo de red/permisos y guardado. No se promete continuidad de descargas al destruir la actividad. |
| MMA-025 | P2 | Completar validación de Listas 1.6.0 | Diseño aprobado por Aina. Instalada en Pixel: captura de colección con carátulas y apertura de lista de 17 canciones verificadas. Pendientes selección al regresar, TalkBack, texto aumentado e importación nueva con referencias ausentes. |
| MMA-026 | P2 | Recuperar listas inaccesibles | El importador actual omite resúmenes que no puede leer o resolver. Diseñar la recuperación de acceso y el tratamiento de listas sin canciones disponibles. |
| MMA-027 | P2 | Completar validación del reproductor | Diseño 1.7 aprobado. Siguiente en Azar corregido en 1.8 y cubierto por pruebas de ronda. Pendientes batería táctil de controles, arrastre, fuentes grandes, TalkBack, errores de audio, bloqueo y Bluetooth. |
| MMA-028 | P1 | Revisar Letra y Siguiente en Azar 1.8 en Pixel | Se conservan en 1.9. ADB recuperado; falta prueba nativa. Retomar por `docs/CONTINUIDAD.md`. Verificar lectura sincronizada, interrupción por arrastre y Seguir canción, salto por verso, búsqueda/vista previa, escritura y quitar con archivos de prueba. Revisar también Siguiente en ambos controles con Azar, incluido final de ronda. |
| MMA-029 | P1 | Completar regresión de vídeo integrado | 1.9.1 instalada y vídeo visible en Buscar tras corregir iframe de altura cero. Pantalla completa/retorno observados en diagnóstico con corrección en vivo; falta batería completa en APK final: seek, orientación, pausa por pestaña/segundo plano, exclusión con audio local, fallos de red/inserción y accesibilidad. |

| MMA-030 | P1 | Fortalecer actualizaciones de yt-dlp | Verificar artefactos con firma/digest autenticado y estudiar canal estable/retroceso; el actualizador upstream 0.18.1 usa HTTPS pero no verifica firma independiente. |
| MMA-031 | P2 | Ampliar validación de publicación | Probar dispositivos ARM64 adicionales, controladores del sistema tras endurecimiento y reproducción/descargas con fallos. Revisar fuentes de componentes nativos y automatizar análisis de dependencias. |

| MMA-033 | P2 | Activar CI en GitHub | Copiar `docs/ci/android.yml` a `.github/workflows/android.yml` con una conexión autorizada para workflows y verificar su primera ejecución. La conexión inicial no tiene ese alcance. |

| MMA-035 | P2 | Ampliar validación de borrado | En Pixel: Cancelar conserva archivo; borrar audio de prueba en reproducción lo elimina y deja siguiente pista pausada; 110 canciones originales conservadas. Pendientes errores/permisos de proveedores, no actual mientras suena otra, última pista y listas repetidas en dispositivo. Cola con duplicados/vacía cubierta por pruebas unitarias. |
| MMA-037 | P2 | Ampliar validación de podcasts | 1.11.0 final: clasificación reversible, navegación sin cambiar cola, reanudación por episodio/reinicio, segundo plano, avance automático, finalizado y descargas a ambos destinos comprobados en Pixel. Corregida y probada reanudación al borrar. 51 pruebas por variante. Pendientes listas Markdown mixtas en dispositivo, errores de proveedores/red, Bluetooth y otros móviles. No hay importación recursiva ni recuperación del catálogo tras desinstalar. |

## Hecho

| ID | Fecha | Trabajo | Verificación |
| --- | --- | --- | --- |
| MMA-040 | 2026-09-06 | Canales de YouTube y orden por última publicación | Canal real resuelto, cancelación/reintento y MP3 de 60:45 reproducido en Pixel; regresión RSS completa. 89 pruebas por variante, lint sin errores. Debug 1.13 instalada preservando datos; ocho programas ordenados y Novedades de 72 horas observados. Ficha PODCASTS_YOUTUBE_1_13.md. |
| MMA-039 | 2026-09-06 | Cerrar la validación de Podcasts RSS 1.12 | Batería nativa aislada completa: cancelación, reintento, interrupción del proceso, cortes HTTP/fallos SAF controlados y aviso que abre Novedades sin duplicados. Debug final instalada conservando preferencias y catálogo; ventana de 72 horas revisada con tres episodios reales. 82 pruebas por variante, lint sin errores y APK oficial firmada publicada como v1.12.0 tras integrar el PR #1. Método y límites en VALIDACION_PODCASTS.md. |
| MMA-040 | 2026-09-06 | Añadir capturas de Podcasts RSS a GitHub | Tres PNG reales de Pixel 1.12.0 revisados y enlazados en README y ficha: Siguiendo Medianoche, Siguiendo Rosa y episodios/descargas Rosa al 130 %. Copias idénticas a los originales seleccionados, referencias comprobadas; sin cambios de app. |
| MMA-038 | 2026-09-06 | Implementar seguimiento RSS público y descargas manuales | Código 1.12.0: programas, novedades, actualización/avisos y descarga con filtros de pago/recortes. 80 pruebas por variante y lint sin errores. Datos reales de iVoox: 209 entradas admitidas, 41 excluidas y recorte detectado. APK debug instalada en Pixel; recorridos principales y diseño verificados con datos reales. Pruebas adicionales en MMA-039. |
| MMA-036 | 2026-09-05 | Separar Música y Podcasts | Aprobado por Aina e implementado en 1.11.0: secciones, destino MP3, reclasificación, colas independientes de la navegación y posición por episodio. 51 pruebas por variante y validación nativa de clasificación, colas, progreso, borrado y descargas en el historial. |
| MMA-034 | 2026-09-05 | Publicar comunidad y revisar seguridad | Repositorio público y release v1.10.0 con APK firmadas, capturas reales autorizadas, IA explícita, GPL, privacidad, contribución y fuentes. 40 pruebas por variante, lint sin errores; límites de revisión documentados y CI pendiente en MMA-033. |
| MMA-032 | 2026-09-05 | Icono adaptativo y paquete Niagara | Ambas APK compiladas y firmadas; paquete sin permisos instalado, tres variantes reconocidas por Niagara y Medianoche aplicada a Michi. |
| MMA-021 | 2026-09-05 | Aprobar la nueva dirección visual de apertura | Aina aprueba `docs/mockups/reinicio/01-apertura-medianoche.png` y pide crear la portada. Las propuestas previas quedan como historial. |
| MMA-019 | 2026-09-04 | Recordar varias playlists Markdown | Colección persistente de URI autorizadas, resumen, selección activa y vuelta a toda la música implementados; `test lint assembleDebug` aprobado en 1.3.0. |
| MMA-018 | 2026-09-04 | Recordar y reanudar la escucha | Canción, fuente y posición se guardan; Continuar retoma bajo petición sin reproducción automática; pruebas de estados límite aprobadas. |
| MMA-016 | 2026-09-04 | Implementar la arquitectura UX aprobada | Música · Buscar · Listas, Ahora suena/Letra contextuales, mini reproductor solo tras interacción y estética Rosa/Medianoche implementados; validación automática completa. |
| MMA-017 | 2026-09-04 | Reiniciar la propuesta UX desde el producto real | Mockups anteriores eliminados; escritorio, Android y guías de plataforma auditados. Dirección corregida después para permitir funciones nuevas y adoptar el escritorio como referencia visual. |
| MMA-015 | 2026-09-04 | Implementar el rediseño UX/UI móvil | Onboarding, Biblioteca, navegación inferior persistente, mini reproductor, Ahora suena sin falsas carátulas y selector de skins implementados; `test lint assembleDebug` aprobado en 1.2.1. Validación visual y táctil en Pixel 7 pendiente en `MMA-012`. |
| MMA-014 | 2026-09-04 | Mantener actualizado `yt-dlp` antes de preescuchar o descargar | Actualización NIGHTLY integrada, serializada y limitada a una comprobación correcta cada 24 horas; `test lint assembleDebug` aprobado. |
| MMA-013 | 2026-09-04 | Igualar en Android el buscador de YouTube del escritorio | Hasta ocho resultados internos con título/canal/duración, Oír/Pausa y MP3 por resultado; enlaces directos limitados a un vídeo. Compilación aprobada; prueba real pendiente en `MMA-012`. |
| MMA-011 | 2026-09-04 | Buscar/abrir YouTube y descargar MP3 en Android | Entrada validada, `yt-dlp` 0.18.1 y FFmpeg integrados para ARM64; compilación aprobada. Descarga real pendiente en `MMA-012`. |
| MMA-010 | 2026-09-04 | Buscar dentro de la lista activa y leer metadatos | Filtrado sin distinguir mayúsculas ni acentos; título, artista, álbum y duración obtenidos sin alterar los archivos. |
| MMA-004 | 2026-09-04 | Buscar, asociar, sincronizar y quitar letras | Parser LRC probado; almacenamiento en `Michi Letras` y cliente LRCLIB compilados. API/escritura real pendientes en `MMA-012`. |
| MMA-003 | 2026-09-04 | Importar playlists Markdown | Parser probado con orden, rutas relativas, nombres sin extensión, ausentes y repetidas; cola sincronizada con la selección. |
| MMA-008 | 2026-09-04 | Sincronizar la cola de reproducción al cambiar o recargar la carpeta | Pruebas unitarias de cambio, igualdad y reordenación; `clean test lint assembleDebug` aprobado. APK 1.0.1 copiada al Pixel y verificada por hash; validación funcional manual pendiente en `MMA-002`. |
| MMA-007 | 2026-09-03 | Completar la validación del MVP y generar la primera APK | `clean test lint assembleDebug` aprobado sin red; APK y manifiesto inspeccionados. Instalación real pendiente en `MMA-002`. |
| MMA-001 | 2026-09-03 | Crear el código fuente del MVP Android nativo de Michi Música. | Estructura y pruebas unitarias creadas; compilación validada posteriormente en `MMA-007`. |

## Seguimiento tras 1.11.1

- Niagara: Play/Pausa/Anterior/Siguiente comprobados sobre sesión activa y pausada. Pendiente ampliar a Bluetooth y reanudación del sistema después de destruir el servicio/reiniciar Android; no equiparar estos escenarios con una sesión activa.
- Caché de biblioteca: ampliar proveedores SAF (remotos, permisos revocados y proveedores sin fecha/tamaño). Sin marcas fiables se releen las etiquetas; «Releer música» fuerza también los archivos aparentemente iguales. No se añade exploración recursiva de subcarpetas.
