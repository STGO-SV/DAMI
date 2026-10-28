# Pruebas manuales de Semana 2

Este protocolo permite comprobar la aplicación en IntelliJ IDEA o mediante `gradlew.bat run`. Cada escenario independiente comienza con una nueva ejecución, salvo indicación de continuar en la misma sesión. La terminal debe usar UTF-8, según las instrucciones del README.

## Sesión con varios pedidos

1. Elegir 1. Ingresar José, Ñuñoa 123, +56901234567 y jose@example.com. Elegir Margarita, cantidad 4, responder n y dejar las notas vacías.
2. Comprobar subtotal $32.000, descuento $3.200, impuesto $5.472, total $34.272, estado En preparación, Sin notas y stock Masa 6, Tomate 12, Queso 12, Pepperoni 5.
3. Elegir 3 e indicar PED-001: el estado cambia a Listo. Elegir 4 e indicar PED-001: Diego, a 500 m, recibe el pedido y pasa a En camino. Elegir 5 e indicar PED-001: pasa a Entregado y libera al repartidor.
4. Crear otro pedido de Ana, dirección Centro, teléfono 12345678, correo ana@example.cl, Pepperoni, cantidad 2 y notas sin sal. El total es $21.420 y el stock queda Masa 4, Tomate 8, Queso 8, Pepperoni 3.
5. Crear un tercer pedido de Luis con teléfono abc y correo mal, Margarita, cantidad 11. Antes de aceptar 11, introducir 0, -2 y texto: deben solicitarse nuevas cantidades. El pedido se rechaza; se informa Masa disponible 4/requerido 11, Tomate 8/22 y Queso 8/22. El inventario se conserva, el total es cero y los contactos usan predeterminados.
6. Intentar reabastecer Masa con -1: se muestra un error y continúa la sesión. Intentar marcar PED-003 como Listo: se rechaza la transición.
7. Consultar inventario y reporte. Deben aparecer tres pedidos: uno completado, uno pendiente y uno rechazado. Salir con 0.

Esta sesión se ejecutó realmente en consola; la transcripción se conserva en `evidencias/semana-2/consola-interactiva-utf8.txt`. Las secuencias de control de la terminal se quitaron de la transcripción; sus cortes de línea pueden reflejar el ancho de la ventana.

## Escenarios complementarios reproducibles

| Escenario | Procedimiento | Resultado esperado |
| --- | --- | --- |
| Lote mixto | Pedir 2 Margarita y añadir 2 Pepperoni antes de terminar | Subtotal $34.000, descuento $3.400, impuesto $5.814, total $36.414; stock 6/12/12/3 |
| Reabastecimiento | En una sesión nueva pedir 11 Margarita; registrar Masa +1, Tomate +2, Queso +2; crear nuevamente el pedido | Primer pedido rechazado; segundo aceptado, total $94.248 y stock 0/0/0/5 |
| Stock exacto | Pedir 10 Margarita | Aceptación y stock de Masa, Tomate y Queso igual a cero |
| Límites de descuento | En sesiones nuevas pedir 3 y luego 4 Margarita | 3 sin descuento; 4 con 10 % antes del impuesto |
| Repartidores ocupados | Crear y marcar Listo cuatro pedidos de una pizza; enviar tres sin entregar | Asignación Diego, Camila, Paula; el cuarto sigue Listo sin repartidor |
| Transiciones inválidas | Intentar enviar En preparación; repetir una entrega; intentar entregar con un repartidor diferente en prueba de dominio | Mensaje de error y estado conservado |
| Notificación condicional | Comparar correo válido e inválido | Solo el válido produce la notificación local simulada |
| Selección inválida | Ingresar sdfjn, -1 y después 2 al seleccionar pizza | Se repite solo la selección y se conserva el cliente; se selecciona Pepperoni |
| Segunda selección inválida | Incorporar 2 Margarita, añadir otra pizza, ingresar abc, -1 y finalmente 2; pedir 2 Pepperoni | Se conservan ambas líneas; total $36.414 y stock 6/12/12/3 |
| Cantidades inválidas | Seleccionar Pepperoni; ingresar 0, -2, texto, 2147483648 y finalmente 3 | Se repite solo la cantidad; se registra una línea de 3 Pepperoni |
| Entrada finalizada | Finalizar la entrada en el menú o durante cualquier campo del alta, incluso tras una línea válida | El menú sale; el alta se cancela sin pedidos parciales ni cambios de inventario o reparto, sin bucle infinito |
| Línea vacía | Enviar líneas vacías en cliente, dirección, contactos, selección y notas; ingresar una cantidad válida | Se conservan los predeterminados, incluida Margarita y Sin notas; una cantidad vacía se solicita nuevamente |
| Tasa configurable | Ejecutar con `--args=0` y pedir 4 Margarita | Descuento $3.200, impuesto cero y total histórico $28.800 |
| Tasa inválida | Ejecutar con `--args=-1` | Mensaje explicativo y cierre normal sin iniciar pedidos |

Los escenarios de lote mixto, reabastecimiento, fin de entrada y tasas se pueden repetir automáticamente contra el programa compilado, sin modificar la lógica:

```powershell
.\gradlew.bat installDist
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\Verificar-Consola-Semana2.ps1
```

El script verifica las salidas y códigos de término y escribe las evidencias actuales en `docs/evidencias/semana-2/recuperacion-entradas/`. También comprueba la recuperación ante selecciones y cantidades inválidas en un pedido mixto. Las evidencias anteriores se conservan como registro de la versión previa. Los escenarios indicados como protocolo no implican una ejecución manual adicional; los resultados ejecutados están registrados en el estado final.

## Verificación manual adicional

El 28 de octubre de 2026 se cerificó desde IntelliJ IDEA la recuperación ante selecciones y cantidades inválidas durante el registro de un pedido.

Se introdujeron identificadores de pizza inexistentes y cantidades incorrectas antes de proporcionar valores válidos. La aplicación permitió corregir cada entrada sin reiniciar el registro ni perder la información previamente capturada.

La operación terminó con el pedido esperado, conservando los datos del cliente y aplicando correctamente los cálculos correspondientes.
