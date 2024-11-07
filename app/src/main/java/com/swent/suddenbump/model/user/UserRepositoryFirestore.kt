package com.swent.suddenbump.model.user

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.swent.suddenbump.model.image.ImageRepository
import com.swent.suddenbump.model.image.ImageRepositoryFirebaseStorage
import com.swent.suddenbump.model.location.GeoLocation
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class UserRepositoryFirestore(private val db: FirebaseFirestore) : UserRepository {

  private val logTag = "UserRepositoryFirestore"
  private val helper = UserRepositoryFirestoreHelper()

  private val usersCollectionPath = "Users"
  private val emailCollectionPath = "Emails"

  private val storage = Firebase.storage("gs://sudden-bump-swent.appspot.com")
  private val profilePicturesRef: StorageReference = storage.reference.child("profilePictures")

  override val imageRepository: ImageRepository = ImageRepositoryFirebaseStorage(storage)

  override fun init(onSuccess: () -> Unit) {
    imageRepository.init(onSuccess)
    onSuccess()
  }

  override fun getNewUid(): String {
    return db.collection(usersCollectionPath).document().id
  }

  override fun verifyNoAccountExists(
      emailAddress: String,
      onSuccess: (Boolean) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(emailCollectionPath)
        .get()
        .addOnFailureListener { onFailure(it) }
        .addOnCompleteListener { result ->
          if (result.isSuccessful) {
            val resultEmail = result.result.documents.map { it.id }
            onSuccess(!resultEmail.contains(emailAddress))
          } else {
            result.exception?.let { onFailure(it) }
          }
        }
  }

  override fun createUserAccount(
      user: User,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(usersCollectionPath)
        .document(user.uid)
        .set(helper.userToMapOf(user))
        .addOnFailureListener { exception ->
          Log.e(logTag, exception.toString())
          onFailure(exception)
        }
        .addOnCompleteListener { result ->
          if (result.isSuccessful) {
            db.collection(emailCollectionPath)
                .document(user.emailAddress)
                .set(mapOf("uid" to user.uid))
                .addOnFailureListener { onFailure(it) }
                .addOnSuccessListener {
                  user.profilePicture?.let { it1 ->
                    imageRepository.uploadImage(
                        it1,
                        helper.uidToProfilePicturePath(user.uid, profilePicturesRef),
                        onSuccess = { onSuccess() },
                        onFailure = { e -> onFailure(e) })
                  }
                }
          } else {
            result.exception?.let { onFailure(it) }
          }
        }
  }

  override fun getUserAccount(onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(emailCollectionPath)
        .document(FirebaseAuth.getInstance().currentUser!!.email.toString())
        .get()
        .addOnFailureListener { onFailure(it) }
        .addOnSuccessListener { resultEmail ->
          db.collection(usersCollectionPath)
              .document(resultEmail.data!!["uid"].toString())
              .get()
              .addOnFailureListener { onFailure(it) }
              .addOnCompleteListener { resultUser ->
                if (resultUser.isSuccessful) {
                  val path =
                      helper.uidToProfilePicturePath(
                          resultEmail.data!!["uid"].toString(), profilePicturesRef)
                  imageRepository.downloadImage(
                      path,
                      onSuccess = {
                        onSuccess(helper.documentSnapshotToUser(resultUser.result, it))
                      },
                      onFailure = { onFailure(it) })
                } else {
                  resultUser.exception?.let { onFailure(it) }
                }
              }
        }
  }

  override fun getUserAccount(
      uid: String,
      onSuccess: (User) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(usersCollectionPath)
        .document(uid)
        .get()
        .addOnFailureListener { onFailure(it) }
        .addOnCompleteListener { resultUser ->
          if (resultUser.isSuccessful) {
            val path = helper.uidToProfilePicturePath(uid, profilePicturesRef)
            imageRepository.downloadImage(
                path,
                onSuccess = { image ->
                  onSuccess(helper.documentSnapshotToUser(resultUser.result, image))
                },
                onFailure = { onFailure(it) })
          } else {
            resultUser.exception?.let { onFailure(it) }
          }
        }
  }

  override fun updateUserAccount(
      user: User,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(usersCollectionPath)
        .document(user.uid)
        .set(helper.userToMapOf(user))
        .addOnFailureListener { exception ->
          Log.e(logTag, exception.toString())
          onFailure(exception)
        }
        .addOnCompleteListener { result ->
          if (result.isSuccessful) {
            user.profilePicture?.let { it1 ->
              imageRepository.uploadImage(
                  it1,
                  helper.uidToProfilePicturePath(user.uid, profilePicturesRef),
                  onSuccess = { onSuccess() },
                  onFailure = { e -> onFailure(e) })
            }
          } else {
            result.exception?.let { onFailure(it) }
          }
        }
  }

  override fun deleteUserAccount(
      id: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(usersCollectionPath)
        .document(id)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e -> onFailure(e) }
  }

  override fun getUserFriendRequests(
      user: User,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(usersCollectionPath)
        .document(user.uid)
        .get()
        .addOnFailureListener { e -> onFailure(e) }
        .addOnSuccessListener { result ->
          val friendRequestsUidList =
              result.data?.get("friendRequests") as? List<String> ?: emptyList()
          if (friendRequestsUidList.isEmpty()) {
            onSuccess(emptyList())
            return@addOnSuccessListener
          }

          val tasks =
              friendRequestsUidList.map { uid ->
                db.collection(usersCollectionPath).document(uid).get()
              }

          Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
              .addOnSuccessListener { documents ->
                val friendRequestsList =
                    documents.mapNotNull { document ->
                      helper.documentSnapshotToUser(document, null)
                    }
                onSuccess(friendRequestsList)
              }
              .addOnFailureListener { e -> onFailure(e) }
        }
  }

  override fun getSentFriendRequests(
      user: User,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(usersCollectionPath)
        .document(user.uid)
        .get()
        .addOnFailureListener { e -> onFailure(e) }
        .addOnSuccessListener { result ->
          val sentFriendRequestsUidList =
              result.data?.get("sentFriendRequests") as? List<String> ?: emptyList()
          if (sentFriendRequestsUidList.isEmpty()) {
            onSuccess(emptyList())
            return@addOnSuccessListener
          }

          val tasks =
              sentFriendRequestsUidList.map { uid ->
                db.collection(usersCollectionPath).document(uid).get()
              }

          Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
              .addOnSuccessListener { documents ->
                val sentFriendRequestsList =
                    documents.mapNotNull { document ->
                      helper.documentSnapshotToUser(document, null)
                    }
                onSuccess(sentFriendRequestsList)
              }
              .addOnFailureListener { e -> onFailure(e) }
        }
  }

  override fun createFriend(
      user: User,
      friend: User,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // Update the user document to remove the friend from the friendRequests or sentFriendRequest
    // list and add them to the friends list
    db.collection(usersCollectionPath)
        .document(user.uid)
        .get()
        .addOnFailureListener { e -> onFailure(e) }
        .addOnSuccessListener { result ->
          val friendsUidList = result.data?.get("friendsList") as? List<String> ?: emptyList()
          val friendRequestsUidList =
              result.data?.get("friendRequests") as? List<String> ?: emptyList()
          val sentFriendRequestsUidList =
              result.data?.get("sentFriendRequests") as? List<String> ?: emptyList()

          val mutableFriendRequestsUidList = friendRequestsUidList.toMutableList()
          val mutableFriendsUidList = friendsUidList.toMutableList()

          if (friend.uid in mutableFriendRequestsUidList) {
            mutableFriendRequestsUidList.remove(friend.uid)
            mutableFriendsUidList.add(friend.uid)
            db.collection(usersCollectionPath)
                .document(user.uid)
                .update("friendRequests", mutableFriendRequestsUidList)
                .addOnFailureListener { e -> onFailure(e) }
                .addOnSuccessListener {
                  db.collection(usersCollectionPath)
                      .document(user.uid)
                      .update("friendsList", mutableFriendsUidList)
                      .addOnFailureListener { e -> onFailure(e) }
                      .addOnSuccessListener { onSuccess() }
                }
          } else if (friend.uid in sentFriendRequestsUidList) {
            mutableFriendRequestsUidList.remove(friend.uid)
            mutableFriendsUidList.add(friend.uid)
            db.collection(usersCollectionPath)
                .document(user.uid)
                .update("sentFriendRequests", mutableFriendRequestsUidList)
                .addOnFailureListener { e -> onFailure(e) }
                .addOnSuccessListener {
                  db.collection(usersCollectionPath)
                      .document(user.uid)
                      .update("friendsList", mutableFriendsUidList)
                      .addOnFailureListener { e -> onFailure(e) }
                      .addOnSuccessListener { onSuccess() }
                }
          } else {
            onFailure(Exception("Friend request not found"))
          }
        }

    // Update the friend document to add the user to the friends list
    db.collection(usersCollectionPath)
        .document(friend.uid)
        .get()
        .addOnFailureListener { e -> onFailure(e) }
        .addOnSuccessListener { result ->
          val friendsUidList = result.data?.get("friendsList") as? List<String> ?: emptyList()
          val friendsSentRequestList =
              result.data?.get("sentFriendRequests") as? List<String> ?: emptyList()
          val friendsRequestList =
              result.data?.get("friendRequests") as? List<String> ?: emptyList()

          val mutableFriendsUidList = friendsUidList.toMutableList()
          val mutableFriendsSentRequestList = friendsSentRequestList.toMutableList()
          val mutableFriendsRequestList = friendsRequestList.toMutableList()

          mutableFriendsUidList.add(user.uid)
          mutableFriendsRequestList.remove(user.uid)
          mutableFriendsSentRequestList.remove(user.uid)
          db.collection(usersCollectionPath)
              .document(friend.uid)
              .update("friendsList", mutableFriendsUidList)
              .addOnFailureListener { e -> onFailure(e) }
              .addOnSuccessListener {
                db.collection(usersCollectionPath)
                    .document(friend.uid)
                    .update("friendRequests", mutableFriendsRequestList)
                    .addOnFailureListener { e -> onFailure(e) }
                    .addOnSuccessListener {
                      db.collection(usersCollectionPath)
                          .document(friend.uid)
                          .update("sentFriendRequests", mutableFriendsSentRequestList)
                          .addOnFailureListener { e -> onFailure(e) }
                          .addOnSuccessListener {
                            db.collection(usersCollectionPath)
                                .document(friend.uid)
                                .update("friendsList", mutableFriendsUidList)
                                .addOnFailureListener { e -> onFailure(e) }
                                .addOnSuccessListener { onSuccess() }
                          }
                    }
              }
        }
  }

  override fun createFriendRequest(
      user: User,
      friend: User,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(usersCollectionPath)
        .document(friend.uid)
        .get()
        .addOnFailureListener { e -> onFailure(e) }
        .addOnSuccessListener { result ->
          val friendRequestsUidList =
              result.data?.get("friendRequests") as? List<String> ?: emptyList()

          db.collection(usersCollectionPath)
              .document(user.uid)
              .get()
              .addOnFailureListener { e -> onFailure(e) }
              .addOnSuccessListener { userResult ->
                val sentFriendRequestsUidList =
                    userResult.data?.get("sentFriendRequests") as? List<String> ?: emptyList()
                val mutableFriendRequestsUidList = friendRequestsUidList.toMutableList()
                val mutableSentFriendRequestsUidList = sentFriendRequestsUidList.toMutableList()

                if (user.uid !in mutableFriendRequestsUidList) {
                  mutableFriendRequestsUidList.add(user.uid)
                  mutableSentFriendRequestsUidList.add(friend.uid)
                  db.collection(usersCollectionPath)
                      .document(friend.uid)
                      .update("friendRequests", mutableFriendRequestsUidList)
                      .addOnFailureListener { e -> onFailure(e) }
                      .addOnSuccessListener {
                        db.collection(usersCollectionPath)
                            .document(user.uid)
                            .update("sentFriendRequests", mutableSentFriendRequestsUidList)
                            .addOnFailureListener { e -> onFailure(e) }
                            .addOnSuccessListener { onSuccess() }
                      }
                } else {
                  onFailure(Exception("Friend request already exists"))
                }
              }
        }
  }

  override fun setSentFriendRequests(
      user: User,
      friendRequestsList: List<User>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val friendRequestsUidList = friendRequestsList.map { it.uid }
    db.collection(usersCollectionPath)
        .document(user.uid)
        .update("sentFriendRequests", friendRequestsUidList)
        .addOnFailureListener { onFailure(it) }
        .addOnSuccessListener { onSuccess() }
  }

  override fun setUserFriendRequests(
      user: User,
      friendRequestsList: List<User>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val friendRequestsUidList = friendRequestsList.map { it.uid }
    db.collection(usersCollectionPath)
        .document(user.uid)
        .update("friendRequests", friendRequestsUidList)
        .addOnFailureListener { onFailure(it) }
        .addOnSuccessListener { onSuccess() }
  }

  override fun getUserFriends(
      user: User,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(usersCollectionPath)
        .document(user.uid)
        .get()
        .addOnFailureListener { e -> onFailure(e) }
        .addOnSuccessListener { result ->
          val friendsUidList = result.data?.get("friendsList") as? List<String> ?: emptyList()
          if (friendsUidList.isEmpty()) {
            onSuccess(emptyList())
            return@addOnSuccessListener
          }

          val tasks =
              friendsUidList.map { uid -> db.collection(usersCollectionPath).document(uid).get() }

          Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
              .addOnSuccessListener { documents ->
                val friendsList =
                    documents.mapNotNull { document ->
                      helper.documentSnapshotToUser(document, null)
                    }
                onSuccess(friendsList)
              }
              .addOnFailureListener { e -> onFailure(e) }
        }
  }

  override fun setUserFriends(
      user: User,
      friendsList: List<User>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val friendsUidList = friendsList.map { it.uid }
    db.collection(usersCollectionPath)
        .document(user.uid)
        .update("friendsList", friendsUidList)
        .addOnFailureListener { onFailure(it) }
        .addOnSuccessListener { onSuccess() }
  }

  override fun getRecommendedFriends(
      user: User,
      friendsList: List<User>,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // For the moment return all users that are not already friends with the current user
    db.collection(usersCollectionPath)
        .get()
        .addOnFailureListener { onFailure(it) }
        .addOnSuccessListener { result ->
          val allUsers = result.documents.mapNotNull { helper.documentSnapshotToUser(it, null) }
          val recommendedFriends = allUsers.filter { it !in friendsList }
          onSuccess(recommendedFriends)
        }
  }

  override fun getBlockedFriends(
      user: User,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(usersCollectionPath)
        .document(user.uid)
        .get()
        .addOnFailureListener { onFailure(it) }
        .addOnSuccessListener { result ->
          if (result.data?.get("blockedList") == null) {
            emptyList<User>()
          } else {
            onSuccess(
                documentSnapshotToUserList(result.data?.get("blockedList").toString(), onFailure))
          }
        }
  }

  override fun setBlockedFriends(
      user: User,
      blockedFriendsList: List<User>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(usersCollectionPath)
        .document(user.uid)
        .update("blockedFriendsList", blockedFriendsList)
        .addOnFailureListener { onFailure(it) }
        .addOnSuccessListener { onSuccess() }
  }

  override fun updateLocation(
      user: User,
      location: Location,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(usersCollectionPath)
        .document(user.uid)
        .update("location", location)
        .addOnFailureListener { onFailure(it) }
        .addOnSuccessListener { onSuccess() }
  }

  @SuppressLint("SuspiciousIndentation")
  override fun getFriendsLocation(
      userFriendsList: List<User>,
      onSuccess: (Map<User, Location?>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.d("FriendsMarkers", "Launched")

    val friendsLocations = mutableMapOf<User, Location?>()
    runBlocking {
      try {
        for (userFriend in userFriendsList) {
          val documentSnapshot =
              db.collection(usersCollectionPath).document(userFriend.uid).get().await()

          if (documentSnapshot.exists()) {
            val friendSnapshot = documentSnapshot.data
            val location = helper.locationParser(friendSnapshot!!.get("location").toString())
            friendsLocations[userFriend] = location
            Log.d("FriendsMarkers", "Succeeded Friends Locations ${userFriend}, ${location}")
          }
        }
      } catch (e: Exception) {
        Log.e(logTag, e.toString())
        onFailure(e)
      }
    }
    onSuccess(friendsLocations)
  }

  private fun documentSnapshotToUserList(
      uidJsonList: String,
      onFailure: (Exception) -> Unit
  ): List<User> {
    val uidList = helper.documentSnapshotToList(uidJsonList)
    val userList = emptyList<User>().toMutableList()

    for (uid in uidList) {
      runBlocking {
        try {
          val documentSnapshot = db.collection(usersCollectionPath).document(uid).get().await()

          if (documentSnapshot.exists()) {
            val path = helper.uidToProfilePicturePath(uid, profilePicturesRef)
            var profilePicture: ImageBitmap? = null
            imageRepository.downloadImage(
                path, onSuccess = { pp -> profilePicture = pp }, onFailure = { e -> onFailure(e) })
            val user = helper.documentSnapshotToUser(documentSnapshot, profilePicture)
            userList += user
          } else {}
        } catch (e: Exception) {
          Log.e(logTag, e.toString())
        }
      }
    }
    return userList
  }
}

