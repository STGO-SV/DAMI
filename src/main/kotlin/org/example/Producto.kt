package org.example

open class Producto(val nombre: String, val precio: Long, ingredientes: Map<String, Long>, val categoria: String = "pizza") {
    private val receta = ingredientes.toMap()
    val ingredientes: Map<String, Long> get() = receta.toMap()

    init {
        require(nombre.isNotBlank() && categoria.isNotBlank() && precio > 0) { "Nombre, categoría y precio del producto inválidos." }
        require(receta.isNotEmpty() && receta.all { (nombre, cantidad) -> nombre.isNotBlank() && cantidad > 0 }) {
            "La receta debe contener ingredientes y cantidades positivas."
        }
    }

    final override fun equals(other: Any?): Boolean = other is Producto &&
        claveIdentidad(nombre) == claveIdentidad(other.nombre) && claveIdentidad(categoria) == claveIdentidad(other.categoria)

    final override fun hashCode(): Int = 31 * claveIdentidad(nombre).hashCode() + claveIdentidad(categoria).hashCode()

    fun detallarProducto(): String = "$nombre | ${pesos(precio)} | Ingredientes: " +
        receta.entries.joinToString { "${it.key}: ${it.value} unidades" }
}

@Promocionable("Pizza clásica")
class Margarita : Producto("Margarita", 8_000, mapOf("Masa" to 1L, "Tomate" to 2L, "Queso" to 2L))

@Promocionable("Pizza con pepperoni")
class Pepperoni : Producto("Pepperoni", 9_000, mapOf("Masa" to 1L, "Tomate" to 2L, "Queso" to 2L, "Pepperoni" to 1L))

data class LineaPedido(val producto: Producto, val cantidad: Int) {
    init { require(cantidad > 0) { "La cantidad debe ser positiva." } }
}

fun catalogoInicial(): List<Producto> = listOf(
    Margarita(), Pepperoni()
)
