package com.ainalluna.michimusica.ui.proposal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ainalluna.michimusica.ui.MichiIcon
import com.ainalluna.michimusica.ui.MichiIconType
import java.text.Normalizer

private val Paper = Color(0xFFFCFAF8)
private val Ink = Color(0xFF282329)
private val Quiet = Color(0xFF71686D)
private val Rose = Color(0xFFA33C60)
private val Rule = Color(0xFFE6DFE0)
private val PlayerPaper = Color(0xFFF3E9EB)
private data class OpeningTrack(val title: String, val artist: String, val duration: String)
private val openingTracks = listOf(
    OpeningTrack("Al otro lado del sol", "Clara del Mar", "3:42"),
    OpeningTrack("Bajo la lluvia", "Elena y los días", "4:16"),
    OpeningTrack("Casi septiembre", "Norte", "3:08"),
    OpeningTrack("La luz de septiembre", "Brisa", "4:08"),
    OpeningTrack("Las cosas pequeñas", "Julia del Río", "2:54"),
    OpeningTrack("Un lugar tranquilo", "Brisa", "3:51"),
    OpeningTrack("Volver despacio", "Norte", "4:22"),
)

/** Una sola pantalla en revisión. Datos ficticios y reproducción simulada, sin permisos ni red. */
@Composable
private fun OpeningScreen(previousListen: Boolean = true, initiallyPlaying: Boolean = false, collectionEntry: Boolean = false) {
    var selected by remember { mutableStateOf<Int?>(if (previousListen) 3 else null) }
    var playing by remember { mutableStateOf(previousListen && initiallyPlaying) }
    var query by remember { mutableStateOf("") }
    var settings by remember { mutableStateOf(false) }
    var nextLayer by remember { mutableStateOf<String?>(null) }
    var resumeTime by remember { mutableStateOf("1:24") }
    var viewingSongs by remember { mutableStateOf(!collectionEntry) }
    val colors = lightColorScheme(
        primary = Rose, onPrimary = Color.White, background = Paper,
        surface = Paper, onSurface = Ink, onSurfaceVariant = Quiet,
        surfaceContainerHigh = PlayerPaper, outline = Rule,
    )
    fun choose(index: Int) { selected = index; playing = true; resumeTime = "0:00" }
    MaterialTheme(colorScheme = colors) {
        Scaffold(
            containerColor = Paper,
            bottomBar = {
                Column(Modifier.fillMaxWidth().background(Paper).navigationBarsPadding()) {
                    selected?.let { index ->
                        OpeningPlayer(openingTracks[index], playing, resumeTime,
                            onToggle = { playing = !playing },
                            onNext = { choose((index + 1) % openingTracks.size) },
                            onOpen = { nextLayer = "Ahora suena" })
                    }
                    OpeningNavigation { nextLayer = it }
                }
            },
        ) { safeArea ->
            if (!viewingSongs) {
                CollectionEntry(Modifier.fillMaxSize().padding(safeArea),
                    onBrowse = { viewingSongs = true },
                    onShuffle = { choose((openingTracks.indices - listOfNotNull(selected)).random()) },
                    onSettings = { settings = true })
            } else {
            Column(Modifier.fillMaxSize().padding(safeArea)) {
                if (collectionEntry) TextButton({ viewingSongs = false }, Modifier.padding(start = 16.dp)) { OpeningIcon(MichiIconType.BACK, "", Rose); Text(" Biblioteca") }
                Row(Modifier.fillMaxWidth().padding(start = 28.dp, end = 20.dp, top = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Biblioteca", Modifier.semantics { heading() }, fontFamily = FontFamily.SansSerif,
                            fontSize = 36.sp, lineHeight = 43.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp, color = Ink)
                        Text("${openingTracks.size} canciones · En tu dispositivo", Modifier.padding(top = 5.dp), fontSize = 14.sp, color = Quiet)
                    }
                    IconButton({ settings = true }) { OpeningIcon(MichiIconType.MORE, "Opciones de biblioteca") }
                }
                OpeningSearch(query, { query = it }, Modifier.padding(horizontal = 28.dp, vertical = 24.dp))
                Row(Modifier.fillMaxWidth().padding(start = 28.dp, end = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Canciones", Modifier.weight(1f).semantics { heading() }, fontSize = 21.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                    TextButton({ choose((openingTracks.indices - listOfNotNull(selected)).random()) }, contentPadding = PaddingValues(horizontal = 8.dp)) {
                        OpeningIcon(MichiIconType.SHUFFLE, "", Rose)
                        Text("Azar", Modifier.padding(start = 7.dp), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                val wanted = normalizedOpeningQuery(query)
                val matches = openingTracks.withIndex().filter { wanted in normalizedOpeningQuery("${it.value.title} ${it.value.artist}") }
                if (matches.isEmpty()) {
                    Column(Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("No hay coincidencias", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Text("Prueba con otro título o artista.", color = Quiet)
                        TextButton({ query = "" }) { Text("Borrar búsqueda") }
                    }
                } else LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp)) {
                    itemsIndexed(matches, key = { _, entry -> entry.index }) { row, entry ->
                        val isSelected = entry.index == selected
                        OpeningSong(entry.value, isSelected, playing && isSelected) { choose(entry.index) }
                        if (row < matches.lastIndex) HorizontalDivider(Modifier.padding(start = 28.dp, end = 28.dp), thickness = .5.dp, color = Rule)
                    }
                }
            }
            }
        }
        if (settings) {
            AlertDialog(onDismissRequest = { settings = false }, title = { Text("Biblioteca") }, text = {
                Column {
                    Text("Carpeta de música", fontWeight = FontWeight.SemiBold)
                    Text("Música", color = Quiet, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
                    TextButton({ settings = false; nextLayer = "Elegir carpeta" }) { Text("Cambiar carpeta") }
                    TextButton({ settings = false; nextLayer = "Actualizar biblioteca" }) { Text("Actualizar biblioteca") }
                }
            }, confirmButton = { TextButton({ settings = false }) { Text("Cerrar") } })
        }
        nextLayer?.let { destination ->
            // No se inventa el diseño de otras pantallas en esta revisión de la apertura.
            AlertDialog(onDismissRequest = { nextLayer = null }, title = { Text(destination) },
                text = { Text("Este acceso continúa a $destination. En esta maqueta estamos revisando únicamente la pantalla de apertura.") },
                confirmButton = { TextButton({ nextLayer = null }) { Text("Volver a la maqueta") } })
        }
    }
}

/** Alternativa de arquitectura: portada de colección → canciones. No sustituye silenciosamente A. */
@Composable
private fun CollectionEntry(modifier: Modifier, onBrowse: () -> Unit, onShuffle: () -> Unit, onSettings: () -> Unit) {
    Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal = 28.dp).padding(top = 20.dp, bottom = 24.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Biblioteca", Modifier.weight(1f).semantics { heading() }, fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-.7).sp, color = Ink)
            IconButton(onSettings) { OpeningIcon(MichiIconType.MORE, "Opciones de biblioteca") }
        }
        Spacer(Modifier.height(24.dp))
        CollectionArtwork(Modifier.fillMaxWidth().height(216.dp))
        Text("TU COLECCIÓN", Modifier.padding(top = 28.dp), color = Rose, fontSize = 11.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold)
        Text("Toda tu música.", Modifier.padding(top = 8.dp).semantics { heading() }, fontFamily = FontFamily.Serif, fontSize = 38.sp, lineHeight = 43.sp, color = Ink)
        Text("${openingTracks.size} canciones en este dispositivo", Modifier.padding(top = 8.dp), fontSize = 15.sp, color = Quiet)
        Button(onBrowse, Modifier.fillMaxWidth().padding(top = 26.dp).heightIn(min = 56.dp),
            shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Ink), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)) {
            Text("Ver canciones", Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            OpeningIcon(MichiIconType.NEXT, "", Color.White)
        }
        TextButton(onShuffle, Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp).heightIn(min = 48.dp)) {
            OpeningIcon(MichiIconType.SHUFFLE, "", Rose)
            Text("Escuchar al azar", Modifier.padding(start = 10.dp), fontSize = 15.sp)
        }
    }
}

