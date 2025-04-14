package com.example.chatapp.repository

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRingtoneManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var incomingRingTonePlayer: Ringtone? = null
    private var outgoingRingTonePlayer: MediaPlayer? = null


     fun playIncomingRingtone() {


        stopAllSounds()

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        incomingRingTonePlayer = RingtoneManager.getRingtone(context, uri)

        incomingRingTonePlayer?.apply {
            audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            play()
        }
    }

     fun playOutGoingRingtone(
        useSpeaker: Boolean = false
    ) {
        stopAllSounds()

        val uri = Uri.parse("android.resource://${context.packageName}/raw/ringback")
        outgoingRingTonePlayer = MediaPlayer().apply {
            try {

                setDataSource(context, uri)
                isLooping = true
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                prepare()
                configureAudioRouting(context, this, useSpeaker)
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun configureAudioRouting(
        context: Context,
        player: MediaPlayer,
        useSpeaker: Boolean = true
    ) {

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val devices = audioManager.getDevices(
                AudioManager.GET_DEVICES_OUTPUTS
            )

            val targetDevice = devices.firstOrNull { device ->

                if (useSpeaker) {
                    device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                } else {
                    device.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
                }
            }

            if (targetDevice != null) {
                player.setPreferredDevice(targetDevice)
            }
        } else {

            // Fallback for older version
            @Suppress("DEPRECATION")
            audioManager.isSpeakerphoneOn = useSpeaker
        }
    }

    fun stopAllSounds() {

        incomingRingTonePlayer?.stop()
        incomingRingTonePlayer = null

        outgoingRingTonePlayer?.apply {
            stop()
            release()
        }
        outgoingRingTonePlayer = null
    }
}