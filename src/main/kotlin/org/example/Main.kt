package org.example

import java.math.BigDecimal

fun main(args: Array<String>) {
    val tasa = try {
        (args.firstOrNull()?.toBigDecimal() ?: BigDecimal("0.19")).also {
            require(it >= BigDecimal.ZERO && it <= BigDecimal.ONE)
        }
    } catch (_: IllegalArgumentException) {
        println("Tasa inválida. Use un decimal entre 0 y 1, por ejemplo: --args=0.19")
        return
    }
    ejecutarJornada(jornada = Jornada(tasaImpuesto = tasa))
}

fun ejecutarJornada(
    leer: () -> String? = { readLine() },
    mostrar: (String) -> Unit = { println(it) },
    jornada: Jornada = Jornada()
) {
    mostrar("=== Pizzería Kotlin | Semana 2 ===")
    mostrar("Impuesto configurable: ${jornada.tasaImpuesto} (supuesto académico). Distancias de reparto simuladas desde el local.")
    while (true) {
        mostrar("1 Nuevo pedido | 2 Reporte | 3 Marcar Listo | 4 Enviar | 5 Entregar | 6 Inventario | 7 Reabastecer | 0 Salir")
        val opcion = leer()?.trim() ?: "0"
        try {
            when (opcion) {
                "0" -> { mostrar(jornada.reporte()); return }
                "1" -> registrarDesdeConsola(leer, mostrar, jornada)
                "2" -> mostrar(jornada.reporte())
                "3", "4", "5" -> {
                    mostrar("Identificador del pedido (por ejemplo PED-001):")
                    val id = leer()?.trim()
                    val pedido = jornada.pedidos().find { it.idPedido == id }
                        ?: throw IllegalArgumentException("Pedido no encontrado.")
                    when (opcion) {
                        "3" -> pedido.cambiarEstado(EstadoPedido.LISTO)
                        "4" -> {
                            val asignado = jornada.enviar(pedido)
                            mostrar(asignado?.describirUsuario() ?: "Sin repartidores disponibles. El pedido permanece Listo.")
                        }
                        "5" -> (pedido.repartidor ?: throw IllegalArgumentException("Pedido sin repartidor."))
                            .entregarPedido(pedido)
                    }
                    mostrar(pedido.resumen())
                    pedido.notificarCliente()?.let(mostrar)
                }
                "6" -> mostrar("Inventario (unidades de receta): ${jornada.inventario.consultar()}")
                "7" -> {
                    mostrar("Ingrediente (respete el nombre mostrado en inventario):")
                    val ingrediente = leer()?.trim() ?: ""
                    mostrar("Cantidad de unidades recibidas (entero positivo):")
                    val cantidad = leer()?.trim()?.toLongOrNull()
                        ?: throw IllegalArgumentException("Cantidad de inventario inválida.")
                    jornada.inventario.reabastecer(ingrediente, cantidad)
                    mostrar("Inventario actualizado: ${jornada.inventario.consultar()}")
                }
                else -> mostrar("Opción inválida. Seleccione una opción del menú.")
            }
        } catch (error: IllegalArgumentException) {
            mostrar("Datos inválidos: ${error.message}")
        } catch (_: ArithmeticException) {
            mostrar("Cantidad o importe fuera del rango permitido. Reduzca los valores e intente nuevamente.")
        }
    }
}

private fun registrarDesdeConsola(leer: () -> String?, mostrar: (String) -> Unit, jornada: Jornada) {
    mostrar("Nombre del cliente:")
    val nombre = leer() ?: return cancelarRegistro(mostrar)
    mostrar("Dirección de entrega:")
    val direccion = leer() ?: return cancelarRegistro(mostrar)
    mostrar("Teléfono de contacto:")
    val telefono = leer() ?: return cancelarRegistro(mostrar)
    if (telefonoValido(telefono) == null) mostrar("Teléfono inválido o ausente. Se registrará: No informado.")
    mostrar("Correo electrónico:")
    val correo = leer() ?: return cancelarRegistro(mostrar)
    if (correoValido(correo) == null) mostrar("Correo inválido o ausente. Se registrará: correo@invalido.com.")
    val cliente = Cliente(nombre, telefono, correo, direccion)
    val catalogo = catalogoInicial()
    val lineas = mutableListOf<LineaPedido>()
    var agregarOtra: Boolean
    do {
        catalogo.forEachIndexed { indice, producto -> mostrar("${indice + 1} ${producto.detallarProducto()}") }
        val producto = leerProducto(catalogo, leer, mostrar) ?: return cancelarRegistro(mostrar)
        mostrar("Pizza ${producto.nombre} seleccionada.")
        val cantidad = leerCantidad(leer, mostrar) ?: return cancelarRegistro(mostrar)
        lineas += LineaPedido(producto, cantidad)
        mostrar("¿Agregar otra pizza al pedido? (s/n):")
        val respuesta = leer() ?: return cancelarRegistro(mostrar)
        agregarOtra = respuesta.trim().lowercase() == "s"
    } while (agregarOtra)
    mostrar("Notas opcionales (vacío: Sin notas):")
    val notas = leer() ?: return cancelarRegistro(mostrar)
    val (pedido, faltantes) = jornada.registrar(cliente, lineas, notas)
    if (faltantes.isEmpty()) mostrar("Disponibilidad confirmada. Puede prepararse el pedido. Inventario actualizado.")
    else {
        mostrar("Pedido rechazado: ingredientes insuficientes. El inventario se conserva.")
        faltantes.forEach { mostrar(it.instrucciones()) }
    }
    mostrar(pedido.resumen())
    pedido.notificarCliente()?.let(mostrar)
    mostrar("Inventario restante: ${jornada.inventario.consultar()}")
}

private fun cancelarRegistro(mostrar: (String) -> Unit) {
    mostrar("Fin de entrada. Registro del pedido cancelado; no se modificó el inventario.")
}

fun pesos(monto: Long): String = "$" + monto.toString().reversed().chunked(3).joinToString(".").reversed() + " CLP"
fun pesos(monto: Int): String = pesos(monto.toLong())
