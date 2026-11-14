package cl.duoc.pizzeriaapp.domain

import cl.duoc.pizzeriaapp.domain.inventory.Inventario
import cl.duoc.pizzeriaapp.domain.inventory.inventarioInicial
import cl.duoc.pizzeriaapp.domain.model.LineaPedido
import cl.duoc.pizzeriaapp.domain.model.Margarita
import cl.duoc.pizzeriaapp.domain.model.Pepperoni
import cl.duoc.pizzeriaapp.domain.model.Producto
import cl.duoc.pizzeriaapp.domain.support.normalizarTelefono
import cl.duoc.pizzeriaapp.domain.support.telefonoValido
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.Test

class UsabilidadSemana3Test {
    private val cliente = listOf("1", "José", "Ñuñoa 123", "956781234", "jose@example.com")

    @Test fun normalizaMovilesNacionalesYConCodigoDePais() {
        for (entrada in listOf("956781234", "+56956781234", "+56 9 5678 1234", " (+56) 9 5678 1234 ")) {
            assertEquals("(+56) 9 5678 1234", normalizarTelefono(entrada))
        }
    }

    @Test fun normalizaFijosYConservaTodosLosDigitosNacionales() {
        for (entrada in listOf("223456789", "+56223456789", "(2) 2345-6789")) {
            val normalizado = assertNotNull(normalizarTelefono(entrada))
            assertEquals("(+56) 2 2345 6789", normalizado)
            assertEquals(entrada.filter(Char::isDigit).takeLast(9), normalizado.filter(Char::isDigit).drop(2))
        }
        assertEquals("(+56) 9 0123 4567", normalizarTelefono("+56 (9) 0123-4567"))
    }

    @Test fun rechazaPrefijosMalFormadosLongitudesIncorrectasYLetras() {
        for (entrada in listOf("9567812", "+5695678123", "+569567812345", "1234567890123456", "+56 9 5678 abcd",
            "++56956781234", "56+956781234", "(+57) 9 5678 1234", "+001234567890")) {
            assertNull(telefonoValido(entrada), entrada)
            assertNull(normalizarTelefono(entrada), entrada)
        }
    }

    @Test fun capacidadesInicialesUsanTodosLosIngredientes() {
        val inventario = inventarioInicial()
        assertEquals(10, inventario.capacidad(Margarita(), emptyList()))
        assertEquals(5, inventario.capacidad(Pepperoni(), emptyList()))
        val pocoQueso = Inventario(mapOf("Masa" to 10L, "Tomate" to 20L, "Queso" to 5L))
        assertEquals(2, pocoQueso.capacidad(Margarita(), emptyList()))
        assertEquals(0, pocoQueso.capacidad(Pepperoni(), emptyList()))
        assertEquals(0, Inventario(emptyMap()).capacidad(Margarita(), emptyList()))
    }

    @Test fun capacidadRestaLineasMixtasYRepetidasSinConsumir() {
        val inventario = inventarioInicial()
        val inicial = inventario.consultar()
        val primera = listOf(LineaPedido(Margarita(), 8))
        assertEquals(2, inventario.capacidad(Pepperoni(), primera))
        assertEquals(2, inventario.capacidad(Margarita(), primera))
        val repetidas = primera + LineaPedido(Margarita(), 2)
        assertEquals(0, inventario.capacidad(Margarita(), repetidas))
        assertEquals(0, inventario.capacidad(Pepperoni(), repetidas + LineaPedido(Pepperoni(), 1)))
        assertEquals(inicial, inventario.consultar())
    }

    @Test fun capacidadRecalculaTrasReabastecerYAplicaLimiteSinDesbordar() {
        val inventario = Inventario(mapOf("Masa" to Long.MAX_VALUE, "Tomate" to Long.MAX_VALUE, "Queso" to 0L))
        assertEquals(0, inventario.capacidad(Margarita(), emptyList()))
        inventario.reabastecer("Queso", Long.MAX_VALUE)
        assertEquals(100, inventario.capacidad(Margarita(), emptyList()))
        val gigante = Producto("Gigante", 1, mapOf("Masa" to Long.MAX_VALUE))
        assertFailsWith<ArithmeticException> { inventario.capacidad(Margarita(), listOf(LineaPedido(gigante, 2))) }
        assertEquals(Long.MAX_VALUE, inventario.consultar().getValue("Masa"))
    }
}
