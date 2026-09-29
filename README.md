# Mobile Secure Demo

Ejemplo académico de evolución de un cliente Android vulnerable a una versión con mejores prácticas de codificación segura.

## Objetivo

Demostrar:

- Inyección de una API Key con \`BuildConfig\`.
- Archivo local de secretos ignorado por Git.
- Uso obligatorio de HTTPS.
- Bloqueo de tráfico HTTP mediante Network Security Configuration.
- Validación de entradas.
- Manejo controlado de excepciones.
- No exposición de información sensible al usuario.

## 1. Configurar la API Key local

Copia:

\`\`\`text
secrets.properties.example
\`\`\`

como:

\`\`\`text
secrets.properties
\`\`\`

y reemplaza:

\`\`\`properties
API_KEY=REEMPLAZAR_LOCALMENTE
\`\`\`

por tu valor de desarrollo.

\`secrets.properties\` ya está incluido en \`.gitignore\`.

## 2. Verificar antes del primer commit

Ejecuta:

\`\`\`bash
git status
\`\`\`

\`secrets.properties\` NO debe aparecer entre los archivos a subir.

También puedes comprobar:

\`\`\`bash
git check-ignore -v secrets.properties
\`\`\`

## 3. Archivos importantes

- \`VulnerableClient.kt\`: ejemplo inseguro para comparación.
- \`SecureClient.kt\`: versión mejorada.
- \`InputValidator.kt\`: validación de datos.
- \`app/build.gradle.kts\`: lectura del secreto e inyección con \`BuildConfig\`.
- \`network_security_config.xml\`: prohíbe tráfico en texto claro.
- \`.gitignore\`: impide versionar el archivo local de secretos.

## Importante sobre BuildConfig

\`BuildConfig\` sirve para evitar que la clave quede escrita directamente en el código fuente o en Git. Sin embargo, una credencial estática incluida en una APK puede ser extraída mediante ingeniería inversa.

Para secretos de alto valor, la mejor arquitectura es conservarlos en un backend y hacer que la aplicación se autentique contra ese servidor.

## Historial limpio

No subas nunca una API Key real y luego intentes borrarla en un commit posterior. Si una clave real llegó a un repositorio remoto, debes considerarla comprometida, revocarla y generar una nueva.

## Artículo

El archivo \`MEDIUM_ARTICLE.md\` contiene el artículo preparado para publicar en Medium.
