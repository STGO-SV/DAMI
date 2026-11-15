package cl.duoc.pizzeriaapp.domain.model

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Promocionable(val descripcion: String)
