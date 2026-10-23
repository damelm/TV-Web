#!/usr/bin/env python3
"""Crea playlists reales en TU cuenta de YouTube Music.

Se ejecuta en tu computadora: tu sesión nunca sale de ahí.

Preparación (una sola vez):
    python3 -m venv ~/ytm && source ~/ytm/bin/activate
    pip install ytmusicapi
    cd <carpeta de este archivo>
    ytmusicapi browser          # te pide pegar los "request headers" (ver README.md)

Uso:
    python crear_playlists.py --dry-run            # solo muestra qué elegiría
    python crear_playlists.py                      # crea las 4 playlists
    python crear_playlists.py --only "Viernes con amigos"
"""
import argparse
import sys

from ytmusicapi import YTMusic

AUTH_FILE = "browser.json"

# (búsqueda "Artista - Título", ID de video de respaldo)
PLAYLISTS = {
    "Melodic After Dark": [
        ("Anyma Chris Avantgarde Consciousness", "MpbZDxx44qE"),
        ("Adriatique WhoMadeWho Miracle", "4oYrQt_zRFg"),
        ("ARTBAT Dino Lenny Fading", "1LNr2qQ8aQ0"),
        ("Monolink Return to Oz ARTBAT Remix", "Lo0ELoepTCM"),
        ("Tale of Us Nova", "5LHyicZB2-E"),
        ("Mind Against Walking Away", "NtqjtXCOuDI"),
        ("Colyn Resolve", "P4vmlvvZK2o"),
        ("Anyma Pictures of You", "a9vUIc3lFEk"),
        ("Agents of Time The Mirage", "NaaXFSKFtAY"),
        ("CamelPhat ARTBAT For a Feeling", "fg21FNkNtYQ"),
        ("Anyma Chris Avantgarde Eternity", "xAfNUoqQppU"),
        ("Anyma Rebuke Syren", "KduOAoaE1MM"),
        ("Anyma Meg Myers Running", "VT87ibdFUBI"),
        ("Tale of Us Vaal Monument", "n0AL2h0DWzg"),
        ("Tale of Us Another Earth", "C0pACItGzpQ"),
        ("Mind Against Isolate", "XOvary_mX9Q"),
        ("ARTBAT Horizon", "50zeHzEwgoI"),
        ("ARTBAT Closer WhoMadeWho", "6MfdjuzcPJA"),
        ("Monolink Burning Sun", "CRLw8p_vLls"),
        ("Agents of Time Inception", "fQjbZn8yp3A"),
        ("Colyn Patterns Maurits Colijn", "XPavfcEt4lw"),
        ("Fideles Interface Technology", "AR4cC2w3SZk"),
        ("Undercatt Britannia", "ybuW4QCrM78"),
        ("8Kays Morning After The Rave", "S7Zk6DcIvPI"),
        ("CamelPhat Elderbrook Cola", "qke-jOUqSXU"),
        ("Stephan Bodzin Singularity", "AaxFuXpcQmE"),
        ("RUFUS DU SOL Innerbloom", "Tx9zMFodNtA"),
        ("Eelke Kleijn Moments of Clarity", "eWukNc5gj2w"),
        ("Innellea Lost In Fades", "LjKbMHuBQG8"),
        ("Mathame Skywalking", "J5GyxeBTrps"),
    ],
    "Entrenamiento": [
        ("Colyn Patterns Maurits Colijn", "-HXZJLi661o"),
        ("Agents of Time Inception", "fQjbZn8yp3A"),
        ("Mind Against Isolate", "HMb1cfBz9eU"),
        ("Stephan Bodzin Singularity", "AaxFuXpcQmE"),
        ("CamelPhat Elderbrook Cola", "qke-jOUqSXU"),
        ("Anyma Meg Myers Running", "VT87ibdFUBI"),
        ("ARTBAT Closer WhoMadeWho", "6MfdjuzcPJA"),
        ("Innellea Lost In Fades", "5NJ9nr_MjKY"),
        ("Hernan Cattaneo Soundexile The Voice In My Head", "z9-s9bF7m_0"),
        ("Argy Omnya Aria", "QZk3WKJc1ME"),
        ("ARTBAT Horizon", "50zeHzEwgoI"),
        ("FISHER Losing It", "u31thuMehjM"),
        ("Mau P Drugs From Amsterdam", "lOtl4W_ZCu4"),
        ("Kevin de Vries Mau P Metro", "Db4hVWCh4zk"),
        ("Adam Beyer Bart Skils Your Mind", "aU6z-pPEmY0"),
        ("Charlotte de Witte Overdrive", "u1v-TN6j0Rk"),
        ("Amelie Lens Hypnotized", "QW5mv0l6s0Q"),
        ("I Hate Models Daydream", "aOyPI9A9jtE"),
        ("Reinier Zonneveld Mind Game", "l5b8dX4Z1NM"),
        ("Boris Brejcha Laura Korinth Gravity", "hVQe9LW8GT0"),
        ("Quevedo Bizarrap BZRP Music Sessions 52", "A_g3lMcWVy0"),
        ("Trueno Nicki Nicole Bizarrap Mamichula", "NUteQQCMa_k"),
        ("Duki Goteo", "FRthkpJ_NFo"),
        ("Shakira Bizarrap BZRP Music Sessions 53", "CocEMWdc7Ck"),
        ("RUFUS DU SOL Innerbloom", "W10Vof1acps"),
        ("Eelke Kleijn Moments of Clarity", "eWukNc5gj2w"),
        ("Recondite Caldera", "aMcp7IUsIKs"),
    ],
    "Viernes de entrenamiento": [
        ("Peggy Gou It Goes Like Nanana", "KnBdf559ofQ"),
        ("Disclosure Latch Sam Smith", "nBsltqimKII"),
        ("Daft Punk Harder Better Faster Stronger", "yydNF8tuVmU"),
        ("Dom Dolla Go Freek Define", "DMkZc_8-rfk"),
        ("Meduza Goodboys Piece Of Your Heart", "lFnSHafR8dw"),
        ("John Summit Hayla Where You Are", "5BqjhUmldDc"),
        ("Fred again Delilah pull me out of this", "Cl6Rz1Uvi2M"),
        ("Fred again Marea We've Lost Dancing", "l4UkYBr1NnA"),
        ("Calvin Harris Feel So Close", "KtqQYPUJk8E"),
        ("Anyma Ellie Goulding Hypnotized", "Hw9UnYyHz04"),
        ("Anyma Sevdaliza Samsara", "pUFgOq51Txg"),
        ("Mathame Skywalking", "eGS7HkNAfow"),
        ("Swedish House Mafia Don't You Worry Child", "6s1w7ryne14"),
        ("Avicii Levels", "TrmeHMzY9hs"),
        ("Soda Stereo De Música Ligera", "tNNciD5CHrk"),
        ("Los Fabulosos Cadillacs Matador", "eJo0CgYRKnc"),
        ("Patricio Rey y sus Redonditos de Ricota Jijiji", "CnJqYsSOgfg"),
        ("Divididos Qué ves", "YLp8uJ4TdiU"),
        ("Fito Páez Dar es dar", "PbKTO3zoXOw"),
        ("WOS Canguro", "l5QAOvBqT3c"),
        ("Emilia Duki Como Si No Importara", "00uPgn-8Nvg"),
        ("TINI Maria Becerra Miénteme", "XvLN495Cn08"),
        ("Milo J Bizarrap BZRP Music Sessions 57", "_6XzJPyAJDI"),
        ("L-Gante Bizarrap BZRP Music Sessions 38", "z7rI82hyels"),
        ("Anyma Chris Avantgarde Eternity Massano Remix", "tOkPhtWlLUI"),
    ],
    "Viernes con amigos": [
        ("Karol G Provenza", "kNgGWDn1zY4"),
        ("Maria Becerra Acaramelao", "yiG4lf06J20"),
        ("Nicki Nicole Colocao", "kh1sF-sbkbw"),
        ("Anitta Envolver", "hFCjGiawJi4"),
        ("Maria Becerra Becky G Wow Wow", "JVPWho8WHvg"),
        ("Los Palmeras Bombón Asesino", "UeFC_9oGqWg"),
        ("Gilda No me arrepiento de este amor", "8iUkmnLc1ec"),
        ("Ráfaga Muero de frío", "auNa0nRPg3o"),
        ("Agapornis La melodía de Dios", "1tKfIYrh14Y"),
        ("Los Pibes Chorros Duraznito", "qeE4NQM7ex4"),
        ("Rodrigo La mano de Dios", "EAk-l1VHzBw"),
        ("Mano Arriba Llamame más temprano", "A8noNO6rlf8"),
        ("Karol G Shakira TQG", "24o_iGVXlG0"),
        ("Anitta Funk Rave", "7kcAz2BH7Zg"),
        ("La Joaqui BZRP Music Sessions", "Ad2DbwHPO8g"),
        ("TINI La Joaqui Steve Aoki Muñecas", "7VO1XlJsNf0"),
        ("La Joaqui Lali Para Dos", "kC3uCL0bLDU"),
        ("Nicki Nicole Wapo Traketero", "0wLrPWueUJI"),
        ("Maria Becerra High", "2Indk29rH6U"),
        ("J Balvin Maria Becerra Qué Más Pues", "16A9Domrl_A"),
        ("Anitta J Balvin Downtown", "wlS6Ix7mA0w"),
        ("Karol G Bichota", "v-fgAIaTFf4"),
        ("Karol G Nicki Minaj Tusa", "ZEvhzK7lbEY"),
        ("Calvin Harris Dua Lipa One Kiss", "oCab3KiXveY"),
        ("David Guetta Bebe Rexha I'm Good Blue", "90RLzVUuXe4"),
        ("Tiësto The Business", "wJS9eb6_o00"),
        ("Meduza Becky Hill Goodboys Lose Control", "Qv4wzkiBA4g"),
        ("Chris Lake Turn Off The Lights", "ahSdkFlepJg"),
        ("Calvin Harris Rihanna This Is What You Came For", "K14MkwIsR9U"),
        ("Tiago PZK Bizarrap BZRP Music Sessions 48", "h7U8TqOVyxc"),
        ("Duki Bizarrap BZRP Music Sessions 50", "Gzs60iBgd3E"),
    ],
}


