package br.edu.iftm.gestorpendencias.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.iftm.gestorpendencias.data.Contexto
import br.edu.iftm.gestorpendencias.data.Pendencia
import br.edu.iftm.gestorpendencias.data.PendenciaRepository
import br.edu.iftm.gestorpendencias.data.Prioridade
import br.edu.iftm.gestorpendencias.data.Status
import br.edu.iftm.gestorpendencias.util.Urgencia
import br.edu.iftm.gestorpendencias.util.Validacao
import br.edu.iftm.gestorpendencias.util.urgencia
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ResumoContexto(
    val contexto: Contexto,
    val limite: Int,
    val ativas: Int,
    val concluidas: Int,
    val total: Int,
    val atrasadas: Int,
    val proximas: Int
) {
    val progresso: Float get() = if (total == 0) 0f else concluidas.toFloat() / total
    val percentual: Int get() = (progresso * 100).toInt()
    val urgencia: Urgencia
        get() = when {
            atrasadas > 0 -> Urgencia.ATRASADA
            proximas > 0 -> Urgencia.PROXIMA
            else -> Urgencia.NO_PRAZO
        }
}

data class ResumoGlobal(val abertas: Int = 0, val concluidasSemana: Int = 0, val taxa: Int = 0)

data class HomeUiState(
    val contextos: List<ResumoContexto> = emptyList(),
    val resumo: ResumoGlobal = ResumoGlobal(),
    val pendencias: List<Pendencia> = emptyList(),
    val filtroStatus: Status? = null,
    val filtroPrioridade: Prioridade? = null
)

class HomeViewModel(
    private val repo: PendenciaRepository,
    private val uid: String
) : ViewModel() {

    private val filtroStatus = MutableStateFlow<Status?>(null)
    private val filtroPrioridade = MutableStateFlow<Prioridade?>(null)
    private val _mensagens = MutableSharedFlow<String>()
    val mensagens: SharedFlow<String> = _mensagens.asSharedFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        repo.pendencias(uid), repo.capacidades(uid), filtroStatus, filtroPrioridade
    ) { lista, caps, fs, fp ->
        val umaSemanaAtras = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000

        val contextos = Contexto.entries.map { c ->
            val doContexto = lista.filter { it.contexto == c }
            ResumoContexto(
                contexto = c,
                limite = caps[c] ?: c.limitePadrao,
                ativas = doContexto.count { it.status != Status.CONCLUIDA },
                concluidas = doContexto.count { it.status == Status.CONCLUIDA },
                total = doContexto.size,
                atrasadas = doContexto.count { it.urgencia() == Urgencia.ATRASADA },
                proximas = doContexto.count { it.urgencia() == Urgencia.PROXIMA }
            )
        }
        val concluidas = lista.count { it.status == Status.CONCLUIDA }
        val resumo = ResumoGlobal(
            abertas = lista.size - concluidas,
            concluidasSemana = lista.count {
                it.status == Status.CONCLUIDA && (it.concluidaEm ?: 0L) >= umaSemanaAtras
            },
            taxa = if (lista.isEmpty()) 0 else concluidas * 100 / lista.size
        )
        val filtradas = lista
            .filter { (fs == null || it.status == fs) && (fp == null || it.prioridade == fp) }
            .sortedWith(compareBy({ it.status == Status.CONCLUIDA }, { it.prazoEpochDay }))

        HomeUiState(contextos, resumo, filtradas, fs, fp)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun alternarFiltroStatus(s: Status) { filtroStatus.value = if (filtroStatus.value == s) null else s }
    fun alternarFiltroPrioridade(p: Prioridade) {
        filtroPrioridade.value = if (filtroPrioridade.value == p) null else p
    }

    fun avancarStatus(p: Pendencia) = viewModelScope.launch {
        val proximo = when (p.status) {
            Status.PENDENTE -> Status.EM_ANDAMENTO
            Status.EM_ANDAMENTO -> Status.CONCLUIDA
            Status.CONCLUIDA -> Status.PENDENTE
        }
        repo.salvar(p.copy(status = proximo))?.let { _mensagens.emit(it) }
    }

    fun excluir(p: Pendencia) = viewModelScope.launch { repo.excluir(p) }

    /** Valida e salva os limites. Retorna mensagem de erro ou null. */
    fun salvarLimites(valores: Map<Contexto, String>): String? {
        val convertidos = valores.mapValues { Validacao.limite(it.value) }
        if (convertidos.values.any { it == null }) return "Use números entre 1 e 20"
        viewModelScope.launch {
            convertidos.forEach { (c, l) -> repo.salvarLimite(uid, c, l!!) }
        }
        return null
    }

    companion object {
        fun factory(repo: PendenciaRepository, uid: String) = viewModelFactory {
            initializer { HomeViewModel(repo, uid) }
        }
    }
}
