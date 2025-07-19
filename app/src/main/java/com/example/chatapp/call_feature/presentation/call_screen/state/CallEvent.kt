package com.example.chatapp.call_feature.presentation.call_screen.state

sealed class CallEvent {

    object InActive : CallEvent()
    object JoiningChannel : CallEvent() // when isJoined false
    object Ringing : CallEvent() // when isJoined true
    object Ongoing : CallEvent() // when remote user joined  uid is not null
    object Ended : CallEvent() // when call is ended (call ends when either user ends the call or remote user left is true)
}