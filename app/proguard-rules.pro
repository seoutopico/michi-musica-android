# Media3 and Compose publish the consumer rules required by the app.

# JavaScript invokes these event methods by name in the embedded video player.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
