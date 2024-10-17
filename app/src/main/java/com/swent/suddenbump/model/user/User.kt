package com.swent.suddenbump.model.user

import androidx.compose.ui.graphics.ImageBitmap

data class User(
    val uid: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val profilePicture: ImageBitmap?,
    val emailAddress: String,
)
