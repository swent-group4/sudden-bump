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

    fun createFriendRequest(
        user: User,
        friend: User,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun createFriend(user: User, friend: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    fun getUserAccount(onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit)

    fun getUserAccount(uid: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit)

    fun updateUserAccount(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    fun deleteUserAccount(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    fun getSentFriendRequests(
        user: User,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun setSentFriendRequests(
        user: User,
        friendRequestsList: List<User>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun getUserFriendRequests(
        user: User,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun setUserFriendRequests(
        user: User,
        friendRequestsList: List<User>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun getUserFriends(user: User, onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit)

    fun setUserFriends(
        user: User,
        friendsList: List<User>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun getRecommendedFriends(
        user: User,
        friendsList: List<User>,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun getBlockedFriends(user: User, onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit)

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

    // New functions to manage notification preferences
    fun setNotificationPreference(
        userId: String,
        isEnabled: Boolean,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun getNotificationPreference(
        userId: String,
        onSuccess: (Boolean) -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun updateUsername(userId: String, newUsername: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    // New functions to manage location sharing status
    suspend fun getLocationSharingStatus(): Boolean
    suspend fun setLocationSharingStatus(enabled: Boolean)
}