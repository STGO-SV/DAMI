package org.example

import java.math.BigDecimal
import java.time.*
import kotlin.reflect.full.findAnnotation
import kotlin.test.*

class IntegracionSemana3Test {
    @Test fun anotacionTieneMetadatosRuntimeEnLasDosPizzasDelCatalogo() {
        assertEquals(AnnotationRetention.RUNTIME, Promocionable::class.findAnnotation<Retention>()!!.value)
        assertEquals(listOf(AnnotationTarget.CLASS), Promocionable::class.findAnnotation<Target>()!!.allowedTargets.toList())
        assertEquals("Pizza clásica", Margarita::class.findAnnotation<Promocionable>()!!.descripcion)
        assertEquals("Pizza con pepperoni", Pepperoni::class.findAnnotation<Promocionable>()!!.descripcion)
        assertNull(Producto::class.findAnnotation<Promocionable>())
    }

    @Test fun reflectionListaMiembrosRealesOrdenadosSinAccesoAEstadoPrivado() {
        for (tipo in listOf(Producto::class, Pedido::class, Margarita::class, Pepperoni::class)) {
            val salida = inspeccionarClase(tipo)
            assertEquals(salida, inspeccionarClase(tipo))
            assertTrue(salida.contains("Clase: ${tipo.simpleName}"))
            val propiedades = salida.lineSequence().single { it.startsWith("Propiedades:") }.removePrefix("Propiedades: ").split(", ")
            assertEquals(propiedades.sorted(), propiedades)
        }
        val producto = inspeccionarClase(Producto::class)
        assertTrue(producto.contains("categoria: kotlin.String"))
        assertTrue(producto.contains("ingredientes: kotlin.collections.Map"))
        assertTrue(producto.contains("detallarProducto(): kotlin.String"))
        assertFalse(producto.contains("receta:"))
        val pedido = inspeccionarClase(Pedido::class)
        assertTrue(pedido.contains("calcularImportes()"))
        assertTrue(pedido.contains("plus(org.example.Pedido)"))
        assertFalse(pedido.contains("cotizacion:"))
        assertTrue(inspeccionarClase(Margarita::class).contains("@Promocionable(Pizza clásica)"))
        assertTrue(inspeccionarClase(Pepperoni::class).contains("@Promocionable(Pizza con pepperoni)"))
        assertTrue(inspeccionarClase(Margarita::class).contains("detallarProducto(): kotlin.String"))
        assertTrue(inspeccionarClase(Pepperoni::class).contains("detallarProducto(): kotlin.String"))
    }

    @Test fun configuracionPermiteDemostrarFechasYTasasSinEditarFuentes() {
        val configuracion = leerConfiguracion(arrayOf("0.10", "--fecha=2026-11-03", "--promocion=2026-11-01,2026-11-07,0.15"))
        assertEquals(BigDecimal("0.10"), configuracion.tasa)
        assertEquals(LocalDate.of(2026, 11, 3), LocalDate.now(configuracion.jornada().reloj))
        assertTrue(configuracion.promocion!!.activa(configuracion.fecha!!))
        assertFalse(configuracion.demostracion)
        assertTrue(leerConfiguracion(arrayOf("--demo")).demostracion)
        assertNull(leerConfiguracion(emptyArray()).promocion)
    }

    @Test fun configuracionInvalidaNoIniciaUnaJornada() {
        for (args in listOf(arrayOf("-1"), arrayOf("1.01"), arrayOf("abc"), arrayOf("0.19", "0.10"),
            arrayOf("--desconocida"), arrayOf("--fecha=2026-01-01", "--fecha=2026-01-02"),
            arrayOf("--promocion=2026-11-07,2026-11-01,0.15"), arrayOf("--promocion=2026-11-01,2026-11-07,2"),
            arrayOf("--promocion=2026-11-01,2026-11-07"))) {
            assertFailsWith<IllegalArgumentException> { leerConfiguracion(args) }
        }
        assertFailsWith<DateTimeException> { leerConfiguracion(arrayOf("--fecha=2026-02-30")) }
    }

    @Test fun demostracionIntegraUtilidadesYNoModificaJornadaPrevia() {
        val jornada = Jornada()
        val cliente = Cliente("Ana", null, "ana@example.com", null)
        val pedido = jornada.registrar(cliente, listOf(LineaPedido(Margarita(), 1)), null).first
        pedido.cambiarEstado(EstadoPedido.LISTO)
        jornada.enviar(pedido)
        val stock = jornada.inventario.consultar()
        val reporte = jornada.reporte()
        val entradas = listOf("8", "0").iterator()
        val salida = mutableListOf<String>()
        ejecutarJornada({ entradas.next() }, salida::add, jornada)
        val texto = salida.joinToString("\n")
        for (esperado in listOf("Correo 'sin-arroba': inválido", "+12 (345) 678-9012", "Cantidad 101: inválida",
            "Promoción activa", "Promoción inactiva", "promocionable=true", "promocionable=false", "Clase: Producto",
            "Clase: Pedido", "Pedidos combinados mediante +", "Cliente desestructurado", "Pedido desestructurado",
            "Clientes ==: true", "Productos ==: true", "duplicado agregado=false", "$36.414 CLP", "$34.391 CLP")) {
            assertTrue(texto.contains(esperado), esperado)
        }
        assertEquals(stock, jornada.inventario.consultar())
        assertEquals(reporte, jornada.reporte())
        assertEquals(EstadoPedido.EN_CAMINO, pedido.estado)
        assertFalse(pedido.repartidor!!.disponible)
        assertEquals(1, jornada.clientesRegistrados().size)
    }
}
