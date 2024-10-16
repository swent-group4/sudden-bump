package com.swent.suddenbump.model.user

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBox
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.suddenbump.model.location.Location

class UserRepositoryFirestore(private val db: FirebaseFirestore) : UserRepository {

  private val logTag = "UserRepositoryFirestore"

  private val usersCollectionPath = "Users"
  private val emailCollectionPath = "Emails"

  override fun init(onSuccess: () -> Unit) {
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
        .set(user)
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
                .addOnSuccessListener { onSuccess() }
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
                  onSuccess(documentSnapshotToUser(resultUser.result))
                } else {
                  resultUser.exception?.let { onFailure(it) }
                }
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
        .set(user)
        .addOnFailureListener { exception ->
          Log.e(logTag, exception.toString())
          onFailure(exception)
        }
        .addOnCompleteListener { result ->
          if (result.isSuccessful) {
            onSuccess()
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
          onSuccess(
              documentSnapshotToUserList(result.data?.get("friendsList").toString(), onFailure))
        }
  }

  override fun setUserFriends(
      user: User,
      friendsList: List<User>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(usersCollectionPath)
        .document(user.uid)
        .update("friendsList", friendsList.map { it.uid })
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
          onSuccess(
              documentSnapshotToUserList(result.data?.get("blockedList").toString(), onFailure))
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

  private fun documentSnapshotToUserList(
      uidJsonList: String,
      onFailure: (Exception) -> Unit
  ): List<User> {
    val uidList = documentSnapshotToList(uidJsonList)
    val userList = emptyList<User>().toMutableList()

    uidList.forEach {
      db.collection(usersCollectionPath)
          .document(it)
          .get()
          .addOnFailureListener { onFailure(it) }
          .addOnCompleteListener { result ->
            if (result.isSuccessful) {
              userList += documentSnapshotToUser(result.result)
            } else {
              onFailure(result.exception!!)
            }
          }
    }

    return userList
  }

  private fun documentSnapshotToUser(document: DocumentSnapshot): User {
    return User(
        uid = document.data!!.get("uid").toString(),
        firstName = document.data!!.get("firstName").toString(),
        lastName = document.data!!.get("lastName").toString(),
        phoneNumber = document.data!!.get("phoneNumber").toString(),
        profilePicture = null,
        emailAddress = document.data!!.get("emailAddress").toString())
  }

  private fun documentSnapshotToList(uidJsonList: String): List<String> {
    val uidList = uidJsonList.trim('[', ']').split(", ").filter { it.isNotBlank() }
    return uidList
  }
}
