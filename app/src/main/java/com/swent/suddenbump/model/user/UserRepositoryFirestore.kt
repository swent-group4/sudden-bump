package com.swent.suddenbump.model.user

import android.location.Location
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.swent.suddenbump.model.image.ImageRepository
import com.swent.suddenbump.model.image.ImageRepositoryFirebaseStorage
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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
    /*db.collection(usersCollectionPath)
    .document(FirebaseAuth.getInstance().currentUser!!.uid)
    .get()
    .addOnFailureListener { onFailure(it) }
    .addOnCompleteListener { resultUser ->
        if (resultUser.isSuccessful) {
            val path =
                helper.uidToProfilePicturePath(FirebaseAuth.getInstance().currentUser!!.uid, profilePicturesRef)
            imageRepository.downloadImage(
                path,
                onSuccess = {
                    onSuccess(helper.documentSnapshotToUser(resultUser.result, it))
                },
                onFailure = { onFailure(it) })
        } else {
            resultUser.exception?.let { onFailure(it) }
        }
    }*/
    db.collection(usersCollectionPath)
        .document(FirebaseAuth.getInstance().currentUser!!.uid)
        .addSnapshotListener { value, error ->
          if (error != null || value == null) {
            Log.e(logTag, error.toString())
            onFailure(Exception(error))
            return@addSnapshotListener
          }
          val user = value.toObject(User::class.java)
          if (user != null) onSuccess(user) else onFailure(Exception("User is null"))
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
            /*val path = helper.uidToProfilePicturePath(uid, profilePicturesRef)
            imageRepository.downloadImage(
                path,
                onSuccess = { image ->
                  onSuccess(helper.documentSnapshotToUser(resultUser.result, image))
                },
                onFailure = { onFailure(it) })*/
            resultUser.result.toObject(User::class.java)?.let(onSuccess)
          } else {
            resultUser.exception?.let { onFailure(it) }
          }
        }
  }

  override suspend fun getUserAccount(uid: String): User? {
    return suspendCancellableCoroutine { continuation ->
      db.collection(usersCollectionPath).document(uid).get().addOnCompleteListener {
        if (it.isSuccessful) {
          continuation.resume(it.result.toObject(User::class.java))
        } else continuation.resume(null)
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

  override suspend fun getUserFriends(user: User, onSuccess: (users: List<User>) -> Unit) {
    val liveUser =
        db.collection(usersCollectionPath)
            .document(user.uid)
            .get()
            .await()
            .toObject(User::class.java) ?: user
    onSuccess.invoke(liveUser.friendsList.mapNotNull { getUserAccount(it)?.copy(isFriend = true) })
  }

  override fun setUserFriends(
      user: User,
      friendsList: List<User>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(usersCollectionPath)
        .document(FirebaseAuth.getInstance().uid.toString())
        .update("friendsList", friendsList.map { it.uid }.distinct())
        .addOnFailureListener { onFailure(it) }
        .addOnSuccessListener { onSuccess() }
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

  override suspend fun getFriendsLocation(
      user: User,
      onSuccess: (Map<User, Location?>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // First, retrieve the user's friends using the existing getUserFriends method
    getUserFriends(
        user,
        { friendsList ->
          val friendsLocations = mutableMapOf<User, Location?>()

          // Loop through each friend in the friendsList and fetch their location
          friendsList.forEach { friend ->
            db.collection(usersCollectionPath)
                .document(friend.uid)
                .get()
                .addOnFailureListener { onFailure(it) }
                .addOnSuccessListener { friendSnapshot ->
                  val location = friendSnapshot.get("location") as? Location
                  friendsLocations[friend] = location

                  // Once all friends have been processed, call onSuccess
                  if (friendsLocations.size == friendsList.size) {
                    onSuccess(friendsLocations)
                  }
                }
          }

          // If no friends, return an empty map
          if (friendsList.isEmpty()) {
            onSuccess(emptyMap())
          }
        })
  }

  override suspend fun getAllUsers(): Result<List<User>> =
      withContext(Dispatchers.IO) {
        try {
          val querySnapshot = db.collection(usersCollectionPath).get().await()
          val userList =
              querySnapshot.mapNotNull { document ->
                val user = document.toObject(User::class.java)
                user.let {
                  val path = helper.uidToProfilePicturePath(it.uid, profilePicturesRef)
                  val profilePicture = imageRepository.downloadImage(path)
                  helper.documentSnapshotToUser(document, profilePicture)
                }
              }
          Result.success(userList)
        } catch (e: Exception) {
          Log.e(logTag, e.toString())
          Result.failure(e)
        }
      }

  override fun getAllOtherUsers(user: User): Flow<List<User>> = callbackFlow {
    val listener =
        db.collection(usersCollectionPath).addSnapshotListener { value, error ->
          if (error != null || value == null) {
            Log.e(logTag, error.toString())
            return@addSnapshotListener
          }
          trySend(
              value.documents
                  .mapNotNull { it.toObject(User::class.java) }
                  .filter { it.uid != user.uid })
        }
    awaitClose { listener.remove() }
  }

  private fun documentSnapshotToUserList(
      uidJsonList: String,
      onFailure: (Exception) -> Unit,
      isFriend: Boolean = false
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
            val user = helper.documentSnapshotToUser(documentSnapshot, profilePicture, isFriend)
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

  fun documentSnapshotToUser(
      document: DocumentSnapshot,
      profilePicture: ImageBitmap?,
      isFriend: Boolean = false
  ): User {
    return User(
        uid = document.data!!.get("uid").toString(),
        firstName = document.data!!.get("firstName").toString(),
        lastName = document.data!!.get("lastName").toString(),
        phoneNumber = document.data!!.get("phoneNumber").toString(),
        profilePicture = profilePicture,
        emailAddress = document.data!!.get("emailAddress").toString(),
        isFriend = isFriend)
  }

  fun documentSnapshotToList(uidJsonList: String): List<String> {
    val uidList = uidJsonList.trim('[', ']').split(", ").filter { it.isNotBlank() }
    return uidList
  }

  fun uidToProfilePicturePath(uid: String, reference: StorageReference): String {
    return reference.child("$uid.jpeg").toString()
  }
}
