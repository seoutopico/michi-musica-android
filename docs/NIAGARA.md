# El nuevo icono en Niagara

Michi Música incluye un icono adaptativo con el gato del diseño actual: trazo rosa sobre Medianoche. También incorpora una capa monocroma para launchers con iconos temáticos.

Para personalizarlo se distribuye **Michi Iconos**, una APK pequeña independiente, sin permisos. Incluye Medianoche, Rosa y el gato rosa con fondo transparente. Niagara no permite elegir directamente una imagen de la galería: utiliza paquetes de iconos.

1. Instala `Michi-Iconos-1.0.0.apk` desde la misma release que Michi Música.
2. En Niagara, mantén pulsada **Michi Música**.
3. Toca el icono de la app dentro del menú que aparece.
4. Pulsa **Usar otros paquetes de iconos** y desplázate hasta **Paquetes de iconos instalados → Michi Iconos**.
5. Confirma **Usar icono** para Medianoche o **Elegir otro icono** para ver las tres variantes. Niagara puede mostrarlas como Michi Midnight, Michi Rose y Michi Outline.

El paquete solo asocia automáticamente Michi Música; no contiene sustituciones para las demás apps. Puedes volver al icono original desde el mismo selector. Si Niagara conserva una elección antigua, vuelve a seleccionar el icono de Michi tras actualizar.

Los archivos editables están en `branding/`: SVG y PNG de 512 píxeles. Un archivo `.ico` es para Windows; para Niagara la entrega instalable es este paquete Android. Su código está en `iconpack/`, incluidos `appfilter.xml` y el selector de tres variantes.

Fuentes: [editar iconos en Niagara](https://help.niagaralauncher.app/article/97-edit-app-icons-and-names) y [recursos para desarrolladores de paquetes](https://help.niagaralauncher.app/article/24-resources-for-icon-pack-developers).

Verificado en Pixel 7: Niagara reconoce el paquete, muestra las tres variantes y aplica Medianoche solo a Michi Música.
