# Revisión de retroalimentación de Semana 1

Fecha de verificación: 31 de octubre de 2026. Rama de trabajo: `semana-3`.

## Alcance y referencias

Se contrastó la retroalimentación con la entrega original `semana-1` (`2deefbf`) y con la implementación vigente al iniciar la revisión (`fab6e01`, base de Semana 2). La consulta histórica se realizó mediante `git show`, sin cambiar de rama ni restaurar archivos. El estado inicial de Git estaba limpio.

No se identificaron defectos reproducibles de descuento o inventario en los casos examinados. La presentación ya contenía los datos del pedido, pero carecía de secciones diferenciadas. Se conservó la lógica de negocio y se mejoró exclusivamente su presentación, reforzando las pruebas de regresión.

## Matriz de trazabilidad

| Observación recibida | Comportamiento existente | Evidencia técnica | Acción adoptada | Resultado de verificación |
| --- | --- | --- | --- | --- |
| Revisar el descuento cuando se solicitan más de tres pizzas y calcularlo sobre el subtotal. | Semana 1 multiplicaba precio por cantidad antes de aplicar `subtotal / 10` únicamente si la cantidad superaba tres. La versión vigente conserva esa regla, sumando las cantidades de todas las líneas. | Histórico: `2deefbf:src/main/kotlin/org/example/Pedido.kt`, `procesarPedido`. Actual: `Pedido.kt`, `calcularSubtotal`, `calcularDescuento` y `calcularTotal`. Las pruebas históricas cubrían 1, 3, 4 y 5 pizzas. | Comportamiento ya correcto: se conservó la implementación. Se agregó el caso de 8 pizzas y se amplió la regresión con tasa cero. | Pasaron los casos de 1, 3, 4 y 8 pizzas, con y sin impuesto; también el descuento de un pedido mixto sobre el subtotal completo. No se reprodujo el defecto señalado. |
| Mantener un redondeo monetario coherente e incorporar los cálculos de Semana 2. | El descuento trunca al peso por división entera; el impuesto usa `HALF_UP` sobre subtotal menos descuento. El dinero se representa mediante `Long`, con detección de desbordamientos. | README, sección Cálculos y validación; `PedidoTest.redondeoTruncaDescuentoYRedondeaImpuestoAlPeso`, `dineroGrandeNoDesbordaInt`, `desbordamientoSeDetectaAntesDeConsumir`. | Se conservaron cálculos y política documentada. Se comprobó también la representación de una tasa configurable de 10,5 %. | Pruebas aprobadas; el resumen muestra los importes calculados y la tasa sin ceros decimales sobrantes. |
| Actualizar inventario después de confirmar disponibilidad. | Semana 1 comprobaba `stockInicial >= cantidadPizzas` y restaba la cantidad solo al confirmar. Semana 2 acumula por ingrediente y comprueba todos los faltantes antes de consumir. | Histórico: `procesarPedido`. Actual: `Inventario.kt`, `calcularIngredientes` y `Inventario.consumir`; `Jornada.registrar`. Pruebas existentes de stock exacto, superior, insuficiente e ingrediente ausente. | Comportamiento ya correcto: se conservó el inventario individual de Semana 2. | Los pedidos aceptados descuentan las cantidades de receta; los rechazados conservan todas las existencias y no generan cobro ni asignación de reparto. |
| Evitar consumos parciales y mantener el inventario compartido. | `Jornada` conserva una única instancia de inventario y verifica las necesidades agregadas de todas las líneas. Existían pruebas de pedidos consecutivos y de falta de un ingrediente para una sola pizza. | `PedidoTest.variosPedidosCompartenInventarioEIdentificadoresUnicos`, `faltaSoloUnIngredienteEvitaConsumoParcial`; nuevas pruebas `loteMixtoRechazadoNoConsumeNiLaPrimeraPizzaDisponible` y `pedidosMixtosConsecutivosCompartenYDescuentanCadaIngrediente`. | Se agregaron dos regresiones con Margarita y Pepperoni: falta exclusiva de Pepperoni y dos pedidos mixtos aceptados seguidos de un rechazo. | Cada ingrediente se descuenta exactamente en los pedidos aceptados. El rechazo conserva el mapa completo, incluso cuando la primera línea podría prepararse. |
| Mostrar todos los datos del cliente, pizza y pedido de forma clara y ordenada. | `Main.kt` delega en `Pedido.resumen()` al registrar y al cambiar estados. El resumen incluía identificador, contacto, productos, importes, notas, estado y repartidor, sin agrupación por secciones. | Histórico: `2deefbf:src/main/kotlin/org/example/Main.kt`. Actual: `Main.kt`, `registrarDesdeConsola` y operaciones 3–5; `Pedido.resumen`. Pruebas existentes de resumen y consola. | Mejora de presentación: cinco secciones con encabezados y espacios consistentes; productos en líneas separadas, detalle unitario con sangría y cantidad total. Se conservaron todos los campos y el formato monetario CLP. | Comparación completa del resumen mixto aprobada; también rechazo, valores predeterminados, notas, tasa configurable, todos los estados y repartidor asignado. La consola presenta el mismo resumen seccionado. |

