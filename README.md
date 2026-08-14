<p align="center">
  <img src="https://i.imgur.com/evq43PO.png" alt="CS_Logo" width="700">
</p>

# BTE Cono Sur
Build The Earth (**BTE**) es un proyecto global y colaborativo que busca recrear al planeta **Tierra** en **Minecraft** en escala **1:1**. Se divide principalmente en países.

Los equipos de **Argentina**, **Chile**, **Perú**, **Bolivia**, **Paraguay** y **Uruguay** comparten un único servidor denominado **BTE Cono Sur**.

## bteCSCore
**bteCSCore** es el plugin central del servidor **BTE Cono Sur**.
Provee los sistemas base necesarios para la gestión de proyectos, permisos, persistencia de datos e infraestructura compartida utilizada por el resto del ecosistema del servidor en las nuevas versiones.

El plugin está diseñado y desarrollado específicamente para cubrir las necesidades internas de **BTE Cono Sur**, priorizando la mantenibilidad, escalabilidad y evolución a largo plazo.

**No está pensado para uso externo**, ni como plugin independiente o solución genérica para otros servidores BTE.

## Documentación (Wiki)
Toda la documentación del servidor, incluyendo guías para jugadores, lista detallada de comandos, reglamentos y flujos de moderación para el Staff, se encuentra disponible en nuestra Wiki oficial:

👉 **[Wiki de BTE Cono Sur](https://resources.buildtheearth.net/s/adbd294f-c787-4ea1-ae13-88fe946aaf8a/doc/bte-cono-sur-DAKmKdHyYG)**

# Características Principales

## Proyectos

* Creación y gestión de proyectos individuales.
* Redefinir y editar proyectos creados.
* Protección de áreas de construcción.
* Añadir/Eliminar miembros.
* Flujo de revisión y aprobación.
* Comandos de Administración.
* Comandos de Manager.
* Comandos de Reviewer.

## Discord

* Vinculación de cuentas Minecraft y Discord.
* Notificaciones con interacciones y botones dinámicos.
* Revisión y aprobación de proyectos desde Discord.
* Chat Global y Chat de País sincronizados.
* `/help minecraft/discord` -> Muestra ayuda sobre comandos.
* `/player` -> Muestra información de un Jugador.
* `/proyecto` -> Muestra información de un Proyecto.
* `/online` -> Muestra los jugadores online.
* `/schematic` -> Obtiene un schematic de WorldEdit por su nombre.

## Minecraft

* `/help` -> Comando general de ayuda.
* `/pais` -> Selección del prefix del chat.
* `/chat` -> Selección de chat.
* Modo Bloc de Notas: No se envían ni reciben mensajes.
* `/config` -> Configuración del jugador.
* `/nickname` -> Cambiar el nombre público.
* `/lobby` -> Ir hacia el lobby general o de un país.
* `/nightvision` -> Activa/Desactiva visión nocturna.
* `/pwarp` -> Comando general para gestionar warps personales.
* `/preset` -> Creación y gestión de presets de bloques para WorldEdit.
* `/get` -> Obtener bloques especiales.
* `/assets` -> Acceso directo a la zona de assets.
* `/tpdir` -> Tepearse a lugares o direcciones reales con filtrado de coordenadas.

## Gestor de Mundos

* Sistema de mundos por capas y regiones.
* Sistema de Países y Ciudades.
* Cargado de regiones con geo data (GeoJSON).

## Admin

* Log de Staff, Log de País y Log de Consola.
* Sistema de reinicios automáticos y seguros con avisos.
* Compatibilidad con muteado de Essentials.
* `/exec` (DS) -> Ejecutar comando en consola.
* `/promote` (MC) -> Cambiar el Rango de Usuario de un Jugador.
* `/btecs defaultgroup` -> Gestión de permisos base in-game.
* `/crud` -> Gestión de entidades del servidor.
* `/deletePlayerData` (Consola) -> Eliminar el archivo de playerdata de un jugador.

#
> Este listado representa las características principales implementadas en la versión **V3**.

## Dependencias

El plugin requiere los siguientes plugins para funcionar correctamente:

* **BTEConoSurLibs** - Librerías utilizadas por BTE Cono Sur: https://github.com/BTEConoSur/bteCSLibs
* **Multiverse-Core**
* **LuckPerms**
* **EssentialsX**
* **WorldEdit** (FAWE)
* **HeadDatabase**
* **WorldGuard**
* **PlaceholderAPI**
* **TAB**


