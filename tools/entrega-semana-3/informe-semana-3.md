# Informe técnico — Semana 3, Forma B

PMY2201 — Desarrollo de Aplicaciones Móviles I, Experiencia 1. Actividad «Creando soluciones con Kotlin». La aplicación continúa el caso de una pizzería e integra validación, promociones por fechas, anotaciones, reflexión, operadores, desestructuración y registros sin duplicados.

## Matriz de trazabilidad

Las fuentes están en `src/main/kotlin/org/example/` y las pruebas en `src/test/kotlin/org/example/`. Las pruebas indicadas pueden ejecutarse con el Gradle Wrapper incluido.

| Criterio de la pauta | Implementación | Comprobación reproducible |
| --- | --- | --- |
| Regex y rangos | `Entrada.kt` valida correo, teléfono y cantidad `1..100`. `Promocion.kt` usa un intervalo de fechas inclusivo. | `ValidacionSemana3Test`, `UsabilidadSemana3Test` y `PromocionTest`. |
| Anotaciones y reflexión | `@Promocionable` marca Margarita y Pepperoni. `Inspeccion.kt` muestra miembros públicos de las clases del dominio. | `PromocionTest`, `IntegracionSemana3Test` y `--demo`. |
| Sobrecarga de operadores | `Pedido.plus` combina líneas y recalcula importes. `Producto` define igualdad por nombre y categoría. | `OperadoresIgualdadTest` y `--demo`. |
| Desestructuración | `Cliente` expone nombre, correo y teléfono. `Pedido` expone cliente, copia de líneas y total. | `OperadoresIgualdadTest` y `--demo`. |
| Igualdad y duplicados | Cliente y Producto implementan `equals` y `hashCode`. `Registro` conserva una ficha por identidad. | `OperadoresIgualdadTest` y `--demo`. |
| Demostración integrada | La opción 8 y `--demo` ejecutan las utilidades con datos fijos. | `IntegracionSemana3Test` y ejecución directa de `--demo`. |
| Presentación técnica y entrega | Fuentes, pruebas, documentación, configuración y Gradle Wrapper. | `clean build` e `installDist` desde el proyecto extraído. |

## Decisiones técnicas

El correo es obligatorio en consola y se valida con una expresión regular. Los teléfonos chilenos de nueve dígitos nacionales, con o sin `+56`, se presentan como `(+56) X XXXX XXXX`. Los internacionales compatibles de doce dígitos usan `+XX (XXX) XXX-XXXX`. La validación es sintáctica. La dirección es texto libre y las distancias simuladas del reparto son independientes de ella.

La capacidad de una pizza es el menor cociente entero entre stock libre y consumo por ingrediente, con tope de 100. El stock libre descuenta de forma lógica las líneas del borrador. Al registrar, `Jornada` calcula de nuevo las necesidades e `Inventario` consume todos los ingredientes de forma atómica cuando hay disponibilidad. `S/s` agrega otra línea y `N/n` cierra la selección. Las entradas inválidas repiten la pregunta. `C` cancela desde el selector y EOF cancela el alta.

Los importes se expresan en pesos enteros. Con más de tres pizzas se calcula un descuento del 10 % sobre el subtotal completo. La promoción por fecha se aplica sobre las líneas de productos anotados durante el intervalo inclusivo. Se aplica el mayor descuento y el empate favorece el descuento por cantidad. Ambos descuentos se truncan al peso. El impuesto se calcula sobre la base descontada con `BigDecimal` y redondeo `HALF_UP`.

La identidad del cliente utiliza nombre y correo. La del producto utiliza nombre y categoría. La comparación normaliza espacios y mayúsculas. Cada pedido conserva sus propios datos de entrega. El operador `+` requiere borradores compatibles y genera una cotización. La desestructuración de Pedido entrega una copia de sus líneas. La reflexión usa `kotlin-reflect` y ordena los miembros públicos.

## Casos de evaluación

Desde la carpeta extraída se puede ejecutar la suite, preparar la distribución y observar la demostración.

```powershell
.\gradlew.bat clean build --console=plain
.\gradlew.bat installDist --console=plain
.\gradlew.bat run --console=plain --args=--demo
```

La demostración muestra un pedido combinado de dos Margarita y dos Pepperoni, la inspección de clases, los componentes de Cliente y Pedido y la prevención de duplicados. Con tasa 19 %, el total esperado del pedido combinado es $36.414. Para comprobar la promoción, ejecutar `run` con `--fecha=2026-11-03 --promocion=2026-11-01,2026-11-07,0.15` y registrar dos pizzas de cada variedad. El subtotal esperado es $34.000, el descuento $5.100, el impuesto $5.491 y el total $34.391. Repetir con `--fecha=2026-11-08` y la misma promoción para comprobar el descuento por cantidad de $3.400 y el total de $36.414.

Para comprobar el límite de stock, elegir ocho Margarita y luego Pepperoni. El máximo esperado es dos. Ingresar `3` debe repetir la cantidad. Para comprobar la cancelación, en una sesión nueva elegir cinco Pepperoni y volver a seleccionar esa variedad. Ingresar `C` tras el aviso de agotamiento y consultar la opción 6. Debe verse el stock inicial de 10 masas, 20 tomates, 20 quesos y 5 unidades de Pepperoni. Los estados En preparación, Listo, En camino y Entregado se comprueban al registrar un pedido y usar las opciones 3, 4 y 5.

Los importes y las salidas descritas son resultados esperados. Los resultados efectivos se consultan en los informes de Gradle generados al ejecutar la suite. [Instrucciones de ejecución](../README.md).
