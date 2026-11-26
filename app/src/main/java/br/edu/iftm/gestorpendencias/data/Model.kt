package br.edu.iftm.gestorpendencias.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Contexto(val rotulo: String, val limitePadrao: Int) {
    ESTUDOS_TRABALHO("Estudos/Trabalho", 3),
    CASA_PESSOAL("Casa/Pessoal", 4),
    URGENCIAS("Urgências", 2)
}

enum class Prioridade(val rotulo: String) {
    ALTA("Alta"), MEDIA("Média"), BAIXA("Baixa")
}

enum class Status(val rotulo: String) {
    PENDENTE("Pendente"), EM_ANDAMENTO("Em Andamento"), CONCLUIDA("Concluída")
}

@Entity(tableName = "pendencias", indices = [Index("userId")])
data class Pendencia(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val titulo: String,
    val contexto: Contexto,
    val prioridade: Prioridade,
    val prazoEpochDay: Long,
    val status: Status,
    val criadaEm: Long = System.currentTimeMillis(),
    val concluidaEm: Long? = null
)

/** Limite de pendências ativas (WIP limit) de um contexto, por usuário. */
@Entity(tableName = "capacidades", primaryKeys = ["userId", "contexto"])
data class CapacidadeContexto(
    val userId: String,
    val contexto: Contexto,
    val limite: Int
)
