# Informe técnico — Semana 3, Forma B

## Alcance y requisitos

La aplicación continúa la pizzería de consola de las semanas anteriores con las utilidades de Kotlin de la actividad «Creando soluciones con Kotlin», PMY2201, Experiencia 1. Se mantienen inventario por ingrediente, pedidos múltiples, reabastecimiento, estados, reparto, reportes y resumen de cinco secciones.

Se consultaron las instrucciones específicas de Semana 3 Forma B, la pauta de evaluación sumativa y la guía de aprendizaje. Los pasos específicos determinan el comportamiento; los descriptores de logro completo exigen cuatro validaciones, anotaciones y reflexión sobre al menos dos clases, ambos operadores, desestructuración de dos tipos, igualdad de dos clases y demostración integrada. La implementación conserva las versiones Kotlin/JVM 2.2.20, Gradle 9.6.0 y toolchain Amazon JDK 21.

## Matriz de trazabilidad

Las rutas de fuentes y pruebas de la tabla corresponden a `src/main/kotlin/org/example/` y `src/test/kotlin/org/example/`. Las evidencias se encuentran en `docs/evidencias/semana-3/`.

| Criterio oficial | Requerimientos concretos | Implementación | Pruebas y evidencias | Estado y alcance |
| --- | --- | --- | --- | --- |
| Regex y Ranges (20 puntos) | Correo, teléfono, intervalo de fechas y cantidad `1..100`. | `Entrada.kt`, `Inventario.kt`, `Promocion.kt` y `Configuracion.kt`. | [Validación](evidencias/semana-3/usabilidad/TEST-org.example.ValidacionSemana3Test.xml) · [Usabilidad](evidencias/semana-3/usabilidad/TEST-org.example.UsabilidadSemana3Test.xml) · [Promociones](evidencias/semana-3/usabilidad/TEST-org.example.PromocionTest.xml). | Formatos chileno y académico, extremos inclusivos y capacidad acotada por receta. |
| Anotaciones y reflexión (20 puntos) | Identificar elementos promocionables e inspeccionar miembros de al menos dos clases. | `Promocion.kt` consulta `@Promocionable` en ejecución. `Producto.kt` anota Margarita y Pepperoni. `Inspeccion.kt` ordena los miembros públicos. | [Pruebas de promoción](evidencias/semana-3/usabilidad/TEST-org.example.PromocionTest.xml) · [Demostración](evidencias/semana-3/usabilidad/consola-demostracion.txt). | Elegibilidad comprobada. La inspección se limita a miembros públicos. |
| Sobrecarga de operadores (15 puntos) | `+` combina productos y recalcula importes. `==` compara productos por nombre y categoría. | `Pedido.kt`, `Producto.kt` y `Demostracion.kt`. | [Operadores e igualdad](evidencias/semana-3/usabilidad/TEST-org.example.OperadoresIgualdadTest.xml) · [Demostración](evidencias/semana-3/usabilidad/consola-demostracion.txt). | La combinación produce una cotización de borradores compatibles. |
| Desestructuración (10 puntos) | Usar los componentes de Cliente y Pedido. | `Usuario.kt` expone nombre, correo y teléfono. `Pedido.kt` expone cliente, copia de líneas y total. `Demostracion.kt` utiliza ambos tipos. | [Operadores e igualdad](evidencias/semana-3/usabilidad/TEST-org.example.OperadoresIgualdadTest.xml) · [Demostración](evidencias/semana-3/usabilidad/consola-demostracion.txt). | Desestructuración de ambos tipos comprobada en ejecución. |
| Igualdad y prevención de duplicados (15 puntos) | `equals` y `hashCode` en Cliente y Producto, con registros sin duplicados. | `Usuario.kt`, `Producto.kt`, `Registro.kt` y `Jornada.kt`. | [Operadores e igualdad](evidencias/semana-3/usabilidad/TEST-org.example.OperadoresIgualdadTest.xml) · [Consola de duplicados](evidencias/semana-3/usabilidad/consola-duplicados.txt). | La primera ficha se conserva y cada pedido mantiene sus datos de entrega. |
| Demostración integrada (10 puntos) | Ejecutar juntas las utilidades de Semana 3. | Opción 8 y `--demo` en `Main.kt`. Datos fijos y cotizaciones en `Demostracion.kt`. | [Integración](evidencias/semana-3/usabilidad/TEST-org.example.IntegracionSemana3Test.xml) · [Demostración desde el menú](evidencias/semana-3/usabilidad/consola-menu-demostracion.txt). | Ejecución integrada comprobada con datos académicos. |
| Presentación técnica y entrega (10 puntos) | Proyecto organizado, compilación, ejecución, documentación y ZIP con Wrapper. | README, informes, protocolo y scripts de comprobación y entrega. | [Compilación local del 7 de noviembre](evidencias/semana-3/usabilidad/compilacion-limpia.txt) · [Validación del ZIP desde una extracción independiente](estado-final-semana-3.md#estado-del-paquete). | Paquete de entrega del 8 de noviembre con 182 entradas verificadas y documentación consolidada. |

## Decisiones de dominio

### Entradas y recuperación

El correo de consola es obligatorio. Una cadena inválida o vacía produce un mensaje y repite únicamente ese campo. EOF cancela el alta antes de registrar clientes, pedidos o consumos. La función reutilizable aplica una Regex acotada; admite subdominios y direcciones como `ana+pedido@example.cl`, recorta extremos y rechaza espacios internos, dominios ausentes y caracteres no admitidos. No se realiza validación de existencia.

Los teléfonos chilenos con nueve dígitos nacionales, con o sin `+56`, se presentan como `(+56) X XXXX XXXX`. Los internacionales compatibles de 12 dígitos conservan el formato académico, por ejemplo `+12 (345) 678-9012`. La comprobación es sintáctica. Las reglas de prefijos y las entradas que conservan su presentación se detallan en [el informe de usabilidad](mejoras-usabilidad-semana-3.md).

El intervalo `1..100` corresponde a cada cantidad ingresada en una línea. La consola muestra el menor cociente entero entre stock libre y consumo por ingrediente, con tope de 100. El stock libre descuenta las líneas del borrador de forma lógica. El dominio confirma la disponibilidad y consume los ingredientes al registrar. El reabastecimiento admite unidades positivas `Long`.

La confirmación para agregar otra pizza admite `S/s` y `N/n`, con espacios exteriores opcionales. Una entrada inválida repite esa pregunta. `C` cancela desde el selector de pizza y EOF cancela desde cualquier campo del alta. Las líneas del borrador se conservan durante los reintentos y se descartan al cancelar.

### Fechas y cálculos

`Promocion` exige inicio anterior o igual a fin y una fracción entre 0 y 1. `LocalDate` y el rango inclusivo `inicio..fin` incluyen ambos extremos. `Jornada` recibe un `Clock`; por defecto usa `America/Santiago` y con `--fecha` usa un reloj fijo. La fecha queda capturada al crear el pedido. No hay promoción por defecto, evitando alterar implícitamente los cálculos anteriores.

El subtotal es la suma de precio por cantidad. El candidato por cantidad equivale al 10 % del subtotal completo cuando hay más de tres unidades. El candidato por fecha equivale al porcentaje configurado del subtotal de las líneas cuyo tipo está anotado y únicamente durante la vigencia. Se aplica el mayor descuento, sin acumulación, y en empate se selecciona cantidad. Ambos candidatos truncan hacia abajo al peso con enteros o `RoundingMode.DOWN`. La base imponible es subtotal menos descuento seleccionado; el impuesto usa `HALF_UP`. El total suma base e impuesto con comprobación de desbordamiento.

`Importes.descuento` representa el descuento total seleccionado. `descuentoPromocional` identifica la parte aplicada por fecha; el descuento de cantidad aplicado se obtiene por diferencia. El resumen muestra ambos cuando hay una promoción configurada, con vigencia, período y política. Un rechazo conserva el subtotal solicitado y pone descuentos, impuesto y total en cero.

Los importes por fecha, la comparación con una promoción del 5 % y el caso de elegibilidad parcial se detallan en la auditoría de consola. [Resultados de promociones](mejoras-usabilidad-semana-3.md#auditoría-automatizada-de-promociones).

### Anotaciones, reflexión y tipos

Las clases finales Margarita y Pepperoni representan las dos pizzas existentes, ambas promocionables; no se amplía el catálogo comercial. Producto permanece como base no anotada para permitir elementos no elegibles. El cálculo consulta la anotación real del tipo en ejecución. Se agregó únicamente `kotlin-reflect` como dependencia de producción, alineada con Kotlin 2.2.20.

La inspección usa propiedades públicas y funciones públicas reflejadas, ordena las firmas y conserva miembros heredados de clases del dominio. Excluye miembros privados y funciones heredadas directamente de `Any`, sin modificar accesibilidad ni estado. Se inspeccionan las dos clases anotadas y también Producto y Pedido.

### Identidad y registros

Cliente utiliza nombre y correo; Producto utiliza nombre y categoría. Se recortan extremos, colapsan espacios y normalizan mayúsculas con `Locale.ROOT` exclusivamente para comparar. Las claves son inmutables y `hashCode` usa los mismos atributos que `equals`. La categoría predeterminada es `pizza`; se admite otra categoría sin añadir productos innecesarios.

Los registros usan un conjunto con orden de inserción y conservan la primera ficha de cada identidad. Una repetición de cliente no reemplaza el contacto inicial; el pedido mantiene el contacto recién ingresado y la consola informa esa política. El catálogo mantiene una ficha única por producto. Una identidad repetida con precio, receta o elegibilidad diferentes se rechaza antes de consumir stock; no se selecciona silenciosamente uno de los precios.

### Combinación y componentes

`plus` exige dos objetos distintos en estado Registrado, cliente equivalente y los mismos teléfono y dirección, fecha, tasa numéricamente equivalente y configuración de promoción. No admite pedidos en preparación, listos, enviados, entregados ni rechazados. Agrupa productos repetidos con suma exacta, comprueba que sus fichas coincidan y recalcula todos los importes. Une notas no vacías distintas en orden, conserva la fecha y crea el identificador `SIM(idA+idB)`.

El resultado queda Registrado, sin repartidor y marcado como simulación. No se incorpora a `Jornada`, no consume inventario y no admite resolver disponibilidad. Los originales no se modifican. La operación demuestra una cotización combinada; la aplicación no traslada ni fusiona pedidos operativos ya aceptados.

Los componentes de Cliente son nombre, correo y teléfono, en ese orden. Los de Pedido son cliente, copia de líneas y total vigente, incluso cero para un rechazo. La demostración utiliza las dos declaraciones de desestructuración explícitamente.

## Compatibilidad y resultados

La auditoría inicial y la compilación limpia verificaron 65 pruebas de base. Se conservaron todos esos casos y se añadieron 32. Se adaptaron tres casos: correo inválido corregido antes del resto del flujo, correo vacío con reintento y fecha añadida al resumen completo. Las adaptaciones responden a contratos de Semana 3 y mantienen sus comprobaciones de recuperación, datos predeterminados, inventario y presentación.

El estado final reúne las fechas, versiones y cifras de las tres etapas automatizadas. Las siete aprobaciones de la auditoría manual independiente se registran por separado. [Verificación automatizada](estado-final-semana-3.md#verificación-automatizada) · [Auditoría manual independiente](estado-final-semana-3.md#auditoría-manual-independiente).

La documentación y las evidencias de las semanas anteriores permanecen conservadas. Los datos siguen siendo secuenciales y en memoria; no hay persistencia, interfaces gráficas, pagos reales ni comunicaciones externas.
