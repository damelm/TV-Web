# TV Web — Navegador con marcadores para Google TV / Android

App Android (WebView) pensada para Google TV / Android TV y también compatible con
móviles (Android 5.0+). Funciona como un **gestor de direcciones**: guardas varias
webs, las abres con el mando y las gestionas (añadir / editar / borrar). Las
direcciones se guardan de forma persistente en el dispositivo.

## Características
- **Gestor de direcciones**: añadir, editar, borrar y guardar URLs (persisten entre cierres).
- **Navegación con mando** (D-pad) con resaltado de foco.
- **Bloqueo de publicidad** por lista de dominios (`assets/adblock/hosts.txt`, ~85k dominios).
- **Bloqueo de pop-ups / ventanas emergentes**.
- **Vídeo a pantalla completa** (gestión de `onShowCustomView`).
- **Modo cursor**: mantén *Atrás* para activar un puntero manejable con las flechas
  (OK = clic), ideal para webs no diseñadas para TV. *Atrás corto* sale del modo cursor.

## Controles (en el navegador)
| Acción | Resultado |
|---|---|
| OK sobre una dirección | Abrir |
| Mantener OK sobre una dirección | Menú Abrir / Editar / Borrar |
| Atrás | Retroceder / cerrar |
| Atrás (mantener) | Activar/desactivar modo cursor |
| Atrás x2 (en vídeo pantalla completa) | Salir de pantalla completa |

## Compilar
Requiere JDK 17 y Android SDK (compileSdk 34). Crear `local.properties` con `sdk.dir`.

```
./gradlew assembleDebug      # APK de depuración
./gradlew assembleRelease    # APK firmada (requiere keystore.properties)
```

### Firma (release)
Crear `keystore.properties` en la raíz (NO se versiona) con:

```
storeFile=ruta/al/keystore.jks
storePassword=...
keyAlias=...
keyPassword=...
```

## Estructura
- `app/src/main/java/com/tv/webview/`
  - `MainActivity` — lista de direcciones (pantalla de inicio).
  - `BrowserActivity` — WebView con adblock, pop-ups, pantalla completa y cursor.
  - `BookmarkStore` / `Bookmark` / `BookmarkAdapter` — persistencia y lista.
  - `AdBlocker` — carga y consulta la lista de dominios bloqueados.
  - `CursorView` — puntero del modo cursor.

## Notas
- Probado en Xiaomi TV Box (Android 14, 32-bit, 2 GB RAM).
- minSdk 21, targetSdk 34.
