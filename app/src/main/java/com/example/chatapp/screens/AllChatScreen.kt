package com.example.chatapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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

@Composable
fun AllChatScreen(
    viewmodel: ChatsViewModel,
    navController: NavHostController,
    paddingValue: PaddingValues,
) {

    // provides all the chat id's where the current user is an participate and also fetch id's of its members
    val activeChatList by viewmodel.activeChatList.collectAsState()


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(paddingValue),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {

        Text(
            text = "ChatsApp",
            fontSize = 35.sp,
            modifier = Modifier.padding(4.dp)
        )

        HorizontalDivider()


        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        )
        {

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {

                items(activeChatList, key = { it.chatId }) { chatItemData ->

                    ChatItem(
                        showLastMsgTime = true,
                        friendId = chatItemData.otherUserId ?: "",
                        viewmodel = viewmodel,
                        navController = navController,
                        chatId = chatItemData.chatId,
                        lastMessageTimStamp = chatItemData.lastMessageTimeStamp,
                        lastMessage = chatItemData.lastMessage?:"",
                    )
                }
            }
        }


    }
}


@Composable
fun ChatItem(
    showLastMsgTime: Boolean,
    friendId: String,
    viewmodel: ChatsViewModel,
    navController: NavHostController,
    chatId: String = "",
    lastMessageTimStamp: Timestamp? = null,
    lastMessage: String = "",

    ) {


    // won't change friend data if this ui goes off like going to main chat screen etc
    val friendData by produceState<FriendData?>(initialValue = null, friendId)
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

    if (friendData == null) {


        // Placeholder / Skeleton Loader

        ChatItemPlaceholder(showLastMsgTime = showLastMsgTime)


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
                    text = if (showLastMsgTime) lastMessage else friendData?.about
                        ?: "",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

            }

            if (showLastMsgTime) {
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





