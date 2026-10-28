package org.example

import kotlin.test.*

class RecuperacionEntradaTest {
    private val cabecera = listOf("1", "José", "Ñuñoa 123", "+56901234567", "jose@example.com")

    @Test fun seleccionInvalidaSeguidaDeValidaRepiteSoloLaPregunta() {
        val (jornada, salida) = conversar(cabecera + listOf("sdfjn", "2", "1", "n", "", "0"))
        assertEquals(1, jornada.pedidos().size)
        assertEquals("Pepperoni", jornada.pedidos().single().productos.single().producto.nombre)
        assertEquals(2, Regex("Seleccione pizza ").findAll(salida).count())
        assertTrue(salida.contains("Pizza inexistente. Seleccione una opción válida."))
        assertTrue(salida.contains("Pizza Pepperoni seleccionada."))
        assertFalse(salida.contains("Datos inválidos:"))
    }

    @Test fun variasSeleccionesInvalidasIncluyenDesbordamientoYRangos() {
        val invalidas = listOf("sdfjn", "-1", "0", "3", "2147483648", "-2147483648", "1.5")
        val (jornada, salida) = conversar(cabecera + invalidas + listOf("2", "1", "n", "", "0"))
        assertEquals(invalidas.size, Regex("Pizza inexistente").findAll(salida).count())
        assertEquals(1, jornada.pedidos().size)
        assertEquals(9L, jornada.inventario.consultar()["Masa"])
    }

    @Test fun datosDelClienteSeConservanSinVolverAPreguntar() {
        val (jornada, salida) = conversar(cabecera + listOf("abc", "-1", "1", "2", "n", "", "0"))
        val cliente = jornada.pedidos().single().cliente
        assertEquals("José", cliente.nombre)
        assertEquals("Ñuñoa 123", cliente.direccion)
        assertEquals("+56901234567", cliente.telefono)
        assertEquals("jose@example.com", cliente.correo)
        for (campo in listOf("Nombre del cliente:", "Dirección de entrega:", "Teléfono de contacto:", "Correo electrónico:")) {
            assertEquals(1, salida.lineSequence().count { it == campo }, campo)
        }
        assertEquals(2, Regex("1 Nuevo pedido").findAll(salida).count())
    }

    @Test fun productosAnterioresSeConservanAlCorregirSegundaSeleccion() {
        val (jornada, _) = conversar(cabecera + listOf("1", "2", "s", "abc", "-1", "2", "2", "n", "sin sal", "0"))
        val pedido = jornada.pedidos().single()
        assertEquals(listOf("Margarita", "Pepperoni"), pedido.productos.map { it.producto.nombre })
        assertEquals(listOf(2, 2), pedido.productos.map { it.cantidad })
        assertEquals("sin sal", pedido.notas)
        assertEquals(Importes(34000, 3400, 5814, 36414), pedido.calcularImportes())
        assertEquals(mapOf("Masa" to 6L, "Tomate" to 12L, "Queso" to 12L, "Pepperoni" to 3L), jornada.inventario.consultar())
    }

    @Test fun cantidadCeroSeguidaDeValidaConservaProducto() = comprobarCantidadCorregida("0")
    @Test fun cantidadNegativaSeguidaDeValidaConservaProducto() = comprobarCantidadCorregida("-2")
    @Test fun cantidadNoNumericaSeguidaDeValidaConservaProducto() = comprobarCantidadCorregida("texto")
    @Test fun cantidadFueraDeIntSeguidaDeValidaConservaProducto() = comprobarCantidadCorregida("2147483648")

    @Test fun eofDuranteSeleccionCancelaSinInterpretarloComoMargarita() {
        val (jornada, salida) = conversar(cabecera)
        assertTrue(jornada.pedidos().isEmpty())
        assertTrue(salida.contains("Registro del pedido cancelado"))
        assertFalse(salida.contains("Pizza inexistente"))
        assertFalse(salida.contains("Disponibilidad confirmada"))
        assertEquals(10L, jornada.inventario.consultar()["Masa"])
    }

