# Pizzería Kotlin

PMY2201 — Desarrollo de Aplicaciones Móviles I, Experiencia 1, Semana 3, Forma B. Actividad «Creando soluciones con Kotlin».

Aplicación de consola que registra clientes y varios pedidos por sesión, controla ingredientes individuales y coordina la preparación y el reparto. Integra Regex, rangos, promociones por fechas, anotaciones, reflexión, operadores, desestructuración y prevención de duplicados, conservando el flujo de las semanas anteriores.

## Entorno y ejecución

Kotlin/JVM 2.2.20, Amazon Corretto JDK 21 y Gradle Wrapper 9.6.0. Se necesita acceso a Internet para la primera descarga de Gradle y dependencias cuando no están en caché.

Desde la carpeta del proyecto en PowerShell:

```powershell
[Console]::InputEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
.\gradlew.bat --console=plain run
.\gradlew.bat test
.\gradlew.bat clean build
```

La terminal debe recibir y mostrar UTF-8 para conservar acentos en los datos ingresados. Las dos primeras líneas configuran la sesión actual de PowerShell. En IntelliJ se utiliza codificación UTF-8 para el proyecto y la consola.

En IntelliJ IDEA se abre esta carpeta como proyecto Gradle y se selecciona Amazon Corretto 21. Se ejecuta `org.example.MainKt` o la tarea `run`. En una configuración directa de `MainKt`, se colocan los argumentos en **Program arguments** y las opciones `-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8` en **VM options**. La tarea Gradle `run` configura UTF-8 y conecta la entrada estándar.

La tasa de impuesto es un **supuesto académico configurable**. El valor predeterminado `0.19` representa un ejemplo de IVA chileno; el enunciado no prescribe una tasa. Puede cambiarse sin editar código:

```powershell
.\gradlew.bat --console=plain run --args=0
.\gradlew.bat --console=plain run --args=0.10
```

Se admite un decimal entre 0 y 1. Para ejecutar `MainKt` directamente se pasa ese decimal como argumento del programa.

La opción 8 del menú ejecuta la demostración integrada. Los ejemplos siguientes muestran la invocación directa y dos fechas para comprobar la misma promoción.

```powershell
.\gradlew.bat --console=plain run --args="--demo"
.\gradlew.bat --console=plain run --args="--fecha=2026-11-03 --promocion=2026-11-01,2026-11-07,0.15"
.\gradlew.bat --console=plain run --args="--fecha=2026-11-08 --promocion=2026-11-01,2026-11-07,0.15"
```

`--fecha=AAAA-MM-DD` fija la fecha de los pedidos. El valor predeterminado corresponde a la fecha de la zona `America/Santiago`. `--promocion=inicio,fin,fracción` configura una promoción con extremos inclusivos y fracción de 0 a 1. La promoción por fechas es opcional. La configuración se valida antes de abrir el menú. `--demo` ejecuta la demostración con sus propios datos fijos.

## Operación

El menú ofrece crear pedidos, consultar el reporte, marcar Listo, enviar, entregar, consultar inventario y reabastecer. Cada pedido permite agregar distintas pizzas y cantidades, además de notas opcionales. Las operaciones de estado solicitan el identificador, por ejemplo `PED-001`. Al salir se imprime el reporte completo.

El catálogo conserva Margarita a $8.000 y añade Pepperoni a $9.000. Cada Margarita requiere Masa 1, Tomate 2 y Queso 2; Pepperoni añade 1 unidad del ingrediente Pepperoni. Las unidades son porciones abstractas de receta. El stock inicial es Masa 10, Tomate 20, Queso 20 y Pepperoni 5. Todos los ingredientes se comprueban antes de consumir; un rechazo informa disponible, requerido y pasos de reabastecimiento, sin descontar ningún ingrediente. Al reabastecer se respeta el nombre y sus mayúsculas.

