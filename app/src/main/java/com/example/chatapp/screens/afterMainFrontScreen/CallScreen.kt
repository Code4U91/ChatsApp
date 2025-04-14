package com.example.chatapp.screens.afterMainFrontScreen

import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Call
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
import com.example.chatapp.screens.mainBottomBarScreens.requestPerm
import com.example.chatapp.viewmodel.CallViewModel
import com.example.chatapp.viewmodel.ChatsViewModel
import com.example.chatapp.viewmodel.GlobalMessageListenerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
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
    chatsViewModel: ChatsViewModel,
    callDocId: String,
    onCallEnd: () -> Unit
) {

    Log.i("TestChannelName", channelName) // using firebase uid user1_User2

    val context = LocalContext.current


    val callEnded by callViewModel.callEnded.collectAsState()  // called when clicked on call end button
    val remoteUserLeft by callViewModel.remoteUserLeft.collectAsState()  // when other user leaves call
    val remoteUserJoined by callViewModel.remoteUserJoined.collectAsState() // contains numeric agora id of other joined user
    val isJoined by callViewModel.isJoined.collectAsState()



    LaunchedEffect(Unit) {
        chatsViewModel.setCallScreenActive(true)
    }
    DisposableEffect(Unit) {
        onDispose {
            chatsViewModel.setCallScreenActive(false)
        }
    }


    LaunchedEffect(remoteUserLeft, callEnded) {
        if (callEnded || remoteUserLeft) {

            Log.i("ON_END_CALLED", "callEnd: $callEnded remoteUser : $remoteUserLeft ")

            callViewModel.stopCallService(context)
            chatsViewModel.resetCallServiceFlag()
            onCallEnd()
        }

    }

    // auto leave channel or call end if call not connected within 48seconds
    LaunchedEffect(remoteUserJoined) {

        if (isJoined) {
            // Wait for 45sec for the remote user to join

            delay(45000)

            if (remoteUserJoined == null) {
                // If still null after 45sec, end call
                callViewModel.leaveChannel()
            }
        }

    }

    // switch compose based on the call type passes as parameter to callScreen compose
    if (callType == "video") {
        StartVideoCall(
            callViewModel = callViewModel,
            channelName = channelName,
            isCaller,
            receiverId,
            globalMessageListenerViewModel,
            chatsViewModel,
            callDocId
        )
    } else {

        StartVoiceCall(
            callViewModel = callViewModel,
            channelName = channelName,
            isCaller = isCaller,
            receiverId,
            globalMessageListenerViewModel,
            chatsViewModel,
            callDocId
        )

    }

}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun StartVoiceCall(
    callViewModel: CallViewModel,
    channelName: String,
    isCaller: Boolean,
    receiverId: String,
    globalMessageListenerViewModel: GlobalMessageListenerViewModel,
    chatsViewModel: ChatsViewModel,
    callDocId: String
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
    val callEnded by callViewModel.callEnded.collectAsState() // may be replace with remoteUserLeft
    val context = LocalContext.current

    val permissionState = requestPerm()


    LaunchedEffect(isJoined, otherUserData, callEnded, permissionState) {

        if (permissionState.allPermissionsGranted)
        {
            if (!chatsViewModel.hasStartedCallService.value) {

                if (otherUserData != null && !isJoined && !callEnded) {

                    callViewModel.startCallService(
                        context = context,
                        channelName = channelName,
                        callType = "voice",
                        callerName = currentUserData?.name.orEmpty(),
                        receiverName = otherUserData?.name.orEmpty(),
                        isCaller = isCaller,
                        callReceiverId = otherUserData?.uid ?: receiverId,
                        callDocId = callDocId
                    )

                    chatsViewModel.markCallServiceStarted()
                }
            }
        } else {
            Toast.makeText(context, "Please grant all the required permissions to continue the call.", Toast.LENGTH_SHORT).show()
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
                ControlButtons(callType = "voice", callViewModel, isCaller)
                {

                    if (permissionState.allPermissionsGranted)
                    {
                        callViewModel.joinChannel(
                            channelName,
                            "voice"
                        )
                    } else {
                        Toast.makeText(context, "Can't join the call. Grant permissions to join the call", Toast.LENGTH_SHORT).show()
                    }

                }
            }

        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun StartVideoCall(
    callViewModel: CallViewModel,
    channelName: String,
    isCaller: Boolean,
    receiverId: String,
    globalMessageListenerViewModel: GlobalMessageListenerViewModel,
    chatsViewModel: ChatsViewModel,
    callDocId: String
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
    val permissionState = requestPerm()


    val activity = LocalActivity.current

    LaunchedEffect(isJoined, otherUserData, permissionState) {

        if (permissionState.allPermissionsGranted)
        {
            if (!chatsViewModel.hasStartedCallService.value) {

                if (otherUserData != null && !isJoined && !callEnded) {

                    callViewModel.enableVideoPreview()

                    callViewModel.startCallService(
                        context = context,
                        channelName = channelName,
                        callType = "video",
                        callerName = currentUserData?.name.orEmpty(),
                        receiverName = otherUserData?.name.orEmpty(),
                        isCaller = isCaller,
                        callReceiverId = otherUserData?.uid ?: receiverId,
                        callDocId = callDocId
                    )

                    chatsViewModel.markCallServiceStarted()
                }
            }
        }else {
            Toast.makeText(context, "Please grant all the required permissions to continue the call.", Toast.LENGTH_SHORT).show()
        }

    }


    LaunchedEffect(isJoined) {

        if (isJoined) {

            delay(200) // Small delay to ensure the SurfaceView is ready
            callViewModel.setUpLocalVideo(localView)

            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    LaunchedEffect(remoteUserJoined) {

        if (remoteUserJoined != null) {
            callViewModel.setUpRemoteVideo(remoteView, remoteUserJoined!!)
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
                    modifier = Modifier.align(Alignment.Center)
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
                            fontSize = 18.sp,
                            color = Color.Green
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
                    ControlButtons(
                        callType = "video",
                        callViewModel = callViewModel,
                        isCaller = isCaller
                    ) {
                        // when the call receiver accepts the call

                       if (permissionState.allPermissionsGranted)
                       {
                           callViewModel.joinChannel(
                               channelName,
                               "video"
                           )
                       } else {
                           Toast.makeText(context, "Can't join the call. Grant permissions to join the call", Toast.LENGTH_SHORT).show()
                       }

                    }
                }
            }

        }
    }
}

@Composable
fun ControlButtons(
    callType: String,
    callViewModel: CallViewModel,
    isCaller: Boolean,
    onJoinCall: () -> Unit
) {

    val isMuted by callViewModel.isMuted.collectAsState()
    val isRemoteAudioDeafen by callViewModel.isRemoteAudioDeafen.collectAsState()
    val isSpeakerPhoneEnabled by callViewModel.isSpeakerPhoneEnabled.collectAsState()
    val remoteUserJoined by callViewModel.remoteUserJoined.collectAsState()

    if (remoteUserJoined == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.BottomCenter
        ) {

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // call not connected, do not show ongoing call control buttons
                if (isCaller) {
                    // current user is a caller, only show call cut button till the remote user joins

                    // common


                    IconButton(onClick = {
                        callViewModel.leaveChannel()
                    }) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.Red,
                            modifier = Modifier
                                .size(250.dp)
                                .clip(CircleShape)
                        )
                    }
                } else {
                    // current user is not a caller but a receiver, give option to accept or reject the call

                    // cancel button
                    IconButton(onClick = {
                        callViewModel.declineTheCall(true)
                    }) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.Red,
                            modifier = Modifier
                                .size(200.dp)
                                .clip(CircleShape)
                        )
                    }

                    // accept button

                    IconButton(onClick = {
                        // start the call
                        onJoinCall()

                    }) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "End Call",
                            tint = Color.Green,
                            modifier = Modifier
                                .size(200.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }

        }


    } else {
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


}