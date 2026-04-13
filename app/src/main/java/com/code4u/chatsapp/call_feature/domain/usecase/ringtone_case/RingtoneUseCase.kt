package com.code4u.chatsapp.call_feature.domain.usecase.ringtone_case

data class RingtoneUseCase(
    val playIncomingRingtone: PlayIncomingRingtone,
    val playOutgoingRingtone: PlayOutgoingRingtone,
    val stopAllRingtone: StopAllRingtone
)
