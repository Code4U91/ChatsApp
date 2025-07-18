package com.example.chatapp.call.domain.usecase.ringtone_case

import com.example.chatapp.call.domain.repository.CallRingtoneRepo

class PlayOutgoingRingtone (
    private val callRingtoneRepo: CallRingtoneRepo
) {
    operator fun invoke(useSpeaker : Boolean){
        callRingtoneRepo.playOutgoingRingtone(useSpeaker)
    }
}