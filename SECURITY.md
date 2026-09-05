# Seguridad

## Comunicar un problema

Usa [Report a vulnerability](https://github.com/seoutopico/michi-musica-android/security/advisories/new) para comunicar problemas en privado. No publiques claves, rutas de archivos personales ni ejemplos con datos sensibles en un issue público. Incluye versión, Android, pasos y resultado esperado/observado. Si la opción privada no está disponible, abre un issue pidiendo un canal privado sin detallar la vulnerabilidad.

Se mantiene la última versión publicada. No existe un SLA de respuesta.

## Alcance de la revisión

La publicación 1.10 incluye revisión de código de permisos, sesión multimedia, lecturas de archivos/red, descargas, WebView, firma y contenido del repositorio. Se acompaña de tests y Android lint. Es una revisión asistida por IA y pruebas funcionales, **no una auditoría independiente ni una garantía de ausencia de vulnerabilidades**.

Correcciones aplicadas y límites: [Revisión de seguridad](docs/REVISION_SEGURIDAD.md). Datos tratados: [Privacidad](PRIVACY.md).

## Instalar y contribuir

- Descargar APK de las [releases de este repositorio](https://github.com/seoutopico/michi-musica-android/releases). Cada entrega incluye sumas SHA-256 y huella del certificado de firma.
- Las APK públicas usan una clave privada de publicación, separada de la clave de desarrollo. Nunca se publica la clave ni sus contraseñas.
- Para contribuir, compilar con una clave propia. No solicitar ni añadir credenciales de firma al repositorio.
- Mantener Android y Android System WebView actualizados. El contenido externo, los decodificadores y las dependencias necesitan mantenimiento continuo.
