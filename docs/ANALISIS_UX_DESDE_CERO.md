# Dirección UX — Michi Música Android

**Documento histórico, sustituido por [Continuidad](CONTINUIDAD.md) y las fichas de pantalla 1.4–1.8.** La tarjeta superior Continuar fue descartada; se usa el mini reproductor inferior persistente. El historial de hasta 20 escuchas sigue pendiente (MMA-020). Las intenciones descritas aquí no equivalen a funciones implementadas ni a requisitos visuales vigentes.

Fecha: 2026-09-04  
Estado: dirección de producto acordada; pendiente de reconstruir mockups e implementar.

## Visión

Michi Android debe sentirse como la versión móvil de Michi Música de escritorio, no como un reproductor Material genérico. Se conservarán su carácter felino, su jerarquía musical, los temas Rosa y Medianoche, el protagonismo del control central y el lenguaje sencillo.

La adaptación no será una copia literal de una ventana de Windows. Android no necesita botones de minimizar/cerrar, deslizador de volumen ni una carcasa física alrededor de la pantalla. El resto del lenguaje visual puede trasladarse con mucha fidelidad.

El paquete permanece `com.ainalluna.michimusica`.

## Producto actual que se conserva

- Carpeta local mediante Storage Access Framework.
- MP3, WAV, OGG, M4A, AAC, FLAC y OPUS.
- Media3 en segundo plano, pantalla bloqueada, auriculares y Bluetooth.
- Anterior, Play/Pausa, siguiente, progreso, Azar sin repetición y Repetir una pista.
- Búsqueda local por título, artista, álbum o archivo.
- Playlists Markdown que referencian audios sin copiarlos.
- Búsqueda de YouTube o enlace directo, hasta ocho resultados, preescucha y descarga MP3.
- Letras LRCLIB, `.lrc`/`.txt`, seguimiento sincronizado y retirada sin modificar el audio.
- Temas Rosa y Medianoche.

## Funciones nuevas aceptadas

### Continuar escuchando

Michi guardará:

- identificador de la canción;
- fuente de la cola: toda la música o playlist;
- posición exacta;
- duración conocida;
- momento de la última escucha.

Al abrir la app de nuevo:

- no empezará a sonar automáticamente;
- mostrará una tarjeta `Continuar escuchando`;
- tocarla restaurará canción, cola y minuto;
- si la canción ya no existe, se retirará ese estado y se explicará con claridad;
- si Media3 continúa reproduciendo realmente en segundo plano, se mostrará el mini reproductor porque la sesión sigue activa.

### Último escuchado

Se conservará una lista local y privada de pistas reproducidas recientemente. No se sube, no se comparte y puede limitarse a las últimas 20 entradas. Tocar una entrada podrá comenzar desde el principio o retomar su última posición cuando tenga sentido.

### Colección de playlists

La implementación actual recuerda una sola playlist Markdown activa. La nueva versión podrá recordar varias URI autorizadas y mostrarlas como una colección:

- nombre obtenido del encabezado Markdown;
- número de canciones válidas;
- ausentes y duplicadas;
- última utilización;
- selección activa.

Las listas seguirán siendo archivos externos: Michi no copiará audio ni alterará su contenido.

## Arquitectura principal

La navegación inferior será:

### Música

Es la entrada a la aplicación y el equivalente móvil del núcleo del Michi de escritorio.

- Sin historial: muestra Biblioteca directamente.
- Con estado anterior: muestra `Continuar escuchando` en la cabecera, seguido de Biblioteca.
- Campo `Buscar en tu música`.
- Lista de canciones locales.
- Acceso a carpeta, relectura y apariencia desde el menú superior.
- Indicador de la fuente activa cuando procede de una playlist.

No aparece un mini reproductor solo porque Media3 haya preparado la canción 0. Únicamente aparece tras una selección real o mientras existe reproducción activa.

### Buscar

Esta sección corresponde exactamente al buscador de YouTube del escritorio.

- Título `Buscar música`.
- Texto `Busca por nombre o pega un enlace de YouTube`.
- Resultados con miniatura, título, canal y duración.
- Acciones separadas `Oír` y `MP3`/`Guardar`.
- Una única preescucha activa, que pausa la música local.
- Descarga con progreso persistente aunque se cambie de sección.
- Al terminar, la canción aparece en Música.

