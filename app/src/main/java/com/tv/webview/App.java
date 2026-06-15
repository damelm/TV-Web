package com.tv.webview;

import android.app.Application;

/** Precarga la lista de bloqueo de anuncios al iniciar. */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AdBlocker.init(this);
    }
}
