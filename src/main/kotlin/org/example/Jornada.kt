package org.example

import java.math.BigDecimal

class Jornada(
    val inventario: Inventario = inventarioInicial(),
    val repartidores: List<Repartidor> = repartidoresIniciales(),
    val tasaImpuesto: BigDecimal = BigDecimal("0.19")
) {
    private var pedidosDelDia: Array<Pedido> = emptyArray()
    init { require(tasaImpuesto >= BigDecimal.ZERO && tasaImpuesto <= BigDecimal.ONE) }
    fun pedidos(): Array<Pedido> = pedidosDelDia.copyOf()
    fun completados(): List<Pedido> = pedidosDelDia.filter { it.completado }
    fun pendientes(): List<Pedido> = pedidosDelDia.filter { it.pendiente }
    fun rechazados(): List<Pedido> = pedidosDelDia.filter { it.estado == EstadoPedido.RECHAZADO }

    fun registrar(cliente: Cliente, productos: List<LineaPedido>, notas: String?): Pair<Pedido, List<Faltante>> {
        val pedido = Pedido("PED-${(pedidosDelDia.size + 1).toString().padStart(3, '0')}",
            cliente, productos, notas, tasaImpuesto)
        val necesarios = calcularIngredientes(productos)
        val faltantes = inventario.consumir(necesarios)
        pedido.resolverInventario(faltantes.isEmpty())
        pedidosDelDia += pedido
        return pedido to faltantes
    }

    fun enviar(pedido: Pedido): Repartidor? {
        require(pedidosDelDia.any { it === pedido } && pedido.estado == EstadoPedido.LISTO) {
            "Seleccione un pedido de la jornada en estado Listo."
        }
        val repartidor = buscarRepartidorCercano(repartidores)
        repartidor?.tomarPedido(pedido)
        return repartidor
    }

    fun reporte(): String = buildString {
        appendLine("=== Pedidos del día ===")
        for (pedido in pedidosDelDia) {
            val pizzas = pedido.productos.joinToString { "${it.producto.nombre} x${it.cantidad}" }
            appendLine("${pedido.idPedido} | ${pedido.cliente.nombre} | $pizzas | ${pesos(pedido.calcularImportes().total)} | ${pedido.estado.descripcion}")
        }
        append("Completados (Entregado): ${completados().size} | Pendientes (Registrado, En preparación, Listo, En camino): ${pendientes().size} | Rechazados: ${rechazados().size}")
    }
}
