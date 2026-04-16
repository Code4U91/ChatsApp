package com.code4u.chatsapp.chat_feature.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil3.compose.AsyncImage
import com.code4u.chatsapp.R
import com.code4u.chatsapp.call_feature.presentation.call_history_screen.TopAppBarTemplate
import com.code4u.chatsapp.core.util.formatTimestamp
import com.code4u.chatsapp.core.util.shimmerEffect
import com.code4u.chatsapp.friend_feature.domain.model.Friend
import com.code4u.chatsapp.shared.presentation.ChatListState
import com.code4u.chatsapp.shared.presentation.viewmodel.GlobalMessageListenerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged


@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class, FlowPreview::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AllChatScreen(
    navController: NavHostController,
    globalMessageListenerViewModel: GlobalMessageListenerViewModel,
) {

    requestPerm()

    val chatState by globalMessageListenerViewModel
        .activeChats
        .collectAsStateWithLifecycle()

    val activeChatList = when (chatState) {
        is ChatListState.Success -> (chatState as ChatListState.Success).chats
        else -> emptyList()
    }


    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }

    var showSearchBar by rememberSaveable {
        mutableStateOf(false)
    }


    val filteredActiveChatList = remember(searchQuery, activeChatList) {
        activeChatList.filter {
            it.otherUserName.trim().contains(searchQuery.trim(), ignoreCase = true)
        }.sortedBy { it.otherUserName.lowercase() }
    }


    val visibleChatList = if (searchQuery.isEmpty()) activeChatList else filteredActiveChatList

    val latestList by rememberUpdatedState(visibleChatList)

    val lazyListState = rememberLazyListState()


    LaunchedEffect(lazyListState) {

        snapshotFlow {
            val layoutInfo = lazyListState.layoutInfo

            layoutInfo.visibleItemsInfo.mapNotNull { itemInfo ->
                latestList.getOrNull(itemInfo.index)?.otherUserId
            }
        }
            .debounce(200)
            .distinctUntilChanged()
            .collect { visibleIds ->

                Log.i("VISIBLE_FRIEND_ALL", visibleIds.toString())

                globalMessageListenerViewModel.updateVisibleFriendIds(visibleIds.toSet())
            }
    }




    BackHandler(enabled = showSearchBar) {
        showSearchBar = false
        searchQuery = ""
    }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {

                    if (showSearchBar) {
                        TopAppBarTemplate(
                            searchQuery = searchQuery,
                            placeHolderText = "Search in chat list..",
                            onQueryChanged = { newQuery ->
                                searchQuery = newQuery
                            }
                        ) { newState ->
                            showSearchBar = newState

                        }
                    } else {

                        Text(
                            text = stringResource(id = R.string.app_name),
                            fontSize = 30.sp
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
                                contentDescription = "Localized description",
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    }
                },
                modifier = Modifier.wrapContentHeight()
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

            if (!showSearchBar) {
                HorizontalDivider()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            )
            {

                when (chatState) {

                    is ChatListState.Loading -> {
                        // Do nothing
                    }

                    is ChatListState.Success -> {

                        val chats = (chatState as ChatListState.Success).chats

                        if (chats.isEmpty()) {
                            Text(
                                text = "No active chats",
                                fontSize = 18.sp
                            )
                        } else {


                            LazyColumn(
                                state = lazyListState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(10.dp)

                            ) {


                                itemsIndexed(
                                    items = visibleChatList,
                                    key = { _, chat -> chat.chatId }
                                ) { _, chat ->

                                    // adds the chat which are not friend with the user (incoming new message -> add them to friend for now)
                                    val friendData by globalMessageListenerViewModel
                                        .getOrFetchFriend(chat.otherUserId)
                                        .collectAsStateWithLifecycle()


                                    Log.i(
                                        "FRIEND_LOCAL",
                                        "${friendData.toString()} and ${chat.otherUserId}"
                                    )

                                    ChatItemAndFriendListItem(
                                        friendId = chat.otherUserId,
                                        globalMessageListenerViewModel = globalMessageListenerViewModel,
                                        navController = navController,
                                        chatId = chat.chatId,
                                        isChatList = true,
                                        friendData = friendData,
                                        selectedForDeletion = {}
                                    )
                                }

                                // adding space at the end of list so it doesn't get covered by bottom bar
                                item {
                                    Spacer(modifier = Modifier.height(72.dp))
                                }
                            }
                        }


                    }
                }
            }


        }

    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatItemAndFriendListItem(
    friendId: String,
    globalMessageListenerViewModel: GlobalMessageListenerViewModel,
    navController: NavHostController,
    chatId: String,
    isChatList: Boolean,
    isDeleteBarActive: Boolean = false,
    friendData: Friend?,
    selectedForDeletion: (String) -> Unit,
) {

    val messages by globalMessageListenerViewModel
        .getMessage(chatId)
        .collectAsStateWithLifecycle(
            initialValue = emptyList()
        )

    LaunchedEffect(chatId) {

        if (messages.isEmpty()) {
            globalMessageListenerViewModel.loadMessagesOnceForOldChat(chatId)
        }
    }

    val lastMessage = if (messages.isNotEmpty()) messages.maxByOrNull { it.timeInMills } else null


    val dateAndTime by remember(lastMessage?.timeInMills) {

        mutableStateOf(formatTimestamp(lastMessage?.timeInMills))
    }


    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    if (chatId.isEmpty()) {

        // Placeholder / Skeleton Loader
        ChatItemPlaceholder(showLastMsgTime = isChatList)

    } else {


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .combinedClickable(
                    onClick = {

                        if (isDeleteBarActive) {
                            selectedForDeletion(friendData?.uid.orEmpty())
                        } else {
                            navController.navigate(
                                "MainChat/$friendId/$chatId"
                            )
                        }

                    },

                    onLongClick = {

                        if (currentRoute == "FriendListScreen") {

                            selectedForDeletion(friendData?.uid.orEmpty())

                        }
                    }

                )

        ) {

            //Profile image
            AsyncImage(
                model = friendData?.photoUrl,
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
                    text = friendData?.name.orEmpty(),
                    fontSize = 20.sp,
                    modifier = Modifier.padding(2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )




                Text(
                    text = if (isChatList) lastMessage?.messageContent.orEmpty() else friendData?.about.orEmpty(),
                    fontSize = 16.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

            }

            if (isChatList) {
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun requestPerm(): MultiplePermissionsState {

    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = buildList {

            // Only needed for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    )

    LaunchedEffect(Unit) {
        multiplePermissionsState.launchMultiplePermissionRequest()
    }

    return multiplePermissionsState
}







