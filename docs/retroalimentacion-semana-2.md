# Retroalimentación de Semana 2 — Respuestas S/N

Fecha de verificación: 6 de noviembre de 2026, zona America/Santiago.

## Observación y comportamiento detectado

Se revisó la observación relativa a preguntas afirmativas o negativas en las que una respuesta distinta de `S` podía interpretarse automáticamente como negativa.

La búsqueda en las fuentes de producción identificó una única pregunta de este tipo: agregar otra pizza durante el registro. La implementación anterior evaluaba `respuesta.trim().lowercase() == "s"`. Por tanto, `quizás`, `no`, `abc`, números y cadenas vacías terminaban la selección como si se hubiera respondido `N`. EOF ya disponía de una ruta de cancelación controlada y no requería transformarse en respuesta negativa.

El defecto se reprodujo antes de corregir la implementación. La nueva prueba `respuestaInvalidaNoTerminaLaSeleccionDeProductos` esperaba Margarita y Pepperoni tras corregir una respuesta inválida con `s`, pero obtenía únicamente Margarita. En esa ejecución se procesaron 98 casos: los 97 anteriores aprobaron y la nueva reproducción falló. Se conservan el registro y XML de esa ejecución, identificados como reproducción del defecto, no como resultado final.

## Corrección aplicada

`Entrada.kt` incorpora `leerSiNo(pregunta, leer, mostrar): Boolean?`. La función recorta espacios exteriores y admite ambas capitalizaciones en un único lugar. Acepta exclusivamente las letras ASCII `S`/`s` y `N`/`n`, devuelve `true` o `false` respectivamente y devuelve `null` ante EOF. Las demás entradas muestran `Respuesta inválida. Ingrese S o N.` y vuelven a formular la misma pregunta. No se utilizan excepciones para controlar respuestas incorrectas.

`Main.kt` utiliza la función en la pregunta de agregar otra pizza. Un resultado nulo se integra con `cancelarRegistro`, antes de registrar el pedido o consumir inventario. Los reintentos conservan el cliente y todas las líneas ya incorporadas. No se modificaron los cálculos monetarios, inventario, reparto, promociones ni igualdad.

| Entrada | Resultado |
| --- | --- |
| `S`, `s`, ` S ` | Agregar otra pizza |
| `N`, `n`, ` n ` | Finalizar selección y solicitar notas |
| Vacío, solo espacios, `sí`, `no`, `abc`, `0`, `1` | Error y repetición local |
| EOF, incluso después de errores | Cancelación del registro sin efectos parciales |

## Archivos modificados

- `src/main/kotlin/org/example/Entrada.kt`: lector reutilizable S/N.
- `src/main/kotlin/org/example/Main.kt`: integración y cancelación existente.
- `src/test/kotlin/org/example/ConfirmacionSNTest.kt`: 15 nuevas pruebas.
- `src/test/kotlin/org/example/RecuperacionEntradaTest.kt`: una entrada ajustada a `n` explícita en el caso de valores predeterminados. Se conservan sus comprobaciones de cliente, teléfono, correo, pizza, notas y cantidad; la cadena vacía ya no es una respuesta negativa válida.
- `tools/Verificar-Consola-Semana3.ps1`: tres escenarios adicionales; las evidencias actuales se guardan en una subcarpeta separada.
- `README.md`, `docs/informe-semana-3.md`, `docs/estado-final-semana-3.md` y `docs/pruebas-manuales-semana-3.md`: contrato de entrada, resultados actuales y distinción del paquete anterior.
- `docs/retroalimentacion-semana-2.md` y `docs/evidencias/semana-3/retroalimentacion-semana-2/`: trazabilidad y evidencias.

## Cobertura y resultados

