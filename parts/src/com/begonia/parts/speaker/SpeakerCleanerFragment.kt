package com.begonia.parts.speaker

import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.preference.SwitchPreferenceCompat
import com.android.settingslib.collapsingtoolbar.BasePreferencesFragment
import com.begonia.parts.R
import java.io.IOException

class SpeakerCleanerFragment : BasePreferencesFragment() {

    companion object {
        private const val TAG = "SpeakerCleaner"
        private const val KEY_TONE = "speaker_flush_tone"
        private const val SWEEP_MILLIS = 30_000L
    }

    private val sweeper = Handler(Looper.getMainLooper())
    private var player: MediaPlayer? = null
    private var toneSwitch: SwitchPreferenceCompat? = null

    override fun getTitle(): CharSequence = getString(R.string.speaker_title)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.speaker_cleaner, rootKey)

        toneSwitch = findPreference<SwitchPreferenceCompat>(KEY_TONE)
        toneSwitch?.setOnPreferenceChangeListener { _, newValue ->
            val wanted = newValue as Boolean
            if (wanted && beginSweep()) {
                sweeper.removeCallbacksAndMessages(null)
                sweeper.postDelayed(::haltSweep, SWEEP_MILLIS)
                true
            } else {
                false
            }
        }
    }

    override fun onStop() {
        super.onStop()
        haltSweep()
    }

    private fun beginSweep(): Boolean {
        requireActivity().volumeControlStream = AudioManager.STREAM_MUSIC
        val next = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            isLooping = true
            setVolume(1.0f, 1.0f)

            setOnErrorListener { _, what, extra ->
                Log.e(TAG, "Sweep playback error: what=$what extra=$extra")
                haltSweep()
                true
            }
        }
        try {
            resources.openRawResourceFd(R.raw.clear_speaker_sound).use { fd: AssetFileDescriptor ->
                next.setDataSource(fd)
            }
            next.prepare()

            player = next
            toneSwitch?.isEnabled = false
            next.start()
        } catch (e: IOException | IllegalArgumentException | IllegalStateException) {
            Log.e(TAG, "Unable to begin speaker sweep", e)
            next.reset()
            next.release()
            return false
        }
        return true
    }

    private fun haltSweep() {
        sweeper.removeCallbacksAndMessages(null)
        val active = player
        player = null
        if (active != null) {
            try {
                active.stop()
            } catch (e: IllegalStateException) {
                Log.w(TAG, "Player already stopped", e)
            } finally {
                active.reset()
                active.release()
            }
        }
        toneSwitch?.isEnabled = true
        toneSwitch?.isChecked = false
    }
}