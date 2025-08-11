package com.example.chatapp.shared.presentation

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
import com.example.chatapp.core.util.CALL_HISTORY_INTENT
import com.example.chatapp.core.util.MESSAGE_FCM_INTENT
import com.example.chatapp.shared.presentation.navigation.AuthNavigationHost
import com.example.chatapp.shared.presentation.navigation.MainNavigationHost
import com.example.chatapp.shared.presentation.navigation.Screen
import com.example.chatapp.ui.theme.ChatsAppTheme
import com.example.chatapp.auth_feature.presentation.viewmodel.AuthState
import com.example.chatapp.auth_feature.presentation.viewmodel.AuthViewModel
import com.example.chatapp.core.model.MessageFcmMetadata
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var auth: FirebaseAuth

    private val authViewModel:  AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            handleIntent(intent)

        }

        val startDestination = when (intent.action) {

            MESSAGE_FCM_INTENT -> {

                if (authViewModel.fcmMessageMetadata.value != null) {
                    val data = authViewModel.fcmMessageMetadata.value!!

                    "MainChat/${data.senderId}/${data.chatId}"

                } else {
                    Screen.AllChatScreen.route
                }

            }

            CALL_HISTORY_INTENT -> {
                Screen.CallHistoryScreen.route
            }

            else -> {
                Screen.AllChatScreen.route
            }
        }


        enableEdgeToEdge()

        setContent {
            ChatsAppTheme("system") {

                DisposableEffect(Unit) {
                    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->

                        authViewModel.checkAuthStatus(firebaseAuth.currentUser)
                    }

                    auth.addAuthStateListener(listener)

                    onDispose {
                        auth.removeAuthStateListener(listener)
                    }
                }


                ChatAppRoot(authViewModel, startDestination)
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

            MESSAGE_FCM_INTENT -> {

                val metaData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra("fcmMessage", MessageFcmMetadata::class.java)
                } else {
                    // Fallback for older version
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra("fcmMessage")
                }

                authViewModel.setFcmMessageMetaData(metaData)
            }

            CALL_HISTORY_INTENT -> {
                authViewModel.moveToCallHistory(true)
            }

            else -> {}
        }

    }
}

@Composable
fun ChatAppRoot(authViewModel:  AuthViewModel, startDestination: String) {
    val rootNavController = rememberNavController()

    val authState by authViewModel.authState.collectAsState()

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

                MainNavigationHost(authViewModel = authViewModel, startDestination)
            }

            composable("Auth") {

                AuthNavigationHost(authViewModel = authViewModel)
            }

        }
    }


}

