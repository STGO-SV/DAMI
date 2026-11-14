# Retroalimentación docente de Semana 3

La retroalimentación reconoce las validaciones, las anotaciones y la reflexión, los operadores, los contratos de igualdad, el inventario y las pruebas unitarias. En la Fase A de Semana 4 se aborda la organización de las clases en paquetes por responsabilidad.

La regla vigente aplica el mayor descuento entre el descuento por cantidad y la promoción general por fechas, sin acumularlos. En caso de empate prevalece el descuento por cantidad. `@Promocionable` identifica las clases elegibles para la promoción general y conserva su atributo descriptivo.

## Evolución propuesta

Evaluar un atributo de descuento individual en `@Promocionable` y definir su interacción con la promoción general. La propuesta queda fuera del alcance implementado en Semana 4. Antes de desarrollarla, se requiere una especificación comercial y casos de prueba que cubran:

- Promociones individuales y generales vigentes simultáneamente.
- Criterios de prioridad, empate y acumulación entre descuentos.
- Base de cálculo, orden de aplicación y redondeo monetario.
- Vigencia temporal, incluidos los extremos del período.
- Pedidos mixtos con productos sin descuento individual.
- Conservación de las reglas vigentes cuando no existan promociones individuales.
