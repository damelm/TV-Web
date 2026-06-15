package com.tv.webview;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * Bloqueador de anuncios por lista de dominios (assets/adblock/hosts.txt).
 * Carga la lista una sola vez en memoria y comprueba host + dominios padre.
 */
public class AdBlocker {

    private static final String TAG = "TVWeb";
    private static final Set<String> BLOCKED = new HashSet<>();
    private static volatile boolean loaded = false;

    /** Carga la lista en segundo plano (llamar al iniciar la app). */
    public static void init(final Context ctx) {
        if (loaded) return;
        new Thread(() -> load(ctx.getApplicationContext())).start();
    }

    private static synchronized void load(Context ctx) {
        if (loaded) return;
        long t0 = System.currentTimeMillis();
        try (InputStream is = ctx.getAssets().open("adblock/hosts.txt");
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && line.charAt(0) != '#') {
                    BLOCKED.add(line);
                }
            }
            Log.i(TAG, "AdBlock: " + BLOCKED.size() + " dominios en "
                    + (System.currentTimeMillis() - t0) + " ms");
        } catch (Exception e) {
            Log.e(TAG, "AdBlock: error cargando lista", e);
        }
        loaded = true;
    }

    /** true si el host (o un dominio padre) esta en la lista de bloqueo. */
    public static boolean isAd(String host) {
        if (host == null || BLOCKED.isEmpty()) return false;
        host = host.toLowerCase();
        // Comprueba host completo y va quitando subdominios: a.b.c -> b.c -> c
        int idx = 0;
        while (idx != -1) {
            String candidate = host.substring(idx);
            if (BLOCKED.contains(candidate)) return true;
            idx = host.indexOf('.', idx);
            if (idx != -1) idx += 1;
        }
        return false;
    }
}
