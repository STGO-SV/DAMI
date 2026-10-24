# Informe de implementación y verificación — Semana 1

## 1. Identificación

PMY2201, Desarrollo de Aplicaciones Móviles I. Experiencia 1, Semana 1, Forma B: «Mi primera app en Kotlin». Proyecto `pizzeria-kotlin`, paquete `org.example`, entrada `Main.kt`. Fecha de referencia de la actividad: 22 de octubre de 2026, zona America/Santiago. Las marcas temporales de las evidencias corresponden a sus ejecuciones reales.

## 2. Objetivo y alcance

Se implementó una aplicación Kotlin/JVM de consola para un pedido de pizza: captura del cliente, cantidad, cálculo de descuento, control de stock, asignación determinista de reparto y resumen. Se mantuvo una estructura introductoria con tres archivos de producción, funciones auxiliares y un registro de resultado. No se incorporaron capas, servicios, bases de datos ni dependencias de producción adicionales.

## 3. Requerimientos implementados

Se capturan nombre, dirección y teléfono. Se declaran nombre de pizza, precio, inventario, identificador, cantidad, total, estado y repartidor. Se emplean `String`, `Int`, `Long`, `Boolean`, `val` y `var`, conversiones seguras, interpolación y expresiones `if`. Se valida la cantidad positiva y el formato básico del teléfono. Se admiten valores nulos sin excepción en el flujo de consola.

Se calcula `precioUnitario.toLong() * cantidadPizzas`. El descuento depende exclusivamente de `cantidadPizzas > 3`. La comprobación de inventario es una expresión `if` que actualiza el stock solo si alcanza. El resumen en `Main.kt` expone todos los campos exigidos y distingue los pedidos rechazados sin cobro ni reparto.

## 4. Estructura final

```text
pizzeria-kotlin/
  .gitignore
  build.gradle.kts
  settings.gradle.kts
  gradlew
  gradlew.bat
  gradle/wrapper/gradle-wrapper.jar
  gradle/wrapper/gradle-wrapper.properties
  src/main/kotlin/org/example/
    Main.kt
    Entrada.kt
    Pedido.kt
  src/test/kotlin/org/example/PedidoTest.kt
  README.md
  tools/Preparar-Entrega.ps1
  docs/
    informe-semana-1.md
    estado-final-semana-1.md
    evidencias/
```

Las carpetas generadas se conservan localmente cuando corresponda y se excluyen del ZIP. Las evidencias seleccionadas se copian a documentación para conservar resultados fuera de `build/`.

## 5. Decisiones técnicas

| Decisión | Criterio adoptado |
|---|---|
| Pizza y precio | Margarita, $8.000 CLP, valores de ejemplo explícitos |
| Inventario | 10 unidades; una unidad representa los ingredientes necesarios para una pizza |
| Dinero | Pesos enteros; `Long` para evitar desbordamiento de productos `Int` |
| Redondeo | División entera del subtotal entre 10; exacta al precio elegido, truncamiento del descuento hacia abajo si aparecen fracciones con otro precio |
| Estado | Confirmado o rechazado por falta de inventario |
| Rechazo | Total a pagar y descuento aplicado cero; subtotal solicitado visible; stock intacto |
| Reparto | R01 solo para confirmados; sin asignar para rechazados |
| Identificador | PED-001 para el único pedido de cada ejecución |
| Mutabilidad | `var ingredientesDisponibles` cambia al consumir stock; los demás datos son `val` |
| Datos ausentes | Nombre: Cliente sin nombre; dirección: Retiro en local; teléfono: No informado; cantidad nula: 1 con aviso |
| Cantidad inválida | Se repregunta; `toIntOrNull()` evita excepciones de conversión |
| Teléfono | Entre 8 y 15 dígitos, con prefijo y separadores básicos; se conserva como texto |

## 6. Entorno verificado

Windows 11 amd64. Gradle Wrapper 9.6.0, plugin Kotlin/JVM 2.2.20 y toolchain Java 21. El Kotlin 2.3.21 que aparece en `gradlew --version` corresponde al Kotlin integrado de Gradle, no al plugin del proyecto.

Inicialmente `JAVA_HOME` apuntaba a Microsoft OpenJDK 21.0.12; la primera ejecución confirmada se realizó con ese entorno. Se comprobó la instalación de Amazon Corretto 21.0.12.1 y se seleccionó mediante una variable de proceso para las pruebas, la compilación limpia y las otras ejecuciones. No se modificaron variables globales. IntelliJ ya seleccionaba `corretto-21`.

Las opciones UTF-8 se configuraron en las tareas `JavaExec`. Se conservaron todas las piezas del Wrapper. Evidencias: `evidencias/entorno-gradle.txt`, `evidencias/jdks-detectados.txt`, `build.gradle.kts` y `gradle/wrapper/gradle-wrapper.properties`.

## 7. Procedimiento de ejecución

Se abre la carpeta en IntelliJ como proyecto Gradle y se selecciona JDK 21, o se ejecuta desde PowerShell:

