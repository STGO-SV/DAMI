package org.example

import kotlin.test.*

class ConfirmacionSNTest {
    private val cabecera = listOf("1", "José", "Ñuñoa 123", "+56901234567", "jose@example.com")
    private val pregunta = "¿Agregar otra pizza al pedido? (S/N):"
    private val error = "Respuesta inválida. Ingrese S o N."

    @Test fun aceptaSMayusculaYMinuscula() {
        for (entrada in listOf("S", "s")) assertEquals(true, leerRespuesta(listOf(entrada)).first)
    }

    @Test fun aceptaNMayusculaYMinuscula() {
        for (entrada in listOf("N", "n")) assertEquals(false, leerRespuesta(listOf(entrada)).first)
    }

    @Test fun recortaEspaciosExterioresSinAceptarPalabras() {
        for (entrada in listOf(" S ", "\ts\t")) assertEquals(true, leerRespuesta(listOf(entrada)).first)
        for (entrada in listOf(" N ", "\tn\t")) assertEquals(false, leerRespuesta(listOf(entrada)).first)
        assertEquals(listOf(pregunta, error, pregunta), leerRespuesta(listOf("s n", "n")).second)
    }

    @Test fun cadenaVaciaRepiteLaMismaPregunta() {
        val (respuesta, salida) = leerRespuesta(listOf("", "N"))
        assertEquals(false, respuesta)
        assertEquals(listOf(pregunta, error, pregunta), salida)
    }

    @Test fun soloEspaciosRepiteLaMismaPregunta() {
        val (respuesta, salida) = leerRespuesta(listOf("   ", "S"))
        assertEquals(true, respuesta)
        assertEquals(listOf(pregunta, error, pregunta), salida)
    }

    @Test fun textoArbitrarioSeguidoDeSAfirma() {
        val (respuesta, salida) = leerRespuesta(listOf("quizás", "S"))
        assertEquals(true, respuesta)
        assertEquals(listOf(pregunta, error, pregunta), salida)
    }

    @Test fun textoArbitrarioSeguidoDeNNiega() {
        val (respuesta, salida) = leerRespuesta(listOf("abc", "N"))
        assertEquals(false, respuesta)
        assertEquals(listOf(pregunta, error, pregunta), salida)
    }

    @Test fun variasInvalidasNoSeInterpretanComoNegativa() {
        val invalidas = listOf("", "  ", "sí", "no", "abc", "0", "1", "2", "ss", "nn", "ſ")
        val (respuesta, salida) = leerRespuesta(invalidas + "s")
        assertEquals(true, respuesta)
        assertEquals(invalidas.size, salida.count { it == error })
        assertEquals(invalidas.size + 1, salida.count { it == pregunta })
    }

    @Test fun eofSinRespuestaCancelaEnUnaSolaLectura() {
        val (respuesta, salida) = leerRespuesta(emptyList())
        assertNull(respuesta)
        assertEquals(listOf(pregunta), salida)
    }

    @Test fun eofDespuesDeInvalidasCancelaSinConvertirseEnN() {
        val (respuesta, salida) = leerRespuesta(listOf("sí", "0", ""))
        assertNull(respuesta)
        assertEquals(3, salida.count { it == error })
        assertEquals(4, salida.count { it == pregunta })
    }

    @Test fun respuestaInvalidaNoTerminaLaSeleccionDeProductos() {
        val jornada = Jornada()
        val entradas = (cabecera + listOf("1", "2", "quizás", "2", "s", "2", "2", "n", "sin sal", "0")).iterator()
        val salida = mutableListOf<String>()
        ejecutarJornada({ if (entradas.hasNext()) entradas.next() else null }, salida::add, jornada)
        val pedido = jornada.pedidos().single()
        assertEquals(listOf("Margarita", "Pepperoni"), pedido.productos.map { it.producto.nombre })
        assertEquals(listOf(2, 2), pedido.productos.map { it.cantidad })
        assertEquals(2, salida.count { it == "Respuesta inválida. Ingrese S o N." })
        assertEquals("sin sal", pedido.notas)
        assertEquals("José", pedido.cliente.nombre)
        assertEquals("Ñuñoa 123", pedido.cliente.direccion)
        assertEquals("(+56) 9 0123 4567", pedido.cliente.telefono)
        assertEquals("jose@example.com", pedido.cliente.correo)
        for (campo in listOf("Nombre del cliente:", "Dirección de entrega:", "Teléfono de contacto:", "Correo electrónico:")) {
            assertEquals(1, salida.count { it == campo })
        }
        assertEquals(Importes(34000, 3400, 5814, 36414), pedido.calcularImportes())
        assertEquals(mapOf("Masa" to 6L, "Tomate" to 12L, "Queso" to 12L, "Pepperoni" to 3L), jornada.inventario.consultar())
    }

