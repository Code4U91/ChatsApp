package com.example.chatapp.call.domain.usecase.ringtone_case

import com.example.chatapp.call.domain.repository.CallRingtoneRepo

class PlayIncomingRingtone (
    private val callRingtoneRepo: CallRingtoneRepo
) {

    operator fun invoke(){

        callRingtoneRepo.playIncomingRingtone()
    }
}