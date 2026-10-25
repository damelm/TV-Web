# Crear las playlists en tu YouTube Music

`watch_videos` solo arma una cola temporal de videos sueltos. Para tener una **playlist real** hay que crearla dentro de tu cuenta. El script `crear_playlists.py` lo hace desde tu computadora, así tu sesión no sale de ahí.

Crea 4 playlists privadas: *Melodic After Dark* (30), *Entrenamiento* (27), *Viernes de entrenamiento* (25) y *Viernes con amigos* (31).

## 1. Preparar Python (una sola vez)

```bash
python3 -m venv ~/ytm
source ~/ytm/bin/activate
pip install ytmusicapi
cd ~/TV-Web/playlists      # la carpeta donde está crear_playlists.py
```

## 2. Dar acceso a tu cuenta (una sola vez)

1. Abrí Firefox y entrá a <https://music.youtube.com> con tu cuenta.
2. Apretá **F12** y andá a la pestaña **Red** (Network).
3. Escribí `browse` en el filtro y recargá la página (F5).
4. Hacé clic en una fila con método **POST** hacia `music.youtube.com/youtubei/v1/browse`.
5. Clic derecho → **Copiar valor → Copiar encabezados de la solicitud**.
6. En la terminal:

```bash
ytmusicapi browser
```

Pegá lo copiado, apretá Enter y después **Ctrl+D**. Se crea el archivo `browser.json`.

**`browser.json` equivale a tu sesión.** No se lo pases a nadie ni lo subas a ningún lado. Ya está en el `.gitignore`.

## 3. Probar sin crear nada

```bash
python crear_playlists.py --dry-run
```

Muestra qué tema elegiría para cada búsqueda. Si no puede resolver la versión "canción", usa el ID de video de respaldo.

## 4. Crear las playlists

```bash
python crear_playlists.py                        # las 4
python crear_playlists.py --only "Viernes con amigos"   # una sola
```

Al final imprime el link de cada playlist: `https://music.youtube.com/playlist?list=...`

## Si algo falla

- `FileNotFoundError: browser.json`: corré el comando desde la carpeta donde hiciste el paso 2.
- Error de autenticación: repetí el paso 2. Los encabezados vencen con el tiempo.
- Algún tema no entra: el script lo muestra. Agregalo a mano desde la app.
