package cl.duoc.pizzeriaapp.domain.order

import cl.duoc.pizzeriaapp.domain.inventory.Faltante
import cl.duoc.pizzeriaapp.domain.inventory.Inventario
import cl.duoc.pizzeriaapp.domain.inventory.calcularIngredientes
import cl.duoc.pizzeriaapp.domain.inventory.inventarioInicial
import cl.duoc.pizzeriaapp.domain.model.Cliente
import cl.duoc.pizzeriaapp.domain.model.LineaPedido
import cl.duoc.pizzeriaapp.domain.model.Producto
import cl.duoc.pizzeriaapp.domain.model.catalogoInicial
import cl.duoc.pizzeriaapp.domain.promotion.Promocion
import cl.duoc.pizzeriaapp.domain.promotion.mismaFicha
import cl.duoc.pizzeriaapp.domain.support.Registro
import cl.duoc.pizzeriaapp.domain.support.pesos
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class Jornada(
    val inventario: Inventario = inventarioInicial(),
    val repartidores: List<Repartidor> = repartidoresIniciales(),
    val tasaImpuesto: BigDecimal = BigDecimal("0.19"),
    val promocion: Promocion? = null,
    val reloj: Clock = Clock.system(ZoneId.of("America/Santiago"))
) {
    private var pedidosDelDia: Array<Pedido> = emptyArray()
    private val clientes = Registro<Cliente>()
    private val catalogo = Registro<Producto>().also { registro -> catalogoInicial().forEach { registro.registrar(it) } }
    fun clientesRegistrados(): List<Cliente> = clientes.listar()
    fun productosRegistrados(): List<Producto> = catalogo.listar()
    fun clienteRegistrado(cliente: Cliente): Boolean = clientes.buscar(cliente) != null
    init { require(tasaImpuesto >= BigDecimal.ZERO && tasaImpuesto <= BigDecimal.ONE) }
    fun pedidos(): Array<Pedido> = pedidosDelDia.copyOf()
    fun completados(): List<Pedido> = pedidosDelDia.filter { it.completado }
    fun pendientes(): List<Pedido> = pedidosDelDia.filter { it.pendiente }
    fun rechazados(): List<Pedido> = pedidosDelDia.filter { it.estado == EstadoPedido.RECHAZADO }

    fun registrar(cliente: Cliente, productos: List<LineaPedido>, notas: String?): Pair<Pedido, List<Faltante>> {
        val fichas = productos.map { it.producto }
        for (producto in fichas) {
            val registrados = fichas.filter { it == producto } + listOfNotNull(catalogo.buscar(producto))
            require(registrados.all { it.mismaFicha(producto) }) { "Producto duplicado con ficha incompatible: ${producto.nombre}." }
        }
        val pedido = Pedido("PED-${(pedidosDelDia.size + 1).toString().padStart(3, '0')}",
            cliente, productos, notas, tasaImpuesto, LocalDate.now(reloj), promocion)
        val necesarios = calcularIngredientes(productos)
        val faltantes = inventario.consumir(necesarios)
        pedido.resolverInventario(faltantes.isEmpty())
        clientes.registrar(cliente)
        fichas.forEach { catalogo.registrar(it) }
        pedidosDelDia += pedido
        return pedido to faltantes
    }

    fun enviar(pedido: Pedido): Repartidor? {
        require(pedidosDelDia.any { it === pedido } && pedido.estado == EstadoPedido.LISTO) {
            "Seleccione un pedido de la jornada en estado Listo."
        }
        val repartidor = buscarRepartidorCercano(repartidores)
        repartidor?.tomarPedido(pedido)
        return repartidor
    }

    fun reporte(): String = buildString {
        appendLine("=== Pedidos del día ===")
        for (pedido in pedidosDelDia) {
            val pizzas = pedido.productos.joinToString { "${it.producto.nombre} x${it.cantidad}" }
            appendLine("${pedido.idPedido} | ${pedido.cliente.nombre} | $pizzas | ${pesos(pedido.calcularImportes().total)} | ${pedido.estado.descripcion}")
        }
        append("Completados (Entregado): ${completados().size} | Pendientes (Registrado, En preparación, Listo, En camino): ${pendientes().size} | Rechazados: ${rechazados().size}")
    }
}