Los pedidos aceptados pasan a **En preparación → Listo → En camino → Entregado**. Solo Entregado se considera completado. Registrado, En preparación, Listo y En camino permanecen pendientes; los rechazados se informan aparte. Registrado es transitorio durante el alta. Los rechazados no pueden reactivarse: después de reabastecer se registra un nuevo pedido. El total cobrado, descuento e impuesto de un rechazo son cero y se conserva el subtotal solicitado como referencia.

Se asigna el repartidor disponible más cercano entre distancias precargadas desde el local: Paula 1500 m, Diego 500 m y Camila 900 m. Son una simulación común para todos los clientes, sin ubicaciones reales. `sortedBy` y `find` seleccionan el mínimo elegible; los empates conservan el orden de la lista. El repartidor queda ocupado hasta entregar. Sin repartidores, el pedido permanece Listo.

## Cálculos y validación

El dinero se representa en pesos enteros con `Long`. Se calcula el subtotal y se compara el descuento del 10 % por más de tres unidades con el descuento por fechas sobre el subtotal de productos anotados con `@Promocionable`. Se aplica el mayor, sin acumularlos; en empate se conserva el descuento por cantidad. Ambos descuentos truncan hacia abajo al peso. El impuesto usa `BigDecimal` y `HALF_UP` sobre subtotal menos descuento. El total es subtotal menos descuento más impuesto. Las operaciones detectan desbordamientos antes de consumir inventario. Sin promoción configurada se conservan los cálculos históricos:

| Margarita | Subtotal | Descuento | Impuesto 19 % | Total |
| --- | ---: | ---: | ---: | ---: |
| 1 | $8.000 | $0 | $1.520 | $9.520 |
| 3 | $24.000 | $0 | $4.560 | $28.560 |
| 4 | $32.000 | $3.200 | $5.472 | $34.272 |
| 5 | $40.000 | $4.000 | $6.840 | $42.840 |
| 8 | $64.000 | $6.400 | $10.944 | $68.544 |

El resumen presenta teléfonos chilenos como `(+56) X XXXX XXXX`. Se puede ingresar el número nacional de nueve dígitos o incluir `+56`. Las entradas internacionales compatibles de 12 dígitos se presentan como `+XX (XXX) XXX-XXXX`. Las demás entradas genéricas válidas conservan su presentación. La validación es sintáctica y un teléfono inválido se muestra como `No informado`.

El correo es obligatorio en consola: los valores inválidos o vacíos repiten únicamente ese campo. La Regex académica admite usuario ASCII con letras, dígitos y `_+%-`, puntos separados por segmentos no vacíos, dominio con etiquetas alfanuméricas y guiones interiores, y extensión alfabética de al menos dos caracteres. Se recortan espacios exteriores; se rechazan espacios internos y formatos mal formados. No se verifica la existencia real del contacto. Para objetos construidos directamente sin correo se conserva el marcador histórico `correo@invalido.com`, sin notificación. Nombre y dirección vacíos usan `Cliente sin nombre` y `Retiro en local`.

La dirección de entrega se recibe como texto libre. No se comprueba su existencia, estructura postal ni ubicación geográfica. La asignación de repartidores utiliza distancias simuladas independientes de la dirección ingresada.

Una selección de pizza vacía utiliza Margarita. Al seleccionar la variedad se muestra el máximo según su receta y los ingredientes comprometidos en el borrador, con tope de 100 por línea. Una cantidad inválida o superior al máximo repite solo ese campo. Si la variedad se agota, se vuelve a la selección y se puede elegir otra o ingresar `C` para cancelar. El inventario real se comprueba y descuenta al registrar el pedido. El rango `1..100` se aplica a las cantidades ingresadas en consola.

La pregunta de agregar otra pizza acepta `S`, `s`, `N` o `n` y recorta los espacios exteriores. Ante otra entrada se repite la pregunta y se conservan el cliente y las líneas del borrador.

EOF cancela el borrador en cualquier campo del alta y finaliza la sesión. El registro y el consumo de ingredientes ocurren después de completar el alta. Una línea vacía activa los valores predeterminados de nombre, dirección, teléfono, pizza y notas, o repite la pregunta de correo, cantidad o S/N. Un correo válido habilita una notificación local simulada.

