package com.example.chatapp.call_feature.presentation.call_screen.activity

import android.app.KeyguardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.chatapp.call_feature.presentation.call_screen.screen.CallScreen
import com.example.chatapp.call_feature.presentation.call_screen.state.CallEvent
import com.example.chatapp.call_feature.presentation.call_screen.viewmodel.CallViewModel
import com.example.chatapp.core.util.CALL_INTENT
import com.example.chatapp.core.model.CallMetadata
import com.example.chatapp.ui.theme.ChatsAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CallActivity : ComponentActivity() {

    private val callViewModel: CallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setBackgroundDrawable(null)
        window.setWindowAnimations(0)

        handleCallIntent(intent)

        enableEdgeToEdge()
        setContent {

            val activityContext = LocalActivity.current

            val callEvent by callViewModel.callEvent.collectAsState()

            LaunchedEffect(callEvent) {
                if(callEvent is CallEvent.Ended){
                    activityContext?.apply {
                        finishAndRemoveTask()
                        overridePendingTransition(0,0)

                    }
                }
            }


            ChatsAppTheme(themeString = "system") {

                    CallScreen(
                        callViewModel = callViewModel,
                    )

            }


        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleCallIntent(intent)
    }

    private fun handleCallIntent(intent: Intent) {

        if (intent.action == CALL_INTENT){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)

                val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
                keyguardManager.requestDismissKeyguard(this, null)
            } else {
                @Suppress("DEPRECATION")
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

            Log.i("CHECK_ACTIVITY", metaData.toString())

            callViewModel.setCallScreenData(metaData)
        }
    }
}