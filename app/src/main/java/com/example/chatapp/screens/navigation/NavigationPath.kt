package com.example.chatapp.screens.navigation

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
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil3.compose.rememberAsyncImagePainter
import com.example.chatapp.screens.AllChatScreen
import com.example.chatapp.screens.CallScreen
import com.example.chatapp.screens.FriendListScreen
import com.example.chatapp.screens.MainChatScreen
import com.example.chatapp.screens.ProfileSettingScreen
import com.example.chatapp.screens.logInSignUp.ForgotPasswordScreen
import com.example.chatapp.screens.logInSignUp.SignInScreenUI
import com.example.chatapp.screens.logInSignUp.SignUpScreenUI
import com.example.chatapp.viewmodel.ChatsViewModel


// Authentication screen navigation
@Composable
fun AuthNavigationHost(viewModel: ChatsViewModel) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) {
        NavHost(
            modifier = Modifier.padding(it),
            navController = navController,
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
                ForgotPasswordScreen(viewModel, navController)
            }
        }
    }

}


// Main screen navigation, only appear after user is authenticated
@Composable
fun MainNavigationHost(viewModel: ChatsViewModel) {
    val navController = rememberNavController()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val currentChatId = currentBackStackEntry?.arguments?.getString("chatId")


    LaunchedEffect(currentChatId) {

        Log.i("CurrentChatId", currentChatId.toString())
        viewModel.setCurrentOpenChatId(currentChatId)
    }

    val noBottomBarRouteList = listOf(
        "MainChat/{friendId}/{chatId}",
        "FriendListScreen",
        "CallScreen/{channelName}"
    )


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentRoute !in noBottomBarRouteList ) {
                BottomNavigationBar(navController, viewModel)
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
            startDestination = Screen.AllChatScreen.route
        ) {
            composable(Screen.AllChatScreen.route) {
                AllChatScreen(viewModel, navController, paddingValue)
            }

            composable(Screen.ProfileScreen.route) {
                ProfileSettingScreen(viewModel)
            }

            composable(Screen.CallHistoryScreen.route) {
                // Add your call history screen UI here
            }

            composable("FriendListScreen") {
                FriendListScreen(viewModel, navController)
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
                    MainChatScreen(viewModel, navController, friendId, chatId ?: "")
                }
            }

            composable("CallScreen/{channelName}",
                arguments = listOf(
                    navArgument("channelName"){ type = NavType.StringType},
                  //  navArgument("token"){type = NavType.StringType}
                )
            )
            {backStackEntry ->
                val channelName = backStackEntry.arguments?.getString("channelName") ?: ""
             //   val token = backStackEntry.arguments?.getString("token") ?: ""

                CallScreen(channelName)
                {
                    navController.popBackStack()
                }

            }
        }
    }
}

// bottom bar navigation ui
@Composable
fun BottomNavigationBar(navController: NavHostController, viewmodel: ChatsViewModel) {

    val navItemList = listOf(
        Screen.AllChatScreen,
        Screen.ProfileScreen,
        Screen.CallHistoryScreen
    )

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route


    val userData by viewmodel.userData.collectAsState()



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




