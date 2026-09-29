package com.example.mobilesecuredemo

object InputValidator {

    fun validarNombre(valor: String): String {
        val limpio = valor.trim()

        require(limpio.length in 2..60) {
            "El nombre debe tener entre 2 y 60 caracteres."
        }

        require(
            limpio.all {
                it.isLetter() ||
                it.isWhitespace() ||
                it == '-' ||
                it == '\''
            }
        ) {
            "El nombre contiene caracteres no permitidos."
        }

        return limpio
    }
}
