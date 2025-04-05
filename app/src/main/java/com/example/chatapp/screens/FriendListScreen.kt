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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Groups
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.chatapp.FriendScreenUiItem
import com.example.chatapp.dialogBox.AddFriendDialogBox
import com.example.chatapp.viewmodel.GlobalMessageListenerViewModel
import kotlinx.coroutines.delay


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

    // main ui container for this page
    Scaffold(
        modifier = Modifier.fillMaxWidth(),
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
                                .padding(top = 2.dp, end = 14.dp, bottom = 10.dp),
                            shape = CircleShape
                        ) {

                            // field where user can enter text to sort the friend by name
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text(text = "Search in friend list..") },
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

                            navController.popBackStack()

                        }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBackIosNew,
                                contentDescription = "Localized description"
                            )
                        }
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
    ) { innerPadding ->


        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopStart
        )
        {

            if (!showSearchBar)
            {
                HorizontalDivider()
            }


            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp)
                    
            ) {

                // shows ui item
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

                // composes profile items of all fetch friends
                items(filteredFriendList, key = { it.friendId })
                { friendList ->

                    ChatItemAndFriendListItem(
                        chatItemWithMsg = false,
                        friendId = friendList.friendId,
                        navController = navController,
                        globalMessageListenerViewModel = globalMessageListenerViewModel,
                        oldFriendName = friendList.friendName,
                        whichList = "friendList",
                        chatId = globalMessageListenerViewModel.calculateChatId(friendList.friendId)
                    )

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