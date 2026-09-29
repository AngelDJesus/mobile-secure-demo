# De un script vulnerable a una app móvil más segura

## Introducción

La seguridad de una aplicación móvil no debería agregarse únicamente antes de publicarla. Cuando una app consume APIs, utiliza credenciales o transmite información sensible, las decisiones de seguridad deben formar parte del código desde las primeras etapas del desarrollo.

En este ejercicio se analiza un cliente Android vulnerable y se muestra su evolución hacia una versión más segura. El objetivo es aplicar principios como defensa en profundidad, mínimo privilegio, validación de entradas, cifrado de datos en tránsito y manejo seguro de credenciales.

> **Repositorio público:** https://github.com/AngelDJesus/mobile-secure-demo

---

## 1. Vulnerabilidades identificadas

El código inicial contiene cuatro problemas principales.

### API Key incluida directamente en el código

Una credencial escrita dentro de un archivo Kotlin puede terminar en un repositorio público y permanecer en el historial de Git incluso si después se elimina.

### Uso de HTTP

Enviar información por HTTP permite que los datos viajen sin la protección proporcionada por TLS. Para información sensible, la comunicación debe realizarse mediante HTTPS.

### Entradas sin validar

Los datos que proporciona un usuario no deben utilizarse directamente para construir una solicitud. Es necesario comprobar longitud, formato y caracteres permitidos, además de codificar correctamente los parámetros.

### Exposición de errores internos

Mostrar directamente `e.message` puede revelar información del sistema que no es necesaria para el usuario.

---

## 2. Código vulnerable

```kotlin
fun buscarPaciente(nombre: String): String {
    val apiKey = "API_KEY_REAL_AQUI"

    val url =
        "http://api.ejemplo.com/pacientes?nombre=$nombre&key=$apiKey"

    return try {
        URL(url).readText()
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}
```

Este fragmento combina varias malas prácticas: la clave está escrita en el código, la comunicación utiliza HTTP, la entrada del usuario se concatena directamente y el mensaje de la excepción se muestra sin control.

---

## 3. Gestión de la API Key con BuildConfig

La primera mejora consiste en evitar que la credencial se escriba directamente dentro del archivo Kotlin.

Se crea un archivo local:

```properties
API_KEY=REEMPLAZAR_LOCALMENTE
```

El archivo se llama `secrets.properties` y se incluye en `.gitignore`:

```gitignore
local.properties
secrets.properties
*.jks
*.keystore
```

En `app/build.gradle.kts` se carga el valor y se genera el campo en `BuildConfig`:

```kotlin
import java.util.Properties

val secrets = Properties()
val secretsFile = rootProject.file("secrets.properties")

if (secretsFile.exists()) {
    secretsFile.inputStream().use { secrets.load(it) }
}

val apiKey = secrets.getProperty("API_KEY")
    ?: System.getenv("API_KEY")
    ?: ""

android {
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField(
            "String",
            "API_KEY",
            "\"<valor-inyectado>\""
        )
    }
}
```

Después, el código puede obtener el valor mediante:

```kotlin
val apiKey = BuildConfig.API_KEY
```

De esta forma la clave real no necesita guardarse en Git.

Es importante aclarar que `BuildConfig` evita dejar el secreto escrito directamente en el código fuente y en el repositorio, pero una credencial estática incluida dentro de una APK puede extraerse mediante ingeniería inversa. Para secretos de alto valor, una arquitectura más segura consiste en mantenerlos en un backend.

---

## 4. Uso estricto de HTTPS

La segunda mejora consiste en rechazar tráfico HTTP.

En `AndroidManifest.xml`:

```xml
<application
    android:usesCleartextTraffic="false"
    android:networkSecurityConfig="@xml/network_security_config">
</application>
```

En `res/xml/network_security_config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false" />
</network-security-config>
```

La URL del servicio también utiliza HTTPS:

```kotlin
private const val BASE_URL = "https://api.ejemplo.com"
```

Esto permite aplicar cifrado TLS durante la transmisión de los datos.

---

## 5. Validación de entradas

Antes de enviar el nombre recibido se revisa su contenido:

```kotlin
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
```

Después, el valor debe codificarse correctamente antes de colocarlo en una URL.

La validación del cliente no reemplaza la validación del servidor. El backend debe considerar cualquier entrada enviada desde un dispositivo como información no confiable.

---

## 6. Manejo seguro de excepciones

En lugar de mostrar detalles internos, la aplicación devuelve mensajes controlados:

```kotlin
sealed class Resultado<out T> {
    data class Exito<T>(val datos: T) : Resultado<T>()
    data class Error(val mensaje: String) : Resultado<Nothing>()
}
```

Y durante la operación:

```kotlin
return try {
    // Operación de red
    Resultado.Exito(respuesta)

} catch (e: IllegalArgumentException) {
    Resultado.Error(
        e.message ?: "Los datos ingresados no son válidos."
    )

} catch (e: IOException) {
    Resultado.Error(
        "No fue posible conectarse al servidor."
    )

} catch (e: Exception) {
    Resultado.Error(
        "Ocurrió un error inesperado."
    )
}
```

Así se evita presentar al usuario trazas internas, rutas, tokens u otros detalles técnicos.

---

## 7. Comparación antes y después

| Aspecto | Código vulnerable | Código mejorado |
|---|---|---|
| API Key | Escrita directamente | Archivo local + BuildConfig |
| Git | Riesgo de subir el secreto | `secrets.properties` ignorado |
| Transporte | HTTP | HTTPS/TLS |
| Entradas | Sin validación | Validación y codificación |
| Excepciones | Detalles internos | Mensajes controlados |
| Seguridad | Protección mínima | Varias capas |

---

## 8. Principios de codificación segura aplicados

### Defensa en profundidad

La aplicación no depende de una sola medida. Se utilizan HTTPS, validación, control de credenciales, configuración de red y manejo de errores.

### Mínimo privilegio

Las credenciales que utilice la aplicación deben tener solamente los permisos indispensables. Si una clave o servicio externo llega a comprometerse, limitar los permisos disminuye el alcance del daño.

### Validación de entradas

Toda información proveniente de usuarios o sistemas externos debe considerarse no confiable hasta comprobar que cumple las reglas esperadas.

### Configuración segura por defecto

Se bloquea el tráfico HTTP y se establece HTTPS como la forma permitida de comunicación.

### Gestión de secretos

Las claves no deben incluirse en el repositorio. `secrets.properties` permanece únicamente en el equipo del desarrollador y se incluye un archivo `.example` sin información privada.

---

## 9. Gestión segura del historial de Git

Antes de hacer el primer commit se debe comprobar:

```bash
git status
```

También puede verificarse que el archivo esté ignorado:

```bash
git check-ignore -v secrets.properties
```

El resultado debe mostrar que `.gitignore` está excluyendo el archivo.

Una práctica importante es **no subir nunca una clave real para luego borrarla**. Si una credencial fue publicada en GitHub, debe considerarse comprometida. La solución correcta es revocarla y generar una nueva.

---

## Conclusión

Pasar de un script vulnerable a una implementación más segura no requiere una única herramienta, sino varias decisiones pequeñas aplicadas de manera consistente.

Separar las credenciales del código, utilizar HTTPS, validar las entradas y controlar las excepciones reduce riesgos frecuentes en aplicaciones móviles. Estas medidas también muestran por qué la seguridad debe formar parte del proceso de desarrollo desde el principio y no tratarse únicamente como una revisión antes del lanzamiento.

El resultado es un proyecto más sencillo de auditar y con mejores condiciones para proteger la información que procesa.
