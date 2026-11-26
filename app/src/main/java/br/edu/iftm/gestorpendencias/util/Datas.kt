package br.edu.iftm.gestorpendencias.util

import br.edu.iftm.gestorpendencias.data.Pendencia
import br.edu.iftm.gestorpendencias.data.Status
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun diaAtual(): Long = LocalDate.now().toEpochDay()

fun Long.formatarDia(): String =
    LocalDate.ofEpochDay(this).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

enum class Urgencia(val rotulo: String) {
    NO_PRAZO("No prazo"),
    PROXIMA("Vence em breve"),
    ATRASADA("Atrasada"),
    CONCLUIDA("Concluída")
}

/** verde (>2 dias) -> amarelo (até 2 dias) -> vermelho (prazo estourado). */
fun Pendencia.urgencia(hoje: Long = diaAtual()): Urgencia = when {
    status == Status.CONCLUIDA -> Urgencia.CONCLUIDA
    prazoEpochDay < hoje -> Urgencia.ATRASADA
    prazoEpochDay - hoje <= 2 -> Urgencia.PROXIMA
    else -> Urgencia.NO_PRAZO
}
