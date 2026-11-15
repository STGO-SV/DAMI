package cl.duoc.pizzeriaapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import cl.duoc.pizzeriaapp.domain.model.catalogoInicial
import org.junit.Rule
import org.junit.Test

class PantallaPizzeriaTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun muestraLaBienvenidaExactaYElCatalogoDelDominio() {
        compose.onNodeWithText("Bienvenidos a la mejor pizzería de la ciudad").assertIsDisplayed()
        catalogoInicial().forEach { producto ->
            compose.onNodeWithText(producto.nombre).performScrollTo().assertIsDisplayed()
        }
    }

    @Test
    fun seleccionarMargaritaMuestraElDesgloseDeUnaUnidad() {
        compose.onNodeWithText("Margarita").performScrollTo().performClick()
        compose.onNodeWithText("Cotizar 1 unidad").performScrollTo().performClick()

        listOf("Cotización de Margarita", "Subtotal", "Descuento", "Impuesto", "Total",
            "\$0 CLP", "\$1.520 CLP", "\$9.520 CLP").forEach { texto ->
            compose.onNodeWithText(texto).performScrollTo().assertIsDisplayed()
        }
    }
}
