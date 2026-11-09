package org.example

import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

data class Configuracion(val tasa: BigDecimal, val fecha: LocalDate?, val promocion: Promocion?, val demostracion: Boolean) {
    fun jornada(): Jornada {
        val zona = ZoneId.of("America/Santiago")
        val reloj = fecha?.let { Clock.fixed(it.atStartOfDay(zona).toInstant(), zona) } ?: Clock.system(zona)
        return Jornada(tasaImpuesto = tasa, promocion = promocion, reloj = reloj)
    }
}

fun leerConfiguracion(args: Array<String>): Configuracion {
    val impuesto = args.filter { !it.startsWith("--") }
    require(impuesto.size <= 1) { "Se admite una tasa de impuesto." }
    require(args.filter { it.startsWith("--") }.all {
        it == "--demo" || it.startsWith("--fecha=") || it.startsWith("--promocion=")
    }) { "Opción desconocida." }
    fun opcion(prefijo: String): String? {
        val valores = args.filter { it.startsWith(prefijo) }
        require(valores.size <= 1) { "Opción repetida: $prefijo" }
        return valores.singleOrNull()?.removePrefix(prefijo)
    }
    val tasa = impuesto.singleOrNull()?.toBigDecimal() ?: BigDecimal("0.19")
    require(tasa >= BigDecimal.ZERO && tasa <= BigDecimal.ONE) { "Tasa inválida." }
    val fecha = opcion("--fecha=")?.let { LocalDate.parse(it) }
    val promocion = opcion("--promocion=")?.let {
        val partes = it.split(',')
        require(partes.size == 3)
        Promocion(LocalDate.parse(partes[0]), LocalDate.parse(partes[1]), partes[2].toBigDecimal())
    }
    return Configuracion(tasa, fecha, promocion, args.contains("--demo"))
}
