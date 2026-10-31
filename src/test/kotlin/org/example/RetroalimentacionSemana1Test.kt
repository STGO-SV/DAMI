package org.example

import java.math.BigDecimal
import kotlin.test.*

class RetroalimentacionSemana1Test {
    private val cliente = Cliente("José", "+56901234567", "jose@example.com", "Ñuñoa 123")
    private val catalogo = catalogoInicial()
    private val mixto = listOf(LineaPedido(catalogo[0], 2), LineaPedido(catalogo[1], 2))

    @Test fun loteMixtoRechazadoNoConsumeNiLaPrimeraPizzaDisponible() {
        val inventario = Inventario(mapOf("Masa" to 10L, "Tomate" to 20L, "Queso" to 20L, "Pepperoni" to 1L))
        val jornada = Jornada(inventario)
        val inicial = inventario.consultar()
        val (pedido, faltantes) = jornada.registrar(cliente, mixto, null)
        assertEquals(listOf(Faltante("Pepperoni", 1, 2)), faltantes)
        assertEquals(inicial, inventario.consultar())
        assertEquals(EstadoPedido.RECHAZADO, pedido.estado)
        assertEquals(Importes(34000, 0, 0, 0), pedido.calcularImportes())
        assertTrue(jornada.repartidores.all { it.disponible })
    }

    @Test fun pedidosMixtosConsecutivosCompartenYDescuentanCadaIngrediente() {
        val jornada = Jornada()
        repeat(2) {
            val (pedido, faltantes) = jornada.registrar(cliente, mixto, null)
            assertTrue(faltantes.isEmpty())
            assertEquals(EstadoPedido.EN_PREPARACION, pedido.estado)
            assertEquals(mapOf("Masa" to 10L - 4L * (it + 1), "Tomate" to 20L - 8L * (it + 1),
                "Queso" to 20L - 8L * (it + 1), "Pepperoni" to 5L - 2L * (it + 1)), jornada.inventario.consultar())
        }
        val previo = jornada.inventario.consultar()
        assertEquals(EstadoPedido.RECHAZADO, jornada.registrar(cliente, mixto, null).first.estado)
        assertEquals(previo, jornada.inventario.consultar())
    }

    @Test fun resumenMixtoOrdenaSeccionesYConservaTodosLosDatos() {
        val pedido = Jornada().registrar(cliente, mixto, " sin sal ").first
        val esperado = """
            === Resumen del pedido PED-001 ===
            --- Identificación del pedido ---
            Identificador: PED-001

            --- Datos del cliente ---
            Cliente: José
            Dirección: Ñuñoa 123
            Teléfono: +56901234567
            Correo: jose@example.com

            --- Productos y cantidades ---
            Pizza: Margarita
              Precio unitario: ${'$'}8.000 CLP | Cantidad: 2
            Pizza: Pepperoni
              Precio unitario: ${'$'}9.000 CLP | Cantidad: 2
            Cantidad total de pizzas: 4
            Notas: sin sal

            --- Detalle monetario ---
            Subtotal solicitado: ${'$'}34.000 CLP
            Descuento aplicado: ${'$'}3.400 CLP
            Impuestos (19 %): ${'$'}5.814 CLP
            Total a pagar: ${'$'}36.414 CLP

            --- Estado y asignación de reparto ---
            Estado: En preparación
            Repartidor asignado: Sin asignar
        """.trimIndent()
        assertEquals(esperado, pedido.resumen())
    }

    @Test fun resumenRechazadoConservaPredeterminadosYSubtotalSinCobro() {
        val pedido = Jornada(Inventario(emptyMap())).registrar(Cliente(null, null, null, null), mixto, " ").first
        val resumen = pedido.resumen()
        for (texto in listOf("Cliente: Cliente sin nombre", "Dirección: Retiro en local", "Teléfono: No informado",
            "Correo: correo@invalido.com", "Notas: Sin notas", "Subtotal solicitado: $34.000 CLP",
            "Descuento aplicado: $0 CLP", "Impuestos (19 %): $0 CLP", "Total a pagar: $0 CLP",
            "Estado: Rechazado por falta de inventario", "Repartidor asignado: Sin asignar")) {
            assertTrue(resumen.contains(texto), texto)
        }
    }

    @Test fun resumenReflejaCadaEstadoYRepartidorSinModificarLaJornada() {
        val jornada = Jornada()
        val lineas = listOf(LineaPedido(catalogo[0], 1))
        assertTrue(Pedido("BORRADOR", cliente, lineas).resumen().contains("Estado: Registrado"))
        val pedido = jornada.registrar(cliente, lineas, null).first
        val stock = jornada.inventario.consultar()
        val importes = pedido.calcularImportes()
        fun comprobar(estado: EstadoPedido, repartidor: String) {
            val reporte = jornada.reporte()
            val disponibilidad = jornada.repartidores.map { it.disponible }
            repeat(2) {
                val resumen = pedido.resumen()
                assertTrue(resumen.contains("Estado: ${estado.descripcion}"))
                assertTrue(resumen.contains("Repartidor asignado: $repartidor"))
            }
            assertEquals(estado, pedido.estado)
            assertEquals(stock, jornada.inventario.consultar())
            assertEquals(importes, pedido.calcularImportes())
            assertEquals(reporte, jornada.reporte())
            assertEquals(disponibilidad, jornada.repartidores.map { it.disponible })
        }
        comprobar(EstadoPedido.EN_PREPARACION, "Sin asignar")
        pedido.cambiarEstado(EstadoPedido.LISTO)
        comprobar(EstadoPedido.LISTO, "Sin asignar")
        val repartidor = jornada.enviar(pedido)!!
        comprobar(EstadoPedido.EN_CAMINO, "R02 - Diego")
        repartidor.entregarPedido(pedido)
        comprobar(EstadoPedido.ENTREGADO, "R02 - Diego")
    }

    @Test fun resumenMuestraTasaConfigurableSinCerosDecimalesSobrantes() {
        val pedido = Jornada(tasaImpuesto = BigDecimal("0.1050")).registrar(cliente, mixto, null).first
        assertTrue(pedido.resumen().contains("Impuestos (10.5 %): $3.213 CLP"))
        assertTrue(pedido.resumen().contains("Total a pagar: $33.813 CLP"))
    }

    @Test fun mainPresentaElResumenSeccionadoTrasRegistrar() {
        val entradas = listOf("1", "José", "Ñuñoa 123", "+56901234567", "jose@example.com",
            "1", "4", "n", "sin sal", "0").iterator()
        val salida = mutableListOf<String>()
        val jornada = Jornada()
        ejecutarJornada({ if (entradas.hasNext()) entradas.next() else null }, salida::add, jornada)
        val resumen = salida.single { it.startsWith("=== Resumen del pedido") }
        assertEquals(jornada.pedidos().single().resumen(), resumen)
        assertEquals(5, resumen.lineSequence().count { it.startsWith("--- ") })
        assertTrue(salida.last().contains("PED-001 | José | Margarita x4 | $34.272 CLP | En preparación"))
    }
}
