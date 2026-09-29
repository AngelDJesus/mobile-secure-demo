package com.example.mobilesecuredemo

import java.net.URL

/**
 * EJEMPLO DIDÁCTICO VULNERABLE.
 * No usar en producción.
 */
object VulnerableClient {

    fun buscarPaciente(nombre: String): String {
        // Riesgo 1: secreto escrito directamente en el código.
        val apiKey = "API_KEY_REAL_AQUI"

        // Riesgo 2: HTTP sin cifrado.
        // Riesgo 3: entrada concatenada sin validación ni codificación.
        val url =
            "http://api.ejemplo.com/pacientes?nombre=$nombre&key=$apiKey"

        return try {
            URL(url).readText()
        } catch (e: Exception) {
            // Riesgo 4: se exponen detalles internos.
            "Error: ${e.message}"
        }
    }
}
