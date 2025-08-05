package com.example.chatapp.shared.presentation.dialogBox

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import com.example.chatapp.profile_feature.presentation.onSaveOrCancel
import com.example.chatapp.auth_feature.presentation.viewmodel.ChatsViewModel
import kotlinx.coroutines.delay

// used in profile screen for inputting values like name
@Composable
fun PopUpBox(
    valueDescription: String,
    profileValue: String,
    viewmodel: ChatsViewModel,
    onDismiss: (expanded: Boolean) -> Unit
) {


    var profileValueNew by rememberSaveable {
        mutableStateOf(profileValue)
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val isKeyboardVisible = isKeyboardVisible()
    val focusRequester = remember { FocusRequester() }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    // back press
    BackHandler {
        onDismiss(false)
        keyboardController?.hide()
    }


    Dialog(onDismissRequest = {
        onDismiss(false)
        keyboardController?.hide()
    }) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .clickable {
                    onDismiss(false)
//                    profileValueNew = ""
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
                    modifier = Modifier.padding(15.dp)
                ) {

                    Text(text = "Enter your $valueDescription", fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(15.dp))

                    TextField(
                        value = profileValueNew,
                        onValueChange = { profileValueNew = it },
                        modifier = Modifier
                            .fillMaxWidth()
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

                                viewmodel.updateLoadingIndicator(true)

                                onSaveOrCancel(
                                    valueDescription,
                                    profileValueNew,
                                    viewmodel,
                                    context,
                                )
                                keyboardController?.hide()
                            }
                        )
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

                                    profileValueNew = ""
                                    keyboardController?.hide()
                                    onDismiss(false)
                                }
                        )

                        Text(
                            text = "Save",
                            modifier = Modifier.clickable {

                                if (profileValueNew.isNotEmpty()){

                                    viewmodel.updateLoadingIndicator(true)

                                    onSaveOrCancel(
                                        valueDescription,
                                        profileValueNew,
                                        viewmodel,
                                        context,
                                    )
                                    profileValueNew = ""

                                }

                                keyboardController?.hide()
                                onDismiss(false)
                            }
                        )

                    }
                }
            }
        }

    }
}