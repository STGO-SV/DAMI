package cl.duoc.pizzeriaapp.domain

import cl.duoc.pizzeriaapp.domain.support.convertirCantidad
import cl.duoc.pizzeriaapp.domain.support.correoValido
import cl.duoc.pizzeriaapp.domain.support.normalizarTelefono
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test

class ValidacionSemana3Test {
    private val cabecera = listOf("1", "José", "Ñuñoa 123", "+12 345 678 9012")

    @Test fun regexAdmiteFormatoAcademicoYRechazaCadenasMalFormadas() {
        for (correo in listOf("cliente@example.com", "cliente@example.cl", "ana+pedido@example.cl", "a.b@sub.example.com")) {
            assertEquals(correo, correoValido(correo))
        }
        for (correo in listOf("sin-arroba", "a@", "@example.com", "a b@example.com", "a!b@example.com", "a..b@example.cl",
            "a@-dominio.com", "a@dominio", "", "á@example.com", "a@dominio_.com", "a".repeat(255) + "@b.cl")) {
            assertNull(correoValido(correo), correo)
        }
    }

    @Test fun telefonoCompatibleSeNormalizaSinCambiarDigitosYEsIdempotente() {
        for (entrada in listOf("+12 345 678 9012", "123456789012", "+12 (345) 678-9012")) {
            assertEquals("+12 (345) 678-9012", normalizarTelefono(entrada))
            assertEquals(entrada.filter(Char::isDigit), normalizarTelefono(entrada)!!.filter(Char::isDigit))
        }
        assertEquals("000123456789", normalizarTelefono("000123456789"))
    }

    @Test fun otrasLongitudesValidasConservanPrefijosYCeros() {
        for (entrada in listOf("0012345678", "12345678", "123456789012345", "561234567890")) {
            assertEquals(entrada, normalizarTelefono(entrada))
        }
        for (entrada in listOf(null, "", "++123456789012", "123456789012 ext 4", "1234567", "1234567890123456", "１２３４５６７８")) {
            assertNull(normalizarTelefono(entrada))
        }
    }

    @Test fun rangoIncluyeUnoYCienYRechazaValoresFueraDelIntervalo() {
        for (valor in listOf(1, 3, 4, 100)) assertEquals(valor, convertirCantidad(valor.toString()))
        for (valor in listOf("0", "101", "-1", "texto", "1.5", "2147483648", "2147483647", "", null)) {
            assertNull(convertirCantidad(valor), valor)
        }
    }
}