```powershell
.\gradlew.bat --console=plain run
.\gradlew.bat test
.\gradlew.bat clean build
```

Se ingresan nombre, dirección, teléfono y cantidad en ese orden. El README detalla la selección opcional de Corretto y la ejecución directa de `MainKt` con opciones UTF-8.

## 8. Resultados de compilación

Se ejecutó `gradlew.bat --console=plain clean build`. Resultado real: `BUILD SUCCESSFUL in 5s`; ocho tareas ejecutadas. Se compilaron fuentes y pruebas, se ejecutaron las pruebas y se generaron JAR y distribuciones de la aplicación. Evidencia completa: `evidencias/compilacion-limpia.txt`. Los binarios generados no son necesarios dentro de la entrega fuente.

## 9. Resultados de pruebas

La ejecución previa `test` terminó correctamente. Tras `clean build`, `TEST-org.example.PedidoTest.xml` registra 19 pruebas, 0 fallos, 0 errores y 0 omitidas. Se conserva una copia en `evidencias/TEST-org.example.PedidoTest.xml`. La tabla agrupa escenarios; algunos métodos comprueban varias entradas.

| Escenario | Resultado verificado | Método de prueba |
|---|---|---|
| 1 pizza | $8.000; sin descuento | unaPizzaSinDescuento |
| 3 pizzas | $24.000; sin descuento | tresPizzasSinDescuento |
| 4 pizzas | Descuento $3.200; total $28.800 | cuatroPizzasConDiezPorCiento |
| 5 pizzas | Total $36.000 | cincoPizzasConDiezPorCiento |
| Stock exacto | Confirma 10; quedan 0; total $72.000 | stockExactoConfirmaYAgotaInventario |
| Stock superior | De 10 se consumen 3; quedan 7 | stockSuperiorSeDescuenta |
| Stock insuficiente | Rechazo; quedan 10; total 0; sin reparto | stockInsuficienteConservaInventarioSinCobroNiReparto |
| Stock cero | Rechazo sin reparto | stockCeroRechaza |
| Cantidad cero, negativa, texto, vacío, decimal y desbordada | Conversión rechazada con seguridad | cantidadesInvalidasSeRechazan |
| Parámetros internos inválidos | Precondiciones comprobadas | procesamientoRechazaParametrosInvalidos |
| Teléfono con prefijo o ceros | Se conserva el texto | telefonoConservaPrefijoYCeros |
| Teléfono inválido o nulo | Se detecta; el flujo avisa y usa predeterminado | telefonosInvalidosSeDetectan; entradasInvalidasRepreguntanYTelefonoInvalidoAvisa |
| Nombre y dirección vacíos o nulos | Valores predeterminados | textosVaciosYNulosUsanPredeterminado |
| Entrada completa nula | Finaliza con datos y cantidad predeterminados | entradaNulaCompletaTerminaSinExcepcion |
| Corrección de entradas | Tres repreguntas y luego cantidad 4 | entradasInvalidasRepreguntanYTelefonoInvalidoAvisa |
| Confirmación | R01 asignado | confirmadoTieneRepartidor |
| Resumen confirmado | Todos los campos y cifras esperados | resumenConfirmadoContieneTodosLosDatos |
| Resumen rechazado | Estado, inventario y cobro coherentes | resumenRechazadoEsConsistente |
| Cálculos grandes y fracciones | Producto Long sin desbordamiento y descuento truncado | multiplicacionNoDesbordaIntYDescuentoTruncaFraccion |

Las cuatro ejecuciones reales por consola finalizaron con `BUILD SUCCESSFUL`. Se conservan `ejecucion-confirmada.txt`, `ejecucion-rechazada.txt`, `ejecucion-entradas-invalidas.txt` y `ejecucion-fin-entrada.txt`. La primera contiene José y Ñuñoa sin pérdida de acentos.

## 10. Matriz de cumplimiento de la pauta

Se contrastó la implementación con los requerimientos establecidos en las instrucciones específicas de la actividad (PMY2201_Exp1_S1_Instrucciones_especificas (Forma B))  y con los siete criterios contenidos en la pauta de evaluación. La correspondencia documentada constituye una verificación técnica de cumplimiento.

