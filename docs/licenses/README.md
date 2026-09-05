# Iconos de la portada

Los recursos `app/src/main/res/drawable/ic_home_*.xml`, `ic_search_*.xml` e `ic_lists_*.xml` proceden de [Google Material Design Icons](https://github.com/google/material-design-icons), bajo Apache-2.0. El texto completo está en `material-icons.txt`.

Se convirtieron los SVG Material Icons de 24 px a VectorDrawable, conservando sus trazados y pasando el color a blanco para aplicar el tintado del tema en Compose.

Fuentes en `src/<categoría>/<nombre>/materialicons/24px.svg` del repositorio oficial:

- action: settings, search.
- av: library_music, playlist_play, play_arrow, pause, skip_next, shuffle, skip_previous, repeat, repeat_one, lyrics.
- hardware: keyboard_arrow_down.
- navigation: close, check, chevron_right, arrow_back, more_vert.
- content: add.
- file: file_download (recurso `ic_search_download`).

El emblema felino de Michi es un dibujo propio de la aplicación y no forma parte de ese conjunto.

Los recursos `ic_player_*.xml` del reproductor usan el mismo origen y licencia.

Los recursos `ic_lyrics_*.xml` usan también los trazados oficiales Material y la misma licencia.
