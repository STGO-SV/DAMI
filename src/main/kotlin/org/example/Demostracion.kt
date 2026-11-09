package org.example

import java.math.BigDecimal
import java.time.LocalDate

/** Ejemplo reproducible: no utiliza ni modifica la jornada operativa del menú. */
fun demostrarSemana3(mostrar: (String) -> Unit) {
    mostrar("=== Demostración integrada | Semana 3 ===")
    mostrar("Datos académicos; cotizaciones sin cobro ni consumo de inventario.")
    mostrar("--- Regex y rangos ---")
    for (correo in listOf("cliente@example.com", "cliente@example.cl", "sin-arroba", "a b@example.com")) {
        mostrar("Correo '$correo': ${if (correoValido(correo) != null) "válido" else "inválido"}")
    }
    for (telefono in listOf("+12 345 678 9012", "+56912345678", "abc")) {
        mostrar("Teléfono '$telefono': ${normalizarTelefono(telefono) ?: "inválido"}")
    }
    for (cantidad in listOf("0", "1", "100", "101")) {
        mostrar("Cantidad $cantidad: ${if (convertirCantidad(cantidad) != null) "válida" else "inválida"}")
    }
    val fecha = LocalDate.of(2026, 11, 3)
    val promocion = Promocion(fecha.minusDays(2), fecha.plusDays(4), BigDecimal("0.15"))
    val cliente = Cliente("José", "+12 345 678 9012", "cliente@example.com", "Ñuñoa 123")
    val catalogo = catalogoInicial()
    val referencia = Producto("Producto de referencia", 8000, mapOf("Masa" to 1L))
    mostrar("--- Anotaciones y reflection ---")
    for (producto in catalogo + referencia) {
        mostrar("${producto.nombre}: promocionable=${esPromocionable(producto)}")
    }
    for (tipo in listOf(Margarita::class, Pepperoni::class, Producto::class, Pedido::class)) {
        mostrar(inspeccionarClase(tipo))
    }
    mostrar("--- Operadores y recálculo ---")
    val primero = Pedido("DEMO-A", cliente, listOf(LineaPedido(catalogo[0], 2)), "sin sal", fecha = fecha)
    val segundo = Pedido("DEMO-B", cliente, listOf(LineaPedido(catalogo[1], 2)), "tocar timbre", fecha = fecha)
    val combinado = primero + segundo
    mostrar("Pedidos combinados mediante +: ${primero.idPedido} + ${segundo.idPedido} = ${combinado.idPedido}")
    mostrar("Totales originales: ${pesos(primero.calcularImportes().total)} y ${pesos(segundo.calcularImportes().total)}")
    mostrar(combinado.resumen())
    mostrar("--- Promociones por fechas ---")
    for (dia in listOf(fecha, promocion.fin.plusDays(1))) {
        val pedido = Pedido("DEMO-PROMO-$dia", cliente, combinado.productos, tasaImpuesto = BigDecimal("0.19"),
            fecha = dia, promocion = promocion, esSimulacion = true)
        mostrar("Promoción ${if (promocion.activa(dia)) "activa" else "inactiva"}: $dia")
        mostrar(pedido.resumen())
    }
    val muestraElegibilidad = Pedido("DEMO-ELEGIBILIDAD", cliente,
        listOf(LineaPedido(catalogo[0], 1), LineaPedido(referencia, 1)),
        fecha = fecha, promocion = promocion, esSimulacion = true)
    mostrar(muestraElegibilidad.resumen())
    mostrar("--- Desestructuración ---")
    val (nombre, correo, telefono) = cliente
    mostrar("Cliente desestructurado: $nombre | $correo | $telefono")
    val (clientePedido, productos, total) = combinado
    mostrar("Pedido desestructurado: ${clientePedido.nombre} | ${productos.joinToString { "${it.producto.nombre} x${it.cantidad}" }} | ${pesos(total)}")
    mostrar("--- Igualdad y registros sin duplicados ---")
    val repetido = Cliente(" JOSÉ ", "+56912345678", "CLIENTE@example.com", "Otra dirección")
    val productoRepetido = Producto(" margarita ", 123, mapOf("Masa" to 1L), "PIZZA")
    mostrar("Clientes ==: ${cliente == repetido}")
    mostrar("Productos ==: ${catalogo[0] == productoRepetido}")
    val clientes = Registro<Cliente>()
    val productosRegistrados = Registro<Producto>()
    mostrar("Registro cliente: nuevo=${clientes.registrar(cliente)}, duplicado agregado=${clientes.registrar(repetido)}, fichas=${clientes.listar().size}")
    mostrar("Registro producto: nuevo=${productosRegistrados.registrar(catalogo[0])}, duplicado agregado=${productosRegistrados.registrar(productoRepetido)}, fichas=${productosRegistrados.listar().size}")
    mostrar("Resumen final: combinación=${pesos(total)}; originales ${primero.estado.descripcion}/${segundo.estado.descripcion}; registros únicos=${clientes.listar().size}/${productosRegistrados.listar().size}.")
}
