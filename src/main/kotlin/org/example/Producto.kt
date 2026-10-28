package org.example

class Producto(val nombre: String, val precio: Long, ingredientes: Map<String, Long>) {
    private val receta = ingredientes.toMap()
    val ingredientes: Map<String, Long> get() = receta.toMap()

    init {
        require(nombre.isNotBlank() && precio > 0) { "Nombre y precio del producto inválidos." }
        require(receta.isNotEmpty() && receta.all { (nombre, cantidad) -> nombre.isNotBlank() && cantidad > 0 }) {
            "La receta debe contener ingredientes y cantidades positivas."
        }
    }

    fun detallarProducto(): String = "$nombre | ${pesos(precio)} | Ingredientes: " +
        receta.entries.joinToString { "${it.key}: ${it.value} unidades" }
}

data class LineaPedido(val producto: Producto, val cantidad: Int) {
    init { require(cantidad > 0) { "La cantidad debe ser positiva." } }
}

fun catalogoInicial(): List<Producto> = listOf(
    Producto("Margarita", 8_000, mapOf("Masa" to 1L, "Tomate" to 2L, "Queso" to 2L)),
    Producto("Pepperoni", 9_000, mapOf("Masa" to 1L, "Tomate" to 2L, "Queso" to 2L, "Pepperoni" to 1L))
)
