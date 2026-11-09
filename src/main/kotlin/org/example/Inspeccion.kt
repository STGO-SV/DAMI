package org.example

import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaMethod

fun inspeccionarClase(tipo: KClass<*>): String = buildString {
    appendLine("Clase: ${tipo.simpleName}")
    appendLine("Anotaciones: " + tipo.annotations.map { anotacion ->
        if (anotacion is Promocionable) "@Promocionable(${anotacion.descripcion})" else "@${anotacion.annotationClass.simpleName}"
    }.sorted().joinToString().ifEmpty { "Sin anotaciones" })
    appendLine("Propiedades: " + tipo.memberProperties.filter { it.visibility == KVisibility.PUBLIC }
        .map { "${it.name}: ${it.returnType}" }.sorted().joinToString())
    append("Métodos: " + tipo.memberFunctions.filter {
        it.visibility == KVisibility.PUBLIC && it.javaMethod?.declaringClass != Any::class.java
    }
        .map { funcion -> "${funcion.name}(${funcion.parameters.drop(1).joinToString { it.type.toString() }}): ${funcion.returnType}" }
        .sorted().joinToString())
}
