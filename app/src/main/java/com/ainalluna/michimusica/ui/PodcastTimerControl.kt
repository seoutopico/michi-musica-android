package com.ainalluna.michimusica.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ainalluna.michimusica.R
import com.ainalluna.michimusica.playback.SleepChoice
import com.ainalluna.michimusica.playback.SleepTimerDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastTimerControl(state: SleepTimerDisplay, enabled: Boolean, onChoose: (SleepChoice) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    TextButton({ open = true }, enabled = enabled,
        modifier = Modifier.heightIn(min = 48.dp).semantics {
            stateDescription = if (state.active) "Temporizador activo: ${state.label}" else "Temporizador desactivado"
        }, colors = ButtonDefaults.textButtonColors(contentColor = if (state.active) colors.primary else colors.onSurfaceVariant)) {
        HomeIcon(R.drawable.ic_player_timer, null)
        Text(if (state.active) "Temporizador · ${state.label}" else "Temporizador",
            Modifier.padding(start = 8.dp), fontSize = 14.sp)
    }
    if (open) ModalBottomSheet(onDismissRequest = { open = false }, containerColor = colors.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
            Text("Temporizador", fontFamily = FontFamily.Serif, fontSize = 28.sp)
            Text("Pausar el podcast. Los minutos cuentan desde ahora, también si haces una pausa.",
                Modifier.padding(top = 10.dp, bottom = 12.dp), color = colors.onSurfaceVariant, fontSize = 14.sp)
            Column(Modifier.selectableGroup()) {
                SleepChoice.entries.forEach { choice ->
                    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).selectable(
                        selected = state.choice == choice, role = Role.RadioButton,
                        onClick = { onChoose(choice); open = false }).padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(choice.label, Modifier.weight(1f), fontSize = 16.sp,
                            color = if (state.choice == choice) colors.primary else colors.onSurface)
                        RadioButton(selected = state.choice == choice, onClick = null)
                    }
                }
            }
        }
    }
}