def resolver(yt, consulta, respaldo):
    """Devuelve (videoId, etiqueta). Prefiere la versión 'canción' de YouTube Music."""
    try:
        resultados = yt.search(consulta, filter="songs", limit=3)
    except Exception as e:  # red, bloqueo, etc.
        resultados = []
        print(f"    (búsqueda falló: {e})")
    for r in resultados:
        if r.get("videoId"):
            artistas = ", ".join(a["name"] for a in r.get("artists", []))
            return r["videoId"], f"canción: {r.get('title')} — {artistas}"
    return respaldo, "video (respaldo)"


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--dry-run", action="store_true", help="no crea nada, solo muestra")
    ap.add_argument("--only", help="nombre exacto de una sola playlist")
    args = ap.parse_args()

    nombres = [args.only] if args.only else list(PLAYLISTS)
    for n in nombres:
        if n not in PLAYLISTS:
            sys.exit(f"No existe la playlist '{n}'. Opciones: {', '.join(PLAYLISTS)}")

    yt = YTMusic() if args.dry_run else YTMusic(AUTH_FILE)

    for nombre in nombres:
        print(f"\n== {nombre}")
        ids = []
        for consulta, respaldo in PLAYLISTS[nombre]:
            vid, etiqueta = resolver(yt, consulta, respaldo)
            if vid not in ids:
                ids.append(vid)
            print(f"  - {consulta}\n      -> {etiqueta} [{vid}]")
        if args.dry_run:
            print(f"  (simulación: {len(ids)} temas, no se creó nada)")
            continue
        res = yt.create_playlist(
            nombre,
            "Creada con Claude Code",
            privacy_status="PRIVATE",
            video_ids=ids,
        )
        if isinstance(res, str):
            print(f"  LISTO ({len(ids)} temas): https://music.youtube.com/playlist?list={res}")
        else:
            print(f"  ERROR al crear: {res}")


if __name__ == "__main__":
    main()
