# Activar comprobaciones en GitHub Actions

`android.yml` contiene el flujo preparado para JDK 21, Android SDK 36, pruebas, lint y APK debug. Las acciones están fijadas a SHA y solo reciben permiso de lectura.

Para activarlo, copia este archivo a `.github/workflows/android.yml` mediante una conexión de GitHub con permiso para escribir workflows, o desde el editor web con una cuenta autorizada. Al publicarlo en `main`, comprueba el resultado en Actions. No añadir la clave privada de firma: este flujo no la necesita.

En la primera publicación la conexión OAuth disponible no tiene el alcance `workflow`; GitHub rechazó el push con ese archivo. Por eso se entrega como plantilla, no como automatización activa. La validación local `test lint assembleDebug assembleRelease` sí se completó. No presentar un check remoto como aprobado hasta ejecutarlo.
