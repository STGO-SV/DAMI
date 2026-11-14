package cl.duoc.pizzeriaapp.domain.support

fun pesos(monto: Long): String = "$" + monto.toString().reversed().chunked(3).joinToString(".").reversed() + " CLP"
