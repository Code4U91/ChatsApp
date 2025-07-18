package com.example.chatapp.call.presentation.call_history_screen

import android.content.Intent
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.chatapp.call.presentation.call_screen.activity.CallActivity
import com.example.chatapp.call.presentation.model.CallUiData
import com.example.chatapp.common.presentation.GlobalMessageListenerViewModel
import com.example.chatapp.core.CALL_INTENT
import com.example.chatapp.core.formatDurationText
import com.example.chatapp.core.formatTimestampToDateTime
import com.example.chatapp.core.getDateLabelForMessage
import com.example.chatapp.core.local_database.toEntity
import com.example.chatapp.core.model.CallMetadata
import com.example.chatapp.core.toLocalDate
import com.example.chatapp.screens.afterMainFrontScreen.DateChip
import com.example.chatapp.screens.afterMainFrontScreen.VideoCallButton
import com.example.chatapp.screens.afterMainFrontScreen.VoiceCallButton
import com.example.chatapp.viewmodel.ChatsViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryScreen(
    globalMessageListenerViewModel: GlobalMessageListenerViewModel,
    chatsViewModel: ChatsViewModel
) {


    DisposableEffect(Unit) {

        chatsViewModel.setHistoryScreenActive(true)

        onDispose {
            chatsViewModel.setHistoryScreenActive(false)
        }
    }

    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }

    var showSearchBar by rememberSaveable {
        mutableStateOf(false)
    }

    val callList by globalMessageListenerViewModel.callHistory.collectAsState()


    val filteredCallList = callList.filter {
        it.otherUserName.trim().contains(searchQuery.trim(), ignoreCase = true)
    }

    var showEmptyState by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1000)
        showEmptyState = true

    }


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

                when {

                    callList.isNotEmpty() -> {
                        CallLazyColumn(
                            if (!showSearchBar) callList else filteredCallList,
                            globalMessageListenerViewModel
                        )
                    }

                    showEmptyState -> {
                        Text(
                            text = "Nothing in call history yet",
                            fontSize = 25.sp
                        )
                    }

                }

            }
        }
    }
}

@Composable
fun CallLazyColumn(
    callListData: List<CallUiData>,
    globalMessageListenerViewModel: GlobalMessageListenerViewModel,
    listState: LazyListState = rememberLazyListState()
) {

    val context = LocalContext.current
    val currentUserData by globalMessageListenerViewModel.userData.collectAsState()

    LaunchedEffect(callListData.firstOrNull()?.callId) {

        if (callListData.isNotEmpty() && listState.firstVisibleItemIndex <= 2) {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        state = listState
    ) {


        itemsIndexed(callListData, key = { _, item -> item.callId })
        { index, callData ->


            val nextCallItem = callListData.getOrNull(index - 1)
            val currentCallDate = callData.callEndTime.toLocalDate()
            val previousCallDate = nextCallItem?.callEndTime?.toLocalDate()

            if (currentCallDate != previousCallDate) {
                DateChip(dateLabel = getDateLabelForMessage(currentCallDate))
            }

            CallListItem(
                callData = callData,
                globalMessageListenerViewModel = globalMessageListenerViewModel,
                isCaller = callData.callReceiverId == callData.otherUserId, // checking if other user is caller, otherUserId is other participantId
                startCall = {receiverPhotoUrl ->

                    currentUserData?.let { currentUser ->

                        val callMetaData = CallMetadata(
                            channelName = callData.channelId,
                            uid = currentUser.uid,
                            callType = callData.callType,
                            callerName = currentUser.name,
                            callReceiverId = callData.otherUserId,
                            isCaller = true,
                            receiverPhoto = receiverPhotoUrl,
                            receiverName = callData.otherUserName,
                            callDocId = null
                        )

                        val intent = Intent(context, CallActivity::class.java).apply {

                            this.action = CALL_INTENT
                            putExtra("call_metadata", callMetaData)
                        }

                        context.startActivity(intent)
                    }
                })

        }

        // adding space at the end of list so it doesn't get covered by bottom bar
        item {
            Spacer(modifier = Modifier.height(72.dp))
        }

    }
}

@Composable
fun CallListItem(
    globalMessageListenerViewModel: GlobalMessageListenerViewModel,
    isCaller: Boolean,
    callData: CallUiData,
    startCall: (photoUrl : String) -> Unit
) {

    val friendData by globalMessageListenerViewModel.getFriendData(callData.otherUserId)
        .collectAsState(null)

    DisposableEffect(callData.otherUserId) {

        val listener = globalMessageListenerViewModel.fetchFriendData(callData.otherUserId) { data ->

            globalMessageListenerViewModel.insertFriend(data.toEntity())

        }

        onDispose {
            listener?.remove()
        }
    }

    val time by remember {
        mutableStateOf(formatTimestampToDateTime(callData.callStartTime))
    }

    val duration = remember(callData.callStartTime, callData.callEndTime) {
        formatDurationText(callData.callEndTime - callData.callStartTime)
    }

    val timeText = when (callData.status) {

        "ended" -> {
            "call lasted $duration at $time"
        }

        "missed" -> {
            "missed call at $time"
        }

        "declined" -> {
            "call declined at $time"
        }

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


    val iconTint = when (callData.status) {
        "ended" -> {
            Color.Green
        }

        "missed" -> {
            Color.Red
        }

        "ongoing" -> {
            Color.Green
        }

        "declined" -> {
            Color.Red
        }

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
            model =  friendData?.photoUrl,
            contentDescription = "Profile picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, Color.Transparent, CircleShape)
        )


        Column(modifier = Modifier.wrapContentWidth()) {

            Text(
                text = friendData?.name.orEmpty(),
                fontSize = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (callData.status == "missed" || callData.status == "declined") Color.Red else Color.Unspecified
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

            when (callData.callType) {
                "voice" -> VoiceCallButton {

                    friendData?.let {
                        startCall(it.photoUrl)
                    }



                }

                "video" -> VideoCallButton {

                    friendData?.let {
                        startCall(it.photoUrl)
                    }

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