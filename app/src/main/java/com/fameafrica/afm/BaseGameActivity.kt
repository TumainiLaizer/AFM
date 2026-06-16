package com.fameafrica.afm

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.fameafrica.afm.ui.util.ImmersiveModeManager

abstract class BaseGameActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyImmersiveMode()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applyImmersiveMode()
        }
    }

    override fun onResume() {
        super.onResume()
        applyImmersiveMode()
    }

    private fun applyImmersiveMode() {
        ImmersiveModeManager.enableImmersiveMode(window)
    }
}
