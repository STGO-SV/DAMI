package cl.duoc.pizzeriaapp.ui

import cl.duoc.pizzeriaapp.domain.model.Cliente
import cl.duoc.pizzeriaapp.domain.model.LineaPedido
import cl.duoc.pizzeriaapp.domain.model.Producto
import cl.duoc.pizzeriaapp.domain.order.Importes
import cl.duoc.pizzeriaapp.domain.order.Jornada
import cl.duoc.pizzeriaapp.domain.order.Pedido
import java.time.LocalDate

internal class CotizadorPizzeria(private val jornada: Jornada = Jornada()) {
    fun catalogo(): List<Producto> = jornada.productosRegistrados()

    fun cotizarUnaUnidad(producto: Producto): Importes = Pedido(
        idPedido = "SIM-COTIZACION",
        cliente = Cliente(null, null, null, null),
        productos = listOf(LineaPedido(producto, 1)),
        tasaImpuesto = jornada.tasaImpuesto,
        fecha = LocalDate.now(jornada.reloj),
        promocion = jornada.promocion,
        esSimulacion = true
    ).calcularImportes()
}
