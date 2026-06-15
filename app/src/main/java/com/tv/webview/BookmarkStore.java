package com.tv.webview;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y recupera la lista de direcciones en SharedPreferences (formato JSON).
 * Persiste entre cierres de la app.
 */
public class BookmarkStore {

    private static final String PREFS = "tv_bookmarks";
    private static final String KEY = "items";

    private final SharedPreferences prefs;

    public BookmarkStore(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<Bookmark> load() {
        List<Bookmark> list = new ArrayList<>();
        String raw = prefs.getString(KEY, "[]");
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new Bookmark(o.optString("name"), o.optString("url")));
            }
        } catch (Exception ignored) {
        }
        return list;
    }

    public void save(List<Bookmark> list) {
        JSONArray arr = new JSONArray();
        try {
            for (Bookmark b : list) {
                JSONObject o = new JSONObject();
                o.put("name", b.name);
                o.put("url", b.url);
                arr.put(o);
            }
        } catch (Exception ignored) {
        }
        prefs.edit().putString(KEY, arr.toString()).apply();
    }
}
