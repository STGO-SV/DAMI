package cl.duoc.pizzeriaapp.ui

import cl.duoc.pizzeriaapp.domain.model.catalogoInicial
import cl.duoc.pizzeriaapp.domain.order.Importes
import cl.duoc.pizzeriaapp.domain.order.Jornada
import cl.duoc.pizzeriaapp.domain.promotion.Promocion
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CotizadorPizzeriaTest {
    @Test
    fun recuperaNombreCategoriaYPrecioDelCatalogoDelDominio() {
        val esperado = catalogoInicial().map { Triple(it.nombre, it.categoria, it.precio) }
        val obtenido = CotizadorPizzeria().catalogo().map { Triple(it.nombre, it.categoria, it.precio) }

        assertEquals(esperado, obtenido)
    }

    @Test
    fun cotizaUnaUnidadConElImpuestoDeLaJornada() {
        val cotizador = CotizadorPizzeria()
        val margarita = cotizador.catalogo().first()

        assertEquals(Importes(8_000, 0, 1_520, 9_520), cotizador.cotizarUnaUnidad(margarita))
    }

    @Test
    fun cotizaConLaPromocionVigenteSinConsumirInventarioNiRegistrar() {
        val fecha = LocalDate.of(2026, 11, 3)
        val reloj = Clock.fixed(Instant.parse("2026-11-03T15:00:00Z"), ZoneId.of("America/Santiago"))
        val jornada = Jornada(reloj = reloj, promocion = Promocion(fecha, fecha, BigDecimal("0.15")))
        val inventario = jornada.inventario.consultar()
        val cotizador = CotizadorPizzeria(jornada)
        val margarita = cotizador.catalogo().first()
        val esperado = Importes(8_000, 1_200, 1_292, 8_092, 1_200)

        repeat(2) { assertEquals(esperado, cotizador.cotizarUnaUnidad(margarita)) }
        assertEquals(inventario, jornada.inventario.consultar())
        assertTrue(jornada.pedidos().isEmpty())
        assertTrue(jornada.clientesRegistrados().isEmpty())
        assertTrue(jornada.repartidores.all { it.disponible })
    }
}
