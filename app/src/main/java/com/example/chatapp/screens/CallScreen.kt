package com.example.chatapp.screens

import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.HearingDisabled
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import com.example.chatapp.FriendData
import com.example.chatapp.formatCallDuration
import com.example.chatapp.viewmodel.CallViewModel
import com.example.chatapp.viewmodel.GlobalMessageListenerViewModel
import kotlinx.coroutines.delay

// Active call screen

@Composable
fun CallScreen(
    channelName: String,
    callType: String,
    callViewModel: CallViewModel = hiltViewModel(),
    globalMessageListenerViewModel: GlobalMessageListenerViewModel,
    receiverId: String,
    isCaller: Boolean,
    onCallEnd: () -> Unit
) {

    Log.i("TestChannelName", channelName) // using firebase uid user1_User2

    LaunchedEffect(Unit) {
        callViewModel.updateIsCaller(isCaller)
    }

    val context = LocalContext.current

    val callEnded by callViewModel.callEnded.collectAsState()  // called when clicked on call end button
    val remoteUserLeft by callViewModel.remoteUserLeft.collectAsState()  // when other user leaves call
    val remoteUserJoined by callViewModel.remoteUserJoined.collectAsState() // contains numeric agora id of other joined user

    LaunchedEffect(remoteUserLeft, callEnded) {
        if (callEnded || remoteUserLeft) {
            callViewModel.stopCallService(context)
            onCallEnd()
        }

    }

    // auto leave channel or call end if call not connected within 48seconds
    LaunchedEffect(remoteUserJoined) {

        // Wait for 48sec for the remote user to join
        delay(1 * 60 * 800)

        if (remoteUserJoined == null) {
            // If still null after 48sec, end call
            callViewModel.leaveChannel()
        }

    }

    // switch compose based on the call type passes as parameter to callScreen compose
    if (callType == "video") {
        StartVideoCall(
            callViewModel = callViewModel,
            channelName = channelName,
            isCaller,
            receiverId,
            globalMessageListenerViewModel
        )
    } else {

        StartVoiceCall(
            callViewModel = callViewModel,
            channelName = channelName,
            isCaller = isCaller,
            receiverId,
            globalMessageListenerViewModel
        )

    }

}

@Composable
fun StartVoiceCall(
    callViewModel: CallViewModel,
    channelName: String,
    isCaller: Boolean,
    receiverId: String,
    globalMessageListenerViewModel: GlobalMessageListenerViewModel
) {

    val otherUserData by produceState<FriendData?>(initialValue = null, key1 = receiverId)
    {
        val listener = globalMessageListenerViewModel.fetchFriendData(receiverId)
        { data ->
            value = data
        }

        awaitDispose {

            listener.remove()
        }
    }

    val currentUserData by globalMessageListenerViewModel.userData.collectAsState()
    val isJoined by callViewModel.isJoined.collectAsState()
    val remoteUserJoined by callViewModel.remoteUserJoined.collectAsState()
    val callDuration by callViewModel.callDuration.collectAsState()
    val callEnded by callViewModel.callEnded.collectAsState()
    val context = LocalContext.current


    LaunchedEffect(isJoined, otherUserData) {

        if (otherUserData!= null && !isJoined && !callEnded) {

                callViewModel.startCallService(
                    context = context,
                    channelName = channelName,
                    callType = "voice",
                    callerName = currentUserData?.name.orEmpty(),
                    receiverName = otherUserData?.name.orEmpty(),
                    isCaller = isCaller,
                    callReceiverId = otherUserData?.uid ?: receiverId
                )
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            if (isJoined) {

                // engine created waiting for other user to join call

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Image(
                        painter = rememberAsyncImagePainter(model = otherUserData?.photoUrl),
                        contentDescription = "profile picture",
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(150.dp)
                            .border(1.dp, Color.Gray, shape = CircleShape),
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = otherUserData?.name ?: "",
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))


                    if (remoteUserJoined != null) {  // remote/other user joined the call




                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Call active: ",
                                color = Color.Green,
                                fontSize = 18.sp
                            )

                            Text(
                                text = formatCallDuration(callDuration),
                                fontSize = 18.sp
                            )
                        }

                    } else {
                        Text(
                            text = "waiting for other user to join the call",
                            fontSize = 18.sp

                        )
                    }

                }

            } else {
                Text(
                    text = "Joining Channel...",
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Control Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ControlButtons(callType = "voice", callViewModel)
            }

        }
    }
}


