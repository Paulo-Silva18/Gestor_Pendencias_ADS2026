package br.edu.iftm.gestorpendencias

import br.edu.iftm.gestorpendencias.util.Validacao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacaoTest {
    @Test fun tituloVazioEhInvalido() = assertNotNull(Validacao.titulo("   "))
    @Test fun tituloValidoPassa() = assertNull(Validacao.titulo("Entregar prova"))
    @Test fun prazoNuloEhInvalido() = assertNotNull(Validacao.prazo(null, 100, true))
    @Test fun prazoPassadoEhInvalido() = assertNotNull(Validacao.prazo(99, 100, true))
    @Test fun prazoPassadoPermitidoQuandoNaoExigeFuturo() = assertNull(Validacao.prazo(99, 100, false))
    @Test fun limiteForaDaFaixaEhNulo() = assertEquals(null, Validacao.limite("0"))
    @Test fun limiteValido() = assertEquals(3, Validacao.limite(" 3 "))
    @Test fun emailInvalido() = assertNotNull(Validacao.email("abc"))
}
