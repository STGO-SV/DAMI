package org.example

fun textoOValorPredeterminado(entrada: String?, predeterminado: String): String =
    entrada?.trim()?.takeIf { it.isNotEmpty() } ?: predeterminado

fun telefonoValido(entrada: String?): String? {
    val telefono = entrada?.trim() ?: return null
    val caracteresValidos = telefono.matches(Regex("\\+?[0-9 ()-]+"))
    val cantidadDigitos = telefono.count { it in '0'..'9' }
    return telefono.takeIf { caracteresValidos && cantidadDigitos in 8..15 }
}

fun correoValido(entrada: String?): String? = entrada?.trim()?.takeIf {
    it.length <= 254 && it.matches(Regex("[A-Za-z0-9_+%-]+(?:\\.[A-Za-z0-9_+%-]+)*@[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*(?:\\.[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*)*\\.[A-Za-z]{2,}"))
}

fun convertirCantidad(entrada: String?): Int? = try {
    entrada?.trim()?.toInt()?.takeIf { it > 0 }
} catch (_: NumberFormatException) {
    null
}

fun leerProducto(catalogo: List<Producto>, leer: () -> String?, mostrar: (String) -> Unit): Producto? {
    while (true) {
        mostrar("Seleccione pizza (vacío: Margarita):")
        val entrada = leer() ?: return null
        val seleccion = textoOValorPredeterminado(entrada, "1").toIntOrNull()
        val producto = seleccion?.takeIf { it in 1..catalogo.size }?.let { catalogo[it - 1] }
        if (producto != null) return producto
        mostrar("Pizza inexistente. Seleccione una opción válida.")
    }
}

fun leerCantidad(leer: () -> String?, mostrar: (String) -> Unit): Int? {
    while (true) {
        mostrar("Cantidad de pizzas (entero mayor que cero):")
        val entrada = leer() ?: return null
        val cantidad = convertirCantidad(entrada)
        if (cantidad != null) return cantidad
        mostrar("Cantidad inválida. Ingrese un entero positivo dentro del rango Int.")
    }
}