/** Emblema de la colección local: ilustración de marca, nunca portada atribuida a una canción. */
@Composable
private fun CollectionArtwork(modifier: Modifier) {
    Canvas(modifier.clearAndSetSemantics {}) {
        val w = size.width
        val h = size.height
        drawRect(Color(0xFFE7CFD0))
        val sweep = Path().apply {
            moveTo(0f, h * .72f); cubicTo(w * .35f, h * 1.03f, w * .56f, -h * .1f, w, h * .25f)
            lineTo(w, h); lineTo(0f, h); close()
        }
        drawPath(sweep, Color(0xFFC4848F))
        val disc = Offset(w * .62f, h * .48f)
        val r = h * .435f
        drawCircle(Color(0xFF3C2733), r, disc)
        repeat(7) { i -> drawCircle(Color(0xFF5A3A47), r * (.52f + i * .055f), disc, style = Stroke(1.dp.toPx())) }
        drawCircle(Color(0xFFF4E5DE), r * .35f, disc)
        val face = Path().apply {
            moveTo(disc.x - r * .2f, disc.y + r * .05f)
            lineTo(disc.x - r * .18f, disc.y - r * .19f)
            lineTo(disc.x - r * .07f, disc.y - r * .1f)
            quadraticTo(disc.x, disc.y - r * .13f, disc.x + r * .07f, disc.y - r * .1f)
            lineTo(disc.x + r * .18f, disc.y - r * .19f)
            lineTo(disc.x + r * .2f, disc.y + r * .05f)
        }
        drawPath(face, Rose, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        drawCircle(Rose, 1.6.dp.toPx(), Offset(disc.x - r * .085f, disc.y + r * .025f))
        drawCircle(Rose, 1.6.dp.toPx(), Offset(disc.x + r * .085f, disc.y + r * .025f))
        drawLine(Color(0xFFF4E5DE), Offset(w * .095f, h * .16f), Offset(w * .095f, h * .46f), 2.dp.toPx())
        drawLine(Color(0xFFF4E5DE), Offset(w * .12f, h * .16f), Offset(w * .12f, h * .32f), 2.dp.toPx())
    }
}

@Composable
private fun OpeningSearch(query: String, onChange: (String) -> Unit, modifier: Modifier) {
    BasicTextField(query, onChange, modifier.fillMaxWidth().heightIn(min = 50.dp)
        .background(Color(0xFFF0ECEB), RoundedCornerShape(10.dp)).semantics { contentDescription = "Buscar en tu biblioteca" },
        singleLine = true, textStyle = TextStyle(color = Ink, fontSize = 16.sp), cursorBrush = SolidColor(Rose),
        decorationBox = { input ->
            Row(Modifier.padding(start = 14.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                OpeningIcon(MichiIconType.SEARCH, "", Quiet)
                Box(Modifier.weight(1f).padding(horizontal = 10.dp, vertical = 14.dp)) {
                    if (query.isEmpty()) Text("Buscar en tu biblioteca", fontSize = 16.sp, color = Quiet, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    input()
                }
                if (query.isNotEmpty()) TextButton({ onChange("") }, contentPadding = PaddingValues(4.dp)) { Text("Borrar") }
            }
        })
}

@Composable
private fun OpeningSong(track: OpeningTrack, selected: Boolean, playing: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(role = Role.Button, onClickLabel = "Reproducir ${track.title}", onClick = onClick)
        .padding(start = 28.dp, end = 28.dp, top = 17.dp, bottom = 17.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 16.dp)) {
            Text(track.title, color = if (selected) Rose else Ink, fontSize = 17.sp, lineHeight = 23.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(track.artist, Modifier.padding(top = 3.dp), color = Quiet, fontSize = 14.sp, lineHeight = 19.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (selected) {
            OpeningIcon(if (playing) MichiIconType.MUSIC else MichiIconType.PAUSE, if (playing) "Reproduciendo" else "Seleccionada, en pausa", Rose)
        } else Text(track.duration, fontSize = 13.sp, color = Quiet)
    }
}

@Composable
private fun OpeningPlayer(track: OpeningTrack, playing: Boolean, time: String, onToggle: () -> Unit, onNext: () -> Unit, onOpen: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(PlayerPaper)) {
        // Progreso discreto: separa contenido desplazable de controles persistentes.
        Canvas(Modifier.fillMaxWidth().height(2.dp).clearAndSetSemantics {}) {
            drawLine(Rule, Offset.Zero, Offset(size.width, 0f), 2.dp.toPx())
            drawLine(Rose, Offset.Zero, Offset(size.width * if (time == "0:00") 0f else .34f, 0f), 2.dp.toPx(), StrokeCap.Butt)
        }
        Row(Modifier.fillMaxWidth().padding(start = 28.dp, end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).clickable(role = Role.Button, onClickLabel = "Abrir Ahora suena", onClick = onOpen)
                .padding(end = 8.dp, top = 18.dp, bottom = 18.dp)) {
                Text(track.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 21.sp, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${if (playing) "Reproduciendo" else "En pausa"} · $time", Modifier.padding(top = 3.dp), fontSize = 13.sp, lineHeight = 18.sp, color = Quiet)
            }
            FilledIconButton(onToggle, Modifier.size(48.dp), shape = CircleShape, colors = IconButtonDefaults.filledIconButtonColors(containerColor = Rose, contentColor = Color.White)) {
                OpeningIcon(if (playing) MichiIconType.PAUSE else MichiIconType.PLAY, if (playing) "Pausar" else "Continuar desde $time", Color.White)
            }
            IconButton(onNext) { OpeningIcon(MichiIconType.NEXT, "Siguiente canción") }
        }
    }
}

@Composable
private fun OpeningNavigation(onDestination: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().selectableGroup().padding(top = 6.dp, bottom = 8.dp)) {
        listOf("Biblioteca" to MichiIconType.MUSIC, "Buscar" to MichiIconType.SEARCH, "Listas" to MichiIconType.PLAYLIST).forEachIndexed { index, item ->
            val selected = index == 0
            Column(Modifier.weight(1f).heightIn(min = 62.dp).selectable(selected, role = Role.Tab, onClick = { if (!selected) onDestination(item.first) }).padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                OpeningIcon(item.second, "", if (selected) Rose else Quiet)
                Text(item.first, fontSize = 12.sp, color = if (selected) Rose else Quiet, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable private fun OpeningIcon(type: MichiIconType, label: String, tint: Color = Ink) {
    MichiIcon(type, label, if (label.isEmpty()) Modifier.size(24.dp).clearAndSetSemantics {} else Modifier.size(24.dp), tint)
}
private fun normalizedOpeningQuery(value: String) = Normalizer.normalize(value, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "").lowercase().trim()

@Preview(name = "01 · Apertura habitual", group = "Apertura · revisión", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun OpeningDailyPreview() = OpeningScreen()
@Preview(name = "02 · Mismo inicio reproduciendo", group = "Apertura · revisión", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun OpeningPlayingPreview() = OpeningScreen(initiallyPlaying = true)
@Preview(name = "03 · Sin escucha anterior", group = "Apertura · revisión", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun OpeningFirstPreview() = OpeningScreen(previousListen = false)
@Preview(name = "04 · Texto grande", group = "Apertura · revisión", widthDp = 360, heightDp = 740, fontScale = 1.3f, showSystemUi = true)
@Composable private fun OpeningLargeTextPreview() = OpeningScreen()
@Preview(name = "B1 · Entrada a la colección", group = "Alternativa B · estructura", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun CollectionEntryPreview() = OpeningScreen(collectionEntry = true)
@Preview(name = "B2 · Colección · texto grande", group = "Alternativa B · estructura", widthDp = 360, heightDp = 740, fontScale = 1.3f, showSystemUi = true)
@Composable private fun CollectionEntryLargePreview() = OpeningScreen(collectionEntry = true)
