package cl.duoc.pizzeriaapp.domain.model

data class LineaPedido(val producto: Producto, val cantidad: Int) {
    init { require(cantidad > 0) { "La cantidad debe ser positiva." } }
}

fun calcularSubtotal(productos: List<LineaPedido>): Long = productos.fold(0L) { total, linea ->
    Math.addExact(total, Math.multiplyExact(linea.producto.precio, linea.cantidad.toLong()))
}
