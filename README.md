# Pizzería Kotlin

PMY2201 — Desarrollo de Aplicaciones Móviles I, Experiencia 1, Semana 2. Actividad «Aplicando utilidades de Kotlin».

Aplicación de consola que registra clientes y varios pedidos por sesión, controla ingredientes individuales y coordina la preparación y el reparto. Evoluciona la entrega de Semana 1 con clases, funciones, arreglos, colecciones y manejo seguro de entradas.

## Entorno y ejecución

Kotlin/JVM 2.2.20, Amazon Corretto JDK 21, Gradle Wrapper 9.6.0 y Gradle Kotlin DSL. JUnit 5 y kotlin-test se utilizan exclusivamente en pruebas. Se necesita acceso a Internet para la primera descarga de Gradle y dependencias si no están en caché.

Desde la carpeta del proyecto en PowerShell:

```powershell
[Console]::InputEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
.\gradlew.bat --console=plain run
.\gradlew.bat test
.\gradlew.bat clean build
```

La terminal debe recibir y mostrar UTF-8 para conservar acentos en los datos ingresados. Las dos primeras líneas configuran la sesión actual de PowerShell. En IntelliJ se utiliza codificación UTF-8 para el proyecto y la consola.

En IntelliJ IDEA se abre esta carpeta como proyecto Gradle y se selecciona Amazon Corretto 21. Se ejecuta `org.example.MainKt` o la tarea `run`. La configuración Gradle selecciona explícitamente JDK 21 del proveedor Amazon. Las tareas de ejecución incorporan UTF-8. Al ejecutar directamente en el IDE se agregan las opciones JVM `-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8`.

La tasa de impuesto es un **supuesto académico configurable**. El valor predeterminado `0.19` representa un ejemplo de IVA chileno; el enunciado no prescribe una tasa. Puede cambiarse sin editar código:

```powershell
.\gradlew.bat --console=plain run --args=0
.\gradlew.bat --console=plain run --args=0.10
```

Se admite un decimal entre 0 y 1. Para ejecutar `MainKt` directamente se pasa ese decimal como argumento del programa.

## Operación

El menú ofrece crear pedidos, consultar el reporte, marcar Listo, enviar, entregar, consultar inventario y reabastecer. Cada pedido permite agregar distintas pizzas y cantidades, además de notas opcionales. Las operaciones de estado solicitan el identificador, por ejemplo `PED-001`. Al salir se imprime el reporte completo.

El catálogo conserva Margarita a $8.000 y añade Pepperoni a $9.000. Cada Margarita requiere Masa 1, Tomate 2 y Queso 2; Pepperoni añade 1 unidad del ingrediente Pepperoni. Las unidades son porciones abstractas de receta. El stock inicial es Masa 10, Tomate 20, Queso 20 y Pepperoni 5. Todos los ingredientes se comprueban antes de consumir; un rechazo informa disponible, requerido y pasos de reabastecimiento, sin descontar ningún ingrediente. Al reabastecer se respeta el nombre y sus mayúsculas.

Los pedidos aceptados pasan a **En preparación → Listo → En camino → Entregado**. Solo Entregado se considera completado. Registrado, En preparación, Listo y En camino permanecen pendientes; los rechazados se informan aparte. Registrado es transitorio durante el alta. Los rechazados no pueden reactivarse: después de reabastecer se registra un nuevo pedido. El total cobrado, descuento e impuesto de un rechazo son cero y se conserva el subtotal solicitado como referencia.

Se asigna el repartidor disponible más cercano entre distancias precargadas desde el local: Paula 1500 m, Diego 500 m y Camila 900 m. Son una simulación común para todos los clientes, sin ubicaciones reales. `sortedBy` y `find` seleccionan el mínimo elegible; los empates conservan el orden de la lista. El repartidor queda ocupado hasta entregar. Sin repartidores, el pedido permanece Listo.

## Cálculos y validación

El dinero se representa en pesos enteros con `Long`. Se calcula subtotal, se descuenta el 10 % si la suma de pizzas supera tres, y sobre la base descontada se calcula el impuesto con `BigDecimal`. El descuento conserva la política de Semana 1: truncamiento hacia abajo al peso. El impuesto se redondea al peso con `HALF_UP`. El total es subtotal menos descuento más impuesto. Las operaciones detectan desbordamientos antes de consumir inventario.

