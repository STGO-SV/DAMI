package org.example

fun textoOValorPredeterminado(entrada: String?, predeterminado: String): String =
    entrada?.trim()?.takeIf { it.isNotEmpty() } ?: predeterminado

fun telefonoValido(entrada: String?): String? {
    val telefono = entrada?.trim() ?: return null
    val caracteresValidos = telefono.matches(Regex("\\+?[0-9 ()-]+"))
    val cantidadDigitos = telefono.count { it in '0'..'9' }
    return telefono.takeIf { caracteresValidos && cantidadDigitos in 8..15 }
}

fun convertirCantidad(entrada: String?): Int? = entrada?.trim()?.toIntOrNull()?.takeIf { it > 0 }

fun leerCantidad(leer: () -> String?, mostrar: (String) -> Unit): Int {
    while (true) {
        mostrar("Cantidad de pizzas (entero mayor que cero):")
        val entrada = leer()
        if (entrada == null) {
            mostrar("Fin de entrada. Se utilizará la cantidad predeterminada: 1.")
            return 1
        }
        val cantidad = convertirCantidad(entrada)
        if (cantidad != null) return cantidad
        mostrar("Cantidad inválida. Ingrese un entero positivo dentro del rango Int.")
    }
}
