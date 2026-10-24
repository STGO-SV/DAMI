
package org.example

fun main() {
    ejecutarPedido()
}

// Se puede probar la misma conversación sin depender de una consola interactiva.
fun ejecutarPedido(
    leer: () -> String? = { readLine() },
    mostrar: (String) -> Unit = { println(it) }
) {
    mostrar("=== Pizzería Kotlin | Nuevo pedido ===")
    mostrar("Nombre del cliente:")
    val nombre: String = textoOValorPredeterminado(leer(), "Cliente sin nombre")
    mostrar("Dirección de entrega:")
    val direccion: String = textoOValorPredeterminado(leer(), "Retiro en local")
    mostrar("Teléfono de contacto:")
    val telefono: String = telefonoValido(leer()) ?: run {
        mostrar("Teléfono inválido o ausente. Se registrará: No informado.")
        "No informado"
    }

    val nombrePizza: String = "Margarita"
    val precioUnitario: Int = 8_000
    val ingredientesDisponibles: Int = 10
    mostrar("Pizza: $nombrePizza | Precio unitario: ${pesos(precioUnitario)}")
    mostrar("Inventario: $ingredientesDisponibles unidades (una por pizza).")
    val cantidadPizzas: Int = leerCantidad(leer, mostrar)
    val pedido = procesarPedido(cantidadPizzas, ingredientesDisponibles, precioUnitario)

    mostrar(if (pedido.estadoPedido == "Confirmado") {
        "Disponibilidad confirmada. Inventario actualizado."
    } else {
        "Pedido rechazado: ingredientes insuficientes. El inventario se conserva."
    })
    mostrar("\n=== Resumen del pedido ===")
    mostrar("Cliente: $nombre")
    mostrar("Dirección: $direccion")
    mostrar("Teléfono: $telefono")
    mostrar("Pizza: $nombrePizza")
    mostrar("Precio unitario: ${pesos(precioUnitario)}")
    mostrar("Identificador: ${pedido.idPedido}")
    mostrar("Cantidad: $cantidadPizzas")
    mostrar("Subtotal solicitado: ${pesos(pedido.subtotal)}")
    mostrar("Descuento aplicado: ${pesos(pedido.descuento)}")
    mostrar("Total a pagar: ${pesos(pedido.totalPedido)}")
    mostrar("Estado: ${pedido.estadoPedido}")
    mostrar("Repartidor asignado: ${pedido.repartidorAsignado}")
    mostrar("Inventario restante: ${pedido.ingredientesDisponibles}")
}

fun pesos(monto: Long): String = "$" + monto.toString().reversed().chunked(3).joinToString(".").reversed() + " CLP"

fun pesos(monto: Int): String = pesos(monto.toLong())
