package com.swent.suddenbump.model.user

import androidx.compose.ui.graphics.vector.ImageVector

data class User(
    val uid: String,
    val firstName: String,
    val lastName: String,
    val profilePicture: ImageVector,
    val phoneNumber: String,
    val emailAddress: String,
)
