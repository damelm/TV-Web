# Mantener WebView con interfaces JavaScript si se usaran en el futuro
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
