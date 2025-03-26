package com.example.chatapp.screens

import android.util.Log
import android.view.SurfaceView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.chatapp.viewmodel.CallViewModel
import io.agora.rtc2.video.VideoCanvas

@Composable
fun CallScreen(
    channelName: String,
    callViewModel: CallViewModel = hiltViewModel(),
    onCallEnd: () -> Unit
)
{

    Log.i("TestChannelName", channelName) // using firebase uid user1_User2

    val context = LocalContext.current

    val localSurfaceView = remember {
       SurfaceView(context).apply {
           setZOrderMediaOverlay(true)
       }
    }

    val remoteSurfaceView = remember {
        mutableStateOf<SurfaceView?>(null)
    }
    val isMicMuted = remember { mutableStateOf(false) }
    val isVideoMuted = remember { mutableStateOf(false) }
    val isSpeakerOn = remember { mutableStateOf(true) }


    DisposableEffect(Unit) {

        callViewModel.initializeAgora()

        callViewModel.setRemoteVideoListener { uid ->

            Log.i("AgoraDebugCallScreen", "Remote user joined: $uid")
            val remoteView = SurfaceView(context).apply {
                setZOrderMediaOverlay(false)
            }

            callViewModel.setUpRemoteVideo(
                uid,
                VideoCanvas(remoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid)
            )
            remoteSurfaceView.value = remoteView
        }


        callViewModel.startLocalVideo(
            VideoCanvas(localSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0)
        )
        callViewModel.joinChannel(null, channelName)
        //callViewModel.toggleSpeaker(true)

        onDispose {

            callViewModel.leaveChannel()
            callViewModel.callEnd()

        }
    }






    Scaffold(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().padding(it)) {
            remoteSurfaceView.value?.let { remoteView ->
                AndroidView(factory = { remoteView }, modifier = Modifier.fillMaxSize())
            }

            // Local video preview
            AndroidView(
                factory = { localSurfaceView },
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )

            // Controls (mic, video, end call)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Toggle mic
                    IconButton(onClick = {
                        isMicMuted.value = !isMicMuted.value
                        callViewModel.muteLocalAudio(isMicMuted.value)
                    }) {
                        Icon(
                            imageVector = if (isMicMuted.value) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Toggle Mic",
                            tint = if (isMicMuted.value) Color.Gray else Color.White,
                            modifier = Modifier.size(40.dp).padding(end = 6.dp)
                        )
                    }

                    // Toggle video
                    IconButton(onClick = {
                        isVideoMuted.value = !isVideoMuted.value
                        callViewModel.muteLocalVideo(isVideoMuted.value)
                    }) {
                        Icon(
                            imageVector = if (isVideoMuted.value) Icons.Default.VideocamOff else Icons.Default.Videocam,
                            contentDescription = "Toggle Video",
                            tint = if (isVideoMuted.value) Color.Gray else Color.White,
                            modifier = Modifier.size(40.dp).padding(end = 6.dp)
                        )
                    }


                    // End call button
                    IconButton(onClick = onCallEnd) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.Red,
                            modifier = Modifier.size(50.dp).padding(end = 6.dp)
                        )
                    }



                    // Toggle speaker
                    IconButton(onClick = {
                        isSpeakerOn.value = !isSpeakerOn.value
                        callViewModel.toggleSpeaker(isSpeakerOn.value)
                    }) {
                        Icon(
                            imageVector = if (isSpeakerOn.value) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = "Toggle Speaker",
                            tint = if (isSpeakerOn.value) Color.White else Color.Gray,
                            modifier = Modifier.size(40.dp).padding(end = 6.dp)
                        )
                    }

                }
            }
        }
    }
}