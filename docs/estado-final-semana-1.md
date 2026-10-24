# Estado final — PMY2201, Experiencia 1, Semana 1, Forma B

Proyecto: `C:\Dev\Duoc\DAMI\pizzeria-kotlin`.

- Implementado: captura del cliente, pizza Margarita a $8.000, inventario inicial de 10, descuento del 10 % sobre más de tres pizzas, validaciones, actualización de stock, reparto y resumen completo.
- Verificado: `gradlew.bat test` y `gradlew.bat clean build` finalizaron con `BUILD SUCCESSFUL`. La compilación limpia ejecutó ocho tareas; 19 pruebas, 0 fallos, 0 errores y 0 omitidas.
- Verificado: cuatro ejecuciones de consola (confirmación, rechazo, entradas inválidas y fin de entrada), incluida salida con caracteres acentuados.
- Guardado: README, informe de trece apartados, matriz de los siete criterios y evidencias en `docs/evidencias/`.
- Entorno final: Corretto 21.0.12.1, Kotlin/JVM 2.2.20, Wrapper 9.6.0, Windows 11. Se seleccionó Java solo en la sesión; no se cambió configuración global.
- Empaquetado: generado y validado `C:\Dev\Duoc\DAMI\PMY2201_Exp1_S1_FormaB_pizzeria-kotlin.zip`, con 24 archivos y Wrapper completo. Se comparó el contenido de cada entrada con su original mediante SHA-256; no se incluyeron cachés ni carpetas de salida. Tras guardar este cierre se regenera y valida la copia definitiva. El resultado definitivo y hash se conservan en los archivos `.zip.validacion.txt` y `.zip.sha256` junto al ZIP.
- Procesos: las compilaciones y pruebas finalizaron; los dos daemons Gradle se detuvieron antes del empaquetado.
- Contraste académico: se verificó la correspondencia de la implementación con las instrucciones específicas de la Forma B y los siete criterios de la pauta de evaluación formativa, sin identificar incumplimientos funcionales.
- Validación manual: cuatro comprobaciones independientes aprobadas del 24 de octubre de 2026, correspondientes a descuento, rechazo por inventario insuficiente, límite de descuento y recuperación ante entradas inválidas.
- No aplicable: Android, persistencia, APIs, rutas y autenticación. El stock, identificador y repartidor corresponden a una simulación de un pedido por ejecución.

Evidencias principales: `compilacion-limpia.txt`, `TEST-org.example.PedidoTest.xml`, `ejecucion-confirmada.txt`, `ejecucion-rechazada.txt`, `ejecucion-entradas-invalidas.txt`, `ejecucion-fin-entrada.txt`, `entorno-gradle.txt` y `jdks-detectados.txt`, todos en `docs/evidencias/`.
