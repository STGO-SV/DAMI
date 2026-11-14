package cl.duoc.pizzeriaapp.domain.support

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
