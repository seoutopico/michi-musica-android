package com.ainalluna.michimusica.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ainalluna.michimusica.R
import com.ainalluna.michimusica.library.Song

data class PlaylistCollection(val id: String, val title: String, val songCount: Int,
                              val missingCount: Int = 0, val artworkSongs: List<Song> = emptyList())

@Composable
fun ListsHome(libraryCount: Int, collections: List<PlaylistCollection>, activeId: String?,
              loading: Boolean, artworkRevision: Int, onAllMusic: () -> Unit,
              onAdd: () -> Unit, onSelect: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)) {
        item(key = "header") {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp).heightIn(min = 48.dp)
                .clearAndSetSemantics { contentDescription = "Michi Música" }, verticalAlignment = Alignment.CenterVertically) {
                HomeCat(Modifier.size(26.dp))
                Text("michi", Modifier.padding(start = 10.dp), fontSize = 23.sp, letterSpacing = 1.sp, color = colors.primary)
            }
            FlowRow(Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalArrangement = Arrangement.Center) {
                Text("Listas", Modifier.semantics { heading() }, fontFamily = FontFamily.Serif,
                    fontSize = 36.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold)
                TextButton(onAdd, Modifier.heightIn(min = 48.dp), contentPadding = PaddingValues(start = 8.dp, end = 0.dp)) {
                    HomeIcon(R.drawable.ic_lists_add, null)
                    Text("Añadir lista", Modifier.padding(start = 6.dp), fontSize = 14.sp)
                }
            }
            Text("Tu música, a tu manera.", Modifier.padding(start = 8.dp, top = 8.dp, bottom = 24.dp),
                fontSize = 15.sp, color = colors.onSurfaceVariant)
        }
        item(key = "all") {
            Row(Modifier.fillMaxWidth().clickable(role = Role.Button, onClickLabel = "Abrir toda tu música", onClick = onAllMusic)
                .padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                HomeIcon(R.drawable.ic_home_library_music, null, Modifier.size(32.dp), colors.primary)
                Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                    Text("Toda tu música", fontSize = 17.sp, fontWeight = FontWeight.Medium)
                    Text(if (loading) "Leyendo tu colección…" else songCountLabel(libraryCount), Modifier.padding(top = 5.dp),
                        fontSize = 14.sp, color = colors.onSurfaceVariant)
                }
                if (activeId == null) HomeIcon(R.drawable.ic_search_check, "Selección actual", tint = colors.primary)
                else HomeIcon(R.drawable.ic_lists_chevron_right, null, tint = colors.onSurfaceVariant)
            }
            HorizontalDivider(Modifier.padding(horizontal = 8.dp, vertical = 16.dp), color = colors.outline.copy(alpha = .3f))
        }
        item(key = "section") {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Tus listas", Modifier.weight(1f).semantics { heading() }, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                if (!loading) Text("${collections.size}", fontSize = 14.sp, color = colors.onSurfaceVariant)
            }
        }
        if (loading) item(key = "loading") {
            LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 18.dp))
            Text("Leyendo tus listas…", Modifier.padding(8.dp).semantics { liveRegion = LiveRegionMode.Polite }, color = colors.onSurfaceVariant)
        }
        else if (collections.isEmpty()) item(key = "empty") {
            Column(Modifier.padding(horizontal = 8.dp, vertical = 24.dp)) {
                Text("Una lista para\ncada momento.", fontFamily = FontFamily.Serif, fontSize = 28.sp, lineHeight = 35.sp)
                Text("Reúne las canciones que quieres escuchar juntas. Al añadir una lista, aparecerá aquí.",
                    Modifier.padding(top = 14.dp), fontSize = 15.sp, lineHeight = 23.sp, color = colors.onSurfaceVariant)
                Text("Usa Añadir lista para abrir un archivo Markdown (.md) con canciones de tu carpeta.",
                    Modifier.padding(top = 24.dp), fontSize = 14.sp, lineHeight = 21.sp, color = colors.onSurfaceVariant)
            }
        }
        else {
            items(collections, key = PlaylistCollection::id) { collection ->
                CollectionRow(collection, collection.id == activeId, artworkRevision) { onSelect(collection.id) }
            }
            item(key = "help") {
                Text("Añade listas desde archivos Markdown (.md).\nTus canciones permanecen en su carpeta.",
                    Modifier.padding(horizontal = 8.dp, vertical = 20.dp), fontSize = 13.sp, lineHeight = 20.sp, color = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CollectionRow(collection: PlaylistCollection, selected: Boolean, artworkRevision: Int, onOpen: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(12.dp))
        .background(if (selected) Brush.horizontalGradient(listOf(colors.primaryContainer.copy(alpha = .6f), colors.surfaceContainerHigh.copy(alpha = .5f)))
            else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
        .clickable(role = Role.Button, onClickLabel = "Abrir ${collection.title}", onClick = onOpen)
        .semantics { this.selected = selected }) {
        if (selected) Box(Modifier.align(Alignment.CenterStart).width(2.dp).height(46.dp).background(colors.primary))
        Row(Modifier.padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            CollectionArtwork(collection.artworkSongs, Modifier.size(80.dp), artworkRevision)
            Column(Modifier.weight(1f).padding(start = 16.dp, end = 8.dp)) {
                Text(collection.title, fontSize = 17.sp, lineHeight = 23.sp, fontWeight = FontWeight.Medium,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(songCountLabel(collection.songCount), Modifier.padding(top = 6.dp), fontSize = 14.sp, color = colors.onSurfaceVariant)
                if (selected) Text("Selección actual", Modifier.padding(top = 5.dp), fontSize = 12.sp, color = colors.primary)
                if (collection.missingCount > 0) Text("${collection.missingCount} ${if (collection.missingCount == 1) "canción no disponible" else "canciones no disponibles"}",
                    Modifier.padding(top = 5.dp), fontSize = 12.sp, lineHeight = 18.sp, color = colors.onSurfaceVariant)
            }
            HomeIcon(R.drawable.ic_lists_chevron_right, null, tint = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun CollectionArtwork(songs: List<Song>, modifier: Modifier = Modifier, revision: Int = 0) {
    val colors = MaterialTheme.colorScheme
    Box(modifier.clip(RoundedCornerShape(5.dp)).background(Brush.linearGradient(listOf(colors.primaryContainer, colors.surfaceContainerHigh))),
        contentAlignment = Alignment.Center) {
        if (songs.size >= 4) Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            songs.take(4).chunked(2).forEach { pair ->
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    pair.forEach { SongArtwork(it, Modifier.weight(1f).fillMaxHeight(), revision) }
                }
            }
        } else if (songs.isNotEmpty()) SongArtwork(songs.first(), Modifier.fillMaxSize(), revision)
        else HomeIcon(R.drawable.ic_home_playlist_play, null, Modifier.size(36.dp), colors.primary)
    }
}

private fun songCountLabel(count: Int) = "$count ${if (count == 1) "canción" else "canciones"}"
