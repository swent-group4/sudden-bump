package com.swent.suddenbump.model.user

import android.location.Location
import com.google.firebase.Timestamp
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

  fun verifyUnusedPhoneNumber(
      phoneNumber: String,
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

  fun deleteFriendRequest(
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

  fun updateTimestamp(
      user: User,
      timestamp: Timestamp,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getFriendsLocation(
      userFriendsList: List<User>,
      onSuccess: (Map<User, Location?>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun sendVerificationCode(
      phoneNumber: String,
      onSuccess: (String) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun verifyCode(
      verificationId: String,
      code: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun saveLoginStatus(userId: String)

  fun getSavedUid(): String

  fun isUserLoggedIn(): Boolean

  fun logoutUser()
}
