package com.code4u.chatsapp.call_feature.domain.usecase.ringtone_case

import com.code4u.chatsapp.call_feature.domain.repository.CallRingtoneRepo

class PlayOutgoingRingtone (
    private val callRingtoneRepo: CallRingtoneRepo
) {
    operator fun invoke(useSpeaker : Boolean){
        callRingtoneRepo.playOutgoingRingtone(useSpeaker)
    }
}