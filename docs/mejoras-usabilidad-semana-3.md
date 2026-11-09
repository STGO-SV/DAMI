# Usabilidad y auditoría de Semana 3

Las mejoras responden a oportunidades de interacción observadas durante el pedido mixto y la cancelación por EOF, los dos primeros grupos de la auditoría manual independiente. Las siete aprobaciones por etapas se reúnen en el estado final. [Registro de la auditoría manual](estado-final-semana-3.md#auditoría-manual-independiente).

Antes de las mejoras, el teléfono chileno conservaba su presentación original y podía mostrar un aviso sobre el formato académico. La cantidad se solicitaba con el rango `1..100` y la disponibilidad se comprobaba al registrar el pedido. La ejecución previa a las mejoras aprobó las 112 pruebas de referencia. [Línea base](evidencias/semana-3/usabilidad/linea-base.txt).

## Teléfono y disponibilidad

El formato de presentación chileno es `(+56) X XXXX XXXX`. Se interpreta como nacional una entrada de nueve dígitos sin `+` o de nueve dígitos tras el código `+56`. El propio formato de salida también es una entrada válida. Se admiten espacios, paréntesis y guiones como separadores. Los nueve dígitos nacionales se conservan, incluidos los ceros. El resumen muestra el teléfono normalizado sin aviso de formato para un número chileno válido.

| Entrada | Presentación |
| --- | --- |
| `956781234`, `+56956781234`, `+56 9 5678 1234` | `(+56) 9 5678 1234` |
| `223456789`, `+56223456789` | `(+56) 2 2345 6789` |
| `+12 345 678 9012` | `+12 (345) 678-9012` |

El formato académico se aplica a 12 dígitos compatibles. Se conservan las demás entradas genéricas válidas de 8 a 15 dígitos, incluidos los ceros iniciales sin `+`. Una cadena de 12 dígitos sin `+` que comienza por `56` conserva su presentación porque anteponer `+` produciría un prefijo chileno con longitud incorrecta. Se rechazan `+0`, prefijos mal formados y números con `+56` que no tienen nueve dígitos nacionales. La validación es sintáctica y no comprueba asignación de códigos ni existencia de líneas. Los datos inválidos se presentan como `No informado`.

Por cada ingrediente de la receta se restan las unidades comprometidas en las líneas del borrador del stock real. El resultado se acota a cero y se divide de forma entera por el consumo de una pizza. Se toma el mínimo de esos cocientes y 100. Las cantidades comprometidas se calculan con suma y multiplicación exactas. El límite se aplica antes de convertir a `Int`.

El inventario inicial permite diez Margarita o cinco Pepperoni. Tras seleccionar ocho Margarita, la capacidad de cualquiera de las dos variedades es dos. El exceso repite solo la pregunta de cantidad. Un máximo cero devuelve a la selección de variedad. En ese selector, `C` cancela el borrador. EOF cancela en cualquier campo del alta. Las líneas elegidas son una reserva lógica de la sesión. `Jornada.registrar` vuelve a comprobar todos los ingredientes y `Inventario.consumir` realiza el consumo de forma atómica al registrar. Una regresión de integración comprueba un cambio de stock entre la selección y la confirmación.

## Auditoría automatizada de promociones

La auditoría automatizada del 7 de noviembre ejecutó `MainKt` desde la distribución local. Las fechas de la tabla son argumentos de programa para simular la fecha de pedido. Cada caso registró dos Margarita y dos Pepperoni, con subtotal $34.000 y stock final 6/12/12/3. [Transcripciones de consola](evidencias/semana-3/usabilidad/).

| Fecha | Promoción configurada | Vigencia | Descuento por fecha | Descuento por cantidad | Base imponible | Impuesto | Total |
| --- | ---: | --- | ---: | ---: | ---: | ---: | ---: |
| 2026-10-31 | 15 % | Inactiva | $0 | $3.400 | $30.600 | $5.814 | $36.414 |
| 2026-11-01 | 15 % | Activa | $5.100 | $0 | $28.900 | $5.491 | $34.391 |
| 2026-11-03 | 15 % | Activa | $5.100 | $0 | $28.900 | $5.491 | $34.391 |
| 2026-11-07 | 15 % | Activa | $5.100 | $0 | $28.900 | $5.491 | $34.391 |
| 2026-11-08 | 15 % | Inactiva | $0 | $3.400 | $30.600 | $5.814 | $36.414 |
| 2026-11-03 | 5 % | Activa | $0 | $3.400 | $30.600 | $5.814 | $36.414 |

Se observaron mensajes de error sin apertura del menú para fecha imposible, intervalo invertido, porcentaje negativo, porcentaje superior a uno y promoción incompleta. La demostración combinó una Margarita anotada con un producto de referencia sin `@Promocionable`. El subtotal de $16.000 recibió $1.200 de descuento, equivalente al 15 % de la Margarita. La base de $14.800 produjo $2.812 de impuesto y un total de $17.612. La demostración trabaja con cotizaciones y conserva la jornada operativa.

## Pruebas, archivos y resultados

- `Entrada.kt`, `Inventario.kt` y `Main.kt` implementan el formato telefónico, la capacidad y los reintentos de consola.
- `Pedido.kt` muestra la base imponible de pedidos con promoción. `Demostracion.kt` muestra el cálculo con un producto elegible y otro sin anotación.
- `UsabilidadSemana3Test.kt` aporta 12 pruebas. Cubre teléfonos móviles y fijos, prefijos inválidos, conservación de dígitos, salida del resumen, ingredientes limitantes, stock cero, reserva temporal, líneas repetidas, máximo exacto, reabastecimiento, desbordamiento, cancelación y rechazo atómico tras un cambio de stock.
- `ConfirmacionSNTest.kt`, `OperadoresIgualdadTest.kt`, `PedidoTest.kt`, `RecuperacionEntradaTest.kt`, `RetroalimentacionSemana1Test.kt` y `ValidacionSemana3Test.kt` ajustan las expectativas de formato o del nuevo reintento por stock. Se mantienen las comprobaciones de rango y rechazo atómico en el dominio.
- `Verificar-Consola-Semana3.ps1` cubre vigencia, extremos, configuración inválida, prioridad de descuentos y disponibilidad. El README, el informe, el estado final y el protocolo documentan los cambios de uso.

Los resultados agregados de la ejecución automatizada se reúnen en el estado final. [Verificación automatizada](estado-final-semana-3.md#verificación-automatizada).

## Comprobación manual posterior

El protocolo contiene los pasos para repetir la normalización telefónica, la capacidad, la cancelación y las promociones en IntelliJ. [Teléfono y capacidad](pruebas-manuales-semana-3.md#teléfono-chileno-y-capacidad-dinámica) · [Agotamiento](pruebas-manuales-semana-3.md#agotamiento-y-cancelación-con-c) · [Promociones](pruebas-manuales-semana-3.md#3-promoción-activa-e-inactiva).
