package org.example

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import kotlin.reflect.full.findAnnotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Promocionable(val descripcion: String)

fun esPromocionable(elemento: Any): Boolean = elemento::class.findAnnotation<Promocionable>() != null

data class Promocion(val inicio: LocalDate, val fin: LocalDate, val porcentaje: BigDecimal) {
    init {
        require(inicio <= fin) { "El inicio de la promoción debe ser anterior o igual al fin." }
        require(porcentaje >= BigDecimal.ZERO && porcentaje <= BigDecimal.ONE) { "El porcentaje debe estar entre 0 y 1." }
    }

    fun activa(fecha: LocalDate): Boolean = fecha in inicio..fin

    fun descuento(productos: List<LineaPedido>, fecha: LocalDate): Long {
        if (!activa(fecha)) return 0L
        val elegible = calcularSubtotal(productos.filter { esPromocionable(it.producto) })
        return elegible.toBigDecimal().multiply(porcentaje).setScale(0, RoundingMode.DOWN).longValueExact()
    }
}
