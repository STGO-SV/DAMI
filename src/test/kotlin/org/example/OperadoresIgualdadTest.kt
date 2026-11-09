package org.example

import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.*

class OperadoresIgualdadTest {
    private val cliente = Cliente("José Pérez", "+56912345678", "jose@example.com", "Centro")
    private val fecha = LocalDate.of(2026, 11, 3)
    private fun borrador(id: String, cantidad: Int = 2, cliente: Cliente = this.cliente,
        producto: Producto = Margarita(), notas: String? = null, promocion: Promocion? = null,
        tasa: BigDecimal = BigDecimal("0.19"), fecha: LocalDate = this.fecha) =
        Pedido(id, cliente, listOf(LineaPedido(producto, cantidad)), notas, tasa, fecha, promocion)

    @Test fun igualdadClienteCumpleContratoConNombreYCorreoNormalizados() {
        val segundo = Cliente(" JOSÉ   PÉREZ ", "12345678", "JOSE@example.com", "Otra dirección")
        val tercero = Cliente("josé pérez", null, "jose@EXAMPLE.COM", null)
        assertTrue(cliente == cliente)
        assertTrue(cliente == segundo && segundo == cliente)
        assertTrue(segundo == tercero && cliente == tercero)
        assertEquals(cliente.hashCode(), segundo.hashCode())
        assertEquals(segundo.hashCode(), tercero.hashCode())
        assertFalse(cliente.equals(null))
        assertFalse(cliente.equals("José Pérez"))
        assertNotEquals(cliente, Cliente("Otra persona", null, "jose@example.com", null))
        assertNotEquals(cliente, Cliente("José Pérez", null, "otro@example.com", null))
        assertEquals(1, hashSetOf(cliente, segundo, tercero).size)
        assertTrue(hashSetOf(cliente).contains(segundo))
    }

    @Test fun igualdadProductoCumpleContratoPorNombreYCategoriaSinPrecioNiReceta() {
        val primero: Producto = Margarita()
        val segundo = Producto(" MARGARITA ", 1, mapOf("Masa" to 9L), "PIZZA")
        val tercero = Producto("margarita", 2, mapOf("Queso" to 1L), " pizza ")
        assertTrue(primero == primero)
        assertTrue(primero == segundo && segundo == primero)
        assertTrue(segundo == tercero && primero == tercero)
        assertEquals(primero.hashCode(), segundo.hashCode())
        assertEquals(segundo.hashCode(), tercero.hashCode())
        assertFalse(primero.equals(null))
        assertFalse(primero.equals(cliente))
        assertNotEquals(primero, Pepperoni())
        assertNotEquals(primero, Producto("Margarita", 8000, mapOf("Masa" to 1L), "ingrediente"))
        assertEquals(1, hashSetOf(primero, segundo, tercero).size)
        assertTrue(hashSetOf(primero).contains(segundo))
    }

    @Test fun registrosNoReemplazanLaPrimeraFichaYBuscanPorIgualdad() {
        val registro = Registro<Cliente>()
        val repetido = Cliente(" JOSÉ PÉREZ ", "12345678", "JOSE@example.com", "Otra dirección")
        assertTrue(registro.registrar(cliente))
        assertFalse(registro.registrar(repetido))
        assertSame(cliente, registro.buscar(repetido))
        assertEquals("Centro", registro.listar().single().direccion)
        assertTrue(registro.registrar(Cliente("Ana", null, "ana@example.com", null)))
        assertEquals(2, registro.listar().size)
        val productos = Registro<Producto>()
        val margarita = Margarita()
        assertTrue(productos.registrar(margarita))
        assertFalse(productos.registrar(Producto("margarita", 1, mapOf("Masa" to 1L))))
        assertSame(margarita, productos.buscar(Margarita()))
        assertTrue(productos.registrar(Pepperoni()))
    }

    @Test fun jornadaDeduplicaSinSobrescribirContactoDelPedidoNiFichaInicial() {
        val jornada = Jornada()
        jornada.registrar(cliente, listOf(LineaPedido(Margarita(), 1)), null)
        val repetido = Cliente("JOSÉ PÉREZ", "12345678", "JOSE@example.com", "Otra dirección")
        val pedido = jornada.registrar(repetido, listOf(LineaPedido(Margarita(), 1)), null).first
        assertEquals(1, jornada.clientesRegistrados().size)
        assertSame(cliente, jornada.clientesRegistrados().single())
        assertSame(repetido, pedido.cliente)
        assertEquals(2, jornada.productosRegistrados().size)
        assertEquals(8L, jornada.inventario.consultar()["Masa"])
    }

    @Test fun fichaDeProductoIncompatibleSeRechazaAntesDeConsumirORegistrar() {
        val jornada = Jornada()
        val inicial = jornada.inventario.consultar()
        for (producto in listOf(Producto("Margarita", 1, mapOf("Masa" to 1L)),
            Producto("Margarita", 8000, Margarita().ingredientes))) {
            assertFailsWith<IllegalArgumentException> { jornada.registrar(cliente, listOf(LineaPedido(producto, 1)), null) }
        }
        assertEquals(inicial, jornada.inventario.consultar())
        assertTrue(jornada.pedidos().isEmpty())
        assertTrue(jornada.clientesRegistrados().isEmpty())
    }

