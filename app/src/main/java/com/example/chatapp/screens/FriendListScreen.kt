package com.example.chatapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.chatapp.dialogBox.AddFriendDialogBox
import com.example.chatapp.FriendScreenUiItem
import com.example.chatapp.viewmodel.ChatsViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendListScreen(viewmodel: ChatsViewModel, navController: NavHostController) {

    var expandFriendDialogBox by rememberSaveable {
        mutableStateOf(false)
    }

    var friendList by rememberSaveable {
        // mutableStateOf<List<DocumentSnapshot>>(emptyList()) // can't parse when app is minimized or in background
        mutableStateOf<List<String>>(emptyList())
    }

    LaunchedEffect(Unit) {
        viewmodel.fetchFriendList { updatedFriendList ->
            friendList = updatedFriendList.map { it.id }
        }
    }




    val totalFriends by viewmodel.totalFriend.collectAsState()


    val friendScreenUiItemList = listOf(

        FriendScreenUiItem(icon = Icons.Default.AddComment, itemDescription = "New friend"),
        FriendScreenUiItem(icon = Icons.Default.Groups, itemDescription = "New group")
    )

    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text = "Select contact",
                            fontSize = 18.sp,
                        )
                        Text(
                            text = "$totalFriends contacts",
                            fontSize = 12.sp
                        )

                    }


                },
                navigationIcon = {

                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            contentDescription = "Localized description"
                        )
                    }

                },
                actions = {
                    IconButton(onClick = { /* doSomething() */ }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Localized description"
                        )
                    }
                },


                )


        }
    ) { innerPadding ->


        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopStart
        )
        {

            HorizontalDivider()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {

                items(friendScreenUiItemList) { item ->

                    FriendScreenOptionItem(
                        icon = item.icon,
                        descriptionText = item.itemDescription
                    ) { state ->
                        expandFriendDialogBox = state
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Friends on ChatsApp",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(4.dp)
                    )
                    // HorizontalDivider()
                    Spacer(modifier = Modifier.height(4.dp))
                }

                items(friendList)
                { friendId ->

                    ChatItem(
                        showLastMsgTime = false,
                        friendId = friendId,
                        navController = navController,
                        viewmodel = viewmodel
                    )

                }
            }


            if (expandFriendDialogBox) {
                AddFriendDialogBox(viewModel = viewmodel)
                {
                    expandFriendDialogBox = it
                }
            }

        }
    }


}


@Composable
fun FriendScreenOptionItem(
    icon: ImageVector,
    descriptionText: String,
    onClick: (state: Boolean) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // when clicked add new friend
                if (descriptionText == "New friend") {
                    onClick(true)
                }
            }
    ) {

        Icon(
            imageVector = icon,
            contentDescription = descriptionText + "button",
            modifier = Modifier
                .clip(CircleShape)
                .size(30.dp)
                .background(Color.Green)
                .weight(1f),
            tint = Color.Black
        )

        Text(
            text = descriptionText,
            fontSize = 20.sp,
            modifier = Modifier
                .weight(9f)
                .padding(start = 10.dp)

        )
    }
}