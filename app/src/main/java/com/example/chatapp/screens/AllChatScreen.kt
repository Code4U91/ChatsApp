package com.example.chatapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil3.compose.AsyncImage
import com.example.chatapp.FriendData
import com.example.chatapp.formatTimestamp
import com.example.chatapp.shimmerEffect
import com.example.chatapp.viewmodel.ChatsViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllChatScreen(
    viewmodel: ChatsViewModel,
    navController: NavHostController,
) {

    // provides all the chat id's where the current user is an participate and also fetch id's of its members
    val activeChatList by viewmodel.activeChatList.collectAsState()

    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }

    var showSearchBar by rememberSaveable {
        mutableStateOf(false)
    }

    val filteredActiveChatList = activeChatList.filter {
        it.otherUserName?.trim()?.contains(searchQuery.trim(), ignoreCase = true) == true
    }.sortedByDescending { it.lastMessageTimeStamp?.toDate()?.time ?: 0L }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    // show search bar or not
                    if (showSearchBar) {

                        val keyboardController = LocalSoftwareKeyboardController.current

                        val focusRequester = remember { FocusRequester() }

                        LaunchedEffect(Unit) {
                            delay(200)
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 2.dp, end = 14.dp),
                            shape = CircleShape
                        ) {

                            // field where user can enter text to sort the friend by name
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text(text = "Search in chat list..") },
                                singleLine = true,
                                leadingIcon = {

                                    IconButton(onClick = {

                                        showSearchBar = false
                                        searchQuery = ""

                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBackIosNew,
                                            contentDescription = "back button"
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(30.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                colors = TextFieldDefaults.colors(

                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    errorIndicatorColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    errorContainerColor = Color.Transparent
                                )
                            )
                        }


                    } else {
                        // if search bar isn't opened
                        Text(
                            text = "ChatsApp",
                            fontSize = 35.sp,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                },
                actions = {

                    if (!showSearchBar) {
                        IconButton(onClick = {
                            showSearchBar = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Localized description"
                            )
                        }
                    }
                },

                )


        }
    ) { paddingValue ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValue),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {

            if (!showSearchBar)
            {
                HorizontalDivider()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            )
            {

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(top = 10.dp)
                ) {

                    items(filteredActiveChatList, key = { it.chatId }) { chatItemData ->

                        ChatItemAndFriendListItem(
                            chatItemWithMsg = true,
                            friendId = chatItemData.otherUserId ?: "",
                            viewmodel = viewmodel,
                            navController = navController,
                            chatId = chatItemData.chatId,
                            lastMessageTimStamp = chatItemData.lastMessageTimeStamp,
                            lastMessage = chatItemData.lastMessage ?: "",
                            oldFriendName = chatItemData.otherUserName,
                            whichList = "chatList"
                        )
                    }
                }
            }
        }
    }


}


@Composable
fun ChatItemAndFriendListItem(
    chatItemWithMsg: Boolean,
    friendId: String,
    viewmodel: ChatsViewModel,
    navController: NavHostController,
    chatId: String = "",
    lastMessageTimStamp: Timestamp? = null,
    lastMessage: String = "",
    oldFriendName: String? = null,
    whichList: String

) {


    //  auto offs if ui goes out of view, may be similar to launched effect but used to observe state, snapshot etc
    val friendData by produceState<FriendData?>(initialValue = null, key1 = friendId)
    {
        val listener = viewmodel.fetchFriendData(friendId)
        { data ->
            value = data
        }


        awaitDispose {

            listener.remove()
        }
    }


    val dateAndTime by remember(lastMessageTimStamp) {
        mutableStateOf(formatTimestamp(lastMessageTimStamp ?: Timestamp.now()))
    }


    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    LaunchedEffect(oldFriendName, friendData?.name, chatId) {

        val updatedFriendName = friendData?.name

        if (oldFriendName != null) {
            updatedFriendName?.let {

                if (oldFriendName != updatedFriendName) {
                    // change friend name on the friend list data on the friendList document of the user
                    viewmodel.updateFriendName(
                        friendName = updatedFriendName,
                        friendId = friendData?.uid ?: "",
                        whichList = whichList,
                        chatId = chatId
                    )
                }
            }
        }

    }

    if (friendData == null) {


        // Placeholder / Skeleton Loader

        ChatItemPlaceholder(showLastMsgTime = chatItemWithMsg)


    } else {


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .pointerInput(Unit)
                {
                    detectTapGestures(
                        onLongPress = {

                            if (currentRoute == "FriendListScreen") {
                                viewmodel.deleteFriend(friendId)
                            }
                        },
                        onTap = {

                            viewmodel.updateFriendData(
                                FriendData(
                                    name = friendData?.name ?: "",
                                    photoUrl = friendData?.photoUrl ?: "",
                                    about = friendData?.about ?: "",
                                    uid = friendId
                                )
                            )

                            navController.navigate(
                                "MainChat/$friendId/$chatId"
                            )


                        }
                    )
                }
        ) {

            //Profile image
            AsyncImage(
                model = friendData?.photoUrl ?: "",
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.Transparent, CircleShape)
            )

            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(7f)
            ) {

                // Profile user name
                Text(
                    text = friendData?.name ?: "",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )




                Text(
                    text = if (chatItemWithMsg) lastMessage else friendData?.about
                        ?: "",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

            }

            if (chatItemWithMsg) {
                // Last messaged date/time

                Text(
                    text = dateAndTime,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                )
            }


        }


    }

}


@Composable
fun ChatItemPlaceholder(showLastMsgTime: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .shimmerEffect()
        )
        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(7f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.LightGray)
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.LightGray)
                    .shimmerEffect()
            )
        }
        if (showLastMsgTime) {
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .width(40.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.LightGray)
                    .shimmerEffect()
            )
        }
    }
}





