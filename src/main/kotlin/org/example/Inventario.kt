package org.example

data class Faltante(val ingrediente: String, val disponible: Long, val requerido: Long) {
    fun instrucciones(): String =
        "$ingrediente: disponible $disponible, requerido $requerido. Reabastecer al menos ${requerido - disponible} unidades: adquirir el ingrediente, registrar la recepción en Reabastecer y crear un nuevo pedido."
}

fun calcularIngredientes(productos: List<LineaPedido>): Map<String, Long> {
    require(productos.isNotEmpty())
    val necesarios = mutableMapOf<String, Long>()
    for (linea in productos) {
        for ((ingrediente, unidades) in linea.producto.ingredientes) {
            necesarios[ingrediente] = Math.addExact(necesarios[ingrediente] ?: 0L,
                Math.multiplyExact(unidades, linea.cantidad.toLong()))
        }
    }
    return necesarios.toMap()
}

class Inventario(inicial: Map<String, Long>) {
    private val existencias = inicial.toMutableMap()
    init { require(existencias.all { (nombre, cantidad) -> nombre.isNotBlank() && cantidad >= 0 }) }
    fun consultar(): Map<String, Long> = existencias.toMap()

    fun consumir(necesarios: Map<String, Long>): List<Faltante> {
        require(necesarios.isNotEmpty() && necesarios.all { (nombre, cantidad) -> nombre.isNotBlank() && cantidad > 0 })
        val faltantes = necesarios.mapNotNull { (ingrediente, requerido) ->
            val disponible = existencias[ingrediente] ?: 0L
            when {
                disponible < requerido -> Faltante(ingrediente, disponible, requerido)
                else -> null
            }
        }
        when {
            faltantes.isEmpty() -> necesarios.forEach { (ingrediente, requerido) ->
                existencias[ingrediente] = existencias.getValue(ingrediente) - requerido
            }
            else -> return faltantes
        }
        return emptyList()
    }

    fun reabastecer(ingrediente: String, cantidad: Long) {
        require(ingrediente.isNotBlank() && cantidad > 0) { "Use un ingrediente y una cantidad positiva." }
        existencias[ingrediente] = Math.addExact(existencias[ingrediente] ?: 0L, cantidad)
    }
}

fun inventarioInicial(): Inventario = Inventario(
    mapOf("Masa" to 10L, "Tomate" to 20L, "Queso" to 20L, "Pepperoni" to 5L)
)
