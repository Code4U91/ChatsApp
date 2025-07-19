package com.example.chatapp.call_feature.data.remote_source.repositoryImpl

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import androidx.core.net.toUri
import com.example.chatapp.call_feature.domain.repository.CallRingtoneRepo
import java.io.IOException

// manage incoming and outgoing ringtones
class CallRingtoneManagerIml (
     private val context: Context
) : CallRingtoneRepo {

    private var incomingRingTonePlayer: Ringtone? = null
    private var outgoingRingTonePlayer: MediaPlayer? = null


     override fun playIncomingRingtone() {


        stopAllSounds()

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        incomingRingTonePlayer = RingtoneManager.getRingtone(context, uri)

        incomingRingTonePlayer?.apply {
            audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            {
                incomingRingTonePlayer?.isLooping = true
            }


            play()
        }
    }

     override fun playOutgoingRingtone(
        useSpeaker: Boolean
     ) {
        stopAllSounds()

        val uri = "android.resource://${context.packageName}/raw/ringback".toUri()
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

    override fun configureAudioRouting(
        context: Context,
        player: MediaPlayer,
        useSpeaker: Boolean
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
                player.preferredDevice = targetDevice
            }
        } else {

            // Fallback for older version
            @Suppress("DEPRECATION")
            audioManager.isSpeakerphoneOn = useSpeaker
        }
    }

    override fun stopAllSounds() {

        incomingRingTonePlayer?.stop()
        incomingRingTonePlayer = null

        outgoingRingTonePlayer?.apply {
            stop()
            release()
        }
        outgoingRingTonePlayer = null
    }
}