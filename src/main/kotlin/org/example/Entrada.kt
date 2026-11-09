package org.example

fun textoOValorPredeterminado(entrada: String?, predeterminado: String): String =
    entrada?.trim()?.takeIf { it.isNotEmpty() } ?: predeterminado

private val formatoTelefono = Regex("(?:\\+|\\(\\+56\\))?[0-9 ()-]+")

fun telefonoValido(entrada: String?): String? {
    val telefono = entrada?.trim() ?: return null
    if (!formatoTelefono.matches(telefono)) return null
    val digitos = telefono.filter { it in '0'..'9' }
    if ('+' in telefono && digitos.startsWith('0')) return null
    return telefono.takeIf { digitos.length in 8..15 && (!tienePrefijoChileno(telefono, digitos) || digitos.length == 11) }
}

private fun tienePrefijoChileno(telefono: String, digitos: String): Boolean =
    telefono.contains('+') && digitos.startsWith("56")

fun normalizarTelefono(entrada: String?): String? {
    val original = telefonoValido(entrada) ?: return null
    val digitos = original.filter { it in '0'..'9' }
    if (tienePrefijoChileno(original, digitos) || (digitos.length == 9 && '+' !in original)) {
        val nacional = digitos.takeLast(9)
        return "(+56) ${nacional[0]} ${nacional.substring(1, 5)} ${nacional.takeLast(4)}"
    }
    if (digitos.length == 12 && !digitos.startsWith("56") && !digitos.startsWith('0')) {
        return "+${digitos.take(2)} (${digitos.substring(2, 5)}) ${digitos.substring(5, 8)}-${digitos.takeLast(4)}"
    }
    return original
}

fun correoValido(entrada: String?): String? = entrada?.trim()?.takeIf {
    it.length <= 254 && it.matches(Regex("[A-Za-z0-9_+%-]+(?:\\.[A-Za-z0-9_+%-]+)*@[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*(?:\\.[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*)*\\.[A-Za-z]{2,}"))
}

internal val rangoCantidadPizzas = 1..100

fun convertirCantidad(entrada: String?): Int? =
    entrada?.trim()?.toIntOrNull()?.takeIf { it in rangoCantidadPizzas }

fun leerProducto(catalogo: List<Producto>, leer: () -> String?, mostrar: (String) -> Unit): Producto? {
    while (true) {
        mostrar("Seleccione pizza (vacío: Margarita):")
        mostrar("Ingrese C para cancelar el pedido.")
        val entrada = leer() ?: return null
        if (entrada.trim().equals("c", ignoreCase = true)) return null
        val seleccion = textoOValorPredeterminado(entrada, "1").toIntOrNull()
        val producto = seleccion?.takeIf { it in 1..catalogo.size }?.let { catalogo[it - 1] }
        if (producto != null) return producto
        mostrar("Pizza inexistente. Seleccione una opción válida.")
    }
}

fun leerCantidad(
    leer: () -> String?, mostrar: (String) -> Unit,
    pregunta: String = "Cantidad de pizzas (entero entre 1 y 100):", maximo: Int = rangoCantidadPizzas.last
): Int? {
    require(maximo in rangoCantidadPizzas)
    mostrar(pregunta)
    while (true) {
        val entrada = leer() ?: return null
        val cantidad = convertirCantidad(entrada)
        when {
            cantidad == null -> mostrar("Cantidad inválida. Ingrese un entero entre 1 y 100.")
            cantidad > maximo -> {
                mostrar("La cantidad solicitada supera la disponibilidad.")
                mostrar("Máximo disponible: $maximo.")
            }
            else -> return cantidad
        }
        mostrar("Ingrese una cantidad válida:")
    }
}

fun leerCorreo(leer: () -> String?, mostrar: (String) -> Unit): String? {
    while (true) {
        mostrar("Correo electrónico:")
        val entrada = leer() ?: return null
        val correo = correoValido(entrada)
        if (correo != null) return correo
        mostrar("Correo inválido. Ingrese nombre@dominio.com; el correo es obligatorio.")
    }
}

fun leerSiNo(pregunta: String, leer: () -> String?, mostrar: (String) -> Unit): Boolean? {
    while (true) {
        mostrar(pregunta)
        when (leer()?.trim()) {
            "S", "s" -> return true
            "N", "n" -> return false
            null -> return null
            else -> mostrar("Respuesta inválida. Ingrese S o N.")
        }
    }
}
