package cl.duoc.pizzeriaapp.domain.order

import cl.duoc.pizzeriaapp.domain.model.Usuario

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
