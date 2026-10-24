# Pizzería Kotlin

PMY2201 — Desarrollo de Aplicaciones Móviles I. Experiencia 1, Semana 1, Forma B: «Mi primera app en Kotlin».

Aplicación de consola para registrar un cliente, calcular un pedido, verificar inventario y asignar un repartidor. Se utilizan variables, tipos, conversiones seguras, operadores, interpolación y expresiones `if`.

## Entorno y ejecución

Se requiere Windows con JDK 21 y acceso a Internet para descargar Gradle y dependencias cuando no estén en caché. Se utiliza Kotlin/JVM 2.2.20, Gradle Wrapper 9.6.0 y Gradle Kotlin DSL. Corretto 21.0.12.1 se utilizó para la validación final. JUnit 5 y kotlin-test son dependencias exclusivas de pruebas.

Desde la carpeta del proyecto, en PowerShell:

```powershell
.\gradlew.bat --console=plain run
.\gradlew.bat test
.\gradlew.bat clean build
```

En IntelliJ IDEA se abre la carpeta como proyecto Gradle, se selecciona JDK 21 y se ejecuta `org.example.MainKt`. Para una configuración directa del IDE se agregan las opciones de JVM `-Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8`. Las tareas Gradle de ejecución ya incorporan estas opciones y `-Dfile.encoding=UTF-8`.

## Funcionalidades y decisiones

- Se solicita nombre, dirección, teléfono y cantidad de pizzas.
- Se ofrece pizza Margarita a $8.000 CLP, con inventario inicial de 10 unidades. Una unidad representa el conjunto de ingredientes de una pizza; no se modelan ingredientes individuales.
- El subtotal es precio por cantidad. Se descuenta exactamente el 10 % cuando la cantidad es mayor que tres: 1 pizza cuesta $8.000, 3 cuestan $24.000, 4 cuestan $28.800 y 5 cuestan $36.000.
- Los importes calculados usan `Long` y pesos enteros. El descuento es `subtotal / 10`: con el precio elegido es exacto; si se cambia a un precio que produzca fracciones, se trunca el descuento hacia abajo al peso entero.
- Con stock suficiente se resta la cantidad, se confirma el pedido y se asigna `R01 - Repartidor de turno`. Con stock insuficiente se conserva el inventario y se muestra estado rechazado, descuento aplicado $0, total a pagar $0 y repartidor `Sin asignar`. El subtotal solicitado se informa para conservar el contexto del rechazo.
- El resumen en `Main.kt` contiene todos los datos del cliente, pizza y pedido, además del descuento y stock restante.
- `val` conserva los datos de entrada y resultados; `var` se utiliza exclusivamente para actualizar el inventario local al procesar el pedido.

## Manejo de entradas

Se eliminan espacios de los extremos. Nombre vacío o nulo utiliza `Cliente sin nombre`; dirección vacía o nula utiliza `Retiro en local`. El teléfono se conserva como `String`, admite un `+` inicial, dígitos ASCII, espacios, paréntesis y guiones, y requiere entre 8 y 15 dígitos. Si es inválido o nulo, se avisa y se utiliza `No informado`. Es una comprobación de formato básico, sin validación del número real ni de la disposición de separadores.

La cantidad se convierte con `toIntOrNull()`. Cero, negativos, fracciones, texto, vacío o desbordamiento se rechazan y se solicita otra entrada. Ante fin de entrada (`null`) se avisa y se utiliza 1, lo que permite terminar sin bucles infinitos. Los parámetros internos inválidos se detectan con `require`; la interfaz de consola evita enviarlos.

## Pruebas realizadas

Se ejecutaron `test` y después `clean build`: ambos finalizaron con `BUILD SUCCESSFUL`. El informe XML de la compilación limpia registra **19 pruebas, 0 fallos, 0 errores y 0 omitidas**. Se verificaron cantidades 1, 3, 4 y 5; stock exacto, sobrante, cero e insuficiente; entradas inválidas y nulas; conservación de prefijos telefónicos; asignación de reparto; cálculos grandes y redondeo; y resúmenes completos.

Se realizaron cuatro ejecuciones de consola: confirmación de 4 pizzas con acentos, rechazo de 11 pizzas, entradas inválidas seguidas de corrección y fin de entrada. Las salidas reales, el XML de pruebas y el entorno se conservan en `docs/evidencias/`. El informe de cumplimiento está en `docs/informe-semana-1.md` y el cierre en `docs/estado-final-semana-1.md`.

## Entrega y limitaciones

Se genera en la carpeta superior del proyecto un archivo ZIP (`PMY2201_Exp1_S1_FormaB_pizzeria-kotlin.zip`) que contiene fuentes, pruebas, documentación, configuración y Wrapper completo; excluye `.idea`, `.gradle`, `.kotlin`, `build`, `out` y cachés. Puede regenerarse mediante:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\Preparar-Entrega.ps1
```

Se atiende un pedido por ejecución. El identificador `PED-001`, el inventario y el repartidor se reinician al volver a ejecutar. No hay persistencia, catálogo, logística avanzada ni interfaces externas. Los datos predeterminados permiten una simulación académica; no acreditan un contacto real.
