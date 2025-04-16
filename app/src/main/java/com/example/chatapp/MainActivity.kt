package com.example.chatapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatapp.navigation.AuthNavigationHost
import com.example.chatapp.navigation.MainNavigationHost
import com.example.chatapp.navigation.Screen
import com.example.chatapp.ui.theme.ChatsAppTheme
import com.example.chatapp.viewmodel.AuthState
import com.example.chatapp.viewmodel.ChatsViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var auth: FirebaseAuth

    private val chatsViewModel: ChatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            handleIntent(intent)

        }

        val startDestination = when (intent.action) {

            CALL_INTENT -> {
                if (chatsViewModel.deepLinkData.value != null) {
                    val data = chatsViewModel.deepLinkData.value!!

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        setShowWhenLocked(true)
                        setTurnScreenOn(true)
                    }

                    "CallScreen/${data.channelName}/${data.callType}/${data.isCaller}/${data.callReceiverId}/${data.callDocId}"

                } else {
                    Screen.AllChatScreen.route
                }
            }

            MESSAGE_FCM_INTENT -> {

                if (chatsViewModel.fcmMessageMetadata.value != null) {
                    val data = chatsViewModel.fcmMessageMetadata.value!!

                    "MainChat/${data.senderId}/${data.chatId}"

                } else {
                    Screen.AllChatScreen.route
                }

            }

            CALL_HISTORY_INTENT -> { Screen.CallHistoryScreen.route }

            else -> {
                Screen.AllChatScreen.route
            }
        }


        enableEdgeToEdge()

        setContent {
            ChatsAppTheme("system") {

                DisposableEffect(Unit) {
                    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->

                        chatsViewModel.checkAuthStatus(firebaseAuth.currentUser)
                    }

                    auth.addAuthStateListener(listener)

                    onDispose {
                        auth.removeAuthStateListener(listener)
                    }
                }


                ChatAppRoot(chatsViewModel, startDestination)
            }
        }


    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleIntent(intent)


    }

    // handles intent passed via notification icon click
    private fun handleIntent(intent: Intent) {

        when (intent.action) {
            CALL_INTENT -> {
                val metaData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra("call_metadata", CallMetadata::class.java)
                } else {
                    // Fallback for older version
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra("call_metadata")
                }

                chatsViewModel.setDeepLinkData(metaData)
            }

            MESSAGE_FCM_INTENT -> {

                val metaData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra("fcmMessage", MessageFcmMetadata::class.java)
                } else {
                    // Fallback for older version
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra("fcmMessage")
                }

                chatsViewModel.setFcmMessageMetaData(metaData)
            }

            CALL_HISTORY_INTENT -> { chatsViewModel.moveToCallHistory(true)}

            else -> {}
        }

    }
}

@Composable
fun ChatAppRoot(viewModel: ChatsViewModel, startDestination: String) {
    val rootNavController = rememberNavController()
    val authState by viewModel.authState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    )
    {
        NavHost(
            navController = rootNavController,
            startDestination = if (authState is AuthState.Authenticated) "Main" else "Auth",
            route = "Root"
        ) {

            composable(route = "Main")
            {

                MainNavigationHost(viewModel = viewModel, startDestination)
            }

            composable("Auth") {

                AuthNavigationHost(viewModel = viewModel)
            }

        }
    }


}

