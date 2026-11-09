package org.example

import kotlin.test.*

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

    @Test fun resumenMuestraTelefonoChilenoSinAvisoDeFormato() {
        val salida = conversar(cliente + listOf("1", "1", "n", "", "0"), Jornada())
        assertTrue(salida.any { "Teléfono: (+56) 9 5678 1234" in it.lineSequence() })
        assertFalse(salida.any { "Teléfono inválido" in it || "no es representable" in it })
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

    @Test fun pedidoMixtoRepiteCantidadYAdmiteElMaximoConLasLineasPrevias() {
        val jornada = Jornada()
        val inicial = jornada.inventario.consultar()
        val salida = conversar(cliente + listOf("1", "8", "s", "2", "3", "2", "n", "", "0"), jornada) {
            assertEquals(inicial, jornada.inventario.consultar())
        }
        assertTrue(salida.contains("Máximo disponible: 2."))
        assertEquals(1, salida.count { it == "Nombre del cliente:" })
        assertEquals(listOf(8, 2), jornada.pedidos().single().productos.map { it.cantidad })
        assertEquals(mapOf("Masa" to 0L, "Tomate" to 0L, "Queso" to 0L, "Pepperoni" to 3L), jornada.inventario.consultar())
    }

    @Test fun variedadAgotadaPermiteElegirOtraSinPedirCantidadImposible() {
        val jornada = Jornada()
        val salida = conversar(cliente + listOf("2", "5", "s", "2", "1", "5", "n", "", "0"), jornada)
        assertTrue(salida.any { it.contains("Pepperoni está agotada") })
        assertEquals(1, salida.count { it == "¿Cuántas pizzas Pepperoni desea pedir?" })
        assertEquals(listOf("Pepperoni", "Margarita"), jornada.pedidos().single().productos.map { it.producto.nombre })
    }

    @Test fun cancelarORecibirEofTrasAgotarStockConservaElInventario() {
        for (resto in listOf(listOf("C", "0"), emptyList())) {
            val jornada = Jornada()
            val inicial = jornada.inventario.consultar()
            val salida = conversar(cliente + listOf("1", "10", "s", "2") + resto, jornada)
            assertTrue(salida.any { it.contains("Pepperoni está agotada") })
            assertTrue(salida.any { it.contains("Registro del pedido cancelado") })
            assertTrue(jornada.pedidos().isEmpty())
            assertEquals(inicial, jornada.inventario.consultar())
        }
    }

    @Test fun aceptaCienEnConsolaSiLaRecetaTieneStockSuficiente() {
        val jornada = Jornada(Inventario(mapOf("Masa" to 101L, "Tomate" to 202L, "Queso" to 202L)))
        val salida = conversar(cliente + listOf("1", "101", "100", "n", "", "0"), jornada)
        assertTrue(salida.contains("Unidades disponibles para este pedido: 100"))
        assertEquals(1, salida.count { it.startsWith("Cantidad inválida") })
        assertEquals(100, jornada.pedidos().single().cantidadPizzas)
        assertEquals(1L, jornada.inventario.consultar().getValue("Masa"))
    }

    @Test fun laConfirmacionFinalRechazaAtomicamenteSiCambiaElStockTrasSeleccionar() {
        val jornada = Jornada()
        var cambiado = false
        val salida = conversar(cliente + listOf("1", "1", "s", "2", "1", "n", "", "0"), jornada) { mensajes ->
            if (!cambiado && mensajes.last().startsWith("Notas opcionales")) {
                assertTrue(jornada.inventario.consumir(mapOf("Pepperoni" to 5L)).isEmpty())
                cambiado = true
            }
        }
        assertTrue(salida.any { "Estado: Rechazado por falta de inventario" in it.lineSequence() })
        assertEquals(0L, jornada.pedidos().single().calcularImportes().total)
        assertEquals(mapOf("Masa" to 10L, "Tomate" to 20L, "Queso" to 20L, "Pepperoni" to 0L), jornada.inventario.consultar())
    }

    private fun conversar(
        entradas: List<String>, jornada: Jornada, antesDeLeer: (List<String>) -> Unit = {}
    ): List<String> {
        val salida = mutableListOf<String>()
        val iterador = entradas.iterator()
        var lecturas = 0
        ejecutarJornada({
            assertTrue(++lecturas <= entradas.size + 2)
            if (iterador.hasNext()) {
                if (lecturas < entradas.size) antesDeLeer(salida)
                iterador.next()
            } else null
        }, salida::add, jornada)
        return salida
    }
}
