package com.example.chatapp.screens

import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.chatapp.viewmodel.CallViewModel
import kotlinx.coroutines.delay

@Composable
fun CallScreen(
    channelName: String,
    callType: String,
    callViewModel: CallViewModel = hiltViewModel(),
    onCallEnd: () -> Unit
) {

    Log.i("TestChannelName", channelName) // using firebase uid user1_User2


    if (callType == "video") {
        StartVideoCall(callViewModel = callViewModel, channelName = channelName, onCallEnd)
    } else {

        StartVoiceCall(callViewModel = callViewModel, channelName = channelName, onCallEnd)

    }


}

@Composable
fun StartVoiceCall(
    callViewModel: CallViewModel,
    channelName: String,
    onCallEnd: () -> Unit
) {

    val isJoined by callViewModel.isJoined.collectAsState()
    val remoteUserJoined by callViewModel.remoteUserJoined.collectAsState()
    val remoteUserLeft by callViewModel.remoteUserLeft.collectAsState()
    val isMuted by callViewModel.isMuted.collectAsState()
    val isRemoteAudioDeafen by callViewModel.isRemoteAudioDeafen.collectAsState()
    val isSpeakerPhoneEnabled by callViewModel.isSpeakerPhoneEnabled.collectAsState()
    val callEnded by callViewModel.callEnded.collectAsState()

    LaunchedEffect(remoteUserLeft, callEnded) {
        if (remoteUserLeft || callEnded) onCallEnd()

    }

    LaunchedEffect(Unit) {
        callViewModel.joinChannel(null, channelName, 0, "voice")
    }


    Scaffold(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            if (isJoined) {
                // engine created waiting for other user to join call

                if (remoteUserJoined != null) {  // remote/other user joined the call

                    Text(
                        text = "Call active",
                        modifier = Modifier.align(Alignment.Center)
                    )

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
                IconButton(onClick = { callViewModel.muteOutgoingAudio() }) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",

                        )
                }

                IconButton(onClick = { callViewModel.muteYourSpeaker() }) {
                    Icon(
                        imageVector = if (isRemoteAudioDeafen) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "audio turn on/off",

                        )
                }

                IconButton(onClick = { callViewModel.toggleSpeaker() }) {
                    Icon(
                        imageVector = if (isSpeakerPhoneEnabled) Icons.Default.Hearing else Icons.Default.HearingDisabled,
                        contentDescription = "voice mode : speaker or earpiece",

                        )
                }


                IconButton(onClick = {
                    callViewModel.leaveChannel()
                }) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.Red
                    )
                }
            }
        }
    }


}

@Composable
fun StartVideoCall(
    callViewModel: CallViewModel,
    channelName: String,
    onCallEnd: () -> Unit
) {
    val context = LocalContext.current
    val isJoined by callViewModel.isJoined.collectAsState()
    val remoteUserJoined by callViewModel.remoteUserJoined.collectAsState()
    val remoteUserLeft by callViewModel.remoteUserLeft.collectAsState()
    val isMuted by callViewModel.isMuted.collectAsState()
    val isRemoteAudioDeafen by callViewModel.isRemoteAudioDeafen.collectAsState()

    // Create SurfaceViews for Local and Remote video
    val localView by rememberUpdatedState(SurfaceView(context))
    val remoteView by rememberUpdatedState(SurfaceView(context))

    val callEnded by callViewModel.callEnded.collectAsState()

    val activity = LocalActivity.current

    LaunchedEffect(Unit) {
        // callViewModel.enableVideoPreview()
        callViewModel.joinChannel(null, channelName, 0, "video")
    }

    LaunchedEffect(isJoined) {

        if (isJoined) {
            callViewModel.setUpLocalVideo(localView)
            delay(500) // Small delay to ensure the SurfaceView is ready
            callViewModel.setUpLocalVideo(localView) // Force rebind

            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    LaunchedEffect(remoteUserJoined) {

        if (remoteUserJoined != null) {
            callViewModel.setUpRemoteVideo(remoteView, remoteUserJoined!!)
            callViewModel.setUpLocalVideo(localView) // force rebind
        } else {
            // Wait for 2 minutes for the remote user to join
            delay(2 * 60 * 1000)

            // If still null after 2 minutes, end call
            if (remoteUserJoined == null) callViewModel.leaveChannel()
        }
    }

    LaunchedEffect(remoteUserLeft, callEnded) {
        if (remoteUserLeft || callEnded) onCallEnd()
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
                // Remote video (Large)
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (remoteUserJoined != null) {
                        AndroidView(factory = { remoteView }, modifier = Modifier.fillMaxSize())
                    } else {
                        Text(
                            text = "Waiting for other user to join...",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.White
                        )
                    }
                }

                // Local video (Mini-screen) - Floating at bottom end
                if (remoteUserJoined != null) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(Color.Black, shape = RoundedCornerShape(8.dp))
                    ) {
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { callViewModel.muteOutgoingAudio() }) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = Color.White
                    )
                }

                IconButton(onClick = { callViewModel.muteYourSpeaker() }) {
                    Icon(
                        imageVector = if (isRemoteAudioDeafen) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Speaker",
                        tint = Color.White
                    )
                }

                IconButton(onClick = { callViewModel.switchCamera() }) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Switch Camera",
                        tint = Color.White
                    )
                }

                IconButton(onClick = {
                    callViewModel.leaveChannel()
                }) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}