La búsqueda local permanece dentro de Música para que ambas búsquedas no se confundan.

### Listas

- `Toda la música` como fuente dinámica.
- Playlists Markdown recordadas.
- `Abrir playlist .md` para añadir otra.
- Estado activo inequívoco.
- Detalle con canciones válidas y aviso de ausencias.
- Tocar una lista la activa y abre Música con esa selección.

### Ahora suena

No es una pestaña: es una capa completa que se abre desde `Continuar escuchando`, una canción o el mini reproductor.

- Título, artista/archivo, progreso y tiempos.
- Anterior, Play/Pausa y siguiente como núcleo.
- Azar, Repetir y Detener como controles secundarios heredados de escritorio.
- Acceso a Letra.
- Atrás o gesto descendente vuelve sin detener la música.
- Sin deslizador de volumen: Android ya dispone de botones físicos y controles del sistema.

### Letra

Es una capa contextual de Ahora suena:

- primero muestra la letra guardada;
- si no existe, ofrece buscarla;
- título y artista se pueden corregir;
- la persona elige manualmente la coincidencia;
- `Quitar` requiere confirmación y nunca modifica el audio.

## Secuencia de apertura

### Primera vez

1. Bienvenida Michi.
2. `Elegir carpeta`.
3. Explicación de privacidad y formatos.
4. Escaneo.
5. Música, sin reproducción ni mini reproductor.

### Apertura posterior sin reproducción activa

1. Música.
2. Tarjeta `Continuar escuchando` si existe estado válido.
3. Biblioteca debajo.
4. Nada suena hasta que la persona toca Continuar o una canción.

### Regreso mientras suena en segundo plano

1. Se conserva la sección anterior.
2. El mini reproductor refleja la sesión real.
3. Tocar el mini reproductor abre Ahora suena.

## Dirección visual: casi un clon de escritorio

### Se conserva

- Rosa y Medianoche.
- Fondo cálido y texto ciruela.
- Acento rosa en reproducción y estados activos.
- Rostro mínimo del gato.
- Formas amplias, suaves y concéntricas.
- Control Play/Pausa circular como centro visual.
- Iconografía sencilla.
- Mensajes humanos como `Elige tu música`, `Listo` y `Reproduciendo`.
- Sensación de objeto musical privado, no de plataforma comercial.

### Se adapta a Android

- Las orejas se convierten en una cabecera o silueta interna y no intentan cambiar la forma física del Pixel.
- Los paneles laterales de Windows pasan a pantallas o capas con Atrás.
- El compacto pasa a ser mini reproductor, notificación y controles de bloqueo.
- Cerrar y minimizar desaparecen.
- El volumen se delega al sistema.
- Las zonas táctiles usan mínimo 48 dp.
- Se respetan barras del sistema, gestos, rotación y fuente grande.

## Principios Apple aplicados

- La jerarquía se entiende antes de tocar.
- La interfaz recuerda el contexto sin actuar por sorpresa.
- Cada pantalla tiene una acción principal clara.
- La navegación permanece estable.
- Los controles responden inmediatamente.
- El diseño elimina decisiones innecesarias, no funciones útiles.
- Los estados vacíos explican el siguiente paso.
- La personalidad visual no reduce legibilidad ni familiaridad.

## Diferencia entre estado preparado y escucha real

La corrección P0 consiste en separar tres estados:

1. `Sin selección`: biblioteca cargada; no hay reproductor visible.
2. `Reanudable`: existe historial válido; se muestra Continuar, pero no mini reproductor.
3. `Sesión activa`: la persona ha iniciado o mantiene reproducción; se muestra mini reproductor y controles del sistema.

Esta distinción evita que la primera canción de la cola parezca elegida automáticamente y permite recuperar el minuto correcto cuando la persona lo desea.

## Orden de implementación

1. Persistir canción, cola y posición; corregir los tres estados de reproducción.
2. Modelar varias playlists Markdown autorizadas.
3. Reconstruir navegación Música · Buscar · Listas.
4. Convertir Ahora suena y Letra en capas contextuales.
5. Replicar el sistema visual del escritorio en componentes Compose.
6. Mostrar miniaturas reales de YouTube y, después, carátulas locales reales.
7. Mantener descargas y progreso fuera del ciclo de vida de una pantalla.
8. Validar TalkBack, fuente grande, rotación, bloqueo, Bluetooth y Pixel 7.