    @Test fun eofEnCadaCampoYTrasLineaValidaPreservaLaJornadaPrevia() {
        val completa = cabecera + listOf("1", "2", "s", "2", "1", "n", "notas")
        for (corte in 1 until completa.size) {
            val jornada = Jornada()
            val previo = jornada.registrar(Cliente("Ana", null, null, null),
                listOf(LineaPedido(catalogoInicial()[0], 1)), null).first
            previo.cambiarEstado(EstadoPedido.LISTO)
            val repartidor = jornada.enviar(previo)!!
            val inventarioPrevio = jornada.inventario.consultar()
            val (_, salida) = conversar(completa.take(corte), jornada)
            assertEquals(listOf(previo), jornada.pedidos().toList(), "Corte $corte")
            assertEquals(inventarioPrevio, jornada.inventario.consultar(), "Corte $corte")
            assertEquals(EstadoPedido.EN_CAMINO, previo.estado)
            assertFalse(repartidor.disponible)
            assertTrue(salida.contains("Registro del pedido cancelado"))
        }
    }

    @Test fun intentosInvalidosNoTienenEfectosAntesDeCompletarElAlta() {
        val jornada = Jornada()
        val inicial = jornada.inventario.consultar()
        val entradas = (cabecera + listOf("sdfjn", "-1", "1", "0", "-2", "texto", "2147483648", "2", "n", "")).iterator()
        ejecutarJornada(leer = {
            if (entradas.hasNext()) {
                assertTrue(jornada.pedidos().isEmpty())
                assertEquals(inicial, jornada.inventario.consultar())
                assertTrue(jornada.repartidores.all { it.disponible })
                entradas.next()
            } else null
        }, mostrar = {}, jornada = jornada)
        assertEquals(1, jornada.pedidos().size)
        assertEquals("PED-001", jornada.pedidos().single().idPedido)
        assertEquals(8L, jornada.inventario.consultar()["Masa"])
    }

    @Test fun entradasVaciasConservanPredeterminadosSinConfundirseConEof() {
        val (jornada, salida) = conversar(listOf("1", "", "", "", "", "", "", "1", "", "", "0"))
        val pedido = jornada.pedidos().single()
        assertEquals("Cliente sin nombre", pedido.cliente.nombre)
        assertEquals("Retiro en local", pedido.cliente.direccion)
        assertEquals("No informado", pedido.cliente.telefono)
        assertEquals("correo@invalido.com", pedido.cliente.correo)
        assertEquals("Margarita", pedido.productos.single().producto.nombre)
        assertNull(pedido.notas)
        assertTrue(salida.contains("Notas: Sin notas"))
        assertEquals(1, Regex("Cantidad inválida").findAll(salida).count())
    }

    private fun comprobarCantidadCorregida(invalida: String) {
        val (jornada, salida) = conversar(cabecera + listOf("2", invalida, "3", "n", "", "0"))
        val linea = jornada.pedidos().single().productos.single()
        assertEquals("Pepperoni", linea.producto.nombre)
        assertEquals(3, linea.cantidad)
        assertEquals(1, Regex("Cantidad inválida").findAll(salida).count())
        assertEquals(1, Regex("Seleccione pizza ").findAll(salida).count())
        assertEquals(7L, jornada.inventario.consultar()["Masa"])
    }

    private fun conversar(entradas: List<String>, jornada: Jornada = Jornada()): Pair<Jornada, String> {
        val iterador = entradas.iterator()
        val salida = mutableListOf<String>()
        var lecturas = 0
        ejecutarJornada({
            assertTrue(++lecturas <= entradas.size + 2, "El fin de entrada no debe repetirse indefinidamente.")
            if (iterador.hasNext()) iterador.next() else null
        }, salida::add, jornada)
        return jornada to salida.joinToString("\n")
    }
}
