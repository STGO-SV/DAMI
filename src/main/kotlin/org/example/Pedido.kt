package org.example

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

enum class EstadoPedido(val descripcion: String) {
    REGISTRADO("Registrado"), EN_PREPARACION("En preparación"), LISTO("Listo"),
    EN_CAMINO("En camino"), ENTREGADO("Entregado"), RECHAZADO("Rechazado por falta de inventario")
}

data class Importes(val subtotal: Long, val descuento: Long, val impuesto: Long, val total: Long,
    val descuentoPromocional: Long = 0)

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

fun calcularTotal(productos: List<LineaPedido>, tasa: BigDecimal,
    fecha: LocalDate = LocalDate.now(), promocion: Promocion? = null): Importes {
    require(productos.isNotEmpty())
    val cantidad = productos.fold(0) { total, linea -> Math.addExact(total, linea.cantidad) }
    val subtotal = calcularSubtotal(productos)
    val porCantidad = calcularDescuento(subtotal, cantidad)
    val porFecha = promocion?.descuento(productos, fecha) ?: 0L
    val descuento = maxOf(porCantidad, porFecha)
    val base = subtotal - descuento
    val impuesto = calcularImpuesto(base, tasa)
    return Importes(subtotal, descuento, impuesto, Math.addExact(base, impuesto),
        if (porFecha > porCantidad) porFecha else 0L)
}

class Pedido(
    val idPedido: String,
    val cliente: Cliente,
    productos: List<LineaPedido>,
    notas: String? = null,
    val tasaImpuesto: BigDecimal = BigDecimal("0.19"),
    val fecha: LocalDate = LocalDate.now(java.time.ZoneId.of("America/Santiago")),
    val promocion: Promocion? = null,
    val esSimulacion: Boolean = false
) {
    private val lineas = productos.toList()
    val productos: List<LineaPedido> get() = lineas.toList()
    val notas: String? = notas?.trim()?.takeIf { it.isNotEmpty() }
    private val cotizacion = calcularTotal(lineas, tasaImpuesto, fecha, promocion)
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

    operator fun component1(): Cliente = cliente
    operator fun component2(): List<LineaPedido> = productos
    operator fun component3(): Long = calcularImportes().total

    /** Une únicamente borradores compatibles y devuelve una cotización sin efectos operativos. */
    operator fun plus(otro: Pedido): Pedido {
        require(this !== otro && estado == EstadoPedido.REGISTRADO && otro.estado == EstadoPedido.REGISTRADO) {
            "Solo se combinan dos borradores distintos, sin inventario consumido ni reparto."
        }
        require(cliente == otro.cliente && cliente.telefono == otro.cliente.telefono && cliente.direccion == otro.cliente.direccion) {
            "Los pedidos deben corresponder al mismo cliente y contacto de entrega."
        }
        require(fecha == otro.fecha && tasaImpuesto.compareTo(otro.tasaImpuesto) == 0 && promocion == otro.promocion) {
            "Los pedidos deben compartir fecha, impuesto y promoción."
        }
        val agrupadas = linkedMapOf<Producto, LineaPedido>()
        for (linea in lineas + otro.lineas) {
            val anterior = agrupadas[linea.producto]
            require(anterior == null || anterior.producto.mismaFicha(linea.producto)) {
                "Un producto repetido tiene precio, receta o elegibilidad diferente."
            }
            agrupadas[linea.producto] = LineaPedido(linea.producto, Math.addExact(anterior?.cantidad ?: 0, linea.cantidad))
        }
        val notasUnidas = listOfNotNull(notas, otro.notas).distinct().joinToString(" | ").ifEmpty { null }
        return Pedido("SIM($idPedido+${otro.idPedido})", cliente, agrupadas.values.toList(), notasUnidas,
            tasaImpuesto, fecha, promocion, esSimulacion = true)
    }

    // La aceptación pertenece a Jornada: siempre verifica inventario antes de preparar.
    internal fun resolverInventario(disponible: Boolean) {
        check(estado == EstadoPedido.REGISTRADO && !esSimulacion)
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
            appendLine("--- Identificación del pedido ---")
            appendLine("Identificador: $idPedido")
            appendLine("Fecha: $fecha")
            if (esSimulacion) appendLine("Cotización de simulación: sin registro, cobro, inventario ni reparto.")
            appendLine()
            appendLine("--- Datos del cliente ---")
            appendLine(cliente.describirUsuario())
            appendLine()
            appendLine("--- Productos y cantidades ---")
            for (linea in lineas) {
                appendLine("Pizza: ${linea.producto.nombre}")
                appendLine("  Precio unitario: ${pesos(linea.producto.precio)} | Cantidad: ${linea.cantidad}")
            }
            appendLine("Cantidad total de pizzas: $cantidadPizzas")
            appendLine("Notas: ${notas ?: "Sin notas"}")
            appendLine()
            appendLine("--- Detalle monetario ---")
            appendLine("Subtotal solicitado: ${pesos(importes.subtotal)}")
            appendLine("Descuento aplicado: ${pesos(importes.descuento)}")
            promocion?.let {
                appendLine("Promoción por fechas: ${it.inicio} a ${it.fin} (extremos incluidos), ${it.porcentaje.multiply(BigDecimal(100)).stripTrailingZeros().toPlainString()} %; ${if (it.activa(fecha)) "vigente" else "no vigente"}")
                appendLine("Descuento por cantidad: ${pesos(importes.descuento - importes.descuentoPromocional)}")
                appendLine("Descuento promocional: ${pesos(importes.descuentoPromocional)}")
                val base = if (estado == EstadoPedido.RECHAZADO) 0L else importes.subtotal - importes.descuento
                appendLine("Base imponible: ${pesos(base)}")
                appendLine("Política: mayor descuento, sin acumulación; en empate se aplica cantidad.")
            }
            appendLine("Impuestos (${tasaImpuesto.multiply(BigDecimal(100)).stripTrailingZeros().toPlainString()} %): ${pesos(importes.impuesto)}")
            appendLine("Total a pagar: ${pesos(importes.total)}")
            appendLine()
            appendLine("--- Estado y asignación de reparto ---")
            appendLine("Estado: ${estado.descripcion}")
            append("Repartidor asignado: ${repartidor?.nombre ?: "Sin asignar"}")
        }
    }
}
