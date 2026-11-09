# Pizzería Kotlin — Semana 3, Forma B

Aplicación de consola para la actividad «Creando soluciones con Kotlin» de PMY2201. Permite registrar pedidos, controlar inventario por ingrediente y coordinar preparación y reparto simulado.

## Ejecución

Se requiere Amazon Corretto JDK 21. El Gradle Wrapper descarga Gradle y las dependencias cuando no están disponibles en caché. El proyecto utiliza Kotlin/JVM 2.2.20 y Gradle 9.6.0.

Ejecutar desde la carpeta extraída en PowerShell.

```powershell
[Console]::InputEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
.\gradlew.bat run --console=plain
```

En IntelliJ IDEA se abre la carpeta como proyecto Gradle con Corretto 21. Se puede ejecutar la tarea `run` o `org.example.MainKt`. En la configuración directa, agregar `-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8` en **VM options** y los parámetros de la aplicación en **Program arguments**.

La opción 8 muestra la demostración integrada. Estos comandos permiten ejecutar la demostración y configurar una promoción de ejemplo.

```powershell
.\gradlew.bat run --console=plain --args=--demo
.\gradlew.bat run --console=plain '--args=--fecha=2026-11-03 --promocion=2026-11-01,2026-11-07,0.15'
```

La tasa de impuesto es didáctica. Su valor predeterminado es `0.19` y se puede pasar un decimal entre 0 y 1 como argumento posicional. `--fecha=AAAA-MM-DD` fija la fecha del pedido. El valor predeterminado usa `America/Santiago`. `--promocion=inicio,fin,fracción` configura un período inclusivo y una fracción de 0 a 1. `--demo` usa datos fijos propios.

## Uso del menú

La opción 1 registra un pedido y la 2 muestra el reporte. Las opciones 3, 4 y 5 permiten marcar Listo, enviar y entregar mediante el identificador `PED-001` u otro del reporte. Las opciones 6 y 7 consultan y reabastecen inventario. La opción 0 finaliza con el reporte de jornada.

Para agregar otra pizza se acepta `S` o `N` en ambas capitalizaciones. El selector admite `C` para cancelar. EOF cancela el alta desde cualquier campo. Una línea vacía activa los valores predeterminados en nombre, dirección, teléfono, selección de pizza y notas. El correo, la cantidad y la confirmación S/N se vuelven a solicitar si están vacíos.

El teléfono chileno se presenta como `(+56) X XXXX XXXX`. Las entradas internacionales compatibles utilizan `+XX (XXX) XXX-XXXX`. La dirección se recibe como texto libre y el reparto usa distancias simuladas. El catálogo contiene Margarita y Pepperoni. La cantidad admite `1..100` y la consola informa el máximo según la receta y las líneas del borrador. Los ingredientes se consumen al registrar el pedido.

## Comprobación

```powershell
.\gradlew.bat test --console=plain
.\gradlew.bat clean build --console=plain
.\gradlew.bat installDist --console=plain
```

Los resultados de pruebas se generan en `build/reports/tests/test/index.html` y `build/test-results/test/`. La distribución ejecutable queda en `build/install/pizzeria-kotlin/`. [Criterios, decisiones y casos reproducibles](docs/informe-semana-3.md).
