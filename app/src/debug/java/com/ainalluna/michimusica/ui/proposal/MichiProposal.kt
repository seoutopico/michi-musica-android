package com.ainalluna.michimusica.ui.proposal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ainalluna.michimusica.ui.*

// Propuesta aislada en debug. Todos los datos y las acciones son simulados.
private enum class Page { WELCOME, LIBRARY, SEARCH, RESULTS, LISTS, DETAIL, PLAYER, LYRICS, EMPTY }
private val tracks = listOf("La luz de septiembre", "Un lugar tranquilo", "Cerca del mar", "Todo a su tiempo", "Volver a casa", "La tarde azul")

@Composable
private fun Proposal(
    start: Page,
    night: Boolean = false,
    active: Boolean = false,
    hasPreviousSession: Boolean = start != Page.WELCOME && start != Page.EMPTY,
) {
    var page by remember { mutableStateOf(start) }
    var tab by remember { mutableIntStateOf(if (start == Page.LISTS || start == Page.DETAIL) 2 else if (start == Page.SEARCH || start == Page.RESULTS) 1 else 0) }
    // Una selección restaurada también tiene reproductor, aunque no haya audio activo.
    var engaged by remember { mutableStateOf(hasPreviousSession || active) }
    var playing by remember { mutableStateOf(active) }
    var position by remember { mutableFloatStateOf(if (hasPreviousSession) 84f / 248f else 0f) }
    var query by remember { mutableStateOf(if (start == Page.RESULTS) "Piano tranquilo" else "") }
    var localQuery by remember { mutableStateOf("") }
    var settings by remember { mutableStateOf(false) }
    var dark by remember { mutableStateOf(night) }
    var saved by remember { mutableStateOf(false) }
    var importHint by remember { mutableStateOf(false) }
    var listTitle by remember { mutableStateOf("Tardes tranquilas") }
    var preview by remember { mutableStateOf(false) }
    var returnPage by remember { mutableStateOf(Page.LIBRARY) }
    var currentTitle by remember { mutableStateOf(tracks.first()) }
    MichiTheme(if (dark) MichiSkin.MIDNIGHT else MichiSkin.ROSE) {
        val base = MaterialTheme.colorScheme
        MaterialTheme(colorScheme = base.copy(primary = if (dark) Color(0xFFF0AFC0) else Color(0xFFA33764))) {
            val colors = MaterialTheme.colorScheme
            Scaffold(containerColor = colors.background, bottomBar = {
                if (page != Page.WELCOME) Column(Modifier.fillMaxWidth().background(colors.surface)) {
                    if (engaged && page != Page.PLAYER) {
                        Surface(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), shape = RoundedCornerShape(20.dp), color = colors.primaryContainer) {
                            Row(Modifier.padding(start = 14.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f).clickable { if (page != Page.LYRICS) returnPage = page; page = Page.PLAYER }.padding(vertical = 14.dp)) {
                                    Text(currentTitle, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                                    Caption("Brisa · ${if (playing) "Reproduciendo" else "En pausa"} · ${proposalTime(position)}")
                                }
                                Action(if (playing) MichiIconType.PAUSE else MichiIconType.PLAY, if (playing) "Pausar" else "Continuar desde ${proposalTime(position)}") { preview = false; playing = !playing }
                            }
                        }
                    }
                    NavigationBar(containerColor = colors.surface, tonalElevation = 0.dp) {
                        listOf("Biblioteca" to MichiIconType.MUSIC, "Buscar" to MichiIconType.SEARCH, "Listas" to MichiIconType.PLAYLIST).forEachIndexed { index, item ->
                            NavigationBarItem(selected = tab == index, onClick = { tab = index; page = listOf(Page.LIBRARY, Page.SEARCH, Page.LISTS)[index] }, icon = { MichiIcon(item.second, item.first) }, label = { Text(item.first) })
                        }
                    }
                }
            }) { insets ->
                Column(Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(top = 20.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    when (page) {
                        Page.WELCOME -> {
                            Spacer(Modifier.height(62.dp))
                            MichiFace(Modifier.size(100.dp).align(Alignment.CenterHorizontally))
                            Caption("MICHI MÚSICA")
                            Text("Tu música.\nTu pequeño refugio.", fontSize = 36.sp, lineHeight = 41.sp, fontWeight = FontWeight.Bold)
                            Text("Tus canciones, siempre contigo. Elige la carpeta donde guardas tu música para empezar.", color = colors.onSurfaceVariant, fontSize = 17.sp, lineHeight = 25.sp)
                            Spacer(Modifier.height(24.dp))
                            Button({ page = Page.LIBRARY }, Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("Elegir mi carpeta") }
                            Caption("Solo accedemos a la carpeta que elijas. Tus audios no se suben ni se comparten.")
                        }
                        Page.LIBRARY, Page.EMPTY -> {
                            Header("Biblioteca", "Tu música, a tu ritmo") { settings = true }
                            OutlinedTextField(localQuery, { localQuery = it }, Modifier.fillMaxWidth(), placeholder = { Text("Buscar en tu biblioteca") }, leadingIcon = { MichiIcon(MichiIconType.SEARCH, "Buscar localmente") }, singleLine = true, shape = RoundedCornerShape(18.dp))
                            if (page == Page.EMPTY) {
                                Text("Aquí empieza tu música", style = MaterialTheme.typography.titleLarge)
                                Caption("Esta carpeta todavía no contiene canciones compatibles.")
                                Button({ page = Page.LIBRARY }) { Text("Elegir otra carpeta") }
                            } else {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Canciones", style = MaterialTheme.typography.titleLarge)
                                    TextButton({ engaged = true; playing = true; preview = false; position = 0f; currentTitle = tracks[2] }) { MichiIcon(MichiIconType.SHUFFLE, "Azar", tint = colors.primary); Text(" Azar") }
                                }
                                val matches = tracks.filter { "$it Brisa Un lugar tranquilo".contains(localQuery, ignoreCase = true) }
                                if (matches.isEmpty()) Caption("No hay coincidencias. Prueba con otro título o artista.")
                                matches.forEach { title -> Track(title, engaged && title == currentTitle) { currentTitle = title; engaged = true; playing = true; preview = false; position = 0f } }
                            }
                        }
                        Page.SEARCH, Page.RESULTS -> {
                            Header("Buscar", "Descubre música en YouTube") { settings = true }
                            OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), placeholder = { Text("Canción, artista o enlace") }, leadingIcon = { MichiIcon(MichiIconType.SEARCH, "Buscar") }, singleLine = true, shape = RoundedCornerShape(18.dp))
                            Button({ page = Page.RESULTS }, Modifier.fillMaxWidth().heightIn(min = 52.dp), enabled = query.isNotBlank()) { Text("Buscar en YouTube") }
                            if (page == Page.SEARCH) {
                                Spacer(Modifier.height(26.dp))
                                MichiIcon(MichiIconType.SEARCH, "", Modifier.size(40.dp), colors.primary)
                                Text("Encuentra tu próxima\ncanción favorita", style = MaterialTheme.typography.headlineSmall)
                                Caption("Busca un nombre o pega un enlace. Puedes escuchar antes de guardar el MP3 en tu carpeta.")
                                Caption("Necesita conexión. Para tus archivos, usa el buscador de Biblioteca.")
                            } else {
                                Caption("RESULTADOS DE EJEMPLO")
                                Panel {
                                    Text("Piano para una tarde tranquila", style = MaterialTheme.typography.titleMedium)
                                    Caption("Estudio Brisa · 4:32")
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton({ preview = !preview; playing = false }) { Text(if (preview) "Pausar" else "Escuchar") }
                                        Button({ saved = true; preview = false }, enabled = !saved) { Text(if (saved) "Guardado" else "Guardar MP3") }
                                    }
                                    if (saved) Caption("Guardado en tu carpeta. Disponible en Biblioteca.")
                                }
                                Panel { Text("Piano junto al mar", style = MaterialTheme.typography.titleMedium); Caption("Sesiones del sur · 3:18"); Caption("Los resultados reales mostrarán su título, canal y duración.") }
                            }
                        }
                        Page.LISTS -> {
                            Header("Listas", "Una selección para cada momento") { settings = true }
                            OutlinedButton({ importHint = true }, Modifier.fillMaxWidth().heightIn(min = 52.dp)) { MichiIcon(MichiIconType.ADD, "Importar", tint = colors.primary); Text(" Importar lista") }
                            if (importHint) Caption("En la app se abrirá el selector de archivos para elegir una lista Markdown (.md).")
                            Panel { Text("Toda tu música", style = MaterialTheme.typography.titleMedium); Caption("6 canciones · en tu dispositivo"); TextButton({ tab = 0; page = Page.LIBRARY }) { Text("Ver biblioteca") } }
                            Text("Tus listas", style = MaterialTheme.typography.titleLarge)
                            listOf("Tardes tranquilas" to "6 canciones", "De camino" to "6 canciones", "Domingo en casa" to "6 canciones · 2 no encontradas").forEach { (title, detail) ->
                                Row(Modifier.fillMaxWidth().clickable { listTitle = title; page = Page.DETAIL }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(16.dp), color = colors.secondaryContainer) { Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) { MichiIcon(MichiIconType.PLAYLIST, "") } }
                                    Column(Modifier.weight(1f).padding(start = 16.dp)) { Text(title, fontWeight = FontWeight.SemiBold); Caption(detail) }
                                    MichiIcon(MichiIconType.NEXT, "Ver lista", Modifier.size(18.dp))
                                }
                            }
                        }
                        Page.DETAIL -> {
                            TextButton({ page = Page.LISTS }) { MichiIcon(MichiIconType.BACK, "Volver", tint = colors.primary); Text(" Listas") }
                            MichiIcon(MichiIconType.PLAYLIST, "", Modifier.size(54.dp), colors.primary)
                            Text(listTitle, style = MaterialTheme.typography.headlineMedium)
                            Caption("6 canciones · 24 min")
                            if (listTitle == "Domingo en casa") Caption("2 canciones no están en tu carpeta. Puedes escuchar las 6 disponibles.")
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button({ currentTitle = tracks.first(); engaged = true; playing = true; preview = false; position = 0f }) { Text("Reproducir") }
                                OutlinedButton({ currentTitle = tracks[2]; engaged = true; playing = true; preview = false; position = 0f }) { Text("Azar") }
                            }
                            tracks.forEach { title -> Track(title, engaged && title == currentTitle) { currentTitle = title; engaged = true; playing = true; preview = false; position = 0f } }
                        }
                        Page.PLAYER, Page.LYRICS -> {
                            TextButton({ page = if (page == Page.LYRICS) Page.PLAYER else returnPage }) { MichiIcon(MichiIconType.BACK, "Volver", tint = colors.primary); Text(if (page == Page.LYRICS) " Ahora suena" else " Volver") }
                            Caption(if (playing) "AHORA SUENA" else "EN PAUSA")
                            if (page == Page.PLAYER) {
                                Surface(Modifier.fillMaxWidth().height(150.dp), shape = RoundedCornerShape(28.dp), color = colors.primaryContainer) { Box(contentAlignment = Alignment.Center) { MichiFace(Modifier.size(90.dp)) } }
                                Text(currentTitle, style = MaterialTheme.typography.headlineMedium)
                                Caption("Brisa · Un lugar tranquilo")
                                TextButton({ page = Page.LYRICS }) { Text("Ver letra") }
                                Slider(position, { position = it })
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Caption(proposalTime(position)); Caption("4:08") }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                                    Action(MichiIconType.PREVIOUS, "Anterior") { currentTitle = tracks.last(); position = 0f }
                                    Button({ preview = false; playing = !playing }, Modifier.size(72.dp), shape = CircleShape, contentPadding = PaddingValues(0.dp)) { MichiIcon(if (playing) MichiIconType.PAUSE else MichiIconType.PLAY, "Reproducir o pausar", tint = colors.onPrimary) }
                                    Action(MichiIconType.NEXT, "Siguiente") { currentTitle = tracks[1]; position = 0f }
                                }
                            } else {
                                Text(currentTitle, style = MaterialTheme.typography.titleLarge)
                                Caption("LETRA DE EJEMPLO")
                                Text("La tarde se queda,\nla prisa se va.", fontSize = 28.sp, lineHeight = 42.sp, color = colors.onSurfaceVariant)
                                Text("Hoy tengo tiempo\npara escuchar.", fontSize = 30.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                                Text("Y el mundo espera\nun poco más.", fontSize = 28.sp, lineHeight = 42.sp, color = colors.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            if (settings) AlertDialog(onDismissRequest = { settings = false }, title = { Text("Tu Michi") }, text = { Column { Text("Apariencia"); Row { TextButton({ dark = false }) { Text("Rosa") }; TextButton({ dark = true }) { Text("Medianoche") } }; Caption("Carpeta y actualización de música se reunirán aquí.") } }, confirmButton = { TextButton({ settings = false }) { Text("Listo") } })
        }
    }
}