| Criterio | Estado | Evidencia concreta | Archivos |
|---|---|---|---|
| 1. Variables, var/val y tipos | Implementado y verificado por compilación y revisión del código | Datos estables `val`; inventario `var`; teléfono String y dinero Long | Main.kt, Pedido.kt, Entrada.kt; compilacion-limpia.txt |
| 2. Captura y manejo de nulos/incorrectos | Implementado y verificado | Conversión segura, repregunta, predeterminados; pruebas de EOF y entradas inválidas | Entrada.kt, Main.kt, PedidoTest.kt; ejecucion-entradas-invalidas.txt, ejecucion-fin-entrada.txt |
| 3. Total y descuento | Implementado y verificado | Casos 1, 3, 4 y 5; descuento solo por cantidad mayor que 3 | Pedido.kt, PedidoTest.kt; XML de resultados |
| 4. Inventario | Implementado y verificado | Stock exacto, superior, cero e insuficiente; rechazo sin consumo | Pedido.kt, PedidoTest.kt; ejecucion-rechazada.txt |
| 5. Resumen completo | Implementado y verificado | Pruebas de todos los campos confirmados y del rechazo; salida con acentos | Main.kt, PedidoTest.kt; ejecucion-confirmada.txt, ejecucion-rechazada.txt |
| 6. Código limpio y expresiones | Implementado y revisado | Tres archivos cohesionados, nombres en español, funciones pequeñas, if como expresión | src/main/kotlin/org/example/*.kt; README.md |
| 7. Proyecto comprimido | Implementado y verificado | 24 entradas y comparación SHA-256 de cada archivo con su original; Wrapper completo y ausencia de cachés | tools/Preparar-Entrega.ps1; ZIP y archivo .validacion.txt en carpeta superior |

## 11. Problemas encontrados y soluciones

Durante la configuración y validación del proyecto se identificaron diferencias entre los entornos de ejecución de IntelliJ IDEA y Gradle, particularmente en la selección del JDK y la codificación de caracteres.

`Configuración del JDK`: Se verificó la compatibilidad del proyecto con Java 21 y se utilizó Amazon Corretto 21.0.12.1 para la compilación y las pruebas finales. La configuración permitió mantener un entorno de desarrollo consistente sin modificar las variables globales del sistema operativo.

`Configuración de Gradle`: Se incorporó el plugin `application` para facilitar la ejecución mediante la tarea run y se configuró la entrada estándar para permitir la interacción del programa con la consola. Asimismo, se habilitó el soporte de JUnit para ejecutar las pruebas automatizadas.

`Codificación de caracteres`: Se detectaron problemas de representación de caracteres acentuados en la salida de consola. Se configuró UTF-8 mediante las propiedades de ejecución de la JVM, garantizando la visualización correcta de los textos en español. También se estableció la codificación adecuada para los scripts de PowerShell incluidos en el proyecto.

Las configuraciones se verificaron mediante compilaciones exitosas, pruebas automatizadas y ejecuciones funcionales desde IntelliJ IDEA.

## 12. Limitaciones y pendientes

Implementado: procesamiento de un pedido por ejecución, inventario en memoria y datos académicos de ejemplo. Verificado: compilación limpia, 19 pruebas automatizadas, cuatro ejecuciones documentadas y cuatro comprobaciones manuales satisfactorias. El contraste con las instrucciones específicas de la Forma B y la pauta de evaluación formativa no identificó incumplimientos funcionales.

El identificador no es único entre ejecuciones. El teléfono solo se valida sintácticamente. Los valores predeterminados permiten la demostración aun con entrada nula. El estado físico de suspensión no puede acreditarse antes de solicitarlo: se registra separadamente del éxito de la entrega.

## 13. Estado final de entrega

Se conserva el proyecto fuente, README, este informe, el cierre y las evidencias. El procedimiento `tools/Preparar-Entrega.ps1` genera el ZIP en la carpeta superior y solo declara validación correcta después de leer todas las entradas y comprobar sus bytes. El resultado definitivo del empaquetado y el hash del ZIP se registran fuera del archivo para evitar una referencia circular. No se realizaron publicaciones, pushes ni entregas externas. La suspensión queda reservada a la última operación, una vez cerradas las escrituras y comprobada la entrega.

## 14. Validación funcional manual

Fecha de verificación: 24 de octubre de 2026.

Se ejecutaron cuatro comprobaciones manuales independientes desde IntelliJ IDEA, utilizando la configuración Kotlin/JVM del proyecto.

| Prueba | Entrada relevante | Resultado observado | Estado |
|---|---|---|---|
| Pedido confirmado con descuento | 8 pizzas y teléfono inválido `+569juan` | Subtotal $64.000; descuento $6.400; total $57.600; inventario restante 2; teléfono predeterminado; repartidor R01 | Aprobada |
| Inventario insuficiente | 11 pizzas con inventario inicial de 10 | Pedido rechazado; descuento aplicado $0; total a pagar $0; inventario sin modificaciones; repartidor sin asignar | Aprobada |
| Límite inferior del descuento | 3 pizzas | Subtotal y total $24.000; sin descuento; inventario restante 7; pedido confirmado | Aprobada |
| Recuperación ante entradas inválidas | Teléfono `abc`; cantidades `0`, `-2`, `texto` y finalmente `4` | Teléfono predeterminado; cantidades inválidas rechazadas; procesamiento de 4 pizzas y total $28.800 | Aprobada |

Las cuatro comprobaciones finalizaron con los resultados funcionales esperados. Las ejecuciones permitieron verificar la consistencia de los cálculos, la actualización del inventario, el tratamiento de entradas incorrectas y la información presentada en el resumen del pedido.

La validación manual complementa las 19 pruebas automatizadas registradas y no sustituye la evaluación docente.