## Regresión monetaria

Cada caso utiliza una jornada independiente con stock suficiente y Margarita a $8.000 CLP.

| Cantidad | Subtotal CLP | Descuento CLP | Impuesto 19 % CLP | Total CLP | Total con tasa cero CLP |
| ---: | ---: | ---: | ---: | ---: | ---: |
| 1 | 8.000 | 0 | 1.520 | 9.520 | 8.000 |
| 3 | 24.000 | 0 | 4.560 | 28.560 | 24.000 |
| 4 | 32.000 | 3.200 | 5.472 | 34.272 | 28.800 |
| 8 | 64.000 | 6.400 | 10.944 | 68.544 | 57.600 |

## Pruebas y resultados reales

Después de los cambios se ejecutó `.\gradlew.bat clean build --console=plain`: `BUILD SUCCESSFUL in 24s`, con ocho tareas ejecutadas. La limpieza obligó a compilar nuevamente y ejecutar las pruebas. Los reportes XML resultantes registraron:

| Clase | Pruebas | Fallos | Errores | Omitidas |
| --- | ---: | ---: | ---: | ---: |
| `PedidoTest` | 46 | 0 | 0 | 0 |
| `RecuperacionEntradaTest` | 12 | 0 | 0 | 0 |
| `RetroalimentacionSemana1Test` | 7 | 0 | 0 | 0 |
| Total | 65 | 0 | 0 | 0 |

Se conservaron las 57 pruebas existentes. Se agregó una prueba a `PedidoTest`, se amplió su caso de tasa cero y se incorporaron siete pruebas en `RetroalimentacionSemana1Test`: rechazo mixto atómico, consumo mixto consecutivo, resumen completo por secciones, resumen rechazado, estados y ausencia de efectos al consultar el resumen, tasa configurable e integración desde la consola.

Las pruebas existentes verifican expresamente recuperación ante entradas inválidas, cancelación por EOF en cada campo sin efectos parciales, inventario, redondeo y desbordamientos, transiciones permitidas e inválidas, selección y liberación de repartidores y reportes de jornada. La nueva prueba de estados comprueba además que consultar repetidamente el resumen no modifica importes, inventario, disponibilidad de repartidores ni el reporte.

Los XML de esta ejecución se conservaron en [evidencias/retroalimentacion-semana-1](evidencias/retroalimentacion-semana-1/). El reporte HTML se genera en `build/reports/tests/test/index.html`. Las evidencias históricas no se sobrescribieron. `git diff --check` finalizó sin errores.

## Archivos y comprobación independiente

- `src/main/kotlin/org/example/Pedido.kt`: presentación del resumen.
- `src/test/kotlin/org/example/PedidoTest.kt`: regresión de ocho pizzas y tasa cero.
- `src/test/kotlin/org/example/RetroalimentacionSemana1Test.kt`: siete pruebas adicionales.
- `docs/retroalimentacion-semana-1.md`: trazabilidad y resultados.
- `docs/evidencias/retroalimentacion-semana-1/TEST-*.xml`: resultados de las tres clases.
- `README.md`: enlace a esta revisión.

Para repetir la verificación se ejecuta `.\gradlew.bat clean build`. Para comprobar manualmente la presentación se ejecuta `.\gradlew.bat --console=plain run`, se registra un pedido con dos Margarita y dos Pepperoni y se revisan las cinco secciones, el total de $36.414 CLP y el inventario restante (Masa 6, Tomate 12, Queso 12 y Pepperoni 3). Las opciones 3, 4 y 5 permiten comprobar Listo, En camino y Entregado mediante `PED-001`; la opción 2 muestra el reporte de jornada. En una ejecución nueva, un pedido con una Margarita y seis Pepperoni permite comprobar el rechazo sin consumo parcial.

No se modificaron otras ramas, entregas históricas ni sus ZIP. No se realizaron commits ni pushes. Los ZIP creados dentro de `build/distributions` son productos de la compilación solicitada, no regeneraciones de entregas históricas. El proyecto queda en `semana-3` con cambios disponibles para revisión.
