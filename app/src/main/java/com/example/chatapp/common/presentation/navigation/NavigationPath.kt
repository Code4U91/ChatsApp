package com.example.chatapp.common.presentation.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil3.compose.rememberAsyncImagePainter
import com.example.chatapp.core.AUTH_GRAPH_ROUTE
import com.example.chatapp.core.MAIN_GRAPH_ROUTE
import com.example.chatapp.auth_feature.presentation.screens.ChangeEmailAddressScreen
import com.example.chatapp.friend_feature.FriendListScreen
import com.example.chatapp.chat_feature.presentation.MainChatScreen
import com.example.chatapp.auth_feature.presentation.screens.ForgotPasswordScreen
import com.example.chatapp.auth_feature.presentation.screens.SignInScreenUI
import com.example.chatapp.auth_feature.presentation.screens.SignUpScreenUI
import com.example.chatapp.chat_feature.presentation.AllChatScreen
import com.example.chatapp.call_feature.presentation.call_history_screen.CallHistoryScreen
import com.example.chatapp.profile_feature.presentation.ProfileSettingScreen
import com.example.chatapp.auth_feature.presentation.viewmodel.ChatsViewModel
import com.example.chatapp.common.presentation.GlobalMessageListenerViewModel


// Authentication screen navigation
@Composable
fun AuthNavigationHost(
    viewModel: ChatsViewModel
) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) {

        NavHost(
            modifier = Modifier.padding(it),
            navController = navController,
            route = AUTH_GRAPH_ROUTE,
            startDestination = "SignIn"
        )
        {

            composable("SignIn") {
                SignInScreenUI(viewModel, navController)
            }

            composable("SignUp") {
                SignUpScreenUI(viewModel, navController)
            }

            composable("forgotPwd") {
                ForgotPasswordScreen(viewModel)
            }
        }
    }

}


// Main screen navigation, only appear after user is authenticated
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainNavigationHost(
    viewModel: ChatsViewModel,
    startDestination: String,
    globalMessageListenerViewModel: GlobalMessageListenerViewModel = hiltViewModel()
) {

    val navController = rememberNavController()


    val messageFcmMetadata by viewModel.fcmMessageMetadata.collectAsState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val isCallHistoryActive by viewModel.callHistoryScreenActive.collectAsState()
    val moveToCallHistory by viewModel.moveToCallHistory.collectAsState()

    val currentChatId = currentBackStackEntry?.arguments?.getString("chatId")


    // opens main chat screen on click via a fcm notification
    LaunchedEffect(messageFcmMetadata) {

        messageFcmMetadata?.let { data ->

            // checking if the user is already in the notified chat
            if (currentChatId != data.chatId) {

                navController.navigate("MainChat/${data.senderId}/${data.chatId}")
                viewModel.setFcmMessageMetaData(null)
            }
        }
    }

    // When clicked on missed call notification
    LaunchedEffect(moveToCallHistory) {
        if (moveToCallHistory && !isCallHistoryActive) {
            navController.navigate(Screen.CallHistoryScreen.route)
        }
    }


    // used to store the chatId of the screen where the user visit,
    // later used to identify the active screen/chat and mark message as seen
    LaunchedEffect(currentChatId) {

        // pass even null, pass everything or else old id would remain in viewmodel
        // and cause unnecessary ui updates, alternative method clear value each time
        // when navigating
        Log.i("CurrentChatId", currentChatId.toString())
        globalMessageListenerViewModel.setCurrentOpenChatId(currentChatId)

    }

    val showBottomBarRoutes = listOf(
        Screen.AllChatScreen.route,
        Screen.ProfileScreen.route,
        Screen.CallHistoryScreen.route
    )


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentRoute in showBottomBarRoutes) {
                BottomNavigationBar(navController, globalMessageListenerViewModel)
            }
        },
        floatingActionButton = {
            if (currentRoute == Screen.AllChatScreen.route) {
                FloatingActionButton(onClick = { navController.navigate("FriendListScreen") }) {
                    Icon(
                        imageVector = Icons.Default.AddComment,
                        contentDescription = "Add friend button",
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(30.dp)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValue ->

        NavHost(
            navController = navController,
            startDestination = startDestination,
            route = MAIN_GRAPH_ROUTE,
            modifier = Modifier.fillMaxSize()
        ) {

            composable(Screen.AllChatScreen.route) {
                AllChatScreen(navController, globalMessageListenerViewModel)
            }

            composable(Screen.ProfileScreen.route) {
                ProfileSettingScreen(
                    viewModel,
                    navController,
                    paddingValue,
                    globalMessageListenerViewModel
                )
            }

            composable(Screen.CallHistoryScreen.route) {
                CallHistoryScreen(globalMessageListenerViewModel, viewModel)
            }

            composable("FriendListScreen") {
                FriendListScreen(navController, globalMessageListenerViewModel)
            }

            composable("changePassword") {
                ForgotPasswordScreen(viewModel)
            }

            composable("changeEmail") {
                ChangeEmailAddressScreen(viewModel)
            }

            composable(
                "MainChat/{friendId}/{chatId}",
                arguments = listOf(
                    navArgument("friendId") { type = NavType.StringType },
                    navArgument("chatId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val friendId = backStackEntry.arguments?.getString("friendId")
                val chatId = backStackEntry.arguments?.getString("chatId")

                if (!friendId.isNullOrEmpty()) {
                    MainChatScreen(
                        navController,
                        friendId,
                        chatId ?: "",
                        globalMessageListenerViewModel
                    )
                }
            }
        }
    }
}

// bottom bar navigation ui
@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    globalMessageListenerViewModel: GlobalMessageListenerViewModel
) {

    val navItemList = listOf(
        Screen.AllChatScreen,
        Screen.ProfileScreen,
        Screen.CallHistoryScreen
    )

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route


    val userData by globalMessageListenerViewModel.userData.collectAsState()


    NavigationBar {

        navItemList.forEach { screen ->

            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(Screen.AllChatScreen.route) {
                                saveState = true
                            }
                            restoreState = true
                            launchSingleTop = true
                        }
                    }
                },
                icon = {

                    val photoUrl = userData?.photoUrl

                    if (screen is Screen.ProfileScreen && !photoUrl.isNullOrEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = photoUrl),
                            contentDescription = "profile picture icon",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.Gray, shape = CircleShape)
                        )
                    } else {
                        Icon(imageVector = screen.icon, contentDescription = screen.title)
                    }

                },
                label = {
                    Text(text = screen.title)
                }
            )
        }
    }
}


// bottom bar item
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object AllChatScreen : Screen("AllChat", "Chats", Icons.Default.Email)
    data object CallHistoryScreen : Screen("CallHistory", "Calls", Icons.Default.Call)
    data object ProfileScreen : Screen("Profile", "Profile", Icons.Default.Person)
}




