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
      uid: String,
      fid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun createFriendHelper(
      friendRequestsUidList: MutableList<String>,
      friendsUidList: MutableList<String>,
      uid: String,
      fid: String,
      updateField: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun deleteFriendRequest(
      uid: String,
      fid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun createFriend(uid: String, fid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun getUserAccount(onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit)

  fun getUserAccount(uid: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit)

  fun updateUserAccount(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun deleteUserAccount(uid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun getSentFriendRequests(
      uid: String,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun setSentFriendRequests(
      uid: String,
      friendRequestsList: List<User>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getUserFriendRequests(
      uid: String,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun setUserFriendRequests(
      uid: String,
      friendRequestsList: List<User>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getUserFriends(uid: String, onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit)

  fun setUserFriends(
      uid: String,
      friendsList: List<User>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getRecommendedFriends(
      uid: String,
      onSuccess: (List<UserWithFriendsInCommon>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getBlockedFriends(
      uid: String,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun blockUser(
      currentUser: User,
      blockedUser: User,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun setBlockedFriends(
      uid: String,
      blockedFriendsList: List<User>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun updateUserLocation(
      uid: String,
      location: Location,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun updateTimestamp(
      uid: String,
      timestamp: Timestamp,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getUserStatus(uid: String, onSuccess: (Boolean) -> Unit, onFailure: (Exception) -> Unit)

  fun userFriendsInRadius(userLocation: Location, friends: List<User>, radius: Double): List<User>

  fun updateUserStatus(
      uid: String,
      status: Boolean,
      onSuccess: () -> Unit,
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

  fun saveLoginStatus(uid: String)

  fun getSavedUid(): String

  fun saveNotifiedFriends(friendsUID: List<String>)

  fun saveRadius(radius: Float)

  fun getSavedRadius(): Float

  fun saveNotificationStatus(status: Boolean)

  fun getSavedNotificationStatus(): Boolean

  fun getSavedAlreadyNotifiedFriends(): List<String>

  fun isUserLoggedIn(): Boolean

  fun logoutUser()

  fun shareLocationWithFriend(
      uid: String,
      fid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun stopSharingLocationWithFriend(
      uid: String,
      fid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getSharedWithFriends(
      uid: String,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getSharedByFriends(
      uid: String,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun unblockUser(
      currentUserId: String,
      blockedUserId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun startListeningForFriendRequests(
      uid: String,
      onFriendRequestsChanged: (List<User>) -> Unit,
      onError: (Exception) -> Unit
  )

  fun stopListeningForFriendRequests()

  /**
   * Starts listening for sent friend request changes in real-time.
   *
   * @param uid The user ID to listen for sent friend request changes
   * @param onSentRequestsChanged Callback when sent friend requests change
   * @param onError Callback when an error occurs
   */
  fun startListeningForSentRequests(
      uid: String,
      onSentRequestsChanged: (List<User>) -> Unit,
      onError: (Exception) -> Unit
  )

  /** Stops listening for sent friend request changes. */
  fun stopListeningForSentRequests()

  /**
   * Starts listening for blocked users changes in real-time.
   *
   * @param uid The user ID to listen for blocked users changes
   * @param onBlockedUsersChanged Callback when blocked users list changes
   * @param onError Callback when an error occurs
   */
  fun startListeningForBlockedUsers(
      uid: String,
      onBlockedUsersChanged: (List<User>) -> Unit,
      onError: (Exception) -> Unit
  )

  /** Stops listening for blocked users changes. */
  fun stopListeningForBlockedUsers()

  /**
   * Accepts a friend request from another user.
   *
   * @param uid The user ID accepting the friend request
   * @param fid The friend ID whose request is being accepted
   * @param onSuccess Called when the friend request is successfully accepted
   * @param onFailure Called with an exception if acceptance fails
   */
  fun acceptFriendRequest(
      uid: String,
      fid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Declines a friend request from another user.
   *
   * @param uid The user ID declining the friend request
   * @param fid The friend ID whose request is being declined
   * @param onSuccess Called when the friend request is successfully declined
   * @param onFailure Called with an exception if declining fails
   */
  fun declineFriendRequest(
      uid: String,
      fid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )
}

data class UserWithFriendsInCommon(val user: User, val friendsInCommon: Int)
