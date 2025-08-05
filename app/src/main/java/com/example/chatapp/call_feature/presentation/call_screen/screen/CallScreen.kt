package com.example.chatapp.call_feature.presentation.call_screen.screen

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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.rememberAsyncImagePainter
import com.example.chatapp.call_feature.presentation.call_screen.state.CallEvent
import com.example.chatapp.call_feature.presentation.call_screen.state.CallUIState
import com.example.chatapp.call_feature.presentation.call_screen.viewmodel.CallViewModel
import com.example.chatapp.core.util.formatCallDuration
import com.example.chatapp.core.model.CallMetadata
import com.example.chatapp.chat_feature.presentation.requestPerm
import com.google.accompanist.permissions.ExperimentalPermissionsApi

// Active call screen

@Composable
fun CallScreen(
    callViewModel: CallViewModel
) {

    val callState by callViewModel.uiState.collectAsState()
    val callEvent by callViewModel.callEvent.collectAsState()

    callState.callMetadata?.let { callMetadata ->

        if (callMetadata.callType == "video") {
            StartVideoCall(
                callViewModel = callViewModel,
                callScreenData =   callMetadata,
                callUIState = callState,
                callEvent = callEvent
            )
        } else {

            StartVoiceCall(
                callViewModel = callViewModel,
                callScreenData =  callMetadata,
                callUIState = callState,
                callEvent = callEvent
            )

        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun StartVoiceCall(
    callViewModel: CallViewModel,
    callScreenData: CallMetadata,
    callUIState: CallUIState,
    callEvent: CallEvent
) {

    val isJoined by callViewModel.isJoined.collectAsState()
    val callDuration by callViewModel.callDuration.collectAsState()

    val context = LocalContext.current

    val permissionState = requestPerm()


    LaunchedEffect(isJoined, callUIState.callEnded, permissionState) {

        if (permissionState.allPermissionsGranted) {
            if (!callUIState.hasStartedService) {

                if (!isJoined && !callUIState.callEnded && callScreenData.isCaller) {

                    callViewModel.startCallService(
                        context = context,
                         callMetadata = callScreenData
                    )

                    callViewModel.markCallServiceStarted()
                }
            }
        } else {
            Toast.makeText(
                context,
                "Please grant all the required permissions to continue the call.",
                Toast.LENGTH_SHORT
            ).show()
        }


    }

    Scaffold(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {


            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if(callEvent !is CallEvent.Ended){
                    Image(
                        painter = rememberAsyncImagePainter(model = callScreenData.receiverPhoto),
                        contentDescription = "profile picture",
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(150.dp)
                            .border(1.dp, Color.Gray, shape = CircleShape),
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = callScreenData.receiverName,
                        fontSize = 20.sp
                    )
                }



                Spacer(modifier = Modifier.height(20.dp))

                when(callEvent){

                    is CallEvent.Ongoing -> {

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
                    }

                    is CallEvent.Ringing -> {

                        Text(
                            text = if (callScreenData.isCaller) "waiting for other user to join the call" else "Incoming voice call",
                            fontSize = 18.sp

                        )
                    }
                     else -> {}
                }

            }


            // Control Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ControlButtons(
                    callType = "voice",
                    callViewModel,
                    callScreenData.isCaller,
                    callUIState = callUIState,
                    callDocId = callScreenData.callDocId,
                    callEvent
                )
                {

                    if (permissionState.allPermissionsGranted) {

                        if (!callUIState.hasStartedService) {

                            if (!isJoined && !callUIState.callEnded && !callScreenData.isCaller) {

                                callViewModel.startCallService(
                                    context = context,
                                   callMetadata = callScreenData
                                )

                                callViewModel.markCallServiceStarted()
                            }
                        }

                    } else {
                        Toast.makeText(
                            context,
                            "Can't join the call. Grant permissions to join the call",
                            Toast.LENGTH_SHORT
                        ).show()
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
    callScreenData: CallMetadata,
    callUIState: CallUIState,
    callEvent: CallEvent
) {

    val context = LocalContext.current
    val isJoined by callViewModel.isJoined.collectAsState()
    val remoteUserJoined by callViewModel.remoteUserJoined.collectAsState() // remote user numeric id
    val callDuration by callViewModel.callDuration.collectAsState()



    // Create SurfaceViews for Local and Remote video
    val localView by rememberUpdatedState(SurfaceView(context))
    val remoteView by rememberUpdatedState(SurfaceView(context))


    val permissionState = requestPerm()


    val activity = LocalActivity.current

    LaunchedEffect(isJoined, permissionState) {

        if (permissionState.allPermissionsGranted) {
            if (!callUIState.hasStartedService) {

                if (!isJoined && !callUIState.callEnded && callScreenData.isCaller) {

                    callViewModel.enableVideoPreview()

                    callViewModel.startCallService(
                        context = context,
                        callMetadata = callScreenData
                    )

                    callViewModel.markCallServiceStarted()
                }
            }
        } else {
            Toast.makeText(
                context,
                "Please grant all the required permissions to continue the call.",
                Toast.LENGTH_SHORT
            ).show()
        }

    }


    LaunchedEffect(remoteUserJoined) {

        if (remoteUserJoined != null) {
            callViewModel.setUpRemoteVideo(remoteView, remoteUserJoined!!)
            callViewModel.setUpLocalVideo(localView) //re attach the local view, moving from full screen to mini screen
        }
    }


    DisposableEffect(isJoined) {
        if (isJoined) {

            callViewModel.setUpLocalVideo(localView)

            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
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

                if (callScreenData.isCaller) {
                    Text(
                        text = "Establishing connection...",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Image(
                            painter = rememberAsyncImagePainter(model = callScreenData.receiverPhoto),
                            contentDescription = "profile picture",
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(150.dp)
                                .border(1.dp, Color.Gray, shape = CircleShape),
                        )

                        Spacer(modifier = Modifier.height(15.dp))

                        Text(
                            text = callScreenData.receiverName,
                            fontSize = 20.sp
                        )
                    }
                }

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
                        text = if (callScreenData.isCaller) "Video calling to ${callScreenData.receiverName}" else "Incoming video call from :",
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
                        isCaller = callScreenData.isCaller,
                        callUIState = callUIState,
                        callDocId = callScreenData.callDocId,
                        callEvent = callEvent
                    ) {
                        // when the call receiver accepts the call

                        if (permissionState.allPermissionsGranted) {

                            if (!callUIState.hasStartedService) {

                                if (!isJoined && !callUIState.callEnded && !callScreenData.isCaller) {

                                    callViewModel.enableVideoPreview()

                                    callViewModel.startCallService(
                                        context = context,
                                        callMetadata = callScreenData
                                    )

                                    callViewModel.markCallServiceStarted()
                                }
                            }

                        } else {
                            Toast.makeText(
                                context,
                                "Can't join the call. Grant permissions to join the call",
                                Toast.LENGTH_SHORT
                            ).show()
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
    callUIState: CallUIState,
    callDocId: String?,
    callEvent: CallEvent,
    onJoinCall: () -> Unit
) {


    val context = LocalContext.current

    if (callEvent is CallEvent.Ongoing){

        //common
        IconButton(onClick = { callViewModel.muteOutgoingAudio() }) {
            Icon(
                imageVector = if (callUIState.isLocalAudioMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Mute",
            )
        }

        // video/call/common
        IconButton(onClick = { callViewModel.muteYourSpeaker() }) {
            Icon(
                imageVector = if (callUIState.isRemoteAudioDeafen) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "audio turn on/off",

                )
        }

        // common
        IconButton(onClick = {
            callViewModel.stopCallService(context)
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
                    imageVector = if (callUIState.isSpeakerEnabled) Icons.Default.Hearing else Icons.Default.HearingDisabled,
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

    if(callEvent is CallEvent.JoiningChannel || callEvent is CallEvent.Ringing
        || callEvent is CallEvent.InActive){
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

                    IconButton(onClick = {
                        callViewModel.stopCallService(context = context )
                    }) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "decline the call",
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
                        callDocId?.let {
                            callViewModel.declineTheCall(true, it)
                        }

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
    }


}