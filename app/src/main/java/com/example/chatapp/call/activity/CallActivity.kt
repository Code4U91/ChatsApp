package com.example.chatapp.call.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.chatapp.CALL_INTENT
import com.example.chatapp.CallMetadata
import com.example.chatapp.call.screen.CallScreen
import com.example.chatapp.ui.theme.ChatsAppTheme
import com.example.chatapp.call.viewmodel.CallViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CallActivity : ComponentActivity() {

    private val callViewModel: CallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleCallIntent(intent)

        enableEdgeToEdge()
        setContent {

            val callScreenData by callViewModel.callScreenData.collectAsState()
            val activityContext = LocalActivity.current

            ChatsAppTheme(themeString = "system") {

                callScreenData?.let {

                    CallScreen(
                        channelName = it.channelName,
                        callViewModel = callViewModel,
                        callScreenData = it
                    ) {
                        activityContext?.finishAndRemoveTask()
                    }
                }
            }


        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleCallIntent(intent)
    }

    private fun handleCallIntent(intent: Intent) {

        if (intent.action == CALL_INTENT){

            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
            } else {
                window.addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                )
            }

            val metaData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("call_metadata", CallMetadata::class.java)
            } else {
                @Suppress("DEPRECATION")   // Fallback for older version
                intent.getParcelableExtra("call_metadata")
            }

            callViewModel.setCallScreenData(metaData)
        }
    }
}