package com.swent.suddenbump.model.user

import androidx.compose.ui.graphics.ImageBitmap
import com.swent.suddenbump.model.location.GeoLocation

data class User(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val profilePicture: ImageBitmap? = null,
    val emailAddress: String = "",
    val lastKnownLocation: GeoLocation = GeoLocation(0.0, 0.0),
) {
    // No-argument constructor for Firestore Deserialization
    constructor() : this("", "", "", "", null, "", GeoLocation(0.0, 0.0))
}