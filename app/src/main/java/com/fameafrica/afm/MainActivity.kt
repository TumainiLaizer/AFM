package com.fameafrica.afm

import android.os.Bundle
import androidx.activity.compose.setContent
import com.fameafrica.afm.ui.audio.AudioManager
import com.fameafrica.afm.ui.main.MainScreen
import com.fameafrica.afm.ui.theme.AFM2026Theme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseGameActivity() {

    @Inject
    lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            AFM2026Theme {
                // Handle audio initialization and lifecycle in a controlled way
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    audioManager.startBGM()
                }

                // Observe lifecycle for audio pause/resume
                androidx.compose.runtime.DisposableEffect(this) {
                    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                        when (event) {
                            androidx.lifecycle.Lifecycle.Event.ON_START -> audioManager.onAppForeground()
                            androidx.lifecycle.Lifecycle.Event.ON_STOP -> audioManager.onAppBackground()
                            else -> {}
                        }
                    }
                    lifecycle.addObserver(observer)
                    onDispose {
                        lifecycle.removeObserver(observer)
                    }
                }

                // MainScreen now acts as the top-level container for the app's navigation
                MainScreen()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // audioManager.onAppBackground() // Handled by DisposableEffect now
    }
}
