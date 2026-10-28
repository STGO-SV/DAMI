package org.example

import java.math.BigDecimal
import java.math.RoundingMode

enum class EstadoPedido(val descripcion: String) {
    REGISTRADO("Registrado"), EN_PREPARACION("En preparación"), LISTO("Listo"),
    EN_CAMINO("En camino"), ENTREGADO("Entregado"), RECHAZADO("Rechazado por falta de inventario")
}

data class Importes(val subtotal: Long, val descuento: Long, val impuesto: Long, val total: Long)

fun calcularSubtotal(productos: List<LineaPedido>): Long = productos.fold(0L) { total, linea ->
    Math.addExact(total, Math.multiplyExact(linea.producto.precio, linea.cantidad.toLong()))
}

fun calcularDescuento(subtotal: Long, cantidad: Int): Long {
    require(subtotal >= 0 && cantidad > 0)
    return if (cantidad > 3) subtotal / 10 else 0L
}

fun calcularImpuesto(base: Long, tasa: BigDecimal): Long {
    require(base >= 0 && tasa >= BigDecimal.ZERO && tasa <= BigDecimal.ONE)
    return base.toBigDecimal().multiply(tasa).setScale(0, RoundingMode.HALF_UP).longValueExact()
}

fun calcularTotal(productos: List<LineaPedido>, tasa: BigDecimal): Importes {
    require(productos.isNotEmpty())
    val cantidad = productos.fold(0) { total, linea -> Math.addExact(total, linea.cantidad) }
    val subtotal = calcularSubtotal(productos)
    val descuento = calcularDescuento(subtotal, cantidad)
    val base = subtotal - descuento
    val impuesto = calcularImpuesto(base, tasa)
    return Importes(subtotal, descuento, impuesto, Math.addExact(base, impuesto))
}

class Pedido(
    val idPedido: String,
    val cliente: Cliente,
    productos: List<LineaPedido>,
    notas: String? = null,
    val tasaImpuesto: BigDecimal = BigDecimal("0.19")
) {
    private val lineas = productos.toList()
    val productos: List<LineaPedido> get() = lineas.toList()
    val notas: String? = notas?.trim()?.takeIf { it.isNotEmpty() }
    private val cotizacion = calcularTotal(lineas, tasaImpuesto)
    var estado: EstadoPedido = EstadoPedido.REGISTRADO
        private set
    var repartidor: Repartidor? = null
        private set
    val cantidadPizzas: Int get() = lineas.sumOf { it.cantidad }
    val completado: Boolean get() = estado == EstadoPedido.ENTREGADO
    val pendiente: Boolean get() = estado in setOf(
        EstadoPedido.REGISTRADO, EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO, EstadoPedido.EN_CAMINO
    )

    init { require(idPedido.isNotBlank()) }

    fun calcularImportes(): Importes = if (estado == EstadoPedido.RECHAZADO) {
        Importes(cotizacion.subtotal, 0, 0, 0)
    } else cotizacion

    // La aceptación pertenece a Jornada: siempre verifica inventario antes de preparar.
    internal fun resolverInventario(disponible: Boolean) {
        check(estado == EstadoPedido.REGISTRADO)
        estado = if (disponible) EstadoPedido.EN_PREPARACION else EstadoPedido.RECHAZADO
    }

    fun cambiarEstado(nuevo: EstadoPedido) {
        require(estado == EstadoPedido.EN_PREPARACION && nuevo == EstadoPedido.LISTO) {
            "Transición inválida: ${estado.descripcion} → ${nuevo.descripcion}. Marque Listo después de preparar; use el reparto para enviar y entregar."
        }
        estado = nuevo
    }

    internal fun asignar(candidato: Repartidor) {
        require(estado == EstadoPedido.LISTO && candidato.disponible && repartidor == null)
        repartidor = candidato
        estado = EstadoPedido.EN_CAMINO
    }

    internal fun confirmarEntrega(candidato: Repartidor) {
        require(estado == EstadoPedido.EN_CAMINO && repartidor === candidato)
        estado = EstadoPedido.ENTREGADO
    }

    fun notificarCliente(): String? = cliente.correoNotificable?.let {
        "Notificación local simulada a $it: pedido $idPedido, ${estado.descripcion}."
    }

    fun resumen(): String {
        val importes = calcularImportes()
        return buildString {
            appendLine("=== Resumen del pedido $idPedido ===")
            appendLine(cliente.describirUsuario())
            for (linea in lineas) {
                appendLine("Pizza: ${linea.producto.nombre} | Precio unitario: ${pesos(linea.producto.precio)} | Cantidad: ${linea.cantidad}")
            }
            appendLine("Subtotal solicitado: ${pesos(importes.subtotal)}")
            appendLine("Descuento aplicado: ${pesos(importes.descuento)}")
            appendLine("Impuestos (${tasaImpuesto.multiply(BigDecimal(100)).stripTrailingZeros().toPlainString()} %): ${pesos(importes.impuesto)}")
            appendLine("Total a pagar: ${pesos(importes.total)}")
            appendLine("Estado: ${estado.descripcion}")
            appendLine("Repartidor asignado: ${repartidor?.nombre ?: "Sin asignar"}")
            append("Notas: ${notas?.trim() ?: "Sin notas"}")
        }
    }
}
