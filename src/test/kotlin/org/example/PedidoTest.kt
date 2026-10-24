package org.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PedidoTest {
    @Test fun unaPizzaSinDescuento() {
        val pedido = procesarPedido(1, 10, 8_000)
        assertEquals(0L, pedido.descuento)
        assertEquals(8_000L, pedido.totalPedido)
    }

    @Test fun tresPizzasSinDescuento() {
        val pedido = procesarPedido(3, 10, 8_000)
        assertEquals(0L, pedido.descuento)
        assertEquals(24_000L, pedido.totalPedido)
    }

    @Test fun cuatroPizzasConDiezPorCiento() {
        val pedido = procesarPedido(4, 10, 8_000)
        assertEquals(3_200L, pedido.descuento)
        assertEquals(28_800L, pedido.totalPedido)
    }

    @Test fun cincoPizzasConDiezPorCiento() {
        assertEquals(36_000L, procesarPedido(5, 10, 8_000).totalPedido)
    }

    @Test fun stockExactoConfirmaYAgotaInventario() {
        val pedido = procesarPedido(10, 10, 8_000)
        assertEquals("Confirmado", pedido.estadoPedido)
        assertEquals(0, pedido.ingredientesDisponibles)
        assertEquals(72_000L, pedido.totalPedido)
    }

    @Test fun stockSuperiorSeDescuenta() {
        assertEquals(7, procesarPedido(3, 10, 8_000).ingredientesDisponibles)
    }

    @Test fun stockInsuficienteConservaInventarioSinCobroNiReparto() {
        val pedido = procesarPedido(11, 10, 8_000)
        assertEquals("Rechazado por falta de inventario", pedido.estadoPedido)
        assertEquals(10, pedido.ingredientesDisponibles)
        assertEquals(0L, pedido.totalPedido)
        assertEquals(0L, pedido.descuento)
        assertEquals("Sin asignar", pedido.repartidorAsignado)
    }

    @Test fun stockCeroRechaza() {
        assertEquals("Sin asignar", procesarPedido(1, 0, 8_000).repartidorAsignado)
    }

    @Test fun confirmadoTieneRepartidor() {
        assertEquals("R01 - Repartidor de turno", procesarPedido(1, 10, 8_000).repartidorAsignado)
    }

    @Test fun cantidadesInvalidasSeRechazan() {
        listOf("0", "-1", "pizza", "", "1.5", "2147483648", null).forEach {
            assertNull(convertirCantidad(it))
        }
        assertEquals(4, convertirCantidad(" 4 "))
    }

    @Test fun procesamientoRechazaParametrosInvalidos() {
        assertFailsWith<IllegalArgumentException> { procesarPedido(0, 10, 8_000) }
        assertFailsWith<IllegalArgumentException> { procesarPedido(-1, 10, 8_000) }
        assertFailsWith<IllegalArgumentException> { procesarPedido(1, -1, 8_000) }
        assertFailsWith<IllegalArgumentException> { procesarPedido(1, 10, 0) }
    }

    @Test fun telefonoConservaPrefijoYCeros() {
        assertEquals("+56 9 0123 4567", telefonoValido(" +56 9 0123 4567 "))
        assertEquals("0012345678", telefonoValido("0012345678"))
    }

    @Test fun telefonosInvalidosSeDetectan() {
        listOf(null, "", "abc12345678", "123", "1234567890123456", "++56912345678").forEach {
            assertNull(telefonoValido(it))
        }
    }

    @Test fun textosVaciosYNulosUsanPredeterminado() {
        assertEquals("Retiro en local", textoOValorPredeterminado(null, "Retiro en local"))
        assertEquals("Cliente sin nombre", textoOValorPredeterminado("  ", "Cliente sin nombre"))
        assertEquals("José", textoOValorPredeterminado(" José ", "Cliente sin nombre"))
    }

    @Test fun entradaNulaCompletaTerminaSinExcepcion() {
        val salida = conversar(emptyList())
        listOf("Cliente: Cliente sin nombre", "Dirección: Retiro en local", "Teléfono: No informado",
            "Cantidad: 1", "Estado: Confirmado").forEach { assertTrue(salida.contains(it)) }
    }

    @Test fun entradasInvalidasRepreguntanYTelefonoInvalidoAvisa() {
        val salida = conversar(listOf("Ana", "Centro", "abc", "0", "-2", "texto", "4"))
        assertEquals(3, Regex("Cantidad inválida").findAll(salida).count())
        assertTrue(salida.contains("Teléfono inválido o ausente"))
        assertTrue(salida.contains("Cantidad: 4"))
    }

    @Test fun resumenConfirmadoContieneTodosLosDatos() {
        val salida = conversar(listOf("José", "Ñuñoa 123", "+56901234567", "4"))
        listOf("Cliente: José", "Dirección: Ñuñoa 123", "Teléfono: +56901234567",
            "Pizza: Margarita", "Precio unitario: $8.000 CLP", "Identificador: PED-001",
            "Cantidad: 4", "Subtotal solicitado: $32.000 CLP", "Descuento aplicado: $3.200 CLP",
            "Total a pagar: $28.800 CLP", "Estado: Confirmado",
            "Repartidor asignado: R01 - Repartidor de turno", "Inventario restante: 6").forEach {
            assertTrue(salida.contains(it), "Falta en el resumen: $it")
        }
    }

    @Test fun resumenRechazadoEsConsistente() {
        val salida = conversar(listOf("Ana", "Centro", "12345678", "11"))
        listOf("Pedido rechazado: ingredientes insuficientes", "Total a pagar: $0 CLP",
            "Descuento aplicado: $0 CLP", "Estado: Rechazado por falta de inventario",
            "Repartidor asignado: Sin asignar", "Inventario restante: 10").forEach {
            assertTrue(salida.contains(it))
        }
    }

    @Test fun multiplicacionNoDesbordaIntYDescuentoTruncaFraccion() {
        val pedido = procesarPedido(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)
        assertEquals(4_611_686_014_132_420_609L, pedido.subtotal)
        assertEquals(4_150_517_412_719_178_549L, pedido.totalPedido)
        assertEquals(29L, procesarPedido(4, 4, 8).totalPedido)
    }

    private fun conversar(entradas: List<String>): String {
        val iterador = entradas.iterator()
        val salida = mutableListOf<String>()
        ejecutarPedido({ if (iterador.hasNext()) iterador.next() else null }, { salida.add(it) })
        return salida.joinToString("\n")
    }
}
