package com.swent.suddenbump.model.user

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
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

    override fun getUserFriends(
        user: User,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Log.i("FriendsMarkers", "Called")
        db.collection(usersCollectionPath)
            .document(user.uid)
            .get()
            .addOnFailureListener { e ->
                onFailure(e)
                Log.d("FriendsMarkers", "Failure")
            }
            .addOnSuccessListener { result ->
                result.data?.let {
                    onSuccess(
                        documentSnapshotToUserList(
                            result.data?.get("friendsList").toString(),
                            onFailure
                        )
                    )
                    Log.d(
                        "FriendsMarkers",
                        "On success Friends Locations ${result.data?.get("friendsList").toString()}"
                    )
                }
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
                    val documentSnapshot = db.collection(usersCollectionPath)
                        .document(userFriend.uid)
                        .get().await()

                    if (documentSnapshot.exists()) {
                        val friendSnapshot = documentSnapshot.data
                        val location =
                            helper.locationParser(friendSnapshot!!.get("location").toString())
                        friendsLocations[userFriend] = location
                        Log.d(
                            "FriendsMarkers",
                            "Succeeded Friends Locations ${userFriend}, ${location}"
                        )
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

        Log.i("FriendsMarkers", "Inside")

        for (uid in uidList) {
            runBlocking {
                try {
                    Log.i("FriendsMarkers", "Running")
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
        Log.i("FriendsMarkers", "Done!!")
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

    fun locationParser(mapAttributes: String): Location {
        val locationMap = mapAttributes.removeSurrounding("{", "}")
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
            locationMap["bearingAccuracyDegrees"]?.toFloatOrNull()
                ?.let { this.bearingAccuracyDegrees = it }
            locationMap["verticalAccuracyMeters"]?.toFloatOrNull()
                ?.let { this.verticalAccuracyMeters = it }
            locationMap["speedAccuracyMetersPerSecond"]?.toFloatOrNull()
                ?.let { this.speedAccuracyMetersPerSecond = it }
            locationMap["elapsedRealtimeMillis"]?.toLongOrNull()
                ?.let { this.elapsedRealtimeNanos = it * 1_000_000 }
        }
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
