package br.edu.iftm.gestorpendencias.ui.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.iftm.gestorpendencias.data.Contexto
import br.edu.iftm.gestorpendencias.data.Pendencia
import br.edu.iftm.gestorpendencias.data.PendenciaRepository
import br.edu.iftm.gestorpendencias.data.Prioridade
import br.edu.iftm.gestorpendencias.data.Status
import br.edu.iftm.gestorpendencias.util.Validacao
import br.edu.iftm.gestorpendencias.util.diaAtual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormUiState(
    val titulo: String = "",
    val contexto: Contexto? = null,
    val prioridade: Prioridade = Prioridade.MEDIA,
    val prazoEpochDay: Long? = null,
    val status: Status = Status.PENDENTE,
    val erroTitulo: String? = null,
    val erroContexto: String? = null,
    val erroPrazo: String? = null,
    val erroGeral: String? = null,
    val editando: Boolean = false,
    val salvo: Boolean = false
)

class FormViewModel(
    private val repo: PendenciaRepository,
    private val uid: String,
    private val id: Long
) : ViewModel() {

    private val _ui = MutableStateFlow(FormUiState(editando = id != 0L))
    val ui: StateFlow<FormUiState> = _ui.asStateFlow()
    private var original: Pendencia? = null

    init {
        if (id != 0L) viewModelScope.launch {
            repo.buscar(id)?.let { p ->
                original = p
                _ui.update {
                    it.copy(
                        titulo = p.titulo, contexto = p.contexto, prioridade = p.prioridade,
                        prazoEpochDay = p.prazoEpochDay, status = p.status
                    )
                }
            }
        }
    }

    fun onTitulo(v: String) = _ui.update { it.copy(titulo = v, erroTitulo = null, erroGeral = null) }
    fun onContexto(v: Contexto) = _ui.update { it.copy(contexto = v, erroContexto = null, erroGeral = null) }
    fun onPrioridade(v: Prioridade) = _ui.update { it.copy(prioridade = v) }
    fun onPrazo(v: Long) = _ui.update { it.copy(prazoEpochDay = v, erroPrazo = null) }
    fun onStatus(v: Status) = _ui.update { it.copy(status = v, erroGeral = null) }

    fun salvar() {
        val s = _ui.value
        val ctx = s.contexto
        val dia = s.prazoEpochDay
        val exigirFuturo = s.status != Status.CONCLUIDA &&
            (original == null || original?.prazoEpochDay != dia)

        val eTitulo = Validacao.titulo(s.titulo)
        val eContexto = if (ctx == null) "Selecione um contexto" else null
        val ePrazo = Validacao.prazo(dia, diaAtual(), exigirFuturo)

        if (eTitulo != null || eContexto != null || ePrazo != null || ctx == null || dia == null) {
            _ui.update { it.copy(erroTitulo = eTitulo, erroContexto = eContexto, erroPrazo = ePrazo) }
            return
        }

        viewModelScope.launch {
            val base = original ?: Pendencia(
                userId = uid, titulo = "", contexto = ctx, prioridade = s.prioridade,
                prazoEpochDay = dia, status = s.status
            )
            val erro = repo.salvar(
                base.copy(
                    titulo = s.titulo.trim(), contexto = ctx, prioridade = s.prioridade,
                    prazoEpochDay = dia, status = s.status
                )
            )
            _ui.update { if (erro != null) it.copy(erroGeral = erro) else it.copy(salvo = true) }
        }
    }

    companion object {
        fun factory(repo: PendenciaRepository, uid: String, id: Long) = viewModelFactory {
            initializer { FormViewModel(repo, uid, id) }
        }
    }
}