    @Test fun plusAgrupaRepetidosYRecalculaDescuentoEnVezDeSumarTotales() {
        val a = borrador("A", notas = "sin sal")
        val b = borrador("B", notas = "tocar timbre")
        val resultado = a + b
        assertEquals("SIM(A+B)", resultado.idPedido)
        assertTrue(resultado.esSimulacion)
        assertEquals(1, resultado.productos.size)
        assertEquals(4, resultado.productos.single().cantidad)
        assertEquals(Importes(32000, 3200, 5472, 34272), resultado.calcularImportes())
        assertNotEquals(a.calcularImportes().total + b.calcularImportes().total, resultado.calcularImportes().total)
        assertEquals("sin sal | tocar timbre", resultado.notas)
        assertEquals(fecha, resultado.fecha)
        assertEquals(EstadoPedido.REGISTRADO, resultado.estado)
        assertNull(resultado.repartidor)
        assertEquals(2, a.cantidadPizzas)
        assertEquals(2, b.cantidadPizzas)
        assertEquals("sin sal", a.notas)
        assertFailsWith<IllegalStateException> { resultado.resolverInventario(true) }
    }

    @Test fun plusConservaDistintosYRecalculaLaPromocionYElImpuesto() {
        val promocion = Promocion(fecha, fecha, BigDecimal("0.15"))
        val a = borrador("A", promocion = promocion, notas = "sin sal")
        val b = borrador("B", producto = Pepperoni(), promocion = promocion, notas = "sin sal")
        val resultado = a + b
        assertEquals(listOf("Margarita", "Pepperoni"), resultado.productos.map { it.producto.nombre })
        assertEquals(Importes(34000, 5100, 5491, 34391, 5100), resultado.calcularImportes())
        assertEquals("sin sal", resultado.notas)
        assertEquals(promocion, resultado.promocion)
    }

    @Test fun plusRechazaClientesContactosFechasImpuestosYPromocionesIncompatibles() {
        val a = borrador("A")
        for (b in listOf(
            borrador("B", cliente = Cliente("Ana", null, "ana@example.com", null)),
            borrador("B", cliente = Cliente(cliente.nombre, cliente.telefono, cliente.correo, "Otra dirección")),
            borrador("B", cliente = Cliente(cliente.nombre, "12345678", cliente.correo, cliente.direccion)),
            borrador("B", fecha = fecha.plusDays(1)), borrador("B", tasa = BigDecimal.ZERO),
            borrador("B", promocion = Promocion(fecha, fecha, BigDecimal.ONE)))) {
            assertFailsWith<IllegalArgumentException> { a + b }
        }
        assertFailsWith<IllegalArgumentException> { a + a }
        assertEquals(EstadoPedido.REGISTRADO, a.estado)
    }

    @Test fun plusImpideMezclarPreciosRecetasOElegibilidadDeUnaIdentidad() {
        val a = borrador("A")
        val b = borrador("B", producto = Producto("margarita", 8000, Margarita().ingredientes))
        assertFailsWith<IllegalArgumentException> { a + b }
        assertFailsWith<IllegalArgumentException> { a + borrador("C", producto = Producto("margarita", 1, mapOf("Masa" to 1L))) }
    }

    @Test fun plusNoAdmitePedidosOperativosNiRechazadosYNoDuplicaConsumo() {
        val jornada = Jornada()
        val operativo = jornada.registrar(cliente, listOf(LineaPedido(Margarita(), 1)), null).first
        val borrador = borrador("B")
        val stock = jornada.inventario.consultar()
        for (paso in 0..3) {
            assertFailsWith<IllegalArgumentException> { operativo + borrador }
            assertFailsWith<IllegalArgumentException> { borrador + operativo }
            when (paso) {
                0 -> operativo.cambiarEstado(EstadoPedido.LISTO)
                1 -> jornada.enviar(operativo)
                2 -> operativo.repartidor!!.entregarPedido(operativo)
            }
        }
        assertEquals(stock, jornada.inventario.consultar())
        assertEquals(1, jornada.pedidos().size)
        val rechazado = Jornada(Inventario(emptyMap())).registrar(cliente, listOf(LineaPedido(Margarita(), 1)), null).first
        assertFailsWith<IllegalArgumentException> { rechazado + borrador }
        assertTrue(jornada.repartidores.all { it.disponible })
    }

    @Test fun plusDetectaDesbordamientosDeCantidadSinMutarBorradores() {
        val a = borrador("A", Int.MAX_VALUE, tasa = BigDecimal.ZERO)
        val b = borrador("B", 1, tasa = BigDecimal.ZERO)
        assertFailsWith<ArithmeticException> { a + b }
        assertEquals(Int.MAX_VALUE, a.cantidadPizzas)
        assertEquals(1, b.cantidadPizzas)
    }

    @Test fun desestructuracionClienteYPedidoConservaOrdenYProtegeColecciones() {
        val (nombre, correo, telefono) = cliente
        assertEquals("José Pérez", nombre)
        assertEquals("jose@example.com", correo)
        assertEquals("(+56) 9 1234 5678", telefono)
        val pedido = borrador("A") + borrador("B", producto = Pepperoni())
        val (clientePedido, productos, total) = pedido
        assertSame(cliente, clientePedido)
        assertEquals(listOf("Margarita", "Pepperoni"), productos.map { it.producto.nombre })
        assertEquals(36414L, total)
        (productos as MutableList<LineaPedido>).clear()
        assertEquals(2, pedido.productos.size)
        assertEquals(36414L, pedido.component3())
        val rechazado = Jornada(Inventario(emptyMap())).registrar(cliente, listOf(LineaPedido(Margarita(), 1)), null).first
        assertEquals(0L, rechazado.component3())
    }
}