@Composable
fun StartVideoCall(
    callViewModel: CallViewModel,
    channelName: String,
    isCaller: Boolean,
    receiverId: String,
    globalMessageListenerViewModel: GlobalMessageListenerViewModel
) {

    val otherUserData by produceState<FriendData?>(initialValue = null, key1 = receiverId)
    {
        val listener = globalMessageListenerViewModel.fetchFriendData(receiverId)
        { data ->
            value = data
        }

        awaitDispose {

            listener.remove()
        }
    }

    val currentUserData by globalMessageListenerViewModel.userData.collectAsState()
    val context = LocalContext.current
    val isJoined by callViewModel.isJoined.collectAsState()
    val remoteUserJoined by callViewModel.remoteUserJoined.collectAsState() // remote user numeric id
    val callDuration by callViewModel.callDuration.collectAsState()
    val callEnded by callViewModel.callEnded.collectAsState()

    // Create SurfaceViews for Local and Remote video
    val localView by rememberUpdatedState(SurfaceView(context))
    val remoteView by rememberUpdatedState(SurfaceView(context))


    val activity = LocalActivity.current

    LaunchedEffect(isJoined, otherUserData) {

        if (otherUserData!= null && !isJoined && !callEnded) {

            callViewModel.enableVideoPreview()

            callViewModel.startCallService(
                context = context,
                channelName = channelName,
                callType = "video",
                callerName = currentUserData?.name.orEmpty(),
                receiverName = otherUserData?.name.orEmpty(),
                isCaller = isCaller,
                callReceiverId = otherUserData?.uid ?: receiverId
            )
        }
    }

//    LaunchedEffect(Unit) {
//         // camera preview to show before joining the call
//        //callViewModel.joinChannel(null, channelName, "video")
//    }

    LaunchedEffect(isJoined) {

        if (isJoined) {

            delay(200)
            callViewModel.setUpLocalVideo(localView)
            // Small delay to ensure the SurfaceView is ready
            //callViewModel.setUpLocalVideo(localView) // Force rebind

            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    LaunchedEffect(remoteUserJoined) {

        if (remoteUserJoined != null) {


            callViewModel.setUpRemoteVideo(remoteView, remoteUserJoined!!)
            //callViewModel.setUpLocalVideo(localView) // force rebind
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {

            if (isJoined) {

                // screen context
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {

                    // when the remote user is connected and call is established
                    if (remoteUserJoined != null) {


                        // remote view (full screen)
                        AndroidView(factory = { remoteView }, modifier = Modifier.fillMaxSize())

                        // Local video (Mini-screen) - Floating at bottom end
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .background(Color.Black, shape = RoundedCornerShape(8.dp))
                        ) {
                            AndroidView(factory = { localView }, modifier = Modifier.fillMaxSize())
                        }

                    } else {

                        // if other user is not connected show local user's video preview
                        AndroidView(factory = { localView }, modifier = Modifier.fillMaxSize())
                    }
                }
            } else {
                Text(
                    text = "Joining Channel...",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }


            // Control Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Call Duration Display
                if (remoteUserJoined != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Call active: ",
                            color = Color.Green,
                            fontSize = 18.sp
                        )
                        Text(
                            text = formatCallDuration(callDuration),
                            fontSize = 18.sp
                        )
                    }
                } else {

                    Text(
                        text = "Video Calling....",
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Control Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ControlButtons(callType = "video", callViewModel = callViewModel)
                }
            }

        }
    }
}

@Composable
fun ControlButtons(callType: String, callViewModel: CallViewModel) {
    val isMuted by callViewModel.isMuted.collectAsState()
    val isRemoteAudioDeafen by callViewModel.isRemoteAudioDeafen.collectAsState()
    val isSpeakerPhoneEnabled by callViewModel.isSpeakerPhoneEnabled.collectAsState()


    //common
    IconButton(onClick = { callViewModel.muteOutgoingAudio() }) {
        Icon(
            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
            contentDescription = "Mute",
        )
    }

    // video/call/common
    IconButton(onClick = { callViewModel.muteYourSpeaker() }) {
        Icon(
            imageVector = if (isRemoteAudioDeafen) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = "audio turn on/off",

            )
    }

    // common
    IconButton(onClick = {
        callViewModel.leaveChannel()
    }) {
        Icon(
            imageVector = Icons.Default.CallEnd,
            contentDescription = "End Call",
            tint = Color.Red
        )
    }


    if (callType == "voice") {
        // only voice call
        IconButton(onClick = { callViewModel.toggleSpeaker() }) {
            Icon(
                imageVector = if (isSpeakerPhoneEnabled) Icons.Default.Hearing else Icons.Default.HearingDisabled,
                contentDescription = "voice mode : speaker or earpiece"

            )
        }
    } else {
        // only video
        IconButton(onClick = { callViewModel.switchCamera() }) {
            Icon(
                imageVector = Icons.Default.FlipCameraAndroid,
                contentDescription = "Switch Camera"
            )
        }
    }

}