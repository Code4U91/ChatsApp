package com.example.chatapp.screens.mainBottomBarScreens

import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowOutward
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.chatapp.CallData
import com.example.chatapp.FriendData
import com.example.chatapp.formatDurationText
import com.example.chatapp.formatTimestampToDateTime
import com.example.chatapp.getDateLabelForMessage
import com.example.chatapp.screens.afterMainFrontScreen.DateChip
import com.example.chatapp.screens.afterMainFrontScreen.VideoCallButton
import com.example.chatapp.screens.afterMainFrontScreen.VoiceCallButton
import com.example.chatapp.toLocalDate
import com.example.chatapp.viewmodel.GlobalMessageListenerViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryScreen(
    globalMessageListenerViewModel: GlobalMessageListenerViewModel,
    navController: NavHostController
) {


    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }

    var showSearchBar by rememberSaveable {
        mutableStateOf(false)
    }

    val callList by globalMessageListenerViewModel.callHistoryData.collectAsState()


    val filteredCallList = callList.filter {
        it.otherUserName?.trim()?.contains(searchQuery.trim(), ignoreCase = true) == true
    }.sortedByDescending { it.callEndTime?.toDate()?.time ?: 0L }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {

                    if (showSearchBar) {
                        TopAppBarTemplate(
                            searchQuery = searchQuery,
                            placeHolderText = "Search in call history..",
                            onQueryChanged = { newQuery ->
                                searchQuery = newQuery
                            }) { newState ->
                            showSearchBar = newState
                        }
                    } else {
                        // if search bar isn't opened
                        Text(
                            text = "Calls",
                            fontSize = 25.sp
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
                modifier = Modifier.wrapContentHeight()
            )
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(it),
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

                if (callList.isEmpty()) {
                    Text(
                        text = "Nothing in call history yet",
                        fontSize = 25.sp
                    )
                }

                CallLazyColumn(
                    filteredCallList,
                    globalMessageListenerViewModel,
                    navController
                )

            }
        }
    }
}

@Composable
fun CallLazyColumn(
    filteredCallList: List<CallData>,
    globalMessageListenerViewModel: GlobalMessageListenerViewModel,
    navController: NavHostController,
    listState: LazyListState = rememberLazyListState()
) {
    LaunchedEffect(filteredCallList.firstOrNull()?.callId) {

        if (filteredCallList.isNotEmpty() && listState.firstVisibleItemIndex <= 2) {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        state = listState
    ) {


        itemsIndexed(filteredCallList, key = { _, item -> item.callId })
        { index, callData ->

            val nextCallItem = filteredCallList.getOrNull(index - 1)
            val currentCallDate = callData.callEndTime?.toLocalDate()
            val previousCallDate = nextCallItem?.callEndTime?.toLocalDate()

            if (currentCallDate != previousCallDate) {
                currentCallDate?.let { date ->
                    DateChip(dateLabel = getDateLabelForMessage(date))
                }
            }

            CallListItem(
                otherUserId = callData.otherUserId ?: "",
                globalMessageListenerViewModel = globalMessageListenerViewModel,
                callData.callStartTime,
                callData.callEndTime,
                callType = callData.callType ?: "",
                isCaller = callData.callReceiverId == callData.otherUserId, // checking if other user is caller, otherUserId is other participantId
                callStatus = callData.status ?: ""
            ) {
                navController.navigate("CallScreen/${callData.channelId}/${callData.callType}/true/${callData.otherUserId}/n"){
                    launchSingleTop = true
                }
            }

        }

        // adding space at the end of list so it doesn't get covered by bottom bar
        item {
            Spacer(modifier = Modifier.height(72.dp))
        }

    }
}

@Composable
fun CallListItem(
    otherUserId: String,
    globalMessageListenerViewModel: GlobalMessageListenerViewModel,
    callStartTime: Timestamp?,
    callEndTime: Timestamp?,
    callType: String,
    isCaller: Boolean,
    callStatus: String,
    startCall: () -> Unit
) {

    val friendData by produceState<FriendData?>(initialValue = null, key1 = otherUserId)
    {
        val listener = globalMessageListenerViewModel.fetchFriendData(otherUserId)
        { data ->
            value = data
        }

        awaitDispose {

            listener.remove()
        }
    }

    val time by remember {
        mutableStateOf(formatTimestampToDateTime(callStartTime ?: Timestamp.now()))
    }

    val duration = remember(callStartTime, callEndTime) {
        if (callStartTime != null && callEndTime != null) {
            formatDurationText(callEndTime.toDate().time - callStartTime.toDate().time)
        } else {
            ""
        }
    }

    val timeText = when (callStatus) {

        "ended" -> {
            "call lasted $duration at $time"
        }

        "missed" -> {
            "missed call at $time"
        }

        "declined" -> {"call declined at $time"}

        else -> {
            ""
        }
    }


    val icon = when (isCaller) {
        true -> {
            Icons.Default.ArrowOutward // currentUser is the caller, outgoing call
        }

        false -> {
            Icons.Default.ArrowDownward // currentUser is the receiver, incoming call
        }
    }


    val iconTint = when (callStatus) {
        "ended" -> {
            Color.Green
        }

        "missed" -> {
            Color.Red
        }

        "ongoing" -> {
            Color.Green
        }

        "declined" -> {Color.Red}

        else -> {
            Color.Unspecified
        }
    }


    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
    ) {


        //Profile image
        AsyncImage(
            model = friendData?.photoUrl ?: "",
            contentDescription = "Profile picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, Color.Transparent, CircleShape)
        )


        Column(modifier = Modifier.wrapContentWidth()) {

            Text(
                text = friendData?.name ?: "",
                fontSize = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (callStatus == "missed" || callStatus == "declined") Color.Red else Color.Unspecified
            )

            Row(modifier = Modifier.wrapContentHeight()) {


                Icon(
                    imageVector = icon, contentDescription = "",
                    modifier = Modifier.size(15.dp),
                    tint = iconTint
                )

                Text(
                    text = timeText,
                    fontSize = 12.sp
                )

            }

        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {

            when (callType) {
                "voice" -> VoiceCallButton {
                    startCall()
                }

                "video" -> VideoCallButton {
                    startCall()
                }

                else -> {}
            }

        }
    }
}

@Composable
fun TopAppBarTemplate(
    searchQuery: String,
    placeHolderText: String,
    onQueryChanged: (String) -> Unit,
    updateSearchBarState: (Boolean) -> Unit
) {

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
            .padding(end = 14.dp, bottom = 10.dp),
        shape = CircleShape
    ) {

        // field where user can enter text to sort the friend by name
        TextField(
            value = searchQuery,
            onValueChange = { onQueryChanged(it) },
            placeholder = { Text(text = placeHolderText, fontSize = 14.sp) },
            singleLine = true,
            leadingIcon = {

                IconButton(onClick = {

                    updateSearchBarState(false)
                    onQueryChanged("")

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
            textStyle = TextStyle(
                fontSize = 14.sp
            ),
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

}