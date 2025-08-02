package com.example.chatapp.common.presentation.dialogBox

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.chatapp.profile_feature.presentation.isKeyboardVisible
import com.example.chatapp.common.presentation.GlobalMessageListenerViewModel
import kotlinx.coroutines.delay

@Composable
fun AddFriendDialogBox(
    globalMessageListenerViewModel: GlobalMessageListenerViewModel,
    onDismiss: (state: Boolean) -> Unit
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val isKeyboardVisible = isKeyboardVisible()
    val focusRequester = remember { FocusRequester() }

    val context = LocalContext.current

    var friendUserId by rememberSaveable {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Dialog(onDismissRequest = {
        friendUserId = ""
        onDismiss(false)
        keyboardController?.hide()
    }) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable {

                    friendUserId = ""
                    onDismiss(false)
                    keyboardController?.hide()
                },
            contentAlignment = if (isKeyboardVisible) Alignment.Center else Alignment.BottomCenter
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(10.dp)
            ) {

                Column(
                    modifier = Modifier
                        .padding(15.dp)
                        .fillMaxWidth()
                ) {

                    Text(text = "Enter your friend user id or email", fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(15.dp))

                    TextField(
                        value = friendUserId,
                        onValueChange = { friendUserId = it },
                        modifier = Modifier
                            .padding(4.dp)
                            .focusRequester(focusRequester),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Gray,
                            unfocusedIndicatorColor = Color.Gray
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {

                                globalMessageListenerViewModel.addNewFriend(friendUserId,
                                    onSuccess = {
                                        Toast.makeText(
                                            context,
                                            "Friend added successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onFailure = { message ->

                                        Toast.makeText(
                                            context,
                                            message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    })
                                friendUserId = ""
                                onDismiss(false)
                                keyboardController?.hide()
                            }
                        ),
                        maxLines = 4,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Cancel",
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clickable {

                                    friendUserId = ""
                                    onDismiss(false)
                                    keyboardController?.hide()
                                }
                        )

                        Text(
                            text = "Add",
                            modifier = Modifier.clickable {

                                 globalMessageListenerViewModel.addNewFriend(friendUserId,
                                    onSuccess = {
                                        Toast.makeText(
                                            context,
                                            "Friend added successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onFailure = { message ->

                                        Toast.makeText(
                                            context,
                                            message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    })
                                friendUserId = ""
                                onDismiss(false)
                                keyboardController?.hide()
                            }
                        )

                    }
                }
            }
        }

    }
}
