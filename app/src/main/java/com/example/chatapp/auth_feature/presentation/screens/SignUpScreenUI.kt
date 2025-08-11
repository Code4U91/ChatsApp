package com.example.chatapp.auth_feature.presentation.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.chatapp.core.util.checkEmailPattern
import com.example.chatapp.auth_feature.presentation.viewmodel.AuthState
import com.example.chatapp.auth_feature.presentation.viewmodel.AuthViewModel

@Composable
fun SignUpScreenUI(
    authViewModel:  AuthViewModel,
    navController: NavHostController
) {

    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()


    LaunchedEffect(authState) {
        Log.i("ResultShow2", "$authState")

        if (authState is AuthState.Error) {
            Toast.makeText(
                context,
                "${(authState as AuthState.Error).message}",
                Toast.LENGTH_SHORT
            ).show()

            authViewModel.updateAuthState(AuthState.Unauthenticated) // clearing up error msg
        }

    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {


                    Text(
                        text = "Sign up",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )

                    SignUpCard(authViewModel, authState, context, navController)


                }

            }

            if (authState is AuthState.Loading) {
                CircularProgressIndicator()
            }

        }
    }

}

@Composable
fun SignUpCard(
    authViewModel:  AuthViewModel,
    authState: AuthState,
    context: Context,
    navController: NavHostController
) {


    var email by rememberSaveable {
        mutableStateOf("")
    }

    var password by rememberSaveable {
        mutableStateOf("")
    }

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    var userName by rememberSaveable {
        mutableStateOf("")
    }

    val isEmailValid = checkEmailPattern(email)

    val scrollState = rememberScrollState()

    BackHandler {
        email = ""
        password = ""
        userName = ""

        navController.popBackStack()

    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .verticalScroll(scrollState)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Start
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {

                Text(
                    text = "User name",
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = userName,
                    onValueChange =
                    {
                        userName = it
                    },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,

                    )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Email address",
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = email,
                    onValueChange =
                    {
                        email = it
                    },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,

                    )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Password",
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = password,
                    onValueChange =
                    {
                        password = it
                    },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),

                    trailingIcon = {
                        val icon =
                            if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "Toggle Password Visibility"
                            )
                        }
                    }

                )

            }

        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Sign up button
            Button(
                onClick = {
                    if (isEmailValid && password.length >= 6 && userName.isNotEmpty()) {
                        authViewModel.signUpUsingEmailAndPwd(email, password, userName)

                    } else {
                        Toast.makeText(
                            context,
                            "User name, e mail or password is invalid",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                },
                enabled = authState != AuthState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = "Sign up",
                    fontSize = 18.sp
                )
            }
        }
    }
}

