import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val secrets = Properties()
val secretsFile = rootProject.file("secrets.properties")

if (secretsFile.exists()) {
    secretsFile.inputStream().use { secrets.load(it) }
}

val apiKey = secrets.getProperty("API_KEY")
    ?: System.getenv("API_KEY")
    ?: ""

android {
    namespace = "com.example.mobilesecuredemo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mobilesecuredemo"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        buildConfigField(
            "String",
            "API_KEY",
            "\"${apiKey.replace("\\", "\\\\").replace("\"", "\\\"")}\""
        )
    }

    buildFeatures {
        buildConfig = true
    }
}