    @Test fun invalidasAntesDeNConservanLaPrimeraLineaYLasNotas() {
        val jornada = Jornada()
        val salida = conversar(cabecera + listOf("1", "3", "no", "", "abc", " N ", "sin sal", "0"), jornada)
        val pedido = jornada.pedidos().single()
        assertEquals(listOf(3), pedido.productos.map { it.cantidad })
        assertEquals("Margarita", pedido.productos.single().producto.nombre)
        assertEquals("sin sal", pedido.notas)
        assertEquals(3, salida.count { it == error })
        assertEquals(4, salida.count { it == pregunta })
        assertEquals(28560L, pedido.calcularImportes().total)
    }

    @Test fun eofEnConfirmacionesPreservaPedidoPrevioInventarioYRepartidorOcupado() {
        for (final in listOf(emptyList(), listOf("abc", ""), listOf("s", "2", "2"), listOf("s", "2", "2", "sí", "1"))) {
            val jornada = Jornada()
            val anterior = jornada.registrar(Cliente("Ana", null, "ana@example.com", "Centro"),
                listOf(LineaPedido(Margarita(), 1)), "previo").first
            anterior.cambiarEstado(EstadoPedido.LISTO)
            val repartidor = jornada.enviar(anterior)!!
            val stock = jornada.inventario.consultar()
            val reporte = jornada.reporte()
            val clientes = jornada.clientesRegistrados()
            val disponibles = jornada.repartidores.map { it.disponible }
            val salida = conversar(cabecera + listOf("1", "2") + final, jornada)
            assertTrue(salida.any { it.contains("Registro del pedido cancelado") })
            assertEquals(listOf(anterior), jornada.pedidos().toList())
            assertEquals(stock, jornada.inventario.consultar())
            assertEquals(reporte, jornada.reporte())
            assertEquals(clientes, jornada.clientesRegistrados())
            assertEquals(disponibles, jornada.repartidores.map { it.disponible })
            assertSame(repartidor, anterior.repartidor)
            assertEquals(EstadoPedido.EN_CAMINO, anterior.estado)
            assertFalse(repartidor.disponible)
        }
    }

    @Test fun reintentosNoRegistranNiConsumenAntesDeCompletarNotas() {
        val jornada = Jornada()
        val stock = jornada.inventario.consultar()
        val entradas = (cabecera + listOf("1", "2", "sí", "no", "s", "2", "2", "", "0", "N", "sin sal")).iterator()
        ejecutarJornada({
            if (entradas.hasNext()) {
                assertTrue(jornada.pedidos().isEmpty())
                assertTrue(jornada.clientesRegistrados().isEmpty())
                assertEquals(stock, jornada.inventario.consultar())
                assertTrue(jornada.repartidores.all { it.disponible })
                entradas.next()
            } else null
        }, {}, jornada)
        assertEquals(1, jornada.pedidos().size)
        assertEquals(4, jornada.pedidos().single().cantidadPizzas)
        assertEquals(6L, jornada.inventario.consultar()["Masa"])
    }

    @Test fun pedidosConVariosProductosAceptanSYNConEspaciosYConservanReparto() {
        val jornada = Jornada()
        conversar(cabecera + listOf("1", "2", " S ", "2", "2", " n ", "", "3", "PED-001",
            "4", "PED-001", "5", "PED-001", "0"), jornada)
        val pedido = jornada.pedidos().single()
        assertEquals(listOf("Margarita", "Pepperoni"), pedido.productos.map { it.producto.nombre })
        assertEquals(Importes(34000, 3400, 5814, 36414), pedido.calcularImportes())
        assertEquals(EstadoPedido.ENTREGADO, pedido.estado)
        assertTrue(pedido.repartidor!!.disponible)
        assertEquals(1, jornada.completados().size)
    }

    private fun leerRespuesta(entradas: List<String>): Pair<Boolean?, List<String>> {
        val iterador = entradas.iterator()
        val salida = mutableListOf<String>()
        var lecturas = 0
        val respuesta = leerSiNo(pregunta, {
            assertTrue(++lecturas <= entradas.size + 1, "EOF debe finalizar la lectura.")
            if (iterador.hasNext()) iterador.next() else null
        }, salida::add)
        return respuesta to salida
    }

    private fun conversar(entradas: List<String>, jornada: Jornada): List<String> {
        val iterador = entradas.iterator()
        val salida = mutableListOf<String>()
        var lecturas = 0
        ejecutarJornada({
            assertTrue(++lecturas <= entradas.size + 2, "La cancelación no debe repetir EOF indefinidamente.")
            if (iterador.hasNext()) iterador.next() else null
        }, salida::add, jornada)
        return salida
    }
}
