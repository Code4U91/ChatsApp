package com.example.chatapp.screens.mainBottomBarScreens

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import com.example.chatapp.ProfileItem
import com.example.chatapp.UserData
import com.example.chatapp.dialogBox.LogOutPopUpBox
import com.example.chatapp.dialogBox.PopUpBox
import com.example.chatapp.viewmodel.ChatsViewModel
import com.example.chatapp.viewmodel.GlobalMessageListenerViewModel
import kotlinx.coroutines.launch


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileSettingScreen(
    viewmodel: ChatsViewModel,
    navController: NavHostController,
    paddingValue: PaddingValues,
    globalMessageListenerViewModel: GlobalMessageListenerViewModel
) {

    // this screen needs a major update, looks messy right now

    val userData by globalMessageListenerViewModel.userData.collectAsState()

    val loadingIndicator by viewmodel.loadingIndicator.collectAsState()

    LaunchedEffect(Unit) {

        userData?.email?.let { currentEmailInDBb ->
            viewmodel.checkAndUpdateEmailOnFireStore(currentEmailInDBb)
        }
    }


    var expandEditImg by remember {
        mutableStateOf(false)
    }

    val profileItems =  getProfileItemList(userData)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue),
            verticalArrangement = Arrangement.Top,
            contentPadding = PaddingValues(10.dp)
        ) {

            item {
                Text(
                    text = "Profile",
                    fontSize = 35.sp,

                    )
            }

            item {
                ProfileImageSection(
                    imageUrl = userData?.photoUrl ?: "",
                    onEditClick = { expandEditImg = true }
                )
            }

            item {

                SectionTitle(title = "Account information")
            }



            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {

                    profileItems.forEachIndexed { index, item ->

                        ProfileComponent(
                            primaryIcon = item.primaryIcon,
                            secondaryIcon = item.secondaryIcon,
                            itemDescription = item.itemDescription,
                            itemValue = item.itemValue,
                            viewmodel = viewmodel,
                            navController = navController,
                        )

                        if (index != profileItems.lastIndex) {
                            HorizontalDivider()
                        }
                    }

                }
            }

            item {

                SectionTitle(title = "Security")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {

                    ProfileComponent(
                        primaryIcon = Icons.Default.Lock,
                        secondaryIcon = Icons.Default.ChevronRight,
                        itemDescription = "Password",
                        itemValue = "",
                        viewmodel = viewmodel,
                        navController
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(15.dp))

                LogoutUi(globalMessageListenerViewModel)
            }


            if (expandEditImg) {
                item {
                    PopUpBox(
                        valueDescription = "Image URL",
                        profileValue = userData?.photoUrl.orEmpty(),
                        viewmodel = viewmodel
                    ) {
                        expandEditImg = it
                    }
                }

            }

        }

        if (loadingIndicator) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }


}

@Composable
fun ProfileImageSection(imageUrl: String, onEditClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = imageUrl),
                contentDescription = "profile picture",
                modifier = Modifier
                    .clip(CircleShape)
                    .size(150.dp)
                    .border(1.dp, Color.Gray, shape = CircleShape),
            )

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Edit picture",
                    tint = Color.Black,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(34.dp)
                        .background(Color.Green)
                        .clickable { onEditClick() }
                )
            }
        }
    }
}


@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        color = Color.Gray,
        modifier = Modifier.padding(vertical = 5.dp)
    )
}


@Composable
fun LogoutUi(
    globalMessageListenerViewModel: GlobalMessageListenerViewModel
) {

    var logOutPopBoxExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { logOutPopBoxExpanded = true },
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {

            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Log out",
                modifier = Modifier.size(20.dp),
                tint = Color.Red
            )

            Text(
                text = "Log out",
                fontSize = 18.sp,
                color = Color.Red
            )
        }
    }

    if (logOutPopBoxExpanded) {
        LogOutPopUpBox(globalMessageListenerViewModel) {
            logOutPopBoxExpanded = it
        }
    }


}

