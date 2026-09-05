# Conectar el Pixel e instalar por ADB

Esta guía documenta la recuperación aplicada el 4 de septiembre de 2026 cuando Windows detectaba el Pixel 7, pero ADB mostraba el dispositivo como `unauthorized` y Android no enseñaba el aviso de autorización.

## Qué estaba ocurriendo

El modo USB **Transferencia de archivos** confirma que el cable transmite datos, pero no autoriza ADB. Son permisos distintos.

Los estados relevantes de `adb devices -l` son:

- `device`: conexión autorizada y lista para instalar.
- `unauthorized`: el ordenador detecta el móvil, pero Android no ha aceptado su clave ADB.
- Lista vacía: revisar cable, puerto USB, modo de transferencia y controlador.

No se debe registrar en documentación el número de serie mostrado por ADB.

## Preparación en el Pixel

1. Activar las opciones para desarrolladores desde **Ajustes → Información del teléfono → Número de compilación**, pulsándolo siete veces.
2. Abrir **Ajustes → Sistema → Opciones para desarrolladores**.
3. Activar **Depuración USB**.
4. Conectar el cable con el Pixel desbloqueado y elegir **Transferencia de archivos**.
5. Cuando aparezca **¿Permitir depuración USB?**, marcar **Permitir siempre desde este ordenador** y pulsar **Permitir**.

## Comprobar la conexión desde PowerShell

Las rutas se resuelven en tiempo de ejecución; no se escriben rutas de perfil fijas en el repositorio.

```powershell
$adb = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
& $adb devices -l
```

Si el resultado contiene `device`, se puede pasar directamente a la instalación.

## Recuperar una autorización atascada

Primero se puede reiniciar la negociación existente:

```powershell
$adb = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
& $adb kill-server
& $adb start-server
& $adb devices -l
```

Si continúa como `unauthorized` y no aparece ningún aviso en el Pixel:

1. En el Pixel, pulsar **Revocar autorizaciones de depuración USB**.
2. Desactivar y activar de nuevo **Depuración USB**.
3. Generar una clave nueva para forzar una solicitud de autorización distinta.

En un entorno restringido se puede usar una clave temporal, como se hizo durante esta reparación:

```powershell
$adb = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
$keyDirectory = Join-Path $env:TEMP 'michi-adb'
$keyPath = Join-Path $keyDirectory 'adbkey'

New-Item -ItemType Directory -Force -Path $keyDirectory | Out-Null
if (-not (Test-Path -LiteralPath $keyPath)) {
    & $adb keygen $keyPath
}

& $adb kill-server
$env:ADB_VENDOR_KEYS = $keyPath
& $adb start-server
& $adb devices -l
```

Con el móvil desbloqueado, desconectar y reconectar el cable si hace falta. Android debe mostrar una solicitud nueva. Tras aceptarla, repetir `& $adb devices -l`; el estado esperado es `device`.

La clave privada no debe copiarse al repositorio, documentación o chat. La clave de este procedimiento queda bajo `%TEMP%\michi-adb` y puede dejar de estar disponible cuando Windows limpie los archivos temporales; en ese caso se repite el procedimiento.

## Instalar Michi Música sin borrar sus datos

Desde la raíz del repositorio:

```powershell
$adb = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
$apk = 'app\build\outputs\apk\debug\Michi-Musica-1.1.2.apk'
& $adb install -r $apk
```

`-r` reemplaza la APK instalada conservando los datos de la aplicación. El resultado esperado es `Success`.

Para abrir la aplicación:

```powershell
& $adb shell monkey -p com.ainalluna.michimusica -c android.intent.category.LAUNCHER 1
```

## Resultado de la reparación documentada

- El dispositivo pasó de `unauthorized` a `device` después de reiniciar ADB con una clave temporal nueva y aceptar el aviso en el Pixel.
- `Michi-Musica-1.1.2.apk` se instaló mediante `adb install -r` con resultado `Success`.
- La aplicación se abrió mediante ADB para probar **YOUTUBE → OÍR** y **MP3**.