## Utilidades avanzadas

`Margarita` y `Pepperoni` especializan `Producto` y llevan `@Promocionable`, con objetivo CLASS y retención RUNTIME. La promoción consulta la anotación mediante reflexión, sin banderas de elegibilidad. La inspección ordena propiedades públicas y métodos públicos, incluidos los heredados del dominio; excluye el estado privado y los métodos heredados directamente de `Any`. La demostración inspecciona las dos pizzas, `Producto` y `Pedido`.

La igualdad de Cliente utiliza nombre y correo; la de Producto utiliza nombre y categoría. Las claves recortan extremos, unifican espacios y convierten a minúsculas con `Locale.ROOT`. `equals()` y `hashCode()` mantienen el mismo criterio. Precio, receta, dirección y teléfono no forman parte de esas identidades. El registro conserva la primera ficha; cada pedido conserva su contacto ingresado. Una ficha de producto con la misma identidad pero distinto precio, receta o elegibilidad se rechaza antes de consumir inventario. El catálogo operativo sigue conteniendo solo Margarita y Pepperoni.

`Pedido + Pedido` combina borradores distintos en estado Registrado del mismo cliente, contacto de entrega, fecha, tasa y promoción. Agrupa productos iguales, comprueba fichas compatibles y recalcula importes desde las líneas. Conserva notas distintas en orden y produce `SIM(idA+idB)`, con estado Registrado y sin repartidor. El resultado es una cotización marcada como simulación, que no se registra en la jornada ni consume inventario. Los pedidos operativos y rechazados no pueden combinarse. La demostración utiliza `+`, `==` y la desestructuración explícita de Cliente (nombre, correo, teléfono) y Pedido (cliente, líneas copiadas, total).

## Documentación y verificación

- [Informe y trazabilidad de Semana 3](docs/informe-semana-3.md).
- [Estado técnico y resultados automatizados de Semana 3](docs/estado-final-semana-3.md#verificación-automatizada).
- [Siete grupos aprobados en la auditoría manual independiente](docs/estado-final-semana-3.md#auditoría-manual-independiente).
- [Comprobaciones manuales en IntelliJ](docs/pruebas-manuales-semana-3.md).
- [Revisión de retroalimentación de Semana 1: trazabilidad y 65 pruebas aprobadas](docs/retroalimentacion-semana-1.md).
- [Corrección de respuestas S/N tras retroalimentación de Semana 2](docs/retroalimentacion-semana-2.md).
- [Usabilidad y auditoría de promociones de Semana 3](docs/mejoras-usabilidad-semana-3.md).
- [Evidencias automatizadas de la versión local](docs/evidencias/semana-3/usabilidad/).

El 7 de noviembre de 2026 se registraron 124 pruebas aprobadas, 23 escenarios de consola y 185 comprobaciones de salida, con compilación limpia satisfactoria. La auditoría manual se realizó en distintas etapas. [Repetición automatizada](docs/pruebas-manuales-semana-3.md#10-repetición-de-la-verificación-automatizada).

## Entrega y alcance

El paquete de entrega `PMY2201_Exp1_S3_FormaB_pizzeria-kotlin.zip` se generó el 8 de noviembre de 2026 con 182 entradas. Incluye las fuentes, las evidencias y la documentación consolidada, con las precisiones sobre EOF y dirección de entrega. La compilación y las comprobaciones se ejecutaron desde una extracción independiente. El archivo `.zip.sha256` y los registros de validación se consultan junto al ZIP. [Estado del paquete](docs/estado-final-semana-3.md#estado-del-paquete).

Los datos viven en memoria y se reinician por ejecución. No hay persistencia, interfaz Android, pagos reales, geolocalización ni servicios externos. Las unidades de ingredientes y los impuestos son parámetros didácticos, no un sistema comercial de facturación.