| Grupo de comprobaciones nuevas | Evidencia automatizada |
| --- | --- |
| `S`/`s`, `N`/`n` y espacios exteriores | Tres pruebas de lectura válida y normalización |
| Vacío y solo espacios | Dos pruebas que verifican la repetición exacta de la pregunta |
| Texto arbitrario seguido de `S` o `N` | Dos pruebas que verifican el resultado y el mensaje de error |
| Varias entradas inválidas, incluidos `sí`, `no`, `0` y `1` | Una prueba de reintentos consecutivos |
| EOF inmediato y posterior a errores | Dos pruebas que comprueban resultado nulo y acotan el número de lecturas |
| Conservación del cliente y de productos anteriores | Reproducción corregida con dos productos, notas, todos los datos de contacto e importes |
| Corrección con `N` sin perder primera línea ni notas | Una prueba de integración de negativa corregida |
| Cancelación sin consumo ni nuevas reservas | Una prueba con cuatro cortes y una jornada previa con repartidor ocupado |
| Ausencia de efectos durante los reintentos | Una prueba que inspecciona la jornada antes de cada entrada |
| Regresión de pedido mixto y reparto completo | Una prueba de dos productos hasta Entregado, con importes y liberación del repartidor |

La compilación `.\gradlew.bat clean build --console=plain` terminó con `BUILD SUCCESSFUL`, ocho tareas ejecutadas y **112 pruebas aprobadas**, sin fallos, errores ni omisiones: los 97 casos anteriores conservados y 15 casos nuevos. `installDist` terminó con `BUILD SUCCESSFUL`. El registro de compilación conserva el detalle y la duración real de la ejecución final.

La batería de consola real aprobó **15 escenarios y 111 comprobaciones de salida**, todos con código de término 0. Incluye los doce escenarios anteriores y tres nuevos: varios errores antes de `s` y `N`, corrección con negativa y EOF tras errores conservando un pedido previo En camino. El pedido mixto corregido mantiene subtotal $34.000, descuento $3.400, impuesto $5.814 y total $36.414; el inventario final es Masa 6, Tomate 12, Queso 12 y Pepperoni 3.

Las evidencias se encuentran en `docs/evidencias/semana-3/retroalimentacion-semana-2/`: reproducción fallida previa, compilación limpia final, ocho XML finales, totales, distribución ejecutable, transcripciones de consola y verificación agregada. Las evidencias originales de Semana 3 permanecen conservadas. Las huellas de los archivos de dominio ajenos a la corrección y del ZIP se comparan antes y después; `git diff --check` no presenta errores.

## Comprobación manual en IntelliJ IDEA

Abrir el proyecto Gradle con Amazon Corretto 21 y UTF-8. Ejecutar `org.example.MainKt` sin argumentos o con `--fecha=2026-11-03`. Introducir una línea por respuesta; `[vacío]` indica presionar Enter sin escribir texto:

```text
1
José
Ñuñoa 123
+56901234567
jose@example.com
1
2
quizás
2
s
2
2
[vacío]
no
0
N
sin sal
6
2
0
```

Se esperan cinco mensajes de respuesta inválida. Cada uno debe ir seguido de la misma pregunta S/N, sin solicitar nuevamente datos del cliente. El resumen debe mostrar dos Margarita y dos Pepperoni, notas `sin sal`, total $36.414 y el inventario indicado. Una respuesta incorrecta no debe abrir la pregunta de notas ni terminar anticipadamente la selección.

Para comprobar cancelación, repetir el registro y cerrar la entrada estándar después de un error S/N, tanto tras la primera línea como tras la segunda. No debe registrarse el pedido incompleto. Las pruebas automatizadas verifican además la conservación exacta de inventario, pedidos, clientes y repartidores de una jornada previa.

Para repetir todas las comprobaciones automáticas:

```powershell
.\gradlew.bat clean build --console=plain
.\gradlew.bat installDist --console=plain
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\Verificar-Consola-Semana3.ps1
```

El ZIP de Semana 3 se conserva sin regeneración y corresponde a la versión anterior a esta corrección; la revisión actual se realiza sobre las fuentes locales.

## Posibles mejoras futuras

La asignación geográfica de repartidores con ubicaciones y distancias reales podría sustituir la simulación actual en una ampliación futura. También podrían realizarse pruebas de usabilidad para evaluar claridad de preguntas, mensajes y recuperación de errores. Ambas recomendaciones se registran como trabajo futuro y no se consideran implementadas ni verificadas en esta corrección.
