package org.example

open class Usuario(nombre: String?, telefono: String?, correo: String?) {
    val nombre = textoOValorPredeterminado(nombre, "Cliente sin nombre")
    val telefono = telefonoValido(telefono) ?: "No informado"
    val correo = correoValido(correo) ?: "correo@invalido.com"
    val correoNotificable: String? = correoValido(correo)?.takeUnless { it == "correo@invalido.com" }
    protected fun contacto(): String = "Teléfono: $telefono\nCorreo: $correo"
    open fun describirUsuario(): String = "Usuario: $nombre\n${contacto()}"
}

class Cliente(nombre: String?, telefono: String?, correo: String?, direccion: String?) :
    Usuario(nombre, telefono, correo) {
    val direccion = textoOValorPredeterminado(direccion, "Retiro en local")
    override fun describirUsuario(): String = "Cliente: $nombre\nDirección: $direccion\n${contacto()}"
}

class Repartidor(
    nombre: String, telefono: String?, correo: String?, val distanciaMetros: Int,
    disponibleInicial: Boolean = true
) : Usuario(nombre, telefono, correo) {
    var disponible: Boolean = disponibleInicial
        private set
    private var pedidoActivo: Pedido? = null
    init { require(distanciaMetros >= 0) { "La distancia no puede ser negativa." } }

    fun tomarPedido(pedido: Pedido) {
        require(disponible && pedidoActivo == null) { "Repartidor no disponible." }
        pedido.asignar(this)
        pedidoActivo = pedido
        disponible = false
    }

    fun entregarPedido(pedido: Pedido) {
        require(pedidoActivo === pedido) { "El pedido no pertenece a este repartidor." }
        pedido.confirmarEntrega(this)
        pedidoActivo = null
        disponible = true
    }

    override fun describirUsuario(): String =
        "Repartidor: $nombre | Distancia simulada: $distanciaMetros m | Disponible: $disponible"
}

fun repartidoresIniciales(): List<Repartidor> = listOf(
    Repartidor("R01 - Paula", "912345678", null, 1500),
    Repartidor("R02 - Diego", "923456789", null, 500),
    Repartidor("R03 - Camila", "934567890", null, 900)
)

fun buscarRepartidorCercano(repartidores: List<Repartidor>): Repartidor? =
    repartidores.sortedBy { it.distanciaMetros }.find { it.disponible }
