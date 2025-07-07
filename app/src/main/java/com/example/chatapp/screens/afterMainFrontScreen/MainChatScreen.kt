package com.example.chatapp.screens.afterMainFrontScreen

import android.content.Intent
import android.os.Build
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.chatapp.CALL_INTENT
import com.example.chatapp.CallMetadata
import com.example.chatapp.Message
import com.example.chatapp.appInstance
import com.example.chatapp.call.activity.CallActivity
import com.example.chatapp.formatOnlineStatusTime
import com.example.chatapp.getDateLabelForMessage
import com.example.chatapp.getMessageIconColor
import com.example.chatapp.getMessageStatusIcon
import com.example.chatapp.getTimeOnly
import com.example.chatapp.toEntity
import com.example.chatapp.toLocalDate
import com.example.chatapp.viewmodel.GlobalMessageListenerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainChatScreen(
    navController: NavHostController,
    otherId: String,
    fetchedChatId: String, // channel id might not exist if no message exists in a chat
    globalMessageListenerViewModel: GlobalMessageListenerViewModel
) {

    var messageText by rememberSaveable {
        mutableStateOf("")
    }

    val messageList by globalMessageListenerViewModel.getMessage(fetchedChatId).collectAsState(initial = emptyList())
    val friendData by globalMessageListenerViewModel.getFriendData(otherId).collectAsState(initial = null)


    val currentUserData by globalMessageListenerViewModel.userData.collectAsState()

    var messageDeletionSet by rememberSaveable {
        mutableStateOf<Set<String>>(emptySet())
    }

    val isDeleteBarOn = messageDeletionSet.isNotEmpty()


    DisposableEffect(otherId) {

        val listener = globalMessageListenerViewModel.fetchFriendData(otherId){

            globalMessageListenerViewModel.insertFriend(it.toEntity())
        }
        onDispose {

            listener?.remove()
        }
    }

    val currentChatId by globalMessageListenerViewModel.currentOpenChatId.collectAsState()

    val chatId by remember {
        mutableStateOf(fetchedChatId.ifEmpty {
            globalMessageListenerViewModel.calculateChatId(
                otherId
            ) ?: ""
        })
    } // temporary creates an id when chatId is empty

    val listState = rememberLazyListState()

    val context = LocalContext.current


    val appInstance = context.appInstance()

    val lifecycleOwner = LocalLifecycleOwner.current


    val onlineStatus by produceState(initialValue = "", key1 = otherId) {

        val (dbRef, listener) = globalMessageListenerViewModel.fetchOnlineStatus(otherId)
        { updatedOnlineStatus ->
            value = when (updatedOnlineStatus) {
                1L -> "Online"


                0L -> "failed to fetch online status"


                else -> "last seen " + formatOnlineStatusTime(updatedOnlineStatus)
            }
        }

        awaitDispose {
            dbRef.removeEventListener(listener)
        }
    }

    LaunchedEffect(key1 = chatId) {
        globalMessageListenerViewModel.setActiveChat(chatId)
    }



    // marks message as seen on past message received while the app was on pause/stop etc
    DisposableEffect(lifecycleOwner, chatId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {

                globalMessageListenerViewModel.setActiveChat(chatId)
                // checks if the message has unseen status message
                if (globalMessageListenerViewModel.hasUnseenMessages(
                    messageList
                    ) && currentChatId == chatId && appInstance.isInForeground
                ) {
                    globalMessageListenerViewModel.markAllMessageAsSeen(chatId, otherId)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            globalMessageListenerViewModel.setActiveChat("")
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }



    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            TopAppBar(
                navigationIcon = {


                    IconButton(onClick = {

                        if (!isDeleteBarOn) {
                            navController.popBackStack()
                        } else {
                            messageDeletionSet = emptySet()

                        }


                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            contentDescription = "back button"
                        )
                    }


                },
                title = {

                    if (!isDeleteBarOn) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AsyncImage(
                                model = friendData?.photoUrl ?: "",
                                contentDescription = "Profile picture",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .border(0.dp, Color.Transparent, CircleShape)
                            )

                            Column(modifier = Modifier.wrapContentWidth()) {
                                // Profile user name
                                Text(
                                    text = friendData?.name ?: "",
                                    fontSize = 18.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                Text(
                                    text = onlineStatus,
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {


                                VideoCallButton {

                                    val callMetaData = CallMetadata(
                                        channelName = chatId,
                                        uid = currentUserData?.uid.orEmpty(),
                                        callType = "video",
                                        callerName = currentUserData?.name.orEmpty(),
                                        callReceiverId = friendData?.uid.orEmpty(),
                                        isCaller = true,
                                        receiverPhoto = friendData?.photoUrl.orEmpty(),
                                        receiverName = friendData?.name.orEmpty(),
                                        callDocId = null
                                    )

                                    val intent = Intent(context, CallActivity::class.java).apply {
                                        action = CALL_INTENT
                                        putExtra("call_metadata", callMetaData)
                                    }

                                    context.startActivity(intent)

                                }

                                VoiceCallButton {

                                    val callMetaData = CallMetadata(
                                        channelName = chatId,
                                        uid = currentUserData?.uid.orEmpty(),
                                        callType = "voice",
                                        callerName = currentUserData?.name.orEmpty(),
                                        callReceiverId = friendData?.uid.orEmpty(),
                                        isCaller = true,
                                        receiverPhoto = friendData?.photoUrl.orEmpty(),
                                        receiverName = friendData?.name.orEmpty(),
                                        callDocId = null
                                    )

                                    val intent = Intent(context, CallActivity::class.java).apply {
                                        action = CALL_INTENT
                                        putExtra("call_metadata", callMetaData)
                                    }

                                    context.startActivity(intent)
                                }

                            }

                        }
                    }


                },
                actions = {
                    if (isDeleteBarOn) {
                        IconButton(onClick = {
                            // on click delete the message and close the delete bar
                            globalMessageListenerViewModel.deleteMessage(
                                chatId,
                                messageDeletionSet
                            )
                            messageDeletionSet = emptySet()

                        }) {

                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete button"
                            )

                        }
                    }

                },
                modifier = Modifier.height(55.dp)
            )
        }


    ) { innerPadding ->


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            HorizontalDivider()

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            )
            {

                ChatLazyColumn(
                    messageList = messageList,
                    listState = listState,
                    isCurrentUser = { senderId ->
                        globalMessageListenerViewModel.isCurrentUserASender(
                            senderId
                        )
                    },
                    getDateLabel = { date -> getDateLabelForMessage(date) },
                    updateSelectedMessages = { id ->

                        messageDeletionSet = if (messageDeletionSet.contains(id)) {
                            messageDeletionSet - id
                        } else {
                            messageDeletionSet + id
                        }

                    },
                    selectedMessageSet = messageDeletionSet,
                    isDeleteBarOn = isDeleteBarOn

                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .imePadding()
            ) {

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { newText ->
                        messageText = newText
                    },
                    maxLines = 5,
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    shape = RoundedCornerShape(30.dp),
                    placeholder = { Text(text = "Message") }
                )

                IconButton(
                    onClick = {

                        // sends or uploads the message
                        // if the chat already exists uses chatId (if not empty)
                        // if chat already doesn't exists creates new chat user otherId
                        if (messageText.isNotEmpty()) {

                            globalMessageListenerViewModel.sendMessageToOneFriend(
                                messageText,
                                otherId,
                                chatId,
                                friendData?.name,
                                currentUserData?.name,
                                chatId
                            )
                            messageText = ""
                        }

                    },
                    modifier = Modifier.align(Alignment.Bottom),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send message button",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

    }

}

@Composable
fun ChatLazyColumn(
    messageList: List<Message>,
    listState: LazyListState = rememberLazyListState(),
    isCurrentUser: (String) -> Boolean,
    getDateLabel: (LocalDate) -> String,
    updateSelectedMessages: (String) -> Unit,
    selectedMessageSet: Set<String>, // used to gray out the selected item on the list
    isDeleteBarOn: Boolean,
    contentPadding: PaddingValues = PaddingValues(10.dp)
) {
    LaunchedEffect(messageList.firstOrNull()?.messageId) {
        if (messageList.isNotEmpty() && listState.firstVisibleItemIndex <= 2) {
            listState.animateScrollToItem(0)
        }
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        reverseLayout = true,
        state = listState
    ) {
        itemsIndexed(
            items = messageList,
            key = { _, message -> message.messageId }
        ) { index, message ->

            val nextMessage = messageList.getOrNull(index + 1)
            val currentMessageDate = message.timeStamp?.toLocalDate()
            val previousMessageDate = nextMessage?.timeStamp?.toLocalDate()

            val isNewGroup = nextMessage?.senderId != message.senderId
            val isSelected = selectedMessageSet.contains(message.messageId)

            val color =
                if (isDeleteBarOn && isSelected) Color.Gray else Color.Transparent

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(color, shape = RoundedCornerShape(10.dp))
            ) {
                ChatBubble(
                    message = message,
                    isCurrentUser = isCurrentUser(message.senderId),
                    isNewGroup = isNewGroup,
                    isDeleteBarOn = isDeleteBarOn
                ) { msgId ->

                    updateSelectedMessages(msgId)

                }
            }


            if (isNewGroup) {
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (currentMessageDate != previousMessageDate) {
                currentMessageDate?.let { date ->
                    DateChip(dateLabel = getDateLabel(date))
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: Message,
    isCurrentUser: Boolean,
    isNewGroup: Boolean,
    isDeleteBarOn: Boolean,
    selectedForDeletion: (String) -> Unit
) {
    val bubbleShape = if (isNewGroup) {
        RoundedCornerShape(
            topStart = if (isCurrentUser) 12.dp else 0.dp,
            topEnd = if (isCurrentUser) 0.dp else 12.dp,
            bottomStart = 12.dp,
            bottomEnd = 12.dp
        )
    } else {
        RoundedCornerShape(8.dp)
    }

    // Select icon and color based on message status
    val statusIcon = getMessageStatusIcon(message.status)

    val iconColor = getMessageIconColor(message.status)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = {
                    if (isDeleteBarOn) {
                        selectedForDeletion(message.messageId)
                    }
                },

                onLongClick = {
                    selectedForDeletion(message.messageId)
                }
            ),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {

        if (isCurrentUser) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(
                    text = getTimeOnly(message.timeStamp!!),
                    color = Color.Gray,
                    fontSize = 8.sp,
                    modifier = Modifier
                        .offset(y = 4.dp)
                )

                statusIcon.let {
                    Spacer(modifier = Modifier.width(2.dp))

                    Icon(
                        imageVector = it,
                        contentDescription = message.status,
                        tint = iconColor,
                        modifier = Modifier
                            .size(12.dp)
                            .offset(y = 4.dp)
                    )
                }
            }

        }

        Box(
            modifier = Modifier
                .wrapContentWidth()
                .widthIn(max = 300.dp)
                .background(
                    color = if (isCurrentUser) Color(0xFFDCF8C6) else Color.LightGray,
                    shape = bubbleShape
                )
                .padding(8.dp)
        ) {
            Text(
                text = message.messageContent.orEmpty(),
                fontSize = 16.sp,
                color = Color.Black
            )
        }

        if (!isCurrentUser) {
            Text(
                text = getTimeOnly(message.timeStamp!!),
                color = Color.Gray,
                fontSize = 8.sp,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .offset(y = 4.dp)
            )


        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceCallButton(onStartCall: () -> Unit) {
    val multiplePermissionsState = rememberMultiplePermissionsState(

        permissions = buildList {
            add(android.Manifest.permission.RECORD_AUDIO)
            add(android.Manifest.permission.BLUETOOTH)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(android.Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    )
    IconButton(onClick = {
        if (multiplePermissionsState.allPermissionsGranted) {
            onStartCall()
        } else {
            multiplePermissionsState.launchMultiplePermissionRequest()
        }
    }) {

        Icon(imageVector = Icons.Default.Call, contentDescription = "voice call button")

    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VideoCallButton(onStartCall: () -> Unit) {

    val multiplePermissionsState = rememberMultiplePermissionsState(

        permissions = buildList {
            add(android.Manifest.permission.RECORD_AUDIO)
            add(android.Manifest.permission.CAMERA)
            add(android.Manifest.permission.BLUETOOTH)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(android.Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    )
    IconButton(onClick = {
        if (multiplePermissionsState.allPermissionsGranted) {
            onStartCall()
        } else {
            multiplePermissionsState.launchMultiplePermissionRequest()
        }
    }) {

        Icon(imageVector = Icons.Default.VideoCall, contentDescription = "video call button")

    }

}


@Composable
fun DateChip(dateLabel: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {

        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(vertical = 5.dp)
        ) {

            Text(
                text = dateLabel,
                modifier = Modifier.padding(5.dp),
                color = Color.Gray,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

