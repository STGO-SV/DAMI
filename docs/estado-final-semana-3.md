# Estado técnico — Semana 3, Forma B

Consolidación documental del 8 de noviembre de 2026 para las fuentes locales de la rama `semana-3`. La última verificación automatizada registrada es del 7 de noviembre de 2026, en la zona America/Santiago. [Matriz de trazabilidad](informe-semana-3.md).

## Verificación automatizada

| Fecha y versión examinada | Pruebas aprobadas | Escenarios de consola | Comprobaciones de salida | Evidencia |
| --- | ---: | ---: | ---: | --- |
| 3 de noviembre de 2026, implementación inicial de Semana 3 | 97 | 12 | 88 | [Consola inicial](evidencias/semana-3/verificacion-consola.txt) |
| 6 de noviembre de 2026, corrección S/N | 112 | 15 | 111 | [Informe de la corrección](retroalimentacion-semana-2.md) · [Consola de la corrección](evidencias/semana-3/retroalimentacion-semana-2/verificacion-consola.txt) |
| 7 de noviembre de 2026, mejoras de usabilidad | **124** | **23** | **185** | [Resultados de pruebas](evidencias/semana-3/usabilidad/resultados-pruebas.txt) · [Consola](evidencias/semana-3/usabilidad/verificacion-consola.txt) |

En la ejecución del 7 de noviembre se registraron cero fallos, errores u omisiones. `gradlew.bat clean build --console=plain` terminó con `BUILD SUCCESSFUL` y ocho tareas ejecutadas. La distribución local se preparó con `installDist` y los 23 escenarios terminaron con código 0. [Compilación limpia](evidencias/semana-3/usabilidad/compilacion-limpia.txt) · [Distribución local](evidencias/semana-3/usabilidad/distribucion.txt).

Los casos de promoción usan fechas fijas de pedido para comprobar vigencia, extremos inclusivos, elegibilidad, prioridad del mayor descuento e impuesto sobre la base descontada. Esas fechas forman parte de los datos de entrada. [Resultados por fecha](mejoras-usabilidad-semana-3.md#auditoría-automatizada-de-promociones).

## Auditoría manual independiente

La auditoría funcional independiente informó siete grupos aprobados, realizados en distintas etapas de Semana 3. Los dos primeros se informaron antes de las mejoras de usabilidad. Las fechas y las compilaciones individuales quedan sin consignar cuando no constan. Estos resultados se registran por grupo, separados de la compilación y de los recuentos automatizados.

| Grupo | Resultado | Protocolo para repetirlo |
| --- | --- | --- |
| 1. Validación S/N y pedido mixto | Aprobado | [Respuestas S/N y pedido mixto](pruebas-manuales-semana-3.md#9-respuestas-sn-y-pedido-mixto) |
| 2. Cancelación por EOF | Aprobado | [EOF y ausencia de efectos](pruebas-manuales-semana-3.md#7-eof-y-ausencia-de-efectos) |
| 3. Normalización telefónica chilena y disponibilidad dinámica | Aprobado | [Teléfono chileno y capacidad](pruebas-manuales-semana-3.md#teléfono-chileno-y-capacidad-dinámica) |
| 4. Producto agotado y cancelación con C | Aprobado | [Agotamiento y cancelación con C](pruebas-manuales-semana-3.md#agotamiento-y-cancelación-con-c) |
| 5. Estados del pedido y asignación de repartidor | Aprobado | [Inventario, estados y reparto](pruebas-manuales-semana-3.md#4-inventario-estados-y-reparto) |
| 6. Identidad de clientes y prevención de duplicados | Aprobado | [Duplicados y contacto por pedido](pruebas-manuales-semana-3.md#6-duplicados-y-contacto-por-pedido) |
| 7. Demostración integrada de Semana 3 | Aprobado | [Demostración integrada](pruebas-manuales-semana-3.md#1-demostración-integrada) |

En el primer grupo se registraron dos Margarita y dos Pepperoni. El subtotal fue $34.000, el descuento $3.400, el impuesto $5.814 y el total $36.414. El inventario quedó en Masa 6, Tomate 12, Queso 12 y Pepperoni 3. En el segundo grupo se cerró la entrada después de varias respuestas S/N inválidas y el pedido incompleto se canceló.

## Estado del paquete

El 8 de noviembre de 2026, en la zona America/Santiago, se generó `PMY2201_Exp1_S3_FormaB_pizzeria-kotlin.zip` en la carpeta superior del proyecto. Las 182 entradas se comprobaron por SHA-256 contra los archivos originales. El paquete incluye el Wrapper completo, las fuentes y pruebas, la documentación consolidada, los informes históricos y las evidencias.

La extracción independiente aprobó 124 pruebas, con cero fallos, errores u omisiones. `clean build --console=plain` e `installDist --console=plain` finalizaron con `BUILD SUCCESSFUL`. La batería de consola aprobó 23 escenarios y 185 comprobaciones. Esta ejecución corresponde al paquete del 8 de noviembre y se registra por separado de la auditoría manual y de las verificaciones históricas.

El SHA-256 del ZIP completo se conserva en `PMY2201_Exp1_S3_FormaB_pizzeria-kotlin.zip.sha256`. Junto al paquete se guardan el manifiesto por entrada y los registros de compilación y consola. El registro externo `.zip.anterior.txt` identifica la versión del 3 de noviembre. La documentación consolidada se incluyó antes de generar y verificar el archivo de entrega. [Script de entrega](../tools/Preparar-Entrega-Semana3.ps1).
