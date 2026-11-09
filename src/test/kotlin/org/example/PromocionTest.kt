package org.example

import java.math.BigDecimal
import java.time.*
import kotlin.test.*

class PromocionTest {
    private val inicio = LocalDate.of(2026, 11, 1)
    private val fin = LocalDate.of(2026, 11, 7)
    private val promocion = Promocion(inicio, fin, BigDecimal("0.15"))
    private val cliente = Cliente("José", "+56912345678", "jose@example.com", "Centro")
    private val mixto = catalogoInicial().map { LineaPedido(it, 2) }

    @Test fun rangoDeFechasIncluyeExtremosYExcluyeDiasExternos() {
        for ((fecha, activa) in listOf(inicio.minusDays(1) to false, inicio to true, inicio.plusDays(2) to true,
            fin to true, fin.plusDays(1) to false)) {
            assertEquals(activa, promocion.activa(fecha), fecha.toString())
            val calculado = calcularTotal(mixto, BigDecimal("0.19"), fecha, promocion)
            assertEquals(if (activa) Importes(34000, 5100, 5491, 34391, 5100) else Importes(34000, 3400, 5814, 36414), calculado)
        }
        assertTrue(Promocion(inicio, inicio, BigDecimal.ONE).activa(inicio))
    }

    @Test fun intervalosYPorcentajesInvalidosSeRechazan() {
        assertFailsWith<IllegalArgumentException> { Promocion(fin, inicio, BigDecimal("0.10")) }
        for (porcentaje in listOf("-0.01", "1.01")) {
            assertFailsWith<IllegalArgumentException> { Promocion(inicio, fin, porcentaje.toBigDecimal()) }
        }
    }

    @Test fun soloLosProductosAnotadosRecibenPromocion() {
        val noMarcado = Producto("Referencia", 10000, mapOf("Masa" to 1L))
        val productos = listOf(LineaPedido(Margarita(), 1), LineaPedido(noMarcado, 1))
        assertTrue(esPromocionable(Margarita()))
        assertTrue(esPromocionable(Pepperoni()))
        assertFalse(esPromocionable(noMarcado))
        assertEquals(1200L, promocion.descuento(productos, inicio))
        assertEquals(0L, promocion.descuento(listOf(LineaPedido(noMarcado, 1)), inicio))
        assertEquals(Importes(18000, 1200, 3192, 19992, 1200), calcularTotal(productos, BigDecimal("0.19"), inicio, promocion))
    }

    @Test fun descuentoMenorOEmpatadoNoSeAcumulaConCantidad() {
        for (porcentaje in listOf("0", "0.05", "0.10")) {
            assertEquals(Importes(34000, 3400, 5814, 36414),
                calcularTotal(mixto, BigDecimal("0.19"), inicio, Promocion(inicio, fin, porcentaje.toBigDecimal())))
        }
    }

    @Test fun descuentoDelCienPorCientoNoGeneraImpuestosNiTotalesNegativos() {
        assertEquals(Importes(34000, 34000, 0, 0, 34000),
            calcularTotal(mixto, BigDecimal.ONE, inicio, Promocion(inicio, fin, BigDecimal.ONE)))
    }

    @Test fun redondeoPromocionalTruncaAntesDelImpuestoHalfUp() {
        @Promocionable("Prueba de redondeo")
        class Economica : Producto("Económica", 11, mapOf("Masa" to 1L))
        assertEquals(Importes(11, 1, 1, 11, 1), calcularTotal(listOf(LineaPedido(Economica(), 1)),
            BigDecimal("0.05"), inicio, promocion))
    }

    @Test fun fechaInyectadaSeConservaYRechazoNoCobraPromocion() {
        val zona = ZoneId.of("America/Santiago")
        val reloj = Clock.fixed(inicio.atStartOfDay(zona).toInstant(), zona)
        val jornada = Jornada(promocion = promocion, reloj = reloj)
        val pedido = jornada.registrar(cliente, mixto, null).first
        assertEquals(inicio, pedido.fecha)
        assertEquals(34391L, pedido.calcularImportes().total)
        val stock = jornada.inventario.consultar()
        val rechazado = jornada.registrar(cliente, listOf(LineaPedido(Margarita(), 100)), null).first
        assertEquals(Importes(800000, 0, 0, 0), rechazado.calcularImportes())
        assertEquals(stock, jornada.inventario.consultar())
        assertTrue(rechazado.resumen().contains("Descuento promocional: $0 CLP"))
    }

    @Test fun desbordamientoConPromocionSeDetectaSinEfectosOperativos() {
        val jornada = Jornada(promocion = promocion)
        val stock = jornada.inventario.consultar()
        assertFailsWith<ArithmeticException> {
            jornada.registrar(cliente, listOf(LineaPedido(Producto("Gigante", Long.MAX_VALUE, mapOf("Masa" to 1L)), 2)), null)
        }
        assertEquals(stock, jornada.inventario.consultar())
        assertTrue(jornada.pedidos().isEmpty())
        assertTrue(jornada.clientesRegistrados().isEmpty())
    }
}
