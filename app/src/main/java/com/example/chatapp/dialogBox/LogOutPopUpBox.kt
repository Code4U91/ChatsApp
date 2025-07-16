package com.example.chatapp.dialogBox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.chatapp.common.presentation.GlobalMessageListenerViewModel

@Composable
fun LogOutPopUpBox(
    globalMessageListenerViewModel: GlobalMessageListenerViewModel,
    onDismiss: (expanded: Boolean) -> Unit
) {


    Dialog(
        onDismissRequest = { onDismiss(false) },

        ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss(false) },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                shape = RoundedCornerShape(10.dp)
            ) {

                Text(
                    text = "Log out",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(8.dp)
                )

                Text(
                    text = "Are you sure you want to logout?",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {

                        globalMessageListenerViewModel.signOut()
                        onDismiss(false)
                    },
                    colors = ButtonColors(
                        contentColor = Color.White,
                        containerColor = Color.Red,
                        disabledContentColor = Color.White,
                        disabledContainerColor = Color.Gray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Text(text = "Log out")
                }

                Button(
                    onClick = { onDismiss(false) },
                    colors = ButtonColors(
                        contentColor = Color.White,
                        containerColor = Color.Gray,
                        disabledContentColor = Color.White,
                        disabledContainerColor = Color.Gray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 14.sp
                    )
                }


            }
        }

    }
}