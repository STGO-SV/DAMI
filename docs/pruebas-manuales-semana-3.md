# Comprobación manual independiente — Semana 3

Los pasos describen cómo repetir las comprobaciones sobre las fuentes locales. Las aprobaciones obtenidas en distintas etapas se registran por grupo. [Resultados de la auditoría manual independiente](estado-final-semana-3.md#auditoría-manual-independiente).

## Preparación en IntelliJ IDEA

Abrir `pizzeria-kotlin` como proyecto Gradle. Seleccionar Amazon Corretto JDK 21 y codificación UTF-8. Sincronizar Gradle y ejecutar `clean build`. Se esperan 124 pruebas aprobadas. Crear o utilizar la configuración Kotlin de `org.example.MainKt` con las opciones JVM `-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8`. Escribir los argumentos indicados abajo en Program arguments.

Cada escenario comienza en una ejecución nueva, salvo indicación explícita. Introducir una respuesta por línea. `[vacío]` significa presionar Enter con la línea vacía.

La combinación para enviar EOF depende de la consola. Durante la auditoría manual en IntelliJ IDEA se utilizó `Ctrl+D` satisfactoriamente. En determinadas terminales Windows puede utilizarse `Ctrl+Z` al inicio de una línea, seguido de Enter. La prueba requiere que la aplicación reconozca EOF, ejecute la cancelación controlada y alcance el reporte final. Finalizar el proceso con el botón Stop no equivale a comprobar EOF.

## 1. Demostración integrada

Ejecutar MainKt con `--demo`, sin entrada adicional. Alternativamente iniciar sin argumentos e ingresar `8`, `6`, `0`.

Se esperan las validaciones de correo, `+12 (345) 678-9012`, `(+56) 9 1234 5678` y cantidades 0/101 inválidas y 1/100 válidas para el rango. Se muestran las anotaciones de Margarita y Pepperoni, un producto de referencia no promocionable y la inspección de cuatro clases con propiedades y métodos ordenados. `SIM(DEMO-A+DEMO-B)` incluye dos Margarita y dos Pepperoni, con subtotal $34.000, descuento $3.400, impuesto $5.814 y total $36.414.

La promoción de 15 % está activa el 3 de noviembre y no vigente el 8 de noviembre de 2026. El primer total es $34.391 y el segundo $36.414. Se muestran los componentes de Cliente y Pedido, `Clientes ==: true`, `Productos ==: true` y registros con una ficha y duplicado no agregado. Si se ejecuta desde el menú, la opción 6 debe conservar el inventario inicial 10/20/20/5 y el reporte final debe permanecer sin pedidos.

## 2. Recuperación local y teléfono normalizado

Argumentos: `--fecha=2026-11-03`.

```text
1
José
Ñuñoa 123
+12 345 678 9012
mal
[vacío]
cliente@example.cl
abc
1
101
0
texto
1
n
[vacío]
0
```

Se esperan dos errores de correo y tres de cantidad. La selección `abc` repite solo la pizza. El resumen muestra José, Ñuñoa 123, el correo corregido, teléfono `+12 (345) 678-9012`, una Margarita y total $9.520. Se mantienen cinco secciones, notas `Sin notas` y estado En preparación. Con stock inicial, ingresar 100 pasa la validación de rango y repite la pregunta por superar el máximo de 10 Margarita.

## 3. Promoción activa e inactiva

Argumentos de la primera ejecución: `--fecha=2026-11-03 --promocion=2026-11-01,2026-11-07,0.15`.

```text
1
José
Centro
+56912345678
cliente@example.com
1
2
s
2
2
n
[vacío]
6
0
```

Se espera `(+56) 9 1234 5678` en el resumen. Para un subtotal de $34.000, se aplica descuento promocional de $5.100 y descuento por cantidad de $0. La base es $28.900, el impuesto $5.491 y el total $34.391. El inventario queda en 6 masas, 12 tomates, 12 quesos y 3 unidades de Pepperoni.

Repetir las mismas entradas con fecha `2026-11-08`: promoción no vigente, descuento por cantidad $3.400, promoción $0, impuesto $5.814 y total $36.414. Repetir con fechas `2026-11-01` y `2026-11-07`: ambos extremos mantienen la promoción activa. Con `2026-10-31` permanece inactiva.

## 4. Inventario, estados y reparto

Sin promoción, registrar cuatro Margarita con notas `sin sal`: subtotal $32.000, descuento $3.200, impuesto $5.472 y total $34.272. Después ingresar:

```text
3
PED-001
4
PED-001
5
PED-001
2
6
0
```

Los resúmenes deben mostrar Listo, En camino con R02 - Diego, y Entregado con ese repartidor conservado en el historial. El reporte debe mostrar un completado y cero pendientes; el inventario queda 6/12/12/5. Intentar enviar antes de marcar Listo debe informar transición inválida sin cambiar inventario.

## 5. Capacidad en pedidos mixtos y agotamiento

### Teléfono chileno y capacidad dinámica

Registrar un cliente con el teléfono `956781234` y seleccionar ocho Margarita. Responder `s` y elegir Pepperoni. Debe mostrarse un máximo de dos. Ingresar `3` y corregir a `2` en la misma pregunta de cantidad. Completar con `n` y las notas. El resumen debe mostrar `(+56) 9 5678 1234` y las dos líneas. El inventario queda en 0/0/0/3.

### Agotamiento y cancelación con C

En una sesión nueva, elegir cinco Pepperoni, responder `s` y volver a seleccionar Pepperoni. El selector debe indicar agotamiento y permitir elegir Margarita o cancelar con `C`. Ingresar `C` y consultar la opción 6 en el menú principal. Debe aparecer el inventario inicial 10/20/20/5 y el reporte debe quedar sin pedidos. La prueba automatizada de rechazo final simula un cambio de inventario entre selección y registro.

## 6. Duplicados y contacto por pedido

En una misma ejecución registrar una Margarita para Ana, correo `ana@example.com`, dirección Centro y teléfono `12345678`. Registrar después una Pepperoni para `ANA`, correo `ANA@example.com`, dirección Otra dirección y teléfono `87654321`.

Se espera el aviso de cliente ya registrado y dos pedidos diferentes. El segundo resumen debe conservar Otra dirección y su teléfono. El inventario final es 8/16/16/4. La primera ficha del registro no se reemplaza; la prueba de jornada y la demostración verifican que solo queda una identidad de cliente. Las pruebas de HashSet verifican también nombres/categorías de productos con capitalización diferente y precio ajeno a la identidad.

## 7. EOF y ausencia de efectos

Ingresar `1`, `Ana`, `Centro`, `12345678`, `mal` y cerrar la entrada durante el reintento del correo. Se espera aviso de cancelación y reporte sin pedidos. Repetir cerrando la entrada al seleccionar pizza, al pedir cantidad y después de incorporar una primera línea cuando se solicita la segunda. Las pruebas `RecuperacionEntradaTest` comprueban todos los cortes del registro y conservación de una jornada previa con repartidor ocupado.

Para reproducir el corte durante una confirmación S/N, iniciar un pedido válido, elegir una pizza e ingresar `quizás` y `2` al preguntar si se agrega otra. Cerrar la entrada mientras se repite la pregunta. Deben mostrarse el aviso de cancelación y el reporte final sin el pedido incompleto.

## 8. Configuración inválida y reabastecimiento

Con `--fecha=2026-02-30` se espera `Fecha inválida` y no aparece el menú. Con `--promocion=2026-11-07,2026-11-01,0.15` o porcentaje 2 se espera `Configuración inválida`.

Con argumentos `0 --fecha=2026-11-03`, usar la opción 7 para agregar Masa 1, Tomate 2 y Queso 2. Registrar once Margarita: total $79.200, impuesto $0 e inventario 0/0/0/5. Un reabastecimiento negativo debe rechazarse sin cambios.

## 9. Respuestas S/N y pedido mixto

Iniciar un pedido sin promoción y seleccionar dos Margarita. En la pregunta de agregar otra pizza, ingresar `quizás`, `2` y `s`. Las dos primeras entradas deben mostrar `Respuesta inválida. Ingrese S o N.` y repetir la pregunta. Después de agregar dos Pepperoni, ingresar una línea vacía, `no`, `0` y `N`. Se esperan tres errores y luego la pregunta de notas. El pedido debe conservar ambas líneas y totalizar $36.414. También se admiten ` S ` y ` n ` con espacios exteriores. [Escenario completo de la corrección S/N](retroalimentacion-semana-2.md).

## 10. Repetición de la verificación automatizada

```powershell
.\gradlew.bat clean build --console=plain
.\gradlew.bat installDist --console=plain
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\Verificar-Consola-Semana3.ps1
```

Ejecutar desde la raíz del proyecto. El script utiliza la distribución preparada por `installDist` y escribe las salidas en `docs/evidencias/semana-3/usabilidad/` por defecto. Para conservar la evidencia registrada al repetir la batería, se puede añadir `-DirectorioEvidencias` con una carpeta distinta. [Resultados automatizados de referencia](estado-final-semana-3.md#verificación-automatizada) · [Estado del paquete](estado-final-semana-3.md#estado-del-paquete).
