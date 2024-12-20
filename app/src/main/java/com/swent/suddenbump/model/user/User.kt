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
) {
  companion object {
    lateinit var lastKnownLocation: MutableStateFlow<Location>

    val UnknownUser by lazy {
      User(
          uid = "unknown",
          firstName = "Unknown",
          lastName = "User",
          phoneNumber = "+33 0 00 00 00 00",
          profilePicture = null,
          emailAddress = "mail@mail.com",
          lastKnownLocation = lastKnownLocation)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is User) return false
    return uid == other.uid && profilePicture == other.profilePicture
  }

  override fun hashCode(): Int {
    return uid.hashCode()
  }
}
