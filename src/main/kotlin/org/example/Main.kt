package org.example

fun main(args: Array<String>) {
    val configuracion = try {
        leerConfiguracion(args)
    } catch (_: IllegalArgumentException) {
        println("Configuración inválida. Use impuesto 0..1, --fecha=AAAA-MM-DD y --promocion=inicio,fin,porcentaje (0..1).")
        return
    } catch (_: java.time.DateTimeException) {
        println("Fecha inválida. Use fechas existentes en formato AAAA-MM-DD.")
        return
    }
    if (configuracion.demostracion) demostrarSemana3(::println)
    else ejecutarJornada(jornada = configuracion.jornada())
}

fun ejecutarJornada(
    leer: () -> String? = { readLine() },
    mostrar: (String) -> Unit = { println(it) },
    jornada: Jornada = Jornada()
) {
    mostrar("=== Pizzería Kotlin | Semana 3 ===")
    mostrar("Impuesto configurable: ${jornada.tasaImpuesto} (supuesto académico). Distancias de reparto simuladas desde el local.")
    mostrar("Fecha de pedidos: ${java.time.LocalDate.now(jornada.reloj)} | Promoción: ${jornada.promocion ?: "Sin promoción por fechas"}")
    while (true) {
        mostrar("1 Nuevo pedido | 2 Reporte | 3 Marcar Listo | 4 Enviar | 5 Entregar | 6 Inventario | 7 Reabastecer | 8 Demostración Semana 3 | 0 Salir")
        val opcion = leer()?.trim() ?: "0"
        try {
            when (opcion) {
                "0" -> { mostrar(jornada.reporte()); return }
                "1" -> registrarDesdeConsola(leer, mostrar, jornada)
                "2" -> mostrar(jornada.reporte())
                "8" -> demostrarSemana3(mostrar)
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
    val correo = leerCorreo(leer, mostrar) ?: return cancelarRegistro(mostrar)
    val cliente = Cliente(nombre, telefono, correo, direccion)
    val catalogo = jornada.productosRegistrados()
    val lineas = mutableListOf<LineaPedido>()
    while (true) {
        catalogo.forEachIndexed { indice, producto -> mostrar("${indice + 1} ${producto.detallarProducto()}") }
        val producto = leerProducto(catalogo, leer, mostrar) ?: return cancelarRegistro(mostrar)
        mostrar("Pizza ${producto.nombre} seleccionada.")
        val maximo = jornada.inventario.capacidad(producto, lineas)
        mostrar("Unidades disponibles para este pedido: $maximo")
        if (maximo == 0) {
            mostrar("${producto.nombre} está agotada para este pedido. Seleccione otra variedad o ingrese C para cancelar.")
            continue
        }
        val cantidad = leerCantidad(leer, mostrar, "¿Cuántas pizzas ${producto.nombre} desea pedir?", maximo)
            ?: return cancelarRegistro(mostrar)
        lineas += LineaPedido(producto, cantidad)
        val agregarOtra = leerSiNo("¿Agregar otra pizza al pedido? (S/N):", leer, mostrar)
            ?: return cancelarRegistro(mostrar)
        if (!agregarOtra) break
    }
    mostrar("Notas opcionales (vacío: Sin notas):")
    val notas = leer() ?: return cancelarRegistro(mostrar)
    if (jornada.clienteRegistrado(cliente)) mostrar("Cliente ya registrado: se conserva la primera ficha; este pedido utiliza los datos ingresados.")
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
    mostrar("Registro del pedido cancelado. El inventario se conserva.")
}

fun pesos(monto: Long): String = "$" + monto.toString().reversed().chunked(3).joinToString(".").reversed() + " CLP"
