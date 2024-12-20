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
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is User) return false
    return uid == other.uid && profilePicture == other.profilePicture
  }

  override fun hashCode(): Int {
    return uid.hashCode()
  }
}

val UnknownUser =
    User(
        uid = "unknown",
        firstName = "Unknown",
        lastName = "User",
        phoneNumber = "+33 0 00 00 00 00",
        null,
        "mail@mail.com",
        MutableStateFlow(
            Location("provider").apply {
              latitude = 0.0
              longitude = 0.0
            }))
