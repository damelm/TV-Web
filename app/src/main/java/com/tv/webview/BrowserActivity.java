package com.tv.webview;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/** Navegador WebView con adblock, pop-ups, video a pantalla completa y modo cursor. */
public class BrowserActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "extra_url";

    private WebView webView;
    private FrameLayout root;

    // Pantalla completa de video
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private int savedUiVisibility;
    private long lastBackInFullscreen = 0;
    private static final long DOUBLE_BACK_MS = 1500;

    // Modo cursor
    private CursorView cursor;
    private boolean cursorMode = false;
    private float cx, cy;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean backLongFired = false;
    private final Runnable backLongRunnable = new Runnable() {
        @Override public void run() {
            backLongFired = true;
            setCursorMode(!cursorMode);
        }
    };

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String url = getIntent().getStringExtra(EXTRA_URL);
        if (url == null || url.trim().isEmpty()) {
            finish();
            return;
        }

        root = new FrameLayout(this);
        webView = new WebView(this);
        root.addView(webView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        cursor = new CursorView(this);
        cursor.setVisibility(View.GONE);
        root.addView(cursor, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        setContentView(root);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        s.setSupportMultipleWindows(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                try {
                    String host = request.getUrl().getHost();
                    if (AdBlocker.isAd(host)) {
                        return new WebResourceResponse("text/plain", "utf-8",
                                new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
                    }
                } catch (Exception ignored) {
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String scheme = request.getUrl().getScheme();
                if (scheme != null && (scheme.equals("http") || scheme.equals("https"))) {
                    return false;
                }
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog,
                                          boolean isUserGesture, Message resultMsg) {
                return false;
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    exitFullscreen();
                    return;
                }
                customView = view;
                customViewCallback = callback;
                savedUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
                webView.setVisibility(View.GONE);
                root.addView(customView, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                cursor.bringToFront(); // el cursor siempre encima
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }

            @Override
            public void onHideCustomView() {
                exitFullscreen();
            }
        });

        webView.loadUrl(url);
    }

    private void exitFullscreen() {
        if (customView == null) return;
        root.removeView(customView);
        customView = null;
        webView.setVisibility(View.VISIBLE);
        getWindow().getDecorView().setSystemUiVisibility(savedUiVisibility);
        if (customViewCallback != null) {
            customViewCallback.onCustomViewHidden();
            customViewCallback = null;
        }
    }

    // ---------- Modo cursor ----------

    private void setCursorMode(boolean enabled) {
        cursorMode = enabled;
        if (enabled) {
            if (cx <= 0 && cy <= 0) {
                cx = root.getWidth() > 0 ? root.getWidth() / 2f : 960;
                cy = root.getHeight() > 0 ? root.getHeight() / 2f : 540;
            }
            cursor.setVisibility(View.VISIBLE);
            cursor.bringToFront();
            cursor.setPos(cx, cy);
            Toast.makeText(this, "Cursor ON  (flechas mueven, OK clic, Atras sale)", Toast.LENGTH_SHORT).show();
        } else {
            cursor.setVisibility(View.GONE);
            Toast.makeText(this, "Cursor OFF", Toast.LENGTH_SHORT).show();
        }
    }

    private void moveCursor(int dx, int dy, int repeat) {
        int step = 45 + Math.min(repeat * 10, 160); // acelera al mantener pulsado
        cx += dx * step;
        cy += dy * step;
        int w = root.getWidth(), h = root.getHeight();
        if (cx < 0) cx = 0; if (cx > w) cx = w;
        if (cy < 0) cy = 0; if (cy > h) cy = h;
        cursor.setPos(cx, cy);
    }

    private void clickAtCursor() {
        View target = customView != null ? customView : webView;
        if (target == null) return;
        long t = SystemClock.uptimeMillis();
        MotionEvent down = MotionEvent.obtain(t, t, MotionEvent.ACTION_DOWN, cx, cy, 0);
        MotionEvent up = MotionEvent.obtain(t, t + 60, MotionEvent.ACTION_UP, cx, cy, 0);
        target.dispatchTouchEvent(down);
        target.dispatchTouchEvent(up);
        down.recycle();
        up.recycle();
    }

    private void dispatchEscToWeb() {
        View target = customView != null ? customView : webView;
        if (target == null) return;
        long t = SystemClock.uptimeMillis();
        target.dispatchKeyEvent(new KeyEvent(t, t, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE, 0));
        target.dispatchKeyEvent(new KeyEvent(t, t, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ESCAPE, 0));
    }

    private void handleShortBack() {
        if (cursorMode) {                 // salir del modo cursor
            setCursorMode(false);
            return;
        }
        if (customView != null) {         // pantalla completa de video
            long now = SystemClock.uptimeMillis();
            if (now - lastBackInFullscreen < DOUBLE_BACK_MS) {
                exitFullscreen();
            } else {
                lastBackInFullscreen = now;
                dispatchEscToWeb();
            }
            return;
        }
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();                     // volver a la lista de direcciones
        }
    }

    // Intercepta todas las teclas ANTES que el WebView.
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int code = event.getKeyCode();
        int action = event.getAction();

        // --- Atras: corto = accion normal; largo = activar/desactivar cursor ---
        if (code == KeyEvent.KEYCODE_BACK) {
            if (action == KeyEvent.ACTION_DOWN) {
                if (event.getRepeatCount() == 0) {
                    backLongFired = false;
                    handler.postDelayed(backLongRunnable, 600);
                }
            } else if (action == KeyEvent.ACTION_UP) {
                handler.removeCallbacks(backLongRunnable);
                if (!backLongFired) handleShortBack();
            }
            return true;
        }

        // --- En modo cursor, el D-pad mueve el puntero y OK hace clic ---
        if (cursorMode) {
            if (action == KeyEvent.ACTION_DOWN) {
                switch (code) {
                    case KeyEvent.KEYCODE_DPAD_UP:    moveCursor(0, -1, event.getRepeatCount()); return true;
                    case KeyEvent.KEYCODE_DPAD_DOWN:  moveCursor(0, 1, event.getRepeatCount());  return true;
                    case KeyEvent.KEYCODE_DPAD_LEFT:  moveCursor(-1, 0, event.getRepeatCount()); return true;
                    case KeyEvent.KEYCODE_DPAD_RIGHT: moveCursor(1, 0, event.getRepeatCount());  return true;
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:      clickAtCursor(); return true;
                }
            } else if (action == KeyEvent.ACTION_UP) {
                switch (code) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:      return true;
                }
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(backLongRunnable);
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
