package org.example

import java.math.BigDecimal
import kotlin.test.*

class PedidoTest {
    private val cliente = Cliente("José", "+56901234567", "jose@example.com", "Ñuñoa 123")
    private val margarita = catalogoInicial()[0]
    private fun lineas(cantidad: Int) = listOf(LineaPedido(margarita, cantidad))
    private fun registrar(jornada: Jornada = Jornada(), cantidad: Int = 1, notas: String? = null): Pedido =
        jornada.registrar(cliente, lineas(cantidad), notas).first

    @Test fun unaPizzaSinDescuento() {
        assertEquals(Importes(8000, 0, 1520, 9520), registrar().calcularImportes())
    }
    @Test fun tresPizzasSinDescuento() {
        assertEquals(Importes(24000, 0, 4560, 28560), registrar(cantidad = 3).calcularImportes())
    }
    @Test fun cuatroPizzasConDiezPorCiento() {
        assertEquals(Importes(32000, 3200, 5472, 34272), registrar(cantidad = 4).calcularImportes())
    }
    @Test fun cincoPizzasConDiezPorCiento() {
        assertEquals(42840L, registrar(cantidad = 5).calcularImportes().total)
    }
    @Test fun tasaCeroConservaTotalesSemanaUno() {
        for ((cantidad, total) in listOf(1 to 8000L, 3 to 24000L, 4 to 28800L, 5 to 36000L)) {
            assertEquals(total, registrar(Jornada(tasaImpuesto = BigDecimal.ZERO), cantidad).calcularImportes().total)
        }
    }
    @Test fun redondeoTruncaDescuentoYRedondeaImpuestoAlPeso() {
        assertEquals(3L, calcularDescuento(32, 4))
        assertEquals(1L, calcularImpuesto(5, BigDecimal("0.10")))
        assertEquals(0L, calcularImpuesto(4, BigDecimal("0.10")))
        assertEquals(29L, calcularTotal(listOf(LineaPedido(Producto("P", 8, mapOf("M" to 1L)), 4)), BigDecimal.ZERO).total)
    }
    @Test fun dineroGrandeNoDesbordaInt() {
        val grande = Producto("Grande", Int.MAX_VALUE.toLong(), mapOf("M" to 1L))
        val importes = calcularTotal(listOf(LineaPedido(grande, Int.MAX_VALUE)), BigDecimal.ZERO)
        assertEquals(4_611_686_014_132_420_609L, importes.subtotal)
        assertEquals(4_150_517_412_719_178_549L, importes.total)
    }
    @Test fun desbordamientoSeDetectaAntesDeConsumir() {
        val jornada = Jornada()
        val inicial = jornada.inventario.consultar()
        assertFailsWith<ArithmeticException> {
            jornada.registrar(cliente, listOf(LineaPedido(Producto("P", Long.MAX_VALUE, mapOf("Masa" to 1L)), 2)), null)
        }
        assertEquals(inicial, jornada.inventario.consultar())
        assertTrue(jornada.pedidos().isEmpty())
    }
    @Test fun tasasInvalidasSeRechazan() {
        for (tasa in listOf(BigDecimal("-0.01"), BigDecimal("1.01"))) {
            assertFailsWith<IllegalArgumentException> { Jornada(tasaImpuesto = tasa) }
            assertFailsWith<IllegalArgumentException> { calcularImpuesto(10, tasa) }
        }
    }
    @Test fun calculoDeIngredientesParaLoteMixto() {
        assertEquals(mapOf("Masa" to 5L, "Tomate" to 10L, "Queso" to 10L, "Pepperoni" to 3L),
            calcularIngredientes(listOf(LineaPedido(margarita, 2), LineaPedido(catalogoInicial()[1], 3))))
    }
    @Test fun importesMixtosAcumulanCantidadesParaDescuento() {
        val pedido = Jornada().registrar(cliente,
            listOf(LineaPedido(margarita, 2), LineaPedido(catalogoInicial()[1], 2)), null).first
        assertEquals(4, pedido.cantidadPizzas)
        assertEquals(Importes(34000, 3400, 5814, 36414), pedido.calcularImportes())
    }
    @Test fun listoYEnCaminoPermanecenPendientes() {
        val jornada = Jornada()
        val pedido = registrar(jornada)
        pedido.cambiarEstado(EstadoPedido.LISTO)
        assertEquals(listOf(pedido), jornada.pendientes())
        assertTrue(jornada.completados().isEmpty())
        jornada.enviar(pedido)
        assertEquals(listOf(pedido), jornada.pendientes())
        assertTrue(jornada.completados().isEmpty())
    }
    @Test fun stockExactoConfirmaYAgotaInventario() {
        val jornada = Jornada(tasaImpuesto = BigDecimal.ZERO)
        val pedido = registrar(jornada, 10)
        assertEquals(EstadoPedido.EN_PREPARACION, pedido.estado)
        assertEquals(0L, jornada.inventario.consultar()["Masa"])
        assertEquals(0L, jornada.inventario.consultar()["Tomate"])
        assertEquals(72000L, pedido.calcularImportes().total)
    }
    @Test fun stockSuperiorSeDescuenta() {
        val jornada = Jornada()
        registrar(jornada, 3)
        assertEquals(mapOf("Masa" to 7L, "Tomate" to 14L, "Queso" to 14L, "Pepperoni" to 5L), jornada.inventario.consultar())
    }
    @Test fun stockInsuficienteConservaTodoSinCobroNiReparto() {
        val jornada = Jornada()
        val inicial = jornada.inventario.consultar()
        val (pedido, faltantes) = jornada.registrar(cliente, lineas(11), null)
        assertEquals(inicial, jornada.inventario.consultar())
        assertEquals(EstadoPedido.RECHAZADO, pedido.estado)
        assertEquals(Importes(88000, 0, 0, 0), pedido.calcularImportes())
        assertNull(pedido.repartidor)
        assertEquals(setOf("Masa", "Tomate", "Queso"), faltantes.map { it.ingrediente }.toSet())
        assertEquals(Faltante("Masa", 10, 11), faltantes.first())
        assertTrue(faltantes.first().instrucciones().contains("Reabastecer al menos 1 unidades"))
    }
    @Test fun faltaSoloUnIngredienteEvitaConsumoParcial() {
        val inventario = Inventario(mapOf("Masa" to 10L, "Tomate" to 20L, "Queso" to 0L))
        val inicial = inventario.consultar()
        assertEquals(listOf(Faltante("Queso", 0, 2)), inventario.consumir(calcularIngredientes(lineas(1))))
        assertEquals(inicial, inventario.consultar())
    }
    @Test fun ingredienteAusenteSeConsideraCero() {
        assertEquals(listOf(Faltante("Masa", 0, 1)), Inventario(emptyMap()).consumir(mapOf("Masa" to 1L)))
    }
    @Test fun stockCeroRechaza() {
        assertEquals(EstadoPedido.RECHAZADO, registrar(Jornada(Inventario(emptyMap()))).estado)
    }
    @Test fun reabastecerPermiteNuevoPedidoSinRevivirRechazado() {
        val jornada = Jornada(Inventario(emptyMap()))
        val rechazado = registrar(jornada)
        calcularIngredientes(lineas(1)).forEach { (nombre, cantidad) -> jornada.inventario.reabastecer(nombre, cantidad) }
        assertEquals(EstadoPedido.EN_PREPARACION, registrar(jornada).estado)
        assertEquals(EstadoPedido.RECHAZADO, rechazado.estado)
    }
    @Test fun inventarioInvalidoSeRechazaSinCambios() {
        assertFailsWith<IllegalArgumentException> { Inventario(mapOf("M" to -1L)) }
        val inventario = inventarioInicial()
        val inicial = inventario.consultar()
        for (cantidad in listOf(0L, -1L)) {
            assertFailsWith<IllegalArgumentException> { inventario.reabastecer("Masa", cantidad) }
            assertFailsWith<IllegalArgumentException> { inventario.consumir(mapOf("Masa" to cantidad)) }
        }
        assertEquals(inicial, inventario.consultar())
    }
    @Test fun variosPedidosCompartenInventarioEIdentificadoresUnicos() {
        val jornada = Jornada()
        val pedidos = listOf(registrar(jornada, 4), registrar(jornada, 5), registrar(jornada, 2), registrar(jornada, 1))
        assertEquals(listOf("PED-001", "PED-002", "PED-003", "PED-004"), pedidos.map { it.idPedido })
        assertEquals(EstadoPedido.RECHAZADO, pedidos[2].estado)
        assertEquals(0L, jornada.inventario.consultar()["Masa"])
        assertEquals(3, jornada.pendientes().size)
        assertEquals(1, jornada.rechazados().size)
    }
    @Test fun filtrosYReporteConArregloIncluyenTodosLosPedidos() {
        val jornada = Jornada()
        val entregado = registrar(jornada)
        entregado.cambiarEstado(EstadoPedido.LISTO)
        jornada.enviar(entregado)!!.entregarPedido(entregado)
        val pendiente = registrar(jornada, 2)
        val rechazado = registrar(jornada, 11)
        val arreglo: Array<Pedido> = jornada.pedidos()
        assertEquals(3, arreglo.size)
        assertEquals(listOf(entregado), jornada.completados())
        assertEquals(listOf(pendiente), jornada.pendientes())
        assertEquals(listOf(rechazado), jornada.rechazados())
        val reporte = jornada.reporte()
        for (texto in listOf("PED-001 | José | Margarita x1 | $9.520 CLP | Entregado", "PED-002", "PED-003", "Completados (Entregado): 1")) {
            assertTrue(reporte.contains(texto), texto)
        }
    }
    @Test fun seEligeRealmenteMenorDistanciaDisponible() {
        val repartidores = repartidoresIniciales()
        assertSame(repartidores[1], buscarRepartidorCercano(repartidores))
        val jornada = Jornada(repartidores = repartidores)
        val pedido = registrar(jornada)
        pedido.cambiarEstado(EstadoPedido.LISTO)
        assertSame(repartidores[1], jornada.enviar(pedido))
        assertFalse(repartidores[1].disponible)
        assertSame(repartidores[2], buscarRepartidorCercano(repartidores))
        assertEquals(EstadoPedido.EN_CAMINO, pedido.estado)
    }
    @Test fun empateDeDistanciaConservaOrdenDeLista() {
        val primero = Repartidor("A", null, null, 1)
        assertSame(primero, buscarRepartidorCercano(listOf(primero, Repartidor("B", null, null, 1))))
    }
    @Test fun sinRepartidoresPermaneceListo() {
        val jornada = Jornada(repartidores = listOf(Repartidor("A", null, null, 0, false)))
        val pedido = registrar(jornada)
        pedido.cambiarEstado(EstadoPedido.LISTO)
        assertNull(jornada.enviar(pedido))
        assertEquals(EstadoPedido.LISTO, pedido.estado)
        assertNull(buscarRepartidorCercano(emptyList()))
    }
    @Test fun entregaLiberaRepartidorYCompletaPedido() {
        val jornada = Jornada()
        val pedido = registrar(jornada)
        pedido.cambiarEstado(EstadoPedido.LISTO)
        val repartidor = jornada.enviar(pedido)!!
        repartidor.entregarPedido(pedido)
        assertTrue(repartidor.disponible)
        assertTrue(pedido.completado)
        assertFalse(pedido.pendiente)
        assertFailsWith<IllegalArgumentException> { repartidor.entregarPedido(pedido) }
    }
    @Test fun transicionesInvalidasNoAlteranEstado() {
        val jornada = Jornada()
        val pedido = registrar(jornada)
        assertFailsWith<IllegalArgumentException> { jornada.enviar(pedido) }
        assertFailsWith<IllegalArgumentException> { pedido.cambiarEstado(EstadoPedido.ENTREGADO) }
        assertEquals(EstadoPedido.EN_PREPARACION, pedido.estado)
        pedido.cambiarEstado(EstadoPedido.LISTO)
        jornada.enviar(pedido)
        assertFailsWith<IllegalArgumentException> { jornada.enviar(pedido) }
        assertFailsWith<IllegalArgumentException> { jornada.repartidores[0].entregarPedido(pedido) }
        assertEquals(EstadoPedido.EN_CAMINO, pedido.estado)
    }
    @Test fun rechazadoNoPuedePrepararseEnviarseNiEntregarse() {
        val jornada = Jornada()
        val pedido = registrar(jornada, 11)
        for (estado in EstadoPedido.entries) assertFailsWith<IllegalArgumentException> { pedido.cambiarEstado(estado) }
        assertFailsWith<IllegalArgumentException> { jornada.enviar(pedido) }
        assertFailsWith<IllegalArgumentException> { jornada.repartidores[0].tomarPedido(pedido) }
        assertFailsWith<IllegalArgumentException> { jornada.repartidores[0].entregarPedido(pedido) }
        assertEquals(EstadoPedido.RECHAZADO, pedido.estado)
    }
    @Test fun herenciaYPolimorfismoDescribenRoles() {
        val usuarios: List<Usuario> = listOf(cliente, repartidoresIniciales().first())
        assertTrue(usuarios[0].describirUsuario().startsWith("Cliente: José"))
        assertTrue(usuarios[1].describirUsuario().startsWith("Repartidor:"))
        assertTrue(margarita.detallarProducto().contains("Masa: 1 unidades"))
    }
    @Test fun encapsulacionCopiaRecetasInventarioPedidosYLineas() {
        val receta = mutableMapOf("Masa" to 1L)
        val producto = Producto("P", 1, receta)
        receta["Masa"] = 99
        assertEquals(1L, producto.ingredientes["Masa"])
        val jornada = Jornada()
        val originales = lineas(1).toMutableList()
        val pedido = jornada.registrar(cliente, originales, null).first
        originales.clear()
        assertEquals(1, pedido.productos.size)
        jornada.pedidos()[0] = Pedido("otro", cliente, lineas(1))
        assertSame(pedido, jornada.pedidos()[0])
        val copia = jornada.inventario.consultar().toMutableMap()
        copia["Masa"] = 99
        assertEquals(9L, jornada.inventario.consultar()["Masa"])
    }
    @Test fun procesamientoRechazaParametrosInvalidos() {
        for (cantidad in listOf(0, -1)) assertFailsWith<IllegalArgumentException> { LineaPedido(margarita, cantidad) }
        assertFailsWith<IllegalArgumentException> { Producto("P", 0, mapOf("M" to 1L)) }
        assertFailsWith<IllegalArgumentException> { Producto("P", 1, mapOf("M" to -1L)) }
        assertFailsWith<IllegalArgumentException> { calcularTotal(emptyList(), BigDecimal.ZERO) }
        assertFailsWith<IllegalArgumentException> { Repartidor("R", null, null, -1) }
    }
    @Test fun telefonoConservaPrefijoYCeros() {
        assertEquals("+56 9 0123 4567", telefonoValido(" +56 9 0123 4567 "))
        assertEquals("0012345678", telefonoValido("0012345678"))
    }
    @Test fun telefonosInvalidosSeDetectan() {
        for (valor in listOf(null, "", "abc12345678", "123", "1234567890123456", "++56912345678")) assertNull(telefonoValido(valor))
    }
    @Test fun correoValidaFormatoBasico() {
        assertEquals("ana+pedido@example.cl", correoValido(" ana+pedido@example.cl "))
        for (valor in listOf(null, "", "ana", "a@@b.cl", "a b@c.cl", "a@b", ".a@b.cl", "a@-b.cl", "a..b@c.cl")) assertNull(correoValido(valor))
    }
    @Test fun textosVaciosYNulosUsanPredeterminado() {
        val sinDatos = Cliente(null, "incorrecto", null, "  ")
        assertEquals("Cliente sin nombre", sinDatos.nombre)
        assertEquals("Retiro en local", sinDatos.direccion)
        assertEquals("No informado", sinDatos.telefono)
        assertEquals("correo@invalido.com", sinDatos.correo)
        assertEquals("José", textoOValorPredeterminado(" José ", "Cliente sin nombre"))
    }
    @Test fun cantidadesInvalidasSeRechazanConTryCatch() {
        for (valor in listOf("0", "-1", "pizza", "", "1.5", "2147483648", null)) assertNull(convertirCantidad(valor))
        assertEquals(4, convertirCantidad(" 4 "))
    }
    @Test fun leerCantidadRepreguntaYFinDeEntradaCancela() {
        val entradas = listOf("0", "-2", "texto", "4").iterator()
        val salida = mutableListOf<String>()
        assertEquals(4, leerCantidad({ entradas.next() }, salida::add))
        assertEquals(3, salida.count { it.contains("Cantidad inválida") })
        assertNull(leerCantidad({ null }, salida::add))
    }
    @Test fun notasNulasVaciasYConContenido() {
        assertTrue(registrar().resumen().contains("Notas: Sin notas"))
        assertTrue(registrar(notas = "  ").resumen().contains("Notas: Sin notas"))
        assertTrue(registrar(notas = " sin sal ").resumen().contains("Notas: sin sal"))
    }
    @Test fun letNotificaSoloConCorreoValido() {
        assertTrue(registrar().notificarCliente()!!.contains("Notificación local simulada a jose@example.com"))
        val sinCorreo = Cliente("Ana", null, null, null)
        assertNull(Jornada().registrar(sinCorreo, lineas(1), null).first.notificarCliente())
    }
    @Test fun resumenCompletoIncluyeDatosImportesYEstado() {
        val resumen = registrar(cantidad = 4).resumen()
        for (texto in listOf("PED-001", "Cliente: José", "Dirección: Ñuñoa 123", "Teléfono: +56901234567", "Correo: jose@example.com",
            "Pizza: Margarita", "Precio unitario: $8.000 CLP", "Cantidad: 4", "Subtotal solicitado: $32.000 CLP",
            "Descuento aplicado: $3.200 CLP", "Impuestos (19 %): $5.472 CLP", "Total a pagar: $34.272 CLP",
            "Estado: En preparación", "Repartidor asignado: Sin asignar", "Notas: Sin notas")) assertTrue(resumen.contains(texto), texto)
    }
    @Test fun consolaVariosPedidosYEntregaEnUnaSesion() {
        val jornada = Jornada()
        val salida = conversar(listOf("1", "José", "Ñuñoa 123", "+56901234567", "jose@example.com", "1", "4", "n", "",
            "3", "PED-001", "4", "PED-001", "5", "PED-001",
            "1", "Ana", "Centro", "12345678", "ana@example.cl", "2", "2", "n", "sin sal", "2", "0"), jornada)
        assertEquals(2, jornada.pedidos().size)
        assertEquals(1, jornada.completados().size)
        assertEquals(1, jornada.pendientes().size)
        assertEquals(4L, jornada.inventario.consultar()["Masa"])
        for (texto in listOf("Estado: Listo", "Estado: En camino", "Estado: Entregado", "PED-002", "Notas: sin sal")) assertTrue(salida.contains(texto), texto)
    }
    @Test fun consolaErroresNoTerminaYPermiteReabastecer() {
        val jornada = Jornada()
        val salida = conversar(listOf("x", "7", "Masa", "-1", "7", "Masa", "texto", "3", "inexistente", "7", "Masa", "2", "6", "0"), jornada)
        assertTrue(salida.contains("Opción inválida"))
        assertEquals(3, Regex("Datos inválidos:").findAll(salida).count())
        assertEquals(12L, jornada.inventario.consultar()["Masa"])
    }
    @Test fun consolaRechazoYEntradasInvalidasConservanInventario() {
        val jornada = Jornada()
        val inicial = jornada.inventario.consultar()
        val salida = conversar(listOf("1", "Ana", "Centro", "abc", "mal", "1", "0", "-2", "texto", "11", "n", "", "0"), jornada)
        assertEquals(3, Regex("Cantidad inválida").findAll(salida).count())
        for (texto in listOf("Teléfono inválido", "Correo inválido", "Reabastecer", "Total a pagar: $0 CLP", "Estado: Rechazado")) assertTrue(salida.contains(texto), texto)
        assertEquals(inicial, jornada.inventario.consultar())
    }
    @Test fun entradaNulaTerminaSinExcepcionYAltaIncompletaSeCancela() {
        assertTrue(conversar(emptyList()).contains("Pedidos del día"))
        val jornada = Jornada()
        val salida = conversar(listOf("1"), jornada)
        assertTrue(jornada.pedidos().isEmpty())
        assertTrue(salida.contains("Registro del pedido cancelado"))
    }
    @Test fun consolaProductoInvalidoNoRegistraPedido() {
        val jornada = Jornada()
        assertTrue(conversar(listOf("1", "Ana", "Centro", "12345678", "a@b.cl", "99", "0"), jornada).contains("Pizza inexistente"))
        assertTrue(jornada.pedidos().isEmpty())
        assertEquals(10L, jornada.inventario.consultar()["Masa"])
    }

    private fun conversar(entradas: List<String>, jornada: Jornada = Jornada()): String {
        val iterador = entradas.iterator()
        val salida = mutableListOf<String>()
        ejecutarJornada({ if (iterador.hasNext()) iterador.next() else null }, salida::add, jornada)
        return salida.joinToString("\n")
    }
}
