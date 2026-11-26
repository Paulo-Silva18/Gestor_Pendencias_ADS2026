package br.edu.iftm.gestorpendencias.util

object Validacao {
    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun email(valor: String): String? =
        if (emailRegex.matches(valor.trim())) null else "E-mail inválido"

    fun senha(valor: String): String? =
        if (valor.length >= 6) null else "A senha deve ter ao menos 6 caracteres"

    fun titulo(valor: String): String? = when {
        valor.isBlank() -> "Informe um título"
        valor.trim().length < 3 -> "Mínimo de 3 caracteres"
        valor.trim().length > 80 -> "Máximo de 80 caracteres"
        else -> null
    }

    fun prazo(epochDay: Long?, hoje: Long, exigirFuturo: Boolean): String? = when {
        epochDay == null -> "Selecione um prazo"
        exigirFuturo && epochDay < hoje -> "O prazo não pode estar no passado"
        else -> null
    }

    fun limite(valor: String): Int? = valor.trim().toIntOrNull()?.takeIf { it in 1..20 }
}
