# PizzeriaApp · Semana 4

**PMY2201 — Desarrollo de Aplicaciones Móviles I**
**Experiencia 2 · Semana 4 · Forma B**
**Paquete Android** `cl.duoc.pizzeriaapp`

Aplicación Android en Kotlin con Jetpack Compose, creada a partir de la plantilla Empty Activity. La pantalla muestra «Bienvenidos a la mejor pizzería de la ciudad», presenta el catálogo del dominio y permite seleccionar un producto y cotizar una unidad. La cotización muestra subtotal, descuento, impuesto y total en pesos chilenos.

El cálculo utiliza un pedido de simulación y aplica la tasa y la promoción de la jornada. La operación conserva las existencias y los registros de pedidos y clientes. El registro de pedidos, la reposición, el reparto y los recorridos de captura pertenecen a la aplicación histórica de consola. El dominio Android conserva esas reglas, mientras que la interfaz móvil se limita al catálogo y la cotización. La tasa predeterminada del 19 % se utiliza como supuesto académico.

## Entorno

| Componente | Configuración |
|---|---|
| IDE | Android Studio con soporte para AGP 9.5.1 y SDK 37 |
| Gradle y AGP | Wrapper 9.6.0 y Android Gradle Plugin 9.5.1 |
| Java | JDK 25 para el daemon de Gradle y bytecode Java 11 para `:app` |
| Kotlin y Compose | Kotlin integrado y plugin de Compose 2.2.10, BOM de Compose 2026.02.01 |
| Android SDK | `compileSdk = 37`, `targetSdk = 37`, `minSdk = 23` |
| Compatibilidad de bibliotecas Java | Core library desugaring con `desugar_jdk_libs:2.1.5` |
| Emulador de referencia | Pixel 6 con Android 15 (API 35), imagen Google APIs x86_64 |

Para una primera compilación se requiere el SDK de Android con la plataforma 37 y acceso a los repositorios de Gradle y sus dependencias si aún no están en caché. Para la instrumentación se requiere Android Emulator, Platform-Tools y la imagen del AVD. Android Studio configura la ruta local del SDK en `local.properties`.

## Estructura

| Ruta | Responsabilidad |
|---|---|
| `app/src/main/java/cl/duoc/pizzeriaapp/MainActivity.kt` | Punto de entrada, tema y composición de la pantalla. |
| `app/src/main/java/cl/duoc/pizzeriaapp/ui/` | Catálogo, selección, desglose, cotizador y tema. |
| `app/src/main/java/cl/duoc/pizzeriaapp/domain/model/` | Productos, líneas, usuarios y anotación promocional. |
| `app/src/main/java/cl/duoc/pizzeriaapp/domain/order/` | Jornada, pedidos, importes, estados y reparto. |
| `app/src/main/java/cl/duoc/pizzeriaapp/domain/inventory/` | Existencias, capacidad y consumo de ingredientes. |
| `app/src/main/java/cl/duoc/pizzeriaapp/domain/promotion/` | Promociones por fecha y reglas de elegibilidad. |
| `app/src/main/java/cl/duoc/pizzeriaapp/domain/support/` | Validaciones, registro, identidad y formato CLP. |
| `app/src/main/res/` y `app/src/main/AndroidManifest.xml` | Recursos, textos, tema de arranque y configuración de Android. |
| `app/src/test/` y `app/src/androidTest/` | Pruebas JVM locales e instrumentadas en Android. |

## Abrir, compilar y ejecutar

1. Abrir en Android Studio la carpeta que contiene `settings.gradle.kts` y sincronizar Gradle con JDK 25.
2. En Device Manager, iniciar el Pixel 6 API 35. La actividad admite un AVD con API 31 o superior.
3. Seleccionar la configuración `app` y ejecutar **Run**. Comprobar la bienvenida, seleccionar Margarita y pulsar **Cotizar 1 unidad**. Con la configuración inicial, el subtotal es $8.000 CLP y el total es $9.520 CLP.

Desde la raíz, en PowerShell:

```powershell
.\gradlew.bat --version
.\gradlew.bat :app:assembleDebug --no-daemon
.\gradlew.bat :app:testDebugUnitTest --no-daemon
# Con el emulador iniciado y conectado
.\gradlew.bat :app:connectedDebugAndroidTest --no-daemon
```

El APK debug se genera en `app/build/outputs/apk/debug/`. En sistemas Unix se puede utilizar `sh ./gradlew` con las mismas tareas.

## Resultados verificados

Los informes Gradle del 14 de noviembre de 2026 registran:

| Suite | Aprobadas | Fallos | Errores | Omisiones |
|---|---:|---:|---:|---:|
| Local | 88 | 0 | 0 | 0 |
| Instrumentada, Pixel 6 API 35 | 3 | 0 | 0 | 0 |

La suite local incluye 77 casos recuperados de Semana 3, 7 pruebas focalizadas de dominio, 3 pruebas del cotizador y 1 de plantilla. La instrumentación incluye 2 casos de Compose y 1 prueba de contexto. Los XML se generan en `app/build/test-results/testDebugUnitTest/` y `app/build/outputs/androidTest-results/connected/debug/`, respectivamente.

## Límites de la verificación

La ejecución instrumentada corresponde a la variante debug en API 35. Queda pendiente comprobar en API 23 las fechas y el reloj, la aritmética exacta, la lectura de anotaciones, el inventario y las promociones. La variante release optimizada requiere una comprobación específica de la elegibilidad promocional bajo R8.

## Trazabilidad e historial

- [Validación final de Semana 4](docs/validacion-final-semana-4.md) e [índice de evidencias](docs/evidencias/semana-4/README.md).
- [Trazabilidad de pruebas de Semana 4](docs/trazabilidad-pruebas-semana-4.md).
- [Semana 1](docs/informe-semana-1.md), [Semana 2](docs/informe-semana-2.md) y [Semana 3](docs/informe-semana-3.md).
- [Retroalimentación docente de Semana 3](docs/retroalimentacion-semana-3.md).
- La aplicación JVM de consola se conserva en `src/` y en la rama `semana-3`. Los comandos originales de consola corresponden a esa rama. La suite histórica de 124 casos se conserva como referencia y 77 de esos casos se ejecutan en la suite Android local.
