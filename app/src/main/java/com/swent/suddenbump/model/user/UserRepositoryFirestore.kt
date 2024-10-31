package com.swent.suddenbump.model.user

import android.location.Location
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.swent.suddenbump.model.image.ImageRepository
import com.swent.suddenbump.model.image.ImageRepositoryFirebaseStorage
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
                                    resultEmail.data!!["uid"].toString(), profilePicturesRef
                                )
                            imageRepository.downloadImage(
                                path,
                                onSuccess = {
                                    onSuccess(
                                        helper.documentSnapshotToUser(
                                            resultUser.result,
                                            it
                                        )
                                    )
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
                result.data?.let {
                    onSuccess(
                        documentSnapshotToUserList(
                            result.data?.get("friendsList").toString(),
                            onFailure
                        )
                    )
                } ?: run { onSuccess(emptyList()) }
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
                if (result.data?.get("blockedList") == null) {
                    emptyList<User>()
                } else {
                    onSuccess(
                        documentSnapshotToUserList(
                            result.data?.get("blockedList").toString(),
                            onFailure
                        )
                    )
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

    override fun getFriendsLocation(
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
            },
            onFailure
        )
    }

    override fun searchQueryAddContact(
        query: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(usersCollectionPath).get().addOnFailureListener { onFailure(it) }
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    val matchingUsers = emptyList<User>().toMutableList()
                    val usersDocument = result.result.documents

                    for (documentSnapshot in usersDocument) {
                        runBlocking {
                            try {
                                val userEmail =
                                    documentSnapshot.data!!.get("emailAddress").toString()
                                val userPhoneNumber =
                                    documentSnapshot.data!!.get("phoneNumber").toString()
                                val userFirstName =
                                    documentSnapshot.data!!.get("firstName").toString()
                                val userLastName =
                                    documentSnapshot.data!!.get("lastName").toString()
                                val userUID = documentSnapshot.id

                                if (userEmail.contains(query) || userPhoneNumber.contains(query) || userFirstName.contains(
                                        query
                                    ) || userLastName.contains(query)
                                ) {
                                    val path =
                                        helper.uidToProfilePicturePath(userUID, profilePicturesRef)
                                    imageRepository.downloadImage(
                                        path,
                                        onSuccess = {
                                            matchingUsers += helper.documentSnapshotToUser(
                                                documentSnapshot,
                                                it
                                            )
                                        },
                                        onFailure = { onFailure(it) })
                                }
                            } catch (e: Exception) {
                                Log.e(logTag, e.toString())
                                onFailure(e)
                            }
                        }
                    }
                    onSuccess(matchingUsers)
                } else {
                    result.exception?.let { onFailure(it) }
                }
            }
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
                    val documentSnapshot =
                        db.collection(usersCollectionPath).document(uid).get().await()

                    if (documentSnapshot.exists()) {
                        val path = helper.uidToProfilePicturePath(uid, profilePicturesRef)
                        var profilePicture: ImageBitmap? = null
                        imageRepository.downloadImage(
                            path,
                            onSuccess = { pp -> profilePicture = pp },
                            onFailure = { e -> onFailure(e) })
                        val user = helper.documentSnapshotToUser(documentSnapshot, profilePicture)
                        userList += user
                    } else {
                    }
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
            "emailAddress" to user.emailAddress
        )
    }

    fun documentSnapshotToUser(document: DocumentSnapshot, profilePicture: ImageBitmap?): User {
        return User(
            uid = document.data!!.get("uid").toString(),
            firstName = document.data!!.get("firstName").toString(),
            lastName = document.data!!.get("lastName").toString(),
            phoneNumber = document.data!!.get("phoneNumber").toString(),
            profilePicture = profilePicture,
            emailAddress = document.data!!.get("emailAddress").toString()
        )
    }

    fun documentSnapshotToList(uidJsonList: String): List<String> {
        val uidList = uidJsonList.trim('[', ']').split(", ").filter { it.isNotBlank() }
        return uidList
    }

    fun uidToProfilePicturePath(uid: String, reference: StorageReference): String {
        return reference.child("$uid.jpeg").toString()
    }
}
