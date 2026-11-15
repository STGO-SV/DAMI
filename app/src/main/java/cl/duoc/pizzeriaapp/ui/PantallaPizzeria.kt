package cl.duoc.pizzeriaapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.semantics.Role
import cl.duoc.pizzeriaapp.R
import cl.duoc.pizzeriaapp.domain.model.Producto
import cl.duoc.pizzeriaapp.domain.order.Importes
import cl.duoc.pizzeriaapp.domain.support.pesos

@Composable
internal fun PantallaPizzeria(modifier: Modifier = Modifier) {
    val cotizador = remember { CotizadorPizzeria() }
    val productos = remember(cotizador) { cotizador.catalogo() }
    var seleccionado by remember { mutableStateOf<Producto?>(null) }
    var cotizacion by remember { mutableStateOf<Importes?>(null) }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.bienvenida),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = stringResource(R.string.catalogo),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { heading() }
        )
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            productos.forEach { producto ->
                Card {
                    Row(
                        modifier = Modifier.fillMaxWidth().selectable(
                            selected = seleccionado == producto,
                            role = Role.RadioButton,
                            onClick = {
                                if (seleccionado != producto) {
                                    seleccionado = producto
                                    cotizacion = null
                                }
                            }
                        ).padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = seleccionado == producto, onClick = null)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
                            Text(producto.categoria, style = MaterialTheme.typography.bodyMedium)
                            Text(pesos(producto.precio), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
        Button(
            onClick = { seleccionado?.let { cotizacion = cotizador.cotizarUnaUnidad(it) } },
            enabled = seleccionado != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.cotizar_unidad))
        }
        cotizacion?.let { importes ->
            seleccionado?.let { producto -> DetalleCotizacion(producto, importes) }
        }
    }
}

@Composable
private fun DetalleCotizacion(producto: Producto, importes: Importes) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.cotizacion_producto, producto.nombre),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() }
            )
            Importe(stringResource(R.string.subtotal), importes.subtotal)
            Importe(stringResource(R.string.descuento), importes.descuento)
            Importe(stringResource(R.string.impuesto), importes.impuesto)
            Importe(stringResource(R.string.total), importes.total, destacado = true)
        }
    }
}

@Composable
private fun Importe(etiqueta: String, monto: Long, destacado: Boolean = false) {
    val estilo = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, style = estilo)
        Text(pesos(monto), style = estilo)
    }
}
