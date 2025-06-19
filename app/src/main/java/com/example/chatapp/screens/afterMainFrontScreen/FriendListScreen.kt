package com.example.chatapp.screens.afterMainFrontScreen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import com.example.chatapp.FriendScreenUiItem
import com.example.chatapp.dialogBox.AddFriendDialogBox
import com.example.chatapp.screens.mainBottomBarScreens.ChatItemAndFriendListItem
import com.example.chatapp.screens.mainBottomBarScreens.TopAppBarTemplate
import com.example.chatapp.viewmodel.GlobalMessageListenerViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendListScreen(
    navController: NavHostController,
    globalMessageListenerViewModel: GlobalMessageListenerViewModel
) {


    var expandFriendDialogBox by rememberSaveable {
        mutableStateOf(false)
    }

    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }

    var showSearchBar by rememberSaveable {
        mutableStateOf(false)
    }

    // listens for the friendList and fetches them
    // removes listener when the compose is not on view
    val friendList by produceState(initialValue = emptyList()) {

        val listener = globalMessageListenerViewModel.fetchFriendList { friendListData ->
            value = friendListData.map { it }
        }

        awaitDispose {

            listener?.remove()
        }
    }

    // filters fetched friend list
    val filteredFriendList =
        friendList.filter { it.friendName.trim().contains(searchQuery.trim(), ignoreCase = true) }
            .sortedBy { it.friendName.lowercase() }

    // collects total number of friends
    val totalFriends by globalMessageListenerViewModel.totalFriend.collectAsState()


    // list of ui component
    val friendScreenUiItemList = listOf(
        FriendScreenUiItem(icon = Icons.Default.AddComment, itemDescription = "New friend"),
        FriendScreenUiItem(icon = Icons.Default.Groups, itemDescription = "New group")
    )

    var friendDeleteList by rememberSaveable {
        mutableStateOf<Set<String>>(emptySet())
    }

    val isDeleteTopBarActive = friendDeleteList.isNotEmpty()

    // main ui container for this page
    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        topBar = {
            TopAppBar(
                title = {

                    if (showSearchBar) {

                        TopAppBarTemplate(
                            searchQuery = searchQuery,
                            placeHolderText = "Search in friend list..",
                            onQueryChanged = { newQuery -> searchQuery = newQuery }
                        ) { newState ->
                            showSearchBar = newState

                            if (isDeleteTopBarActive) {
                                friendDeleteList = emptySet()
                            }
                        }


                    } else {
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
                    }
                },
                navigationIcon = {

                    if (!showSearchBar) {

                        IconButton(onClick = {

                            if (isDeleteTopBarActive) {
                                friendDeleteList = emptySet()
                            } else {
                                navController.popBackStack()
                            }


                        }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBackIosNew,
                                contentDescription = "Localized description"
                            )
                        }
                    }

                },
                actions = {


                    if (!showSearchBar && !isDeleteTopBarActive) {
                        IconButton(onClick = {
                            showSearchBar = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Localized description"
                            )
                        }
                    } else if (isDeleteTopBarActive) {

                        Log.i("DELETE_BT", "Active")
                        IconButton(onClick = {
                            // friend delete function
                            globalMessageListenerViewModel.deleteFriend(friendDeleteList)
                            friendDeleteList = emptySet()
                        }) {

                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete button"
                            )
                        }
                    }
                },
                modifier = Modifier.wrapContentHeight()

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

            if (!showSearchBar) {
                HorizontalDivider()
            }


            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(10.dp)
            ) {


                // hides Option item like add friend when search bar active
                if (!showSearchBar) {

                    items(friendScreenUiItemList, key = { it.itemDescription }) { item ->

                        FriendScreenOptionItem(
                            icon = item.icon,
                            descriptionText = item.itemDescription
                        ) { state ->
                            expandFriendDialogBox = state
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                    }
                }

                // like a horizontal divider but with the description
                item(key = "headlineBar") {

                    Text(
                        text = "Friends on ChatsApp",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(4.dp)
                    )
                    // HorizontalDivider()
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // composes profile items of all fetch friends
                items(filteredFriendList, key = { it.friendId })
                { friend ->

                    val isSelected = friendDeleteList.contains(friend.friendId)
                    val color =
                        if (isDeleteTopBarActive && isSelected) Color.Gray else Color.Transparent

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(color, shape = RoundedCornerShape(10.dp))
                    ) {
                        ChatItemAndFriendListItem(
                            chatItemWithMsg = false,
                            friendId = friend.friendId,
                            globalMessageListenerViewModel = globalMessageListenerViewModel,
                            navController = navController,
                            chatId = globalMessageListenerViewModel.calculateChatId(friend.friendId)
                                ?: "",
                            oldFriendName = friend.friendName,
                            whichList = "friendList",
                            isDeleteBarActive = isDeleteTopBarActive
                        ) { selectedFriendId ->
                            Log.i("FRIEND_DELETE", selectedFriendId)
                            // selected id for deletion
                            friendDeleteList = if (friendDeleteList.contains(selectedFriendId)) {
                                friendDeleteList - selectedFriendId
                            } else {
                                friendDeleteList + selectedFriendId
                            }
                        }
                    }

                }
            }

            // when clicked on add friend, activated drop box
            // which takes friend uid or email
            if (expandFriendDialogBox) {
                AddFriendDialogBox(globalMessageListenerViewModel)
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
            .padding(top = 10.dp)
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