package com.example.chatapp.screens.navigation

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
import com.example.chatapp.AUTH_GRAPH_ROUTE
import com.example.chatapp.MAIN_GRAPH_ROUTE
import com.example.chatapp.screens.AllChatScreen
import com.example.chatapp.screens.CallHistoryScreen
import com.example.chatapp.screens.CallScreen
import com.example.chatapp.screens.ChangeEmailAddressScreen
import com.example.chatapp.screens.FriendListScreen
import com.example.chatapp.screens.MainChatScreen
import com.example.chatapp.screens.ProfileSettingScreen
import com.example.chatapp.screens.logInSignUp.ForgotPasswordScreen
import com.example.chatapp.screens.logInSignUp.SignInScreenUI
import com.example.chatapp.screens.logInSignUp.SignUpScreenUI
import com.example.chatapp.viewmodel.ChatsViewModel
import com.example.chatapp.viewmodel.GlobalMessageListenerViewModel


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
    globalMessageListenerViewModel: GlobalMessageListenerViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    Log.d(
        "ViewModelInstance",
        "globalMessageListenerViewModel hash: ${globalMessageListenerViewModel.hashCode()}"
    )


    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val currentChatId = currentBackStackEntry?.arguments?.getString("chatId")

    val incomingCall by globalMessageListenerViewModel.incomingCallData.collectAsState()



    LaunchedEffect(currentChatId) {

        // pass even null, pass everything or else old id would remain in viewmodel
        // and cause unnecessary ui updates, alternative method clear value each time
        // when navigating
        Log.i("CurrentChatId", currentChatId.toString())
        globalMessageListenerViewModel.setCurrentOpenChatId(currentChatId)

    }

    // testing purpose only
    // later replacing it with fcm call notification
    LaunchedEffect(incomingCall) {

        if (incomingCall != null) {
            val channelName = incomingCall?.channelId
            val callType = incomingCall?.callType
            val callerId = incomingCall?.callerId

            Log.i("IncomingCall", incomingCall.toString())

            // currently directly initiating a call, later add accept or decline option
            navController.navigate("CallScreen/$channelName/$callType/false/$callerId")
            globalMessageListenerViewModel.emptyIncomingCall() // clear call log so it doesn't run again
        }
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
            startDestination = Screen.AllChatScreen.route,
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
                CallHistoryScreen(globalMessageListenerViewModel, navController)
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

            composable("CallScreen/{channelName}/{callType}/{isCaller}/{receiverId}",
                arguments = listOf(
                    navArgument("channelName") { type = NavType.StringType },
                    navArgument("callType") { type = NavType.StringType },
                    navArgument("isCaller") { type = NavType.BoolType },
                    navArgument("receiverId") { type = NavType.StringType }
                    //  navArgument("token"){type = NavType.StringType} // token for agora if on secure mode
                )
            )
            { backStackEntry ->
                val channelName = backStackEntry.arguments?.getString("channelName") ?: ""
                val callType = backStackEntry.arguments?.getString("callType") ?: ""
                val isCaller = backStackEntry.arguments?.getBoolean("isCaller") ?: true
                val receiverId = backStackEntry.arguments?.getString("receiverId") ?: ""
                //   val token = backStackEntry.arguments?.getString("token") ?: ""

                CallScreen(
                    channelName,
                    callType,
                    globalMessageListenerViewModel = globalMessageListenerViewModel,
                    isCaller = isCaller,
                    receiverId = receiverId
                )
                {
                    Log.d("CallScreen", "onCallEnd triggered!")

                    navController.popBackStack()
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




