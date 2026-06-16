package com.fameafrica.afm.ui.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.util.Log
import com.fameafrica.afm.data.database.dao.GameSettingsDao
import com.fameafrica.afm.utils.SettingsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsDaoProvider: javax.inject.Provider<GameSettingsDao>,
    private val settingsManager: SettingsManager
) {
    private val TAG = "AFM_AudioManager"
    private val audioScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var bgmPlayer: MediaPlayer? = null
    private var ambiencePlayer: MediaPlayer? = null

    private var musicVolume = 0.5f
    private var ambienceVolume = 0.3f
    private var isMusicEnabled = false
    private var isSoundEnabled = true
    
    private var currentIntensity = 0.0f

    init {
        observeSettings()
    }

    private fun observeSettings() {
        audioScope.launch {
            settingsDaoProvider.get().getSettings().collectLatest { settings ->
                settings?.let {
                    isMusicEnabled = it.music
                    isSoundEnabled = it.soundEnabled
                    
                    // React to toggle changes
                    if (!isMusicEnabled) {
                        pauseBGM()
                    } else if (ambiencePlayer == null) {
                        startBGM()
                    }
                    
                    if (!isSoundEnabled) {
                        stopAmbience()
                    }
                }
            }
        }

        audioScope.launch {
            musicVolume = settingsManager.getPreference("music_volume", 0.5f)
            ambienceVolume = settingsManager.getPreference("ambience_volume", 0.3f)
            updateVolumes()
        }
    }

    private fun MediaPlayer?.isSafePlaying(): Boolean {
        return try {
            this?.isPlaying == true
        } catch (e: IllegalStateException) {
            false
        }
    }

    fun startBGM() {
        if (!isMusicEnabled || ambiencePlayer.isSafePlaying()) return
        
        if (bgmPlayer == null) {
            try {
                val player = MediaPlayer()
                val descriptor: AssetFileDescriptor = context.assets.openFd("music/background_africa_rising.ogg")
                player.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                descriptor.close()
                player.isLooping = true
                player.setVolume(musicVolume, musicVolume)
                player.prepare()
                player.start()
                bgmPlayer = player
                Log.d(TAG, "BGM Started")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting BGM (ensure assets/music/background_africa_rising.ogg exists): ${e.message}")
            }
        } else if (!bgmPlayer.isSafePlaying()) {
            try {
                bgmPlayer?.start()
            } catch (e: Exception) {
                Log.e(TAG, "Error resuming BGM: ${e.message}")
            }
        }
    }

    fun pauseBGM() {
        if (bgmPlayer.isSafePlaying()) {
            try {
                bgmPlayer?.pause()
            } catch (e: Exception) {
                Log.e(TAG, "Error pausing BGM: ${e.message}")
            }
        }
    }

    fun stopBGM() {
        try {
            bgmPlayer?.stop()
            bgmPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping BGM: ${e.message}")
        } finally {
            bgmPlayer = null
        }
    }

    fun startAmbience() {
        if (!isSoundEnabled) return
        
        // Pause BGM when match ambience starts as per requirement
        pauseBGM()

        if (ambiencePlayer == null) {
            try {
                val resId = context.resources.getIdentifier("crowd_noise", "raw", context.packageName)
                if (resId != 0) {
                    val player = MediaPlayer.create(context, resId)
                    if (player != null) {
                        player.isLooping = true
                        player.setVolume(0f, 0f) // Start muted for fade-in
                        player.start()
                        fadeIn(player, ambienceVolume)
                        ambiencePlayer = player
                        Log.d(TAG, "Ambience Started")
                    }
                } else {
                    Log.w(TAG, "crowd_noise.ogg not found in res/raw")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting Ambience: ${e.message}")
            }
        }
    }

    fun playGoalCelebration() {
        if (!isSoundEnabled) return
        
        audioScope.launch {
            try {
                val resId = context.resources.getIdentifier("goal_celebration", "raw", context.packageName)
                if (resId != 0) {
                    val player = MediaPlayer.create(context, resId)
                    if (player != null) {
                        // Temporarily increase intensity for the crowd noise too
                        val prevIntensity = currentIntensity
                        setMatchIntensity(1.0f)
                        
                        player.setVolume(ambienceVolume * 1.5f, ambienceVolume * 1.5f)
                        player.start()
                        player.setOnCompletionListener {
                            it.release()
                            // Restore intensity after celebration ends
                            setMatchIntensity(prevIntensity)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing goal celebration: ${e.message}")
            }
        }
    }

    fun setMatchIntensity(intensity: Float) {
        currentIntensity = intensity.coerceIn(0.0f, 1.0f)
        if (ambiencePlayer.isSafePlaying()) {
            val dynamicVolume = ambienceVolume * (1.0f + currentIntensity * 0.4f)
            val finalVolume = dynamicVolume.coerceAtMost(1.0f)
            try {
                ambiencePlayer?.setVolume(finalVolume, finalVolume)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting ambience volume: ${e.message}")
            }
        }
    }

    fun stopAmbience() {
        val player = ambiencePlayer ?: run {
            if (isMusicEnabled) startBGM()
            return
        }
        ambiencePlayer = null // Clear immediately to avoid multiple stop calls
        fadeOut(player) {
            try {
                player.stop()
                player.release()
                Log.d(TAG, "Ambience Stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing ambience player: ${e.message}")
            }
            // Resume BGM when match ends
            if (isMusicEnabled) startBGM()
        }
    }

    private fun fadeIn(player: MediaPlayer, targetVolume: Float) {
        val duration = 2000L
        val interval = 50L
        val steps = (duration / interval).toInt()
        val volumeStep = targetVolume / steps

        var currentVol = 0f
        audioScope.launch {
            try {
                for (i in 1..steps) {
                    if (!player.isPlaying) break
                    currentVol += volumeStep
                    player.setVolume(currentVol, currentVol)
                    delay(interval)
                }
                if (player.isPlaying) {
                    player.setVolume(targetVolume, targetVolume)
                }
            } catch (e: Exception) {
                Log.w(TAG, "MediaPlayer state error during fadeIn: ${e.message}")
            }
        }
    }

    private fun fadeOut(player: MediaPlayer, onFinished: () -> Unit) {
        val duration = 1500L
        val interval = 50L
        val steps = (duration / interval).toInt()
        val currentVolAtStart = ambienceVolume * (1.0f + currentIntensity * 0.4f)
        val volumeStep = currentVolAtStart / steps

        var currentVol = currentVolAtStart
        audioScope.launch {
            try {
                for (i in 1..steps) {
                    if (!player.isPlaying) break
                    currentVol -= volumeStep
                    if (currentVol < 0) currentVol = 0f
                    player.setVolume(currentVol, currentVol)
                    delay(interval)
                }
            } catch (e: Exception) {
                Log.w(TAG, "MediaPlayer state error during fadeOut: ${e.message}")
            } finally {
                onFinished()
            }
        }
    }

    private fun updateVolumes() {
        try {
            bgmPlayer?.setVolume(musicVolume, musicVolume)
            ambiencePlayer?.setVolume(ambienceVolume, ambienceVolume)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating volumes: ${e.message}")
        }
    }

    fun setMusicVolume(volume: Float) {
        musicVolume = volume
        try {
            bgmPlayer?.setVolume(volume, volume)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting music volume: ${e.message}")
        }
        audioScope.launch { settingsManager.setPreference("music_volume", volume) }
    }

    fun setAmbienceVolume(volume: Float) {
        ambienceVolume = volume
        setMatchIntensity(currentIntensity) // Refresh volume with current intensity
        audioScope.launch { settingsManager.setPreference("ambience_volume", volume) }
    }

    fun onAppForeground() {
        if (isMusicEnabled && bgmPlayer != null && ambiencePlayer == null) {
            try {
                bgmPlayer?.start()
            } catch (e: Exception) {
                Log.e(TAG, "Error starting BGM on foreground: ${e.message}")
            }
        } else if (isSoundEnabled && ambiencePlayer != null) {
            try {
                ambiencePlayer?.start()
            } catch (e: Exception) {
                Log.e(TAG, "Error starting ambience on foreground: ${e.message}")
            }
        }
    }

    fun onAppBackground() {
        try {
            bgmPlayer?.pause()
            ambiencePlayer?.pause()
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing players on background: ${e.message}")
        }
    }
}
