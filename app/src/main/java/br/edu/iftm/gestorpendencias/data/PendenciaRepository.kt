package br.edu.iftm.gestorpendencias.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PendenciaRepository(private val dao: PendenciaDao) {

    fun pendencias(uid: String): Flow<List<Pendencia>> = dao.observarPendencias(uid)

    /** Limite de cada contexto (usa o padrão quando o usuário ainda não configurou). */
    fun capacidades(uid: String): Flow<Map<Contexto, Int>> =
        dao.observarCapacidades(uid).map { lista ->
            Contexto.entries.associateWith { c ->
                lista.firstOrNull { it.contexto == c }?.limite ?: c.limitePadrao
            }
        }

    suspend fun buscar(id: Long): Pendencia? = dao.buscar(id)

    suspend fun excluir(p: Pendencia) = dao.excluir(p)

    suspend fun salvarLimite(uid: String, contexto: Contexto, limite: Int) =
        dao.salvarCapacidade(CapacidadeContexto(uid, contexto, limite))

    /**
     * Insere ou atualiza respeitando o WIP limit do contexto.
     * @return mensagem de erro, ou null se salvou com sucesso.
     */
    suspend fun salvar(p: Pendencia): String? {
        if (p.status != Status.CONCLUIDA) {
            val limite = dao.buscarLimite(p.userId, p.contexto) ?: p.contexto.limitePadrao
            val ativas = dao.contarAtivas(p.userId, p.contexto, p.id)
            if (ativas >= limite) {
                return "Limite de $limite pendências ativas em ${p.contexto.rotulo} atingido. " +
                    "Conclua uma antes de abrir outra."
            }
        }
        val final = p.copy(
            concluidaEm = if (p.status == Status.CONCLUIDA) p.concluidaEm ?: System.currentTimeMillis() else null
        )
        if (final.id == 0L) dao.inserir(final) else dao.atualizar(final)
        return null
    }
}
