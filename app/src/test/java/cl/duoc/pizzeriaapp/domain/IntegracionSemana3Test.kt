package cl.duoc.pizzeriaapp.domain

import cl.duoc.pizzeriaapp.domain.model.Margarita
import cl.duoc.pizzeriaapp.domain.model.Pepperoni
import cl.duoc.pizzeriaapp.domain.model.Producto
import cl.duoc.pizzeriaapp.domain.model.Promocionable
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test

class IntegracionSemana3Test {
    @Test fun anotacionTieneMetadatosRuntimeEnLasDosPizzasDelCatalogo() {
        assertEquals(AnnotationRetention.RUNTIME, Promocionable::class.java.declaredAnnotations.filterIsInstance<Retention>().singleOrNull()!!.value)
        assertEquals(listOf(AnnotationTarget.CLASS), Promocionable::class.java.declaredAnnotations.filterIsInstance<Target>().singleOrNull()!!.allowedTargets.toList())
        assertEquals("Pizza clásica", Margarita::class.java.getAnnotation(Promocionable::class.java)!!.descripcion)
        assertEquals("Pizza con pepperoni", Pepperoni::class.java.getAnnotation(Promocionable::class.java)!!.descripcion)
        assertNull(Producto::class.java.getAnnotation(Promocionable::class.java))
    }
}
