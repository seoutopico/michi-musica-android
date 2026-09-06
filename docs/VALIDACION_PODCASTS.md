# Regresión nativa de Podcasts

La variante `validation` instala **com.ainalluna.michimusica.validation**, con datos, carpeta privada y permisos separados de Michi. No sustituye la aplicación personal. Sus componentes solo existen en `app/src/validation/` y no se empaquetan en debug normal ni release.

La actividad ejecuta el repositorio, parser, servicios, MediaMetadataRetriever, DocumentFile y JobScheduler de producción. Un manejador HTTPS del proceso sirve respuestas controladas y audio WAV sintetizado. Un DocumentsProvider de validación, protegido por el permiso de sistema MANAGE_DOCUMENTS y limitado a sus archivos privados, permite provocar fallos de acceso y creación. Android exige declararlo exportado; no anuncia una raíz en el selector ni concede acceso a otras aplicaciones. No se añaden puntos de inyección ni excepciones a las reglas de red de producción. El host `example.com` se resuelve para pasar la comprobación DNS real, pero los cuerpos HTTP de esta batería son simulados; las descargas HTTPS reales se validaron separadamente con NASA e iVoox.

## Ejecución

Con JDK 21, SDK Android instalado y un dispositivo autorizado y desbloqueado:

```powershell
.\gradlew.bat :app:assembleValidation --offline
.\scripts\validate_podcasts.ps1
```

El script instala la variante aislada y borra únicamente los datos previos de **esa variante de validación**. No elimina datos de `com.ainalluna.michimusica`, ni solicita acceso a la biblioteca personal. Concede las notificaciones a la variante aislada para comprobar el aviso del servicio.

## Casos y evidencia

- Primera importación sin marcar todo el histórico como nuevo.
- Cancelar mientras llegan bytes y reintentar; comprobar contenido WAV exacto y clasificación como Podcast.
- Cancelación después de terminar y durante la finalización: conservar documento y registro completados.
- Cancelar el segundo elemento de la cola sin detener el primero.
- Corte de conexión durante el cuerpo HTTP y reintento.
- Proveedor SAF que deniega acceso o falla al crear por falta de espacio; recuperación sin exponer `.part` como audio.
- Terminar el proceso durante una transferencia y reabrir: registro de interrupción y reintento completo.
- Incorporar una entrada al feed controlado, ejecutar el trabajo real de Android y comprobar el aviso de exactamente un episodio nuevo; su PendingIntent abre Novedades.
- Actualizar de nuevo sin duplicar avisos, marcar como visto y desactivar el trabajo.

Resultados y árbol de la pantalla en `tmp/podcast-regression/`. Un archivo fuente de pruebas o una APK compilada no demuestran que esta batería haya pasado: registrar la ejecución y sus resultados en HISTORIAL_CAMBIOS. La simulación del proveedor no equivale a llenar el almacenamiento del móvil ni revocar la carpeta del usuario. La ejecución forzada del trabajo no certifica que Android lo ejecute puntualmente bajo Doze; la app indica que la frecuencia es aproximada.

El límite de 72 horas se prueba además con reloj fijo en `PodcastFeedTest`: exactamente 72 horas, un milisegundo después, fechas futuras/ausentes, descubrimiento tardío de archivo y caducidad de marcas. La interfaz recalcula el tiempo al volver a primer plano y al caducar un episodio.
