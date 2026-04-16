package com.code4u.chatsapp.call_feature.domain.usecase.ringtone_case

import com.code4u.chatsapp.call_feature.domain.repository.CallRingtoneRepo

class StopAllRingtone (
    private val callRingtoneRepo: CallRingtoneRepo
) {
    operator fun  invoke(){

        callRingtoneRepo.stopAllSounds()
    }
}