@Composable
fun ProfileComponent(
    primaryIcon: ImageVector,
    secondaryIcon: ImageVector,
    itemDescription: String,
    itemValue: String,
    viewmodel: ChatsViewModel,
    navController: NavHostController,
) {

    var expanded by rememberSaveable {
        mutableStateOf(false)
    }
    val clipboardManager = LocalClipboard.current

    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {

                when (itemDescription) {
                    "UserId" -> {
                        coroutineScope.launch {
                            val data = ClipData.newPlainText("id",
                                AnnotatedString(itemValue))
                            clipboardManager.setClipEntry(ClipEntry(data))

                        }
                    }

                    "Email" -> {
                        navController.navigate("changeEmail")
                    }

                    "Password" -> {
                        navController.navigate("changePassword")
                    }

                    else -> {
                        expanded = true
                    }
                }

            }
    ) {

        Icon(
            imageVector = primaryIcon,
            contentDescription = "Profile",
            modifier = Modifier
                .weight(1f)
                .size(20.dp),
            tint = Color.Gray,
        )

        Column(
            modifier = Modifier.weight(8f)
        ) {

            Text(
                text = itemDescription,
                fontSize = 16.sp,
                color = Color.Gray
            )

            if (itemValue.isNotEmpty()) {
                Text(
                    text = itemValue,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

        }

        Icon(
            imageVector = secondaryIcon,
            contentDescription = "Edit button icon",
            modifier = Modifier
                .weight(1f),
            tint = Color.Green
        )

    }

    if (expanded) {
        PopUpBox(itemDescription, itemValue, viewmodel)
        {
            expanded = it
        }
    }


}


fun onSaveOrCancel(
    valueDescription: String,
    profileValueNew: String,
    viewmodel: ChatsViewModel,
    context: Context,
) {


    when (valueDescription) {
        "Name" -> {

            if (profileValueNew.isNotEmpty()) {
                val updateNameData = mapOf(
                    "name" to profileValueNew
                )

                viewmodel.updateUserData(updateNameData,
                    onSuccess = {
                        viewmodel.updateLoadingIndicator(false)
                    },
                    onFailure = { exception ->
                        viewmodel.updateLoadingIndicator(false)
                        Toast.makeText(
                            context,
                            exception.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }

        "About" -> {
            if (profileValueNew.isNotEmpty()) {
                val updateAbout = mapOf(
                    "about" to profileValueNew
                )
                viewmodel.updateUserData(updateAbout,
                    onSuccess = {
                        viewmodel.updateLoadingIndicator(false)

                    },
                    onFailure = { exception ->
                        viewmodel.updateLoadingIndicator(false)
                        Toast.makeText(
                            context,
                            exception.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    })
            }
        }

        "Image URL" -> {

            if (profileValueNew.isNotEmpty()) {

                val updatePpfData = mapOf(
                    "photoUrl" to profileValueNew
                )

                viewmodel.updateUserData(updatePpfData,
                    onSuccess = {
                        viewmodel.updateLoadingIndicator(false)
                    },
                    onFailure = { exception ->
                        viewmodel.updateLoadingIndicator(false)
                        Toast.makeText(
                            context,
                            exception.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }

        }

        else -> {}
    }
}


fun getProfileItemList(userData: UserData?): List<ProfileItem> {

    return  listOf(
        ProfileItem(
            primaryIcon = Icons.Default.Person,
            secondaryIcon = Icons.Default.Edit,
            itemDescription = "Name",
            itemValue = userData?.name ?: ""
        ),
        ProfileItem(
            primaryIcon = Icons.Default.Info,
            secondaryIcon = Icons.Default.Edit,
            itemDescription = "About",
            itemValue = userData?.about ?: ""
        ),
        ProfileItem(
            primaryIcon = Icons.Default.Email,
            secondaryIcon = Icons.Default.ChevronRight,
            itemDescription = "Email",
            itemValue = userData?.email ?: ""
        ),
        ProfileItem(
            primaryIcon = Icons.Default.AssignmentInd,
            secondaryIcon = Icons.Default.ContentCopy,
            itemDescription = "UserId",
            itemValue = userData?.uid ?: ""
        )
    )
}

@Composable
fun isKeyboardVisible(): Boolean {
    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current

    return remember {
        derivedStateOf { imeInsets.getBottom(density) > 0 }
    }.value
}







