package com.ainalluna.michimusica.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

/** No music, network, user preferences or folder access. Not present in public release APKs. */
class ShowcaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val page = intent.getStringExtra("screen")
        val bars = if (page == "rose") SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
            else SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            when(page) {
                "search" -> SearchPreview()
                "lists" -> ListsPreviewContent()
                "player" -> PlayerPreviewContent()
                "lyrics" -> LyricsPreviewContent()
                "rose" -> LibraryHomePreviewContent(skin = MichiSkin.ROSE)
                else -> LibraryHomePreviewContent()
            }
        }
    }
}
