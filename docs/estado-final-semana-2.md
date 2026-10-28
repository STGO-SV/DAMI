# Estado final de Semana 2

Fecha de verificación: 28 de octubre de 2026, zona America/Santiago.

La aplicación de consola implementa inventario individual, registro de múltiples pedidos, cálculo monetario con impuesto configurable, reportes y filtros, validación de contactos, herencia, encapsulación, manejo de errores y reparto simulado. La matriz de los siete criterios oficiales está en `informe-semana-2.md`.

## Resultados técnicos

| Comprobación | Resultado registrado |
| --- | --- |
| Línea base de Semana 1, `test --rerun-tasks` | 19 pruebas ejecutadas y aprobadas; 0 fallos, 0 errores, 0 omitidas |
| Semana 2, `gradlew.bat test` | BUILD SUCCESSFUL |
| Semana 2, `gradlew.bat clean build` | BUILD SUCCESSFUL; 8 tareas ejecutadas |
| Informes XML de la compilación limpia actual | 57 pruebas ejecutadas, 57 aprobadas; 0 fallos, 0 errores, 0 omitidas; 45 casos existentes adaptados y 12 nuevos |
| Distribución ejecutable, `installDist` | BUILD SUCCESSFUL |
| Sesión interactiva UTF-8 | Tres pedidos: Entregado, En preparación y Rechazado; inventario final 4/8/8/3 |
| Comprobaciones actuales de consola | Seis escenarios aprobados, 28 verificaciones de salida y seis códigos de término iguales a cero |
| Revisión de espacios en cambios | `git diff --check` sin errores |
| Conservación de la entrega anterior | El ZIP de Semana 1 coincide con su checksum original |

El entorno utilizado fue Kotlin/JVM 2.2.20, Amazon Corretto 21.0.12.1 y Gradle Wrapper 9.6.0. Se conserva el archivo de configuración de IntelliJ con JDK 21. El proyecto se abre como Gradle desde su carpeta raíz.

## Evidencias

Las evidencias actuales de la corrección están en `docs/evidencias/semana-2/recuperacion-entradas/`: `pruebas.txt`, `compilacion-limpia.txt`, los XML de `PedidoTest` (45 pruebas) y `RecuperacionEntradaTest` (12 pruebas), `distribucion-ejecutable.txt`, seis transcripciones de consola y `verificacion-consola.txt`. La selección inválida repite únicamente la pregunta y preserva cliente y productos anteriores; EOF cancela el alta sin efectos parciales. Se mantienen los cálculos, el inventario, los estados, el reparto y la validación de contactos.

En `docs/evidencias/semana-2/` se conservan los registros históricos de la versión anterior:

- `linea-base-semana-1.xml`: ejecución previa de 19 pruebas.
- `pruebas.txt`, `compilacion-limpia.txt` y `TEST-org.example.PedidoTest.xml`: comandos finales y resultados de 45 pruebas.
- `java.txt`: versión de Amazon Corretto utilizada.
- `consola-interactiva-utf8.txt`: sesión de consola con tres pedidos, preparación, envío, entrega, rechazo y recuperación ante entradas inválidas.
- `consola-lote-mixto.txt`, `consola-reabastecimiento.txt`, `consola-fin-entrada.txt`, `consola-tasa-cero.txt`, `consola-tasa-invalida.txt` y `verificacion-consola.txt`: comprobaciones reproducibles del programa ejecutable.
- `entrega-semana-1-conservada.txt`: referencia SHA-256 de la entrega anterior.
- `pruebas-desde-zip.txt`: compilación y ejecución de las 45 pruebas desde una extracción independiente del ZIP.

La primera prueba de entrada con acentos mostró incompatibilidad entre la codificación de la terminal y UTF-8. La sesión se repitió configurando entrada y salida de PowerShell en UTF-8 y se verificaron José y Ñuñoa sin alteraciones. El README incorpora esa configuración como requisito de ejecución en consola.

## Empaquetado

El script `tools/Preparar-Entrega-Semana2.ps1` genera `PMY2201_Exp1_S2_pizzeria-kotlin.zip` en la carpeta superior del repositorio. Comprueba los archivos obligatorios, las exclusiones y la coincidencia de cada entrada con su origen mediante SHA-256. Junto al ZIP se escriben un informe `.validacion.txt` y un archivo `.sha256`, con el resultado verificable del paquete correspondiente.

El ZIP incluye fuentes, pruebas, documentación, evidencias, scripts y Gradle Wrapper completo. Se excluyen Git, cachés, compilaciones y configuración local del IDE. Se conserva la entrega de Semana 1.

El paquete existente corresponde a la versión actual del proyecto.

## Alcance y comprobaciones independientes

La simulación es secuencial y en memoria, sin datos persistentes. Los ingredientes se expresan en porciones abstractas; las distancias son fijas desde el local. Los contactos se validan sintácticamente y las notificaciones son locales. El impuesto del 19 % es un supuesto configurable, con descuento truncado e impuesto redondeado al peso.

El protocolo `pruebas-manuales-semana-2.md` permite repetir las operaciones en IntelliJ y verificar otros escenarios. Las pruebas automatizadas también comprueban ausencia de repartidores, límites monetarios, protección de colecciones y transiciones rechazadas. Los escenarios recomendados no se confunden con ejecuciones manuales realizadas.
