package com.example.chatapp.call.domain.repository

import android.content.Context
import android.media.MediaPlayer

interface CallRingtoneRepo {

    fun playIncomingRingtone()

    fun playOutGoingRingtone(
        useSpeaker: Boolean = false
    )

    fun configureAudioRouting(
        context: Context,
        player: MediaPlayer,
        useSpeaker: Boolean = true
    )

    fun stopAllSounds()
}