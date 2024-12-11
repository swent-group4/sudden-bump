package com.swent.suddenbump.model.user

import android.location.Location
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.flow.MutableStateFlow

data class User(
    val uid: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val profilePicture: ImageBitmap?,
    val emailAddress: String,
    val lastKnownLocation: MutableStateFlow<Location>,
    val isOnline: Boolean = false,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is User) return false
    return uid == other.uid && profilePicture == other.profilePicture
  }

  override fun hashCode(): Int {
    return uid.hashCode()
  }
}
