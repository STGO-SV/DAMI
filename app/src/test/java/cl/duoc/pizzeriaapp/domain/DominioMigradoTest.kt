package cl.duoc.pizzeriaapp.domain

import cl.duoc.pizzeriaapp.domain.model.Cliente
import cl.duoc.pizzeriaapp.domain.model.LineaPedido
import cl.duoc.pizzeriaapp.domain.model.Margarita
import cl.duoc.pizzeriaapp.domain.model.Pepperoni
import cl.duoc.pizzeriaapp.domain.model.Producto
import cl.duoc.pizzeriaapp.domain.model.Promocionable
import cl.duoc.pizzeriaapp.domain.order.EstadoPedido
import cl.duoc.pizzeriaapp.domain.order.Importes
import cl.duoc.pizzeriaapp.domain.order.Jornada
import cl.duoc.pizzeriaapp.domain.order.Pedido
import cl.duoc.pizzeriaapp.domain.order.calcularTotal
import cl.duoc.pizzeriaapp.domain.promotion.Promocion
import cl.duoc.pizzeriaapp.domain.promotion.esPromocionable
import cl.duoc.pizzeriaapp.domain.support.Registro
import cl.duoc.pizzeriaapp.domain.support.pesos
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DominioMigradoTest {
    private val reloj = Clock.fixed(
        Instant.parse("2026-11-04T01:00:00Z"),
        ZoneId.of("America/Santiago")
    )
    private val fecha = LocalDate.of(2026, 11, 3)
    private val tasa = BigDecimal("0.19")
    private val cliente = Cliente("Ana Pérez", "912345678", "ana@example.cl", null)

    @Test
    fun cotizaEnPesosYAplicaElMayorDescuentoSinAcumular() {
        val lineas = listOf(LineaPedido(Margarita(), 2), LineaPedido(Pepperoni(), 2))
        val cantidad = Importes(34_000, 3_400, 5_814, 36_414)
        assertEquals(cantidad, calcularTotal(lineas, tasa, fecha))
        assertEquals(cantidad, calcularTotal(lineas, tasa, fecha, promocion("0.10")))
        assertEquals(
            Importes(34_000, 5_100, 5_491, 34_391, 5_100),
            calcularTotal(lineas, tasa, fecha, promocion("0.15"))
        )
        assertEquals("\$34.391 CLP", pesos(34_391))
    }

    @Test
    fun consultaLaAnotacionDeLaClaseConcretaYDescuentaSoloLineasElegibles() {
        val generico = Producto("Margarita", 8_000, Margarita().ingredientes)
        assertTrue(esPromocionable(Margarita()))
        assertTrue(esPromocionable(Pepperoni()))
        assertTrue(esPromocionable(BaseAnotada()))
        assertFalse(esPromocionable(SinAnotacion()))
        assertFalse(esPromocionable(generico))

        val promocion = promocion("0.15")
        val lineas = listOf(LineaPedido(generico, 1), LineaPedido(Margarita(), 1))
        assertEquals(1_200L, promocion.descuento(lineas, promocion.inicio))
        assertEquals(1_200L, promocion.descuento(lineas, promocion.fin))
        assertEquals(0L, promocion.descuento(lineas, promocion.fin.plusDays(1)))
    }

    @Test
    fun rechazoMixtoConservaTodoElInventarioYNoCobra() {
        val jornada = Jornada(reloj = reloj)
        val inventarioInicial = jornada.inventario.consultar()
        val lineas = listOf(LineaPedido(Margarita(), 1), LineaPedido(Pepperoni(), 6))
        val (pedido, faltantes) = jornada.registrar(cliente, lineas, null)

        assertEquals(fecha, pedido.fecha)
        assertEquals(EstadoPedido.RECHAZADO, pedido.estado)
        assertEquals(listOf("Pepperoni"), faltantes.map { it.ingrediente })
        assertEquals(0L, pedido.calcularImportes().total)
        assertEquals(inventarioInicial, jornada.inventario.consultar())
    }

    @Test
    fun identidadNormalizadaConservaPrimeraFichaPeroDistingueElegibilidad() {
        val repetido = Cliente(" ANA  PÉREZ ", null, "ANA@example.cl", "Otra dirección")
        assertEquals(cliente, repetido)
        assertEquals(cliente.hashCode(), repetido.hashCode())
        val registro = Registro<Cliente>()
        assertTrue(registro.registrar(cliente))
        assertFalse(registro.registrar(repetido))
        assertSame(cliente, registro.buscar(repetido))

        val jornada = Jornada(reloj = reloj)
        val margarita = Margarita()
        val generico = Producto(" margarita ", margarita.precio, margarita.ingredientes)
        val stock = jornada.inventario.consultar()
        assertTrue(margarita.equals(generico))
        assertThrows(IllegalArgumentException::class.java) {
            jornada.registrar(cliente, listOf(LineaPedido(generico, 1)), null)
        }
        assertEquals(stock, jornada.inventario.consultar())
        assertTrue(jornada.pedidos().isEmpty())
    }

    @Test
    fun combinarBorradoresRecalculaSinModificarLosOriginales() {
        val lineas = listOf(LineaPedido(Margarita(), 2))
        val primero = Pedido("A", cliente, lineas, fecha = fecha)
        val segundo = Pedido("B", cliente, lineas, fecha = fecha)
        val combinado = primero + segundo

        assertTrue(combinado.esSimulacion)
        assertEquals(4, combinado.productos.single().cantidad)
        assertEquals(34_272L, combinado.calcularImportes().total)
        for (original in listOf(primero, segundo)) {
            assertEquals(EstadoPedido.REGISTRADO, original.estado)
            assertEquals(2, original.cantidadPizzas)
        }
    }

    @Test
    fun registrarYEntregarConsumeStockYLiberaAlRepartidorMasCercano() {
        val jornada = Jornada(reloj = reloj)
        val (pedido, faltantes) = jornada.registrar(cliente, listOf(LineaPedido(Margarita(), 1)), null)
        assertTrue(faltantes.isEmpty())
        assertEquals(9L, jornada.inventario.consultar().getValue("Masa"))
        assertEquals(EstadoPedido.EN_PREPARACION, pedido.estado)
        pedido.cambiarEstado(EstadoPedido.LISTO)
        val repartidor = checkNotNull(jornada.enviar(pedido))

        assertEquals(500, repartidor.distanciaMetros)
        assertFalse(repartidor.disponible)
        repartidor.entregarPedido(pedido)
        assertTrue(repartidor.disponible)
        assertEquals(listOf(pedido), jornada.completados())
    }

    @Test
    fun desbordamientoConservaInventarioYRegistros() {
        val jornada = Jornada(reloj = reloj)
        val stock = jornada.inventario.consultar()
        val producto = Producto("Precio extremo", Long.MAX_VALUE, mapOf("Masa" to 1L))

        assertThrows(ArithmeticException::class.java) {
            jornada.registrar(cliente, listOf(LineaPedido(producto, 2)), null)
        }
        assertEquals(stock, jornada.inventario.consultar())
        assertTrue(jornada.pedidos().isEmpty())
        assertTrue(jornada.clientesRegistrados().isEmpty())
    }

    private fun promocion(porcentaje: String) =
        Promocion(fecha, fecha.plusDays(1), BigDecimal(porcentaje))

    @Promocionable("Clase de prueba")
    private open class BaseAnotada : Producto("Prueba", 1, mapOf("Masa" to 1L))

    private class SinAnotacion : BaseAnotada()
}
