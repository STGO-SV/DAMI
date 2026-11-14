package cl.duoc.pizzeriaapp.domain.promotion

import cl.duoc.pizzeriaapp.domain.model.Producto

fun Producto.mismaFicha(otro: Producto): Boolean = this == otro && precio == otro.precio &&
    ingredientes == otro.ingredientes && esPromocionable(this) == esPromocionable(otro)