private fun proposalTime(position: Float): String {
    val seconds = (position * 248).toInt().coerceIn(0, 248)
    return "%d:%02d".format(seconds / 60, seconds % 60)
}

@Composable private fun Header(title: String, subtitle: String, settings: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold); Caption(subtitle) }
        Action(MichiIconType.MORE, "Ajustes", settings)
    }
}
@Composable private fun Caption(text: String) { Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
@Composable private fun Action(icon: MichiIconType, label: String, action: () -> Unit) { IconButton(action) { MichiIcon(icon, label) } }
@Composable private fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content) }
}
@Composable private fun Track(title: String, selected: Boolean, onClick: () -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis); Caption("Brisa · Un lugar tranquilo") }
            if (selected) MichiIcon(MichiIconType.MUSIC, "Canción seleccionada", tint = MaterialTheme.colorScheme.primary) else Caption("4:08")
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .35f))
    }
}

@Preview(name = "01 · Primera apertura", group = "Propuesta UX", widthDp = 412, heightDp = 860, showBackground = true)
@Composable private fun WelcomeMockup() = Proposal(Page.WELCOME)
@Preview(name = "02 · Biblioteca al volver", group = "Propuesta UX", widthDp = 412, heightDp = 860)
@Composable private fun LibraryMockup() = Proposal(Page.LIBRARY)
@Preview(name = "03 · Biblioteca reproduciendo", group = "Propuesta UX", widthDp = 412, heightDp = 860)
@Composable private fun ActiveMockup() = Proposal(Page.LIBRARY, active = true)
@Preview(name = "04 · Buscar inicio", group = "Propuesta UX", widthDp = 412, heightDp = 860)
@Composable private fun SearchMockup() = Proposal(Page.SEARCH)
@Preview(name = "05 · Buscar resultados", group = "Propuesta UX", widthDp = 412, heightDp = 860)
@Composable private fun ResultsMockup() = Proposal(Page.RESULTS)
@Preview(name = "06 · Listas", group = "Propuesta UX", widthDp = 412, heightDp = 860)
@Composable private fun ListsMockup() = Proposal(Page.LISTS, active = true)
@Preview(name = "07 · Detalle de lista", group = "Propuesta UX", widthDp = 412, heightDp = 860)
@Composable private fun DetailMockup() = Proposal(Page.DETAIL)
@Preview(name = "08 · Ahora suena", group = "Propuesta UX", widthDp = 412, heightDp = 860)
@Composable private fun PlayerMockup() = Proposal(Page.PLAYER, active = true)
@Preview(name = "09 · Letra", group = "Propuesta UX", widthDp = 412, heightDp = 860)
@Composable private fun LyricsMockup() = Proposal(Page.LYRICS, active = true)
@Preview(name = "10 · Medianoche", group = "Propuesta UX", widthDp = 412, heightDp = 860)
@Composable private fun NightMockup() = Proposal(Page.LIBRARY, night = true, active = true)
@Preview(name = "11 · Carpeta vacía", group = "Propuesta UX", widthDp = 412, heightDp = 860)
@Composable private fun EmptyMockup() = Proposal(Page.EMPTY)
@Preview(name = "12 · Pantalla pequeña y texto grande", group = "Accesibilidad", widthDp = 360, heightDp = 740, fontScale = 1.3f)
@Composable private fun SmallMockup() = Proposal(Page.LIBRARY, active = true)
@Preview(name = "13 · Biblioteca sin escucha previa", group = "Propuesta UX", widthDp = 412, heightDp = 860)
@Composable private fun FirstLibraryMockup() = Proposal(Page.LIBRARY, hasPreviousSession = false)
@Preview(name = "14 · Listas con escucha pausada", group = "Propuesta UX", widthDp = 412, heightDp = 860)
@Composable private fun PausedListsMockup() = Proposal(Page.LISTS)
