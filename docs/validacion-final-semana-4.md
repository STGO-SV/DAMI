# PizzeriaApp · Validación final de Semana 4, Forma B

## Entorno y línea base

Rama `semana-4`, referencia `64ae2f5`. El proyecto configura Gradle 9.6.0, AGP 9.5.1, JVM 25 para el daemon y bytecode Java 11. El módulo Android declara SDK mínimo 23, SDK de compilación y objetivo 37, y core library desugaring con `desugar_jdk_libs:2.1.5`.

| Verificación | Evidencia examinada |
|---|---|
| Debug | Se constató la presencia de `app-debug.apk` de 13.003.510 bytes y del APK de instrumentación. La compilación debug satisfactoria corresponde a la línea base anterior. |
| Pruebas locales | Los diez XML de Gradle registran 88 aprobadas, con cero fallos, errores u omisiones. Incluyen 77 casos recuperados, siete de dominio, tres del cotizador y uno de plantilla. |
| Instrumentación | El XML de Pixel 6 API 35 registra tres aprobadas, con cero fallos, errores u omisiones. Incluye dos pruebas de Compose y una de contexto. |
| Dispositivo en D2 | ADB identificó `Pixel_6_API_35`, con API 35. Los perfiles e imágenes disponibles corresponden a API 35 y 37. |

En D2 se revisaron los XML y los APK existentes. Las cifras corresponden a su ejecución previa.

## Compatibilidad pendiente

**API 23.** La configuración de desugaring y el APK debug existente acreditan la configuración de compilación. La ejecución en API 23 queda pendiente por falta de un dispositivo o imagen disponible. Al disponer de uno, comprobar en Android:

- `LocalDate`, `Clock.fixed` y `ZoneId.of("America/Santiago")` con una fecha cercana al cambio de día.
- `Math.addExact` y `Math.multiplyExact` con valores ordinarios y con desbordamiento, comprobando `ArithmeticException` y la conservación de existencias.
- Consulta de `@Promocionable` en clases anotadas y no anotadas, límites inclusivos de vigencia y ausencia de descuento fuera del intervalo.
- Rechazo de un pedido mixto con un ingrediente insuficiente, conservando todas las existencias.

**Release.** R8 está habilitado para `androidx.**`, `kotlin.**` y `kotlinx.**`. El dominio de PizzeriaApp queda fuera del alcance declarado. `rules.keep` contiene comentarios de plantilla. No se obtuvo un APK release verificable durante D2. La revisión automática bloqueó el seguimiento de la compilación por una conexión incidental que podía transmitir telemetría o metadatos del proyecto a un servicio externo.

La comprobación release requiere compilar con R8 habilitado y ejecutar el dominio de ese APK en Android. Si se instala un APK de diagnóstico, se utilizará firma de desarrollo fuera del repositorio. Con tasa del 19 %, promoción vigente del 15 % y dos líneas de $8.000 CLP, una Margarita anotada y un producto genérico sin anotación, los valores esperados son:

| Escenario | Subtotal | Descuento | Impuesto | Total |
|---|---:|---:|---:|---:|
| Una Margarita y un producto no anotado, promoción vigente | $16.000 | $1.200 | $2.812 | $17.612 |
| Solo el producto no anotado, promoción vigente | $8.000 | $0 | $1.520 | $9.520 |
| Una Margarita y un producto no anotado, promoción vencida | $16.000 | $0 | $3.040 | $19.040 |

La prueba debe comprobar también la presencia de la anotación en Margarita y su ausencia en el producto genérico. Los valores de esta tabla son criterios de aceptación pendientes de ejecución sobre release.

## Evidencias y aceptación para D3

Durante D2 no se obtuvieron capturas válidas. En D3, etapa 1, se verificaron las tres evidencias principales incorporadas al [índice de capturas](evidencias/semana-4/README.md). Muestran el proyecto abierto, el AVD Pixel 6 API 35 en ejecución y la pantalla inicial de la aplicación. La captura dedicada de cotización es complementaria.

El futuro ZIP debe incluir este informe, el índice de capturas, los PNG obtenidos y la [trazabilidad de Semana 4](trazabilidad-pruebas-semana-4.md). Los vínculos relativos de esos documentos resuelven dentro de ese conjunto. Si se excluyen los informes históricos, la copia del README destinada al ZIP debe presentar el historial como referencia a la rama `semana-3`, sin enlaces relativos a archivos ausentes. El README del repositorio puede conservar esos enlaces. En D3 se debe comprobar la resolución de vínculos, la legibilidad de las capturas y la compilación desde una extracción limpia.

Las comprobaciones API 23 y release corresponden a compatibilidad técnica. El entorno académico acreditado para la entrega es debug en Pixel 6 API 35.
