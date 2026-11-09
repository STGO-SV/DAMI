package org.example

import kotlin.test.*

class ValidacionSemana3Test {
    private val cabecera = listOf("1", "José", "Ñuñoa 123", "+12 345 678 9012")

    @Test fun regexAdmiteFormatoAcademicoYRechazaCadenasMalFormadas() {
        for (correo in listOf("cliente@example.com", "cliente@example.cl", "ana+pedido@example.cl", "a.b@sub.example.com")) {
            assertEquals(correo, correoValido(correo))
        }
        for (correo in listOf("sin-arroba", "a@", "@example.com", "a b@example.com", "a!b@example.com", "a..b@example.cl",
            "a@-dominio.com", "a@dominio", "", "á@example.com", "a@dominio_.com", "a".repeat(255) + "@b.cl")) {
            assertNull(correoValido(correo), correo)
        }
    }

    @Test fun correoInvalidoYVacioRepitenSoloElCampoYConservanCliente() {
        val jornada = Jornada()
        val salida = conversar(cabecera + listOf("sin-arroba", "", "cliente@example.cl", "1", "1", "n", "", "0"), jornada)
        val cliente = jornada.pedidos().single().cliente
        assertEquals("José", cliente.nombre)
        assertEquals("Ñuñoa 123", cliente.direccion)
        assertEquals("+12 (345) 678-9012", cliente.telefono)
        assertEquals("cliente@example.cl", cliente.correo)
        assertEquals(3, salida.count { it == "Correo electrónico:" })
        assertEquals(1, salida.count { it == "Nombre del cliente:" })
        assertEquals(2, salida.count { it.startsWith("Correo inválido.") })
    }

    @Test fun eofDuranteReintentoCorreoNoRegistraClienteNiConsumeInventario() {
        val jornada = Jornada()
        val inicial = jornada.inventario.consultar()
        val salida = conversar(cabecera + listOf("mal"), jornada)
        assertTrue(salida.any { it.contains("Registro del pedido cancelado") })
        assertTrue(jornada.pedidos().isEmpty())
        assertTrue(jornada.clientesRegistrados().isEmpty())
        assertEquals(inicial, jornada.inventario.consultar())
        assertTrue(jornada.repartidores.all { it.disponible })
    }

    @Test fun telefonoCompatibleSeNormalizaSinCambiarDigitosYEsIdempotente() {
        for (entrada in listOf("+12 345 678 9012", "123456789012", "+12 (345) 678-9012")) {
            assertEquals("+12 (345) 678-9012", normalizarTelefono(entrada))
            assertEquals(entrada.filter(Char::isDigit), normalizarTelefono(entrada)!!.filter(Char::isDigit))
        }
        assertEquals("000123456789", normalizarTelefono("000123456789"))
    }

    @Test fun otrasLongitudesValidasConservanPrefijosYCeros() {
        for (entrada in listOf("0012345678", "12345678", "123456789012345", "561234567890")) {
            assertEquals(entrada, normalizarTelefono(entrada))
        }
        for (entrada in listOf(null, "", "++123456789012", "123456789012 ext 4", "1234567", "1234567890123456", "１２３４５６７８")) {
            assertNull(normalizarTelefono(entrada))
        }
    }

    @Test fun rangoIncluyeUnoYCienYRechazaValoresFueraDelIntervalo() {
        for (valor in listOf(1, 3, 4, 100)) assertEquals(valor, convertirCantidad(valor.toString()))
        for (valor in listOf("0", "101", "-1", "texto", "1.5", "2147483648", "2147483647", "", null)) {
            assertNull(convertirCantidad(valor), valor)
        }
    }

    @Test fun cantidadCientoUnoFallaPorRangoYCienPorDisponibilidad() {
        val jornada = Jornada()
        val inicial = jornada.inventario.consultar()
        val salida = conversar(cabecera + listOf("cliente@example.com", "1", "101", "100"), jornada)
        assertEquals(1, salida.count { it.startsWith("Cantidad inválida") })
        assertTrue(salida.contains("Máximo disponible: 10."))
        assertTrue(jornada.pedidos().isEmpty())
        assertEquals(inicial, jornada.inventario.consultar())
    }

    private fun conversar(entradas: List<String>, jornada: Jornada): List<String> {
        val iterador = entradas.iterator()
        val salida = mutableListOf<String>()
        var lecturas = 0
        ejecutarJornada({
            assertTrue(++lecturas <= entradas.size + 2)
            if (iterador.hasNext()) iterador.next() else null
        }, salida::add, jornada)
        return salida
    }
}
