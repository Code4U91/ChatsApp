package com.example.chatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.chatapp.screens.navigation.AuthNavigationHost
import com.example.chatapp.screens.navigation.MainNavigationHost
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

                ChatAppRoot(chatsViewModel)
            }
        }


    }


}

@Composable
fun ChatAppRoot(viewModel: ChatsViewModel) {
    val authState by viewModel.authState.collectAsState()

    if (authState is AuthState.Authenticated)
    {

        MainNavigationHost(viewModel)

    } else {

        AuthNavigationHost(viewModel)
    }
}