| Margarita | Subtotal | Descuento | Impuesto 19 % | Total |
| --- | ---: | ---: | ---: | ---: |
| 1 | $8.000 | $0 | $1.520 | $9.520 |
| 3 | $24.000 | $0 | $4.560 | $28.560 |
| 4 | $32.000 | $3.200 | $5.472 | $34.272 |
| 5 | $40.000 | $4.000 | $6.840 | $42.840 |

La validación telefónica de Semana 1 conserva prefijos y ceros: admite `+` inicial y separadores, con 8 a 15 dígitos ASCII. El correo utiliza una validación sintáctica básica con usuario, dominio y extensión. No se comprueba la existencia real de los contactos. Los inválidos usan `No informado` y `correo@invalido.com`. Nombre y dirección vacíos usan `Cliente sin nombre` y `Retiro en local`.

Las selecciones de pizza inválidas vuelven a solicitarse sin descartar los datos del cliente ni los productos ya incorporados. Las cantidades cero, negativas, no numéricas o fuera del rango `Int` repiten únicamente la pregunta de cantidad. Una selección de pizza vacía utiliza Margarita; las cantidades vacías se consideran inválidas porque no tienen valor predeterminado.

El fin de entrada (`null`/EOF) en cualquier campo del alta cancela el pedido en curso sin registrarlo ni modificar inventario o reparto. No equivale a enviar una línea vacía: los predeterminados de cliente, contactos, pizza y notas siguen aplicándose a líneas vacías. El menú termina al recibir fin de entrada. Las notas nulas o vacías muestran `Sin notas`. Un correo válido habilita un mensaje local de notificación mediante `let`; no se envían comunicaciones externas. Los errores previsibles de entrada y las transiciones inválidas se informan sin cerrar la sesión.

## Diseño y documentación

`Producto` encapsula su receta; `Pedido` conserva líneas e importes y controla estados; `Usuario` comparte contacto entre `Cliente` y `Repartidor`. El resumen usa el comportamiento sobrescrito de Cliente y el menú muestra el de Repartidor. `Inventario` protege un `MutableMap`; `Jornada` conserva un `Array<Pedido>`, informa con `for` y agrupa con `filter`. `Entrada.kt` mantiene las validaciones separadas de la consola.

- [Informe y trazabilidad](docs/informe-semana-2.md).
- [Estado técnico y resultados](docs/estado-final-semana-2.md).
- [Comprobaciones manuales](docs/pruebas-manuales-semana-2.md).
- Evidencias de Semana 2: `docs/evidencias/semana-2/`.
- Historial: `docs/informe-semana-1.md`, `docs/estado-final-semana-1.md` y evidencias anteriores conservadas.

Las pruebas de Semana 1 se adaptan a las clases, al nuevo impuesto y al flujo de estados, manteniendo cobertura de los límites de cantidad, validaciones, rechazo, redondeo, importes grandes y resúmenes. Con tasa cero se verifican los totales históricos.

La revisión de recuperación de entradas aprobó 57 pruebas: 45 casos existentes adaptados al tratamiento de EOF y 12 casos nuevos sobre reintentos, conservación del cliente y productos, rangos numéricos, líneas vacías y cancelación sin efectos parciales. `test` y `clean build` finalizaron correctamente. Las evidencias actuales están en `docs/evidencias/semana-2/recuperacion-entradas/`.

## Entrega y alcance

El ZIP se genera fuera del repositorio y conserva el Wrapper completo. Se puede generar nuevamente con el siguiente comando:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\Preparar-Entrega-Semana2.ps1
```

El resultado es `PMY2201_Exp1_S2_pizzeria-kotlin.zip` en la carpeta superior, acompañado por su SHA-256 y validación de contenido. Excluye Git, cachés, compilaciones y configuración local de IDE. El ZIP de Semana 1 se conserva; el script histórico corresponde exclusivamente a aquella entrega.

Los datos viven en memoria y se reinician por ejecución. No hay persistencia, interfaz Android, pagos reales, geolocalización ni servicios externos. La simulación de distancias no depende de la dirección del cliente. Las unidades de ingredientes y los impuestos son parámetros didácticos, no un sistema comercial de facturación.
