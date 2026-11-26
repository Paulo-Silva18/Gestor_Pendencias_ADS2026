package br.edu.iftm.gestorpendencias.ui.form

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.edu.iftm.gestorpendencias.data.Contexto
import br.edu.iftm.gestorpendencias.data.Prioridade
import br.edu.iftm.gestorpendencias.data.Status
import br.edu.iftm.gestorpendencias.util.formatarDia
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(vm: FormViewModel, onVoltar: () -> Unit) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var mostrarData by remember { mutableStateOf(false) }

    LaunchedEffect(ui.salvo) { if (ui.salvo) onVoltar() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (ui.editando) "Editar pendência" else "Nova pendência") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = ui.titulo, onValueChange = vm::onTitulo,
                label = { Text("Título") }, singleLine = true,
                isError = ui.erroTitulo != null,
                supportingText = { ui.erroTitulo?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Column {
                Text("Contexto *", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Contexto.entries.forEach { c ->
                        FilterChip(selected = ui.contexto == c, onClick = { vm.onContexto(c) },
                            label = { Text(c.rotulo) })
                    }
                }
                ui.erroContexto?.let { Text(it, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall) }
            }

            Column {
                Text("Prioridade", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Prioridade.entries.forEach { p ->
                        FilterChip(selected = ui.prioridade == p, onClick = { vm.onPrioridade(p) },
                            label = { Text(p.rotulo) })
                    }
                }
            }

            Column {
                Text("Prazo limite *", style = MaterialTheme.typography.labelLarge)
                OutlinedButton(onClick = { mostrarData = true }) {
                    Text(ui.prazoEpochDay?.formatarDia() ?: "Selecionar data")
                }
                ui.erroPrazo?.let { Text(it, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall) }
            }

            Column {
                Text("Status", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Status.entries.forEach { s ->
                        FilterChip(selected = ui.status == s, onClick = { vm.onStatus(s) },
                            label = { Text(s.rotulo) })
                    }
                }
            }

            ui.erroGeral?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(onClick = vm::salvar, modifier = Modifier.fillMaxWidth()) { Text("Salvar") }
        }
    }

    if (mostrarData) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { mostrarData = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        vm.onPrazo(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay())
                    }
                    mostrarData = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { mostrarData = false }) { Text("Cancelar") } }
        ) { DatePicker(state = state) }
    }
}
