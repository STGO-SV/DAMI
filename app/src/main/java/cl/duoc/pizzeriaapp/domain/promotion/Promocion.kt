package cl.duoc.pizzeriaapp.domain.promotion

import cl.duoc.pizzeriaapp.domain.model.LineaPedido
import cl.duoc.pizzeriaapp.domain.model.Promocionable
import cl.duoc.pizzeriaapp.domain.model.calcularSubtotal
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

fun esPromocionable(elemento: Any): Boolean = elemento.javaClass.declaredAnnotations.any { it is Promocionable }

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
