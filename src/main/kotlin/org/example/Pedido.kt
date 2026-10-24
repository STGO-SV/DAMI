package org.example

data class Pedido(
    val idPedido: String,
    val cantidadPizzas: Int,
    val subtotal: Long,
    val descuento: Long,
    val totalPedido: Long,
    val estadoPedido: String,
    val repartidorAsignado: String,
    val ingredientesDisponibles: Int
)

fun procesarPedido(cantidadPizzas: Int, stockInicial: Int, precioUnitario: Int): Pedido {
    require(cantidadPizzas > 0) { "La cantidad debe ser positiva." }
    require(stockInicial >= 0) { "El inventario no puede ser negativo." }
    require(precioUnitario > 0) { "El precio debe ser positivo." }

    // Long evita desbordamientos al multiplicar dos valores Int positivos.
    val subtotal: Long = precioUnitario.toLong() * cantidadPizzas
    val descuentoCalculado: Long = if (cantidadPizzas > 3) subtotal / 10 else 0L
    var ingredientesDisponibles: Int = stockInicial
    val estadoPedido: String = if (ingredientesDisponibles >= cantidadPizzas) {
        ingredientesDisponibles -= cantidadPizzas
        "Confirmado"
    } else {
        "Rechazado por falta de inventario"
    }
    val confirmado: Boolean = estadoPedido == "Confirmado"
    val descuento: Long = if (confirmado) descuentoCalculado else 0L
    val totalPedido: Long = if (confirmado) subtotal - descuento else 0L
    val repartidorAsignado: String = if (confirmado) "R01 - Repartidor de turno" else "Sin asignar"

    return Pedido(
        idPedido = "PED-001",
        cantidadPizzas = cantidadPizzas,
        subtotal = subtotal,
        descuento = descuento,
        totalPedido = totalPedido,
        estadoPedido = estadoPedido,
        repartidorAsignado = repartidorAsignado,
        ingredientesDisponibles = ingredientesDisponibles
    )
}
