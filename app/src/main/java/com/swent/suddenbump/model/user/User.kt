package com.swent.suddenbump.model.user

import androidx.compose.ui.graphics.ImageBitmap

data class User(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val profilePicture: ImageBitmap? = null,
    val emailAddress: String = "",
    val profilePictureUrl: String = "",
    val birthDate: String = "",
    val relativeDist: Int = 0,
    val isFriend: Boolean = false,
)
