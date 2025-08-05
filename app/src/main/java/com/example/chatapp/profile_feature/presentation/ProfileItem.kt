package com.example.chatapp.profile_feature.presentation

import androidx.compose.ui.graphics.vector.ImageVector

data class ProfileItem(
    val primaryIcon: ImageVector,
    val secondaryIcon: ImageVector,
    val itemDescription: String,
    val itemValue: String
)
