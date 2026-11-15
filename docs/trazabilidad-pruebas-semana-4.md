# Semana 4 · Fase B — Trazabilidad de las pruebas

La referencia histórica corresponde a los 124 métodos de prueba de `src/test/kotlin/org/example/`. La suite local del módulo Android recupera 77 de esos métodos bajo `app/src/test/java/cl/duoc/pizzeriaapp/domain/`, con los mismos nombres de clase y método.

| Clase histórica | Total histórico | Recuperadas | Exclusivas de la suite histórica |
|---|---:|---:|---:|
| ConfirmacionSNTest | 15 | 0 | 15 |
| IntegracionSemana3Test | 5 | 1 | 4 |
| OperadoresIgualdadTest | 12 | 12 | 0 |
| PedidoTest | 46 | 40 | 6 |
| PromocionTest | 8 | 8 | 0 |
| RecuperacionEntradaTest | 12 | 0 | 12 |
| RetroalimentacionSemana1Test | 7 | 6 | 1 |
| UsabilidadSemana3Test | 12 | 6 | 6 |
| ValidacionSemana3Test | 7 | 4 | 3 |
| **Total** | **124** | **77** | **47** |

Las pruebas recuperadas conservan las entradas, las aserciones y los resultados esperados. Se adaptaron los imports al dominio Android y se usó `org.junit.Test` con el ejecutor local JUnit 4 ya configurado en `:app`. La dependencia de test `kotlin-test` permite conservar las aserciones originales, incluidas las que devuelven el valor comprobado y las que verifican el tipo de excepción.

El caso `IntegracionSemana3Test.anotacionTieneMetadatosRuntimeEnLasDosPizzasDelCatalogo` consulta los metadatos con reflexión Java. Se conservan las cinco aserciones sobre retención, destino, descripciones de ambas pizzas y ausencia de anotación en el producto base.

## Casos conservados en la suite histórica

- `ConfirmacionSNTest` y `RecuperacionEntradaTest` completos.
- `IntegracionSemana3Test` conserva la inspección reflexiva de miembros, la configuración mediante argumentos y la demostración por menú.
- `PedidoTest` conserva el reintento de `leerCantidad`, las cuatro pruebas cuyo nombre comienza por `consola` y la cancelación por entrada nula.
- `RetroalimentacionSemana1Test.mainPresentaElResumenSeccionadoTrasRegistrar` conserva el contrato de texto completo desde el punto de entrada.
- `UsabilidadSemana3Test` conserva el resumen de teléfono por consola y los recorridos de pedido mixto, variedad agotada, cancelación, aceptación de 100 y cambio de stock antes de confirmar.
- `ValidacionSemana3Test` conserva los recorridos de corrección de correo, EOF y diferencia entre cantidad inválida y stock insuficiente por consola.

## Equivalencias para futuros flujos móviles

| Contrato histórico | Comprobación móvil cuando exista el flujo |
|---|---|
| Reintentos por selección, correo o cantidad inválidos | El campo muestra el error y permite corregirlo sin perder los datos previamente aceptados ni registrar el pedido. |
| Cancelación, EOF y confirmaciones S/N | Cancelar explícitamente deja intactos inventario, pedidos y repartidores. Confirmar mediante la acción correspondiente registra el pedido una sola vez. |
| Variedad agotada, límite de cantidad y stock modificado durante la captura | Informar el límite o el agotamiento y recalcular disponibilidad al confirmar. El rechazo conserva el stock y no genera cobro. |
| Resumen, teléfono y cambio de estado en consola | Verificar datos visibles, montos, texto de contacto y estado después de los eventos de la pantalla. |

Los argumentos CLI, la demostración por menú y la inspección de miembros no requieren equivalentes en la interfaz móvil para esta fase. Las pruebas focalizadas añadidas en Fase A y la prueba de plantilla pertenecen a la suite local de `:app` y se contabilizan por separado de los 77 casos recuperados.
