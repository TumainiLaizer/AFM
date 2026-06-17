package com.fameafrica.afm.utils

import android.app.Activity
import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

object ImmersiveModeManager {

    /**
     * Modifier to ensure a layout truly fills the screen in immersive mode
     * and ignores any default system bar insets.
     */
    fun Modifier.immersiveRoot(): Modifier = this
        .windowInsetsPadding(WindowInsets(0, 0, 0, 0))

    @Composable
    fun ImmersiveScreen() {
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            (context as? Activity)?.window?.let {
                enableImmersiveMode(it)
            }
        }
    }

    fun enableImmersiveMode(window: Window) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        // Hide both status and navigation bars
        controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())

        // Use transient bars behavior - swipe from edge shows them temporarily
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Handle display cutout for modern devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val params = window.attributes
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = params
        }
    }

    fun disableImmersiveMode(window: Window) {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
    }
}