package com.example.chatapp.screens.logInSignUp

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.chatapp.R
import com.example.chatapp.checkEmailPattern
import com.example.chatapp.viewmodel.AuthState
import com.example.chatapp.viewmodel.ChatsViewModel


@Composable
fun SignInScreenUI(
    viewmodel: ChatsViewModel,
    navController: NavHostController,
) {

    val context = LocalContext.current
    val activityContext = LocalActivity.current
    val authState by viewmodel.authState.collectAsState()

    LaunchedEffect(authState) {

        if (authState is AuthState.Error) {
            Toast.makeText(
                context,
                "${(authState as AuthState.Error).message}",
                Toast.LENGTH_SHORT
            ).show()

            viewmodel.updateAuthState(AuthState.Unauthenticated) // clearing up error msg
        }

    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
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
                        text = "Sign in",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )

                    LoginCardUi(viewmodel, activityContext, navController, authState, context)

                }

            }

            if (authState is AuthState.Loading) {
                CircularProgressIndicator()
            }


        }
    }


}


@Composable
fun LoginCardUi(
    viewmodel: ChatsViewModel,
    activityContext: Activity?,
    navController: NavHostController,
    authState: AuthState,
    context: Context
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

    val scrollState = rememberScrollState()

    val isEmailValid = checkEmailPattern(email)


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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Forgot password?",
                        fontSize = 14.sp,
                        modifier = Modifier.clickable {
                            navController.navigate("forgotPwd")
                        }
                    )
                }

            }

        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {

                    if (isEmailValid && password.length >= 6) {
                        viewmodel.signInUsingEmailAndPwd(email, password)
                    } else {
                        Toast.makeText(
                            context,
                            "Email or password is invalid",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                },
                enabled = authState != AuthState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = "Sign in",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Create an account",
                fontSize = 14.sp,
                modifier = Modifier.clickable {

                    if (authState != AuthState.Loading) {
                        password = ""
                        navController.navigate("SignUp")
                    }

                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Or",
                fontSize = 14.sp
            )

            Button(
                onClick = {
                    viewmodel.signInUsingGoogle(activityContext)
                },
                enabled = authState != AuthState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {

                Image(
                    painter = painterResource(
                        id = R.drawable.googleicon
                    ),
                    contentDescription = "Google icon",
                    modifier = Modifier
                        .size(20.dp)
                        .scale(1.2f)
                )

                Text(
                    text = "  Sign in with Google",
                    fontSize = 18.sp
                )
            }


        }
    }

}