internal class UserRepositoryFirestoreHelper() {
  fun userToMapOf(user: User): Map<String, String> {
    return mapOf(
        "uid" to user.uid,
        "firstName" to user.firstName,
        "lastName" to user.lastName,
        "phoneNumber" to user.phoneNumber,
        "emailAddress" to user.emailAddress)
  }

  fun locationParser(mapAttributes: String): Location {
    val locationMap =
        mapAttributes
            .removeSurrounding("{", "}")
            .split(", ")
            .map { it.split("=") }
            .associate { it[0].trim() to it.getOrNull(1)?.trim() }

    // Retrieve required attributes with default fallbacks
    val provider = locationMap["provider"] ?: LocationManager.GPS_PROVIDER
    val latitude = locationMap["latitude"]!!.toDouble()
    val longitude = locationMap["longitude"]!!.toDouble()

    // Create the Location object with the mandatory values
    return Location(provider).apply {
      this.latitude = latitude
      this.longitude = longitude

      // Set optional values if present
      locationMap["altitude"]?.toDoubleOrNull()?.let { this.altitude = it }
      locationMap["speed"]?.toFloatOrNull()?.let { this.speed = it }
      locationMap["accuracy"]?.toFloatOrNull()?.let { this.accuracy = it }
      locationMap["bearing"]?.toFloatOrNull()?.let { this.bearing = it }
      locationMap["time"]?.toLongOrNull()?.let { this.time = it }
      locationMap["bearingAccuracyDegrees"]?.toFloatOrNull()?.let {
        this.bearingAccuracyDegrees = it
      }
      locationMap["verticalAccuracyMeters"]?.toFloatOrNull()?.let {
        this.verticalAccuracyMeters = it
      }
      locationMap["speedAccuracyMetersPerSecond"]?.toFloatOrNull()?.let {
        this.speedAccuracyMetersPerSecond = it
      }
      locationMap["elapsedRealtimeMillis"]?.toLongOrNull()?.let {
        this.elapsedRealtimeNanos = it * 1_000_000
      }
    }
  }

  fun documentSnapshotToUser(document: DocumentSnapshot, profilePicture: ImageBitmap?): User {
    val geoPoint = document.getGeoPoint("lastKnownLocation")
    val location = geoPoint?.let { GeoLocation(it.latitude, it.longitude) } ?: GeoLocation(0.0, 0.0)
    return User(
        uid = document.data?.get("uid").toString(),
        firstName = document.data?.get("firstName").toString(),
        lastName = document.data?.get("lastName").toString(),
        phoneNumber = document.data?.get("phoneNumber").toString(),
        emailAddress = document.data?.get("emailAddress").toString(),
        profilePicture = profilePicture,
        lastKnownLocation = location)
  }

  fun documentSnapshotToList(uidJsonList: String): List<String> {
    val uidList = uidJsonList.trim('[', ']').split(", ").filter { it.isNotBlank() }
    return uidList
  }

  fun uidToProfilePicturePath(uid: String, reference: StorageReference): String {
    return reference.child("$uid.jpeg").toString()
  }
}
