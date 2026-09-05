# Revisión de seguridad — publicación 1.10

Fecha: 2026-09-05. Revisión estática asistida por IA, pruebas unitarias, lint y comprobaciones de APK. No se presenta como pentest ni auditoría independiente de terceros.

## Hallazgos corregidos

| Hallazgo | Impacto | Cambio |
| --- | --- | --- |
| Letras y listas se leían completas antes de comprobar su tamaño | Un proveedor o respuesta excesiva podía agotar memoria | `readBoundedText` limita durante la lectura: 300.000 caracteres para letras/JSON y 1.000.000 para Markdown. Test con proveedor de longitud infinita. |
| Una descarga eliminaba el MP3 del mismo nombre antes de copiar el nuevo | Posible pérdida del archivo existente si la copia fallaba | Se conserva el existente y se elige un nombre con sufijo libre. Solo se elimina un documento incompleto recién creado por esa operación. |
| Descargas compartían una carpeta temporal que se limpiaba al empezar | Riesgo de interferir con trabajo de otra sesión | Carpeta privada UUID por operación y limpieza en `finally`. |
| El servicio devolvía la sesión a cualquier controlador | Aplicaciones no confiables podían intentar controlar la sesión o consultar su cola | Solo mismo UID o controladores que Media3/Android considera de confianza. Pendiente ampliar prueba Bluetooth y notificaciones tras el cambio. |
| Copia de datos de aplicación permitida | Preferencias e historial podían entrar en copias gestionadas por Android | `allowBackup=false`. Los archivos de la carpeta elegida siguen bajo el proveedor del usuario. |
| Política de tráfico HTTP implícita | Dependía del valor por defecto de la plataforma | `usesCleartextTraffic=false` explícito. |

## Controles revisados

- Selector SAF con acceso persistente a la carpeta elegida; sin permiso general para todo el almacenamiento ni subida de audio local.
- IDs de vídeo de 11 caracteres permitidos, enlaces con hosts cerrados, consultas limitadas y argumentos de yt-dlp separados. No se ejecuta texto de búsqueda como una orden de shell.
- WebView sin acceso `file://`/contenido local, sin contenido mixto y con HTML generado sin interpolar títulos o consultas. Puente JavaScript limitado a eventos y valores numéricos de reproducción; no expone archivos, tokens ni intents.
- Miniaturas con límite de bytes durante la lectura, tiempos de espera y caché acotada. La lectura de carátulas usa muestreo y concurrencia limitada.
- Firma de publicación RSA de 3072 bits. Secretos externos al repositorio y acceso local restringido. Clones y CI no necesitan esa clave para compilar.
- Plantilla CI con permisos de lectura y acciones fijadas a SHA (pendiente de activar por falta de alcance OAuth `workflow`); escaneo del contenido preparado para Git para evitar claves, rutas de perfil y evidencias privadas.

## Riesgos y límites que permanecen

- YouTube, LRCLIB, WebView, Media3, Python, FFmpeg y yt-dlp son dependencias externas. No se ha auditado línea a línea su código ni los binarios nativos. No afirmar que se ha completado un escaneo de todas las CVE.
- Se conserva la actualización NIGHTLY de yt-dlp solicitada previamente. La biblioteca 0.18.1 descarga desde releases de GitHub por HTTPS; su actualizador no verifica una firma independiente del artefacto. Se confía en GitHub y el proyecto upstream. La actualización incorpora código nuevo fuera del ciclo de firma de la APK; fortalecer su verificación queda en el backlog.
- El servicio admite controladores de confianza del sistema, incluidos los autorizados por Android; no supone que únicamente la interfaz de Michi pueda consultar/controlar la reproducción.
- Escribir letras del mismo formato no es una transacción atómica en todos los proveedores SAF. Revisión ampliada de errores de escritura pendiente.
- Los límites de letras/listas no hacen inocuo cualquier archivo multimedia malformado; mantener Android y los decodificadores actualizados.
- Pruebas manuales ampliadas de red/permisos, TalkBack, grandes bibliotecas, Bluetooth y restauración siguen en el backlog. Los resultados efectivamente completados figuran en el historial de cambios.

## Cómo repetir la revisión

Ejecutar `./gradlew test lint assembleDebug assembleRelease` con JDK 21 y SDK 36. Consultar los informes en `app/build/reports/` y `iconpack/build/reports/`. Verificar las APK con `apksigner verify --verbose --print-certs`, revisar manifiestos y comprobar que la release no contiene `ShowcaseActivity` ni está marcada como depurable. La firma requiere configuración privada solo para la APK oficial; sin ella, release se genera sin firmar.
