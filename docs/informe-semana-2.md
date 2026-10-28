# Informe de Semana 2

La aplicación implementa «Aplicando utilidades de Kotlin» de PMY2201 mediante la evolución de la pizzería de Semana 1. Se mantiene la ejecución en consola con Kotlin/JVM, conforme al trabajo en IntelliJ IDEA indicado en las instrucciones específicas. Los documentos de referencia son la guía de aprendizaje, las instrucciones específicas y la pauta de evaluación de Semana 2.

## Matriz de trazabilidad

| Criterio oficial | Implementación | Verificación |
| --- | --- | --- |
| 1. Flujos de decisión, arreglos, validaciones y formateo | `Inventario.consumir` usa `when`; `Jornada` guarda un `Array<Pedido>`, recorre con `for` y separa estados con `filter`; `Entrada.kt` valida cadenas | Pruebas de stock exacto, sobrante, ausente, faltantes, ausencia de consumos parciales, arreglos, filtros, reporte y contactos |
| 2. Funciones reutilizables y colecciones | Funciones de subtotal, descuento, impuesto, total e ingredientes; inventario `MutableMap`; repartidores `listOf`; selección con `sortedBy` y `find`; estados pendientes con `setOf` | Lote mixto, cálculos de 1/3/4/5 pizzas, tasa cero, redondeo, mínimo disponible, empate y ausencia de repartidores |
| 3. Clases, métodos y encapsulación | `Producto.detallarProducto`, métodos de `Pedido`, `Usuario`, `Cliente`, `Repartidor.entregarPedido`; propiedades con escritura privada y copias de colecciones | Herencia, polimorfismo, encapsulación, entrega y transiciones válidas e inválidas |
| 4. Manejo de errores y valores nulos | `try-catch` en conversión y menú; `?.`, `?:` y `let` para notas y notificación local | Texto, negativos, desbordamiento, EOF, notas vacías/nulas, contactos predeterminados y notificación condicional |
| 5. Resúmenes completos con plantillas de cadenas | `Pedido.resumen` informa cliente, dirección, teléfono, correo, productos, precios, cantidades, subtotal, descuento, impuesto, total, estado, repartidor y notas | Resumen completo; rechazo coherente; sesión con preparación, envío y entrega |
| 6. Código limpio, descriptivo y organizado | Siete archivos de producción por responsabilidad; lógica separada de entrada/salida; invariantes y funciones reutilizables | Compilación, pruebas, revisión de cambios y documentación de políticas |
| 7. Entrega comprimida | `tools/Preparar-Entrega-Semana2.ps1` incluye fuentes, pruebas, documentación y Wrapper completo | Validación de entradas, exclusiones y SHA-256 de cada archivo; informe y checksum junto al ZIP |

## Continuidad y decisiones

La línea base consta de 19 pruebas de Semana 1 ejecutadas antes de los cambios, sin fallos ni errores. Se conserva la validación telefónica, los predeterminados, la lectura tolerante de cantidades, Margarita a $8.000, el descuento por más de tres pizzas y la prohibición de cobrar pedidos rechazados. Los casos se adaptan a entidades con comportamiento, inventario individual y estados explícitos.

El impuesto usa una tasa configurable, con `0.19` como supuesto académico. Los precios se interpretan como valores antes de impuestos. Se aplica primero el descuento truncado y después el impuesto redondeado `HALF_UP`; se utilizan `Long` y `BigDecimal`, sin `Float` ni `Double` para dinero. Con tasa cero se recuperan los totales históricos.

Un pedido rechazado conserva el subtotal solicitado, pero no tiene descuento aplicado, impuesto, cobro ni reparto. Solo una aceptación con todos los ingredientes disponibles habilita En preparación. La preparación puede marcarse Listo; únicamente la asignación de reparto habilita En camino y solo el repartidor asignado confirma Entregado. No se permite reactivar rechazados ni repetir entregas.

Las recetas expresan unidades abstractas. Los pedidos mixtos acumulan todos sus ingredientes antes de consumir. El stock se conserva durante toda la sesión, incluso entre pedidos rechazados y reabastecimientos. Los identificadores se generan secuencialmente por jornada. Se devuelve una copia del arreglo para proteger su estructura.

Las distancias de reparto son datos simulados desde el local, explícitos y ordenados para seleccionar realmente el menor disponible. El empate se resuelve por orden original. Si todos están ocupados el pedido sigue Listo. La entrega libera al repartidor.

La comprobación de contactos es sintáctica; las notificaciones consisten únicamente en texto local. `correo@invalido.com` no se utiliza como destinatario de notificaciones simuladas. Las notas usan operadores seguros y predeterminados.

La selección de pizza se solicita mediante un bucle recuperable. Un valor inexistente o fuera de rango muestra un error y repite solamente esa selección, conservando cliente y líneas anteriores. Las cantidades inválidas mantienen el mismo criterio de recuperación local. Una línea vacía conserva los predeterminados definidos; EOF cancela el alta completa antes de invocar `Jornada.registrar`, sin modificar inventario, reparto ni pedidos existentes. La cancelación también se aplica si ya se habían seleccionado productos o si el EOF ocurre en la pregunta de continuación o en las notas.

La verificación de esta corrección aprobó 57 pruebas, sin fallos, errores ni omisiones: 45 casos existentes adaptados al tratamiento de EOF y 12 nuevos casos de recuperación. La compilación limpia fue correcta. Se comprobaron además seis escenarios del programa ejecutable con 28 verificaciones de salida. Los resultados actuales se conservan en `evidencias/semana-2/recuperacion-entradas/`.

## Evidencias y limitaciones

Los resultados reales de ejecución están en `estado-final-semana-2.md` y en `evidencias/semana-2/`. El protocolo reproducible está en `pruebas-manuales-semana-2.md`. Las evidencias anteriores no se sobrescriben.

La aplicación es monousuario, secuencial y en memoria. No simula tiempos de cocción ni desplazamientos reales. Los filtros consideran completados solamente los pedidos Entregado; los rechazados se cuentan por separado y no permanecen pendientes. No incorpora persistencia, interfaces Android ni comunicaciones externas.
