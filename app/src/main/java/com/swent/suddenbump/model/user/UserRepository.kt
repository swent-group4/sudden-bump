package com.swent.suddenbump.model.user

import android.location.Location
import com.swent.suddenbump.model.image.ImageRepository

interface UserRepository {

    val imageRepository: ImageRepository

    fun init(onSuccess: () -> Unit)

    fun getNewUid(): String

    fun verifyNoAccountExists(
        emailAddress: String,
        onSuccess: (Boolean) -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun createUserAccount(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    fun getUserAccount(onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit)

    fun getUserAccount(uid: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit)

    fun updateUserAccount(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    fun deleteUserAccount(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    fun getUserFriends(user: User, onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit)

    fun setUserFriends(
        user: User,
        friendsList: List<User>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun getBlockedFriends(
        user: User,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun setBlockedFriends(
        user: User,
        blockedFriendsList: List<User>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun updateLocation(
        user: User,
        location: Location,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun getFriendsLocation(
        user: User,
        onSuccess: (Map<User, Location?>) -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun searchQueryAddContact(
        query: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    )
}
