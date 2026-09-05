# Privacidad

Michi Música no tiene servidor propio, cuentas, analítica ni publicidad propia. No sube tus archivos de audio a un servicio de Michi. La reproducción local, las listas y las letras guardadas funcionan sin conexión.

## Permisos y datos locales

- Android permite elegir una carpeta mediante su selector de documentos. Michi conserva acceso a esa carpeta: lee audio y carátulas y, cuando lo pides, guarda MP3 o letras. El proveedor de documentos que elijas tiene sus propias condiciones.
- Carpeta, listas y última escucha se guardan en preferencias privadas. La copia automática de datos de la app está desactivada.
- Reproducción en segundo plano utiliza un servicio multimedia. El sistema, Bluetooth y los controladores que Android considera de confianza pueden acceder a la sesión multimedia.
- Quitar una letra afecta a su archivo asociado. Desinstalar la app no borra los audios ni las letras guardadas en la carpeta elegida.

## Conexiones externas bajo petición

| Acción | Destino y datos necesarios |
| --- | --- |
| Buscar o ver vídeos | YouTube/Google recibe la consulta, el vídeo solicitado, la IP y los datos propios de su reproductor/WebView. Puede mostrar anuncios o aplicar restricciones. |
| Mostrar miniaturas | `i.ytimg.com` recibe el identificador público del vídeo y la IP. |
| Buscar letras | LRCLIB recibe el título y artista escritos para la consulta y la IP. |
| Guardar MP3 | YouTube y sus servidores de medios reciben la petición del vídeo seleccionado. |
| Actualizar el motor de descarga | GitHub y sus servidores de descarga reciben la consulta de versión y la petición de yt-dlp NIGHTLY. |

El vídeo utiliza el reproductor oficial de YouTube. WebView puede mantener caché y almacenamiento web, sujetos a las políticas de Google. Ver un vídeo no añade un MP3 a tu carpeta. No se solicitan credenciales de Google en Michi.

Michi Iconos es una APK independiente sin permisos de red ni acceso a tus archivos; contiene tres imágenes y un selector para launchers.

## Clasificación y progreso de podcasts

Música/Podcasts, la última categoría elegida al guardar y la posición de cada episodio se conservan únicamente en las preferencias privadas de la app. No se suben ni se escriben en las etiquetas del audio. Una actualización conserva estos datos; desinstalar o borrar datos de Michi los elimina. Mover o renombrar archivos puede requerir reclasificarlos porque Android puede cambiar su URI.
