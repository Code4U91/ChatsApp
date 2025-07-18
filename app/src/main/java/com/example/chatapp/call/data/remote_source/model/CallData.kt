package com.example.chatapp.call.data.remote_source.model

import com.google.firebase.Timestamp

data class CallData(
    val callId: String = "", // call document id
    val callerId: String = "", // call initiate or caller/ sender id
    val callReceiverId: String = "", // call receiver
    val callType: String = "", // type of the call, video or voice
    val channelId: String = "", // channel name for agora join
    val status: String = "", // status of the call
    val callStartTime: Timestamp? = null, // call start time
    val callEndTime: Timestamp? =  null, // call end time
    val otherUserName: String = "", // other participant user name
    val otherUserId: String = "" //other participant of the call


)