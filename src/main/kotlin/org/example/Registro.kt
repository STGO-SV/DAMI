package org.example

import java.util.Locale

fun claveIdentidad(texto: String): String = texto.trim().replace(Regex("\\s+"), " ").lowercase(Locale.ROOT)

/** Conserva la primera ficha de una identidad; nunca reemplaza sus datos. */
class Registro<T : Any> {
    private val entidades = linkedSetOf<T>()
    fun registrar(entidad: T): Boolean = entidades.add(entidad)
    fun buscar(entidad: T): T? = entidades.find { it == entidad }
    fun listar(): List<T> = entidades.toList()
}

fun Producto.mismaFicha(otro: Producto): Boolean = this == otro && precio == otro.precio &&
    ingredientes == otro.ingredientes && esPromocionable(this) == esPromocionable(otro)
