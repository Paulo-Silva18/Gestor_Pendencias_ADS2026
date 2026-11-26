package br.edu.iftm.gestorpendencias.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.edu.iftm.gestorpendencias.data.Contexto
import br.edu.iftm.gestorpendencias.data.Pendencia
import br.edu.iftm.gestorpendencias.data.Prioridade
import br.edu.iftm.gestorpendencias.data.Status
import br.edu.iftm.gestorpendencias.ui.theme.cor
import br.edu.iftm.gestorpendencias.ui.theme.fundo
import br.edu.iftm.gestorpendencias.util.formatarDia
import br.edu.iftm.gestorpendencias.util.urgencia

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onNova: () -> Unit,
    onEditar: (Long) -> Unit,
    onSair: () -> Unit
) {
    val ui by vm.uiState.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    var mostrarLimites by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.mensagens.collect { snack.showSnackbar(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Painel") },
                actions = {
                    IconButton(onClick = { mostrarLimites = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Limites por contexto")
                    }
                    TextButton(onClick = onSair) { Text("Sair") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNova) {
                Icon(Icons.Default.Add, contentDescription = "Nova pendência")
            }
        },
        snackbarHost = { SnackbarHost(snack) }
    ) { pad ->
        LazyColumn(
            modifier = Modifier.padding(pad),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ResumoGlobalRow(ui.resumo) }
            item { Text("Carga por contexto", style = MaterialTheme.typography.titleMedium) }
            items(ui.contextos) { ContextoCard(it) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Pendências", style = MaterialTheme.typography.titleMedium)
                    Row(Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Status.entries.forEach { s ->
                            FilterChip(
                                selected = ui.filtroStatus == s,
                                onClick = { vm.alternarFiltroStatus(s) },
                                label = { Text(s.rotulo) }
                            )
                        }
                    }
                    Row(Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Prioridade.entries.forEach { p ->
                            FilterChip(
                                selected = ui.filtroPrioridade == p,
                                onClick = { vm.alternarFiltroPrioridade(p) },
                                label = { Text("Prioridade ${p.rotulo}") }
                            )
                        }
                    }
                }
            }
            if (ui.pendencias.isEmpty()) {
                item { Text("Nenhuma pendência encontrada.", color = Color.Gray) }
            }
            items(ui.pendencias, key = { it.id }) { p ->
                PendenciaCard(
                    p = p,
                    onClick = { onEditar(p.id) },
                    onAvancar = { vm.avancarStatus(p) },
                    onExcluir = { vm.excluir(p) }
                )
            }
        }
    }

    if (mostrarLimites) {
        LimitesDialog(
            atuais = ui.contextos,
            onSalvar = { vm.salvarLimites(it) },
            onFechar = { mostrarLimites = false }
        )
    }
}

@Composable
private fun ResumoGlobalRow(r: ResumoGlobal) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Indicador("Abertas", r.abertas.toString(), Modifier.weight(1f))
        Indicador("Concluídas na semana", r.concluidasSemana.toString(), Modifier.weight(1f))
        Indicador("Taxa de resolução", "${r.taxa}%", Modifier.weight(1f))
    }
}

@Composable
private fun Indicador(rotulo: String, valor: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(rotulo, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ContextoCard(r: ResumoContexto) {
    val lotado = r.ativas >= r.limite
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(r.contexto.rotulo, fontWeight = FontWeight.Bold)
                Text(
                    "Ativas ${r.ativas}/${r.limite}",
                    color = if (lotado) MaterialTheme.colorScheme.error else Color.Unspecified,
                    fontWeight = if (lotado) FontWeight.Bold else FontWeight.Normal
                )
            }
            LinearProgressIndicator(
                progress = { r.progresso },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = r.urgencia.cor(),
                trackColor = r.urgencia.fundo()
            )
            Text("${r.concluidas} de ${r.total} concluídas (${r.percentual}%)",
                style = MaterialTheme.typography.bodySmall)
            if (r.atrasadas > 0) {
                Text("${r.atrasadas} atrasada(s)", color = Color(0xFFC62828),
                    style = MaterialTheme.typography.labelMedium)
            }
            if (r.proximas > 0) {
                Text("${r.proximas} vencendo em até 2 dias", color = Color(0xFF8D6E00),
                    style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun PendenciaCard(p: Pendencia, onClick: () -> Unit, onAvancar: () -> Unit, onExcluir: () -> Unit) {
    val urg = p.urgencia()
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = urg.fundo(), contentColor = Color.Black)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                p.titulo, fontWeight = FontWeight.Bold,
                textDecoration = if (p.status == Status.CONCLUIDA) TextDecoration.LineThrough else null
            )
            Text("${p.contexto.rotulo} • Prioridade ${p.prioridade.rotulo}",
                style = MaterialTheme.typography.bodySmall)
            Text("Prazo ${p.prazoEpochDay.formatarDia()} • ${urg.rotulo} • ${p.status.rotulo}",
                style = MaterialTheme.typography.bodySmall, color = urg.cor())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onAvancar) {
                    Text(
                        when (p.status) {
                            Status.PENDENTE -> "Iniciar"
                            Status.EM_ANDAMENTO -> "Concluir"
                            Status.CONCLUIDA -> "Reabrir"
                        }
                    )
                }
                IconButton(onClick = onExcluir) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir")
                }
            }
        }
    }
}

@Composable
private fun LimitesDialog(
    atuais: List<ResumoContexto>,
    onSalvar: (Map<Contexto, String>) -> String?,
    onFechar: () -> Unit
) {
    val campos = remember {
        mutableStateMapOf<Contexto, String>().apply { atuais.forEach { put(it.contexto, it.limite.toString()) } }
    }
    var erro by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text("Capacidade diária por contexto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Contexto.entries.forEach { c ->
                    OutlinedTextField(
                        value = campos[c] ?: "",
                        onValueChange = { campos[c] = it; erro = null },
                        label = { Text("Máx. ativas em ${c.rotulo}") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val e = onSalvar(campos.toMap())
                if (e == null) onFechar() else erro = e
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } }
    )
}
