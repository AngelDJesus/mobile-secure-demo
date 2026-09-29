package com.example.mobilesecuredemo

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object SecureClient {

    // HTTPS obligatorio.
    private const val BASE_URL = "https://api.ejemplo.com"

    fun buscarPaciente(nombre: String): Resultado<String> {
        var conexion: HttpsURLConnection? = null

        return try {
            val nombreValido = InputValidator.validarNombre(nombre)
            val nombreCodificado = URLEncoder.encode(
                nombreValido,
                StandardCharsets.UTF_8.toString()
            )

            // La clave se inyecta en BuildConfig desde secrets.properties
            // o desde una variable de entorno; nunca se escribe aquí.
            val apiKey = BuildConfig.API_KEY

            require(apiKey.isNotBlank()) {
                "La API Key no está configurada."
            }

            val endpoint =
                "$BASE_URL/pacientes?nombre=$nombreCodificado"

            val url = URL(endpoint)
            conexion = url.openConnection() as HttpsURLConnection

            conexion.requestMethod = "GET"
            conexion.connectTimeout = 10_000
            conexion.readTimeout = 10_000
            conexion.setRequestProperty(
                "Authorization",
                "Bearer $apiKey"
            )
            conexion.setRequestProperty(
                "Accept",
                "application/json"
            )

            val codigo = conexion.responseCode

            if (codigo in 200..299) {
                val cuerpo = BufferedReader(
                    InputStreamReader(conexion.inputStream)
                ).use { it.readText() }

                Resultado.Exito(cuerpo)
            } else {
                // No se devuelve el cuerpo de error del servidor al usuario.
                Resultado.Error(
                    "El servidor rechazó la solicitud."
                )
            }

        } catch (e: IllegalArgumentException) {
            Resultado.Error(
                e.message ?: "Los datos ingresados no son válidos."
            )
        } catch (e: IOException) {
            Resultado.Error(
                "No fue posible conectarse al servidor."
            )
        } catch (e: Exception) {
            // Mensaje genérico: no se exponen trazas ni secretos.
            Resultado.Error(
                "Ocurrió un error inesperado."
            )
        } finally {
            conexion?.disconnect()
        }
    }
}
