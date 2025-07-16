package com.example.chatapp.screens.afterMainFrontScreen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chatapp.core.checkEmailPattern
import com.example.chatapp.viewmodel.ChatsViewModel
import kotlinx.coroutines.delay

@Composable
 fun ChangeEmailAddressScreen(viewModel: ChatsViewModel) {

    var newEmail by rememberSaveable {
        mutableStateOf("")
    }


    var timer by rememberSaveable {
        mutableIntStateOf(0)
    }

    var isButtonEnabled by rememberSaveable {
        mutableStateOf(true)
    }

    val isEmailValid =  checkEmailPattern(newEmail)

    val context = LocalContext.current

    LaunchedEffect(timer) {
        if(timer > 0)
        {
            delay(1000L)
            timer--
        } else
        {
            isButtonEnabled = true
        }
    }

    Scaffold { paddingValue ->

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValue),
            contentAlignment = Alignment.Center
        )
        {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(text = "Change email",
                    fontSize = 25.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    text = "Enter your new email address.",
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = newEmail,
                    onValueChange =
                    {
                        newEmail = it
                    },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,

                    )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopStart
                )
                {

                    if (!isButtonEnabled) {
                        Text(
                            text = "Resend email in $timer seconds",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                }


                Button(
                    onClick = {

                        if (isEmailValid)
                        {

                            viewModel.updateUserEmail(newEmail,
                                onSuccess = {
                                    Toast.makeText(context, "Verification email sent to your new email.", Toast.LENGTH_LONG).show()
                                },
                                onFailure = {response ->
                                    Toast.makeText(context, response, Toast.LENGTH_LONG).show()
                                }
                            )

                            isButtonEnabled = false
                            timer = 120

                        }

                        else
                        {
                            Toast.makeText(context,  "Invalid email format", Toast.LENGTH_SHORT).show()
                        }

                    },
                    enabled = isButtonEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = "Confirm",
                        fontSize = 18.sp
                    )
                }

            }
        }
    }
}