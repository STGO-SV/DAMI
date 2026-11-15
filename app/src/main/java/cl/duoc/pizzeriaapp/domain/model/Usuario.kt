package cl.duoc.pizzeriaapp.domain.model

import cl.duoc.pizzeriaapp.domain.support.claveIdentidad
import cl.duoc.pizzeriaapp.domain.support.correoValido
import cl.duoc.pizzeriaapp.domain.support.normalizarTelefono
import cl.duoc.pizzeriaapp.domain.support.textoOValorPredeterminado

open class Usuario(nombre: String?, telefono: String?, correo: String?) {
    val nombre = textoOValorPredeterminado(nombre, "Cliente sin nombre")
    val telefono = normalizarTelefono(telefono) ?: "No informado"
    val correo = correoValido(correo) ?: "correo@invalido.com"
    val correoNotificable: String? = correoValido(correo)?.takeUnless { it == "correo@invalido.com" }
    protected fun contacto(): String = "Teléfono: $telefono\nCorreo: $correo"
    open fun describirUsuario(): String = "Usuario: $nombre\n${contacto()}"
}

class Cliente(nombre: String?, telefono: String?, correo: String?, direccion: String?) :
    Usuario(nombre, telefono, correo) {
    val direccion = textoOValorPredeterminado(direccion, "Retiro en local")
    override fun describirUsuario(): String = "Cliente: $nombre\nDirección: $direccion\n${contacto()}"

    override fun equals(other: Any?): Boolean = other is Cliente &&
        claveIdentidad(nombre) == claveIdentidad(other.nombre) && claveIdentidad(correo) == claveIdentidad(other.correo)

    override fun hashCode(): Int = 31 * claveIdentidad(nombre).hashCode() + claveIdentidad(correo).hashCode()

    operator fun component1(): String = nombre
    operator fun component2(): String = correo
    operator fun component3(): String = telefono
}
