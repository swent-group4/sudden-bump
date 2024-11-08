package com.swent.suddenbump.model.user

import android.location.Location
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

    override fun setNotificationPreference(
        userId: String,
        isEnabled: Boolean,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userRef = db.collection(usersCollectionPath).document(userId)

        userRef.update("notificationsEnabled", isEnabled)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    override fun getNotificationPreference(
        userId: String,
        onSuccess: (Boolean) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userRef = db.collection(usersCollectionPath).document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                val isEnabled = document.getBoolean("notificationsEnabled") ?: false
                onSuccess(isEnabled)
            }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    override fun updateUsername(userId: String, newUsername: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userRef = db.collection(usersCollectionPath).document(userId)
        userRef.update("username", newUsername)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
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
                    val documents = result.result?.documents ?: emptyList()
                    val emailExists = documents.any { it.id == emailAddress }
                    onSuccess(!emailExists)
                } else {
                    onFailure(result.exception ?: Exception("Unknown error"))
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
            .addOnFailureListener { exception -> onFailure(exception) }
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(result.exception ?: Exception("Unknown error"))
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
                    .document(resultEmail.id)
                    .get()
                    .addOnFailureListener { onFailure(it) }
                    .addOnSuccessListener { resultUser ->
                        val user = helper.documentSnapshotToUser(resultUser, null)
                        onSuccess(user)
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
            .addOnSuccessListener { resultUser ->
                val user = helper.documentSnapshotToUser(resultUser, null)
                onSuccess(user)
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
            .addOnFailureListener { exception -> onFailure(exception) }
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(result.exception ?: Exception("Unknown error"))
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
                val friendRequestsUidList = result.data?.get("friendRequests") as? List<String> ?: emptyList()
                val tasks = friendRequestsUidList.map { db.collection(usersCollectionPath).document(it).get() }
                Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                    .addOnSuccessListener { documents ->
                        val friendRequests = documents.mapNotNull { helper.documentSnapshotToUser(it, null) }
                        onSuccess(friendRequests)
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
                val sentFriendRequestsUidList = result.data?.get("sentFriendRequests") as? List<String> ?: emptyList()
                val tasks = sentFriendRequestsUidList.map { db.collection(usersCollectionPath).document(it).get() }
                Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                    .addOnSuccessListener { documents ->
                        val sentFriendRequests = documents.mapNotNull { helper.documentSnapshotToUser(it, null) }
                        onSuccess(sentFriendRequests)
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
        db.collection(usersCollectionPath)
            .document(user.uid)
            .get()
            .addOnFailureListener { e -> onFailure(e) }
            .addOnSuccessListener { result ->
                val friendsUidList = result.data?.get("friendsList") as? List<String> ?: emptyList()
                val mutableFriendsUidList = friendsUidList.toMutableList()
                mutableFriendsUidList.add(friend.uid)
                db.collection(usersCollectionPath)
                    .document(user.uid)
                    .update("friendsList", mutableFriendsUidList)
                    .addOnFailureListener { e -> onFailure(e) }
                    .addOnSuccessListener {
                        db.collection(usersCollectionPath)
                            .document(friend.uid)
                            .get()
                            .addOnFailureListener { e -> onFailure(e) }
                            .addOnSuccessListener { result ->
                                val friendsUidList = result.data?.get("friendsList") as? List<String> ?: emptyList()
                                val mutableFriendsUidList = friendsUidList.toMutableList()
                                mutableFriendsUidList.add(user.uid)
                                db.collection(usersCollectionPath)
                                    .document(friend.uid)
                                    .update("friendsList", mutableFriendsUidList)
                                    .addOnFailureListener { e -> onFailure(e) }
                                    .addOnSuccessListener { onSuccess() }
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
                val friendRequestsUidList = result.data?.get("friendRequests") as? List<String> ?: emptyList()
                val mutableFriendRequestsUidList = friendRequestsUidList.toMutableList()
                mutableFriendRequestsUidList.add(user.uid)
                db.collection(usersCollectionPath)
                    .document(friend.uid)
                    .update("friendRequests", mutableFriendRequestsUidList)
                    .addOnFailureListener { e -> onFailure(e) }
                    .addOnSuccessListener { onSuccess() }
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
                } else {
                    val tasks = friendsUidList.map { db.collection(usersCollectionPath).document(it).get() }
                    Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                        .addOnSuccessListener { documents ->
                            val friends = documents.mapNotNull { helper.documentSnapshotToUser(it, null) }
                            onSuccess(friends)
                        }
                        .addOnFailureListener { e -> onFailure(e) }
                }
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
                val blockedFriendsUidList = result.data?.get("blockedFriendsList") as? List<String> ?: emptyList()
                if (blockedFriendsUidList.isEmpty()) {
                    onSuccess(emptyList())
                } else {
                    val tasks = blockedFriendsUidList.map { db.collection(usersCollectionPath).document(it).get() }
                    Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                        .addOnSuccessListener { documents ->
                            val blockedFriends = documents.mapNotNull { helper.documentSnapshotToUser(it, null) }
                            onSuccess(blockedFriends)
                        }
                        .addOnFailureListener { e -> onFailure(e) }
                }
            }
    }

    override fun setBlockedFriends(
        user: User,
        blockedFriendsList: List<User>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val blockedFriendsUidList = blockedFriendsList.map { it.uid }
        db.collection(usersCollectionPath)
            .document(user.uid)
            .update("blockedFriendsList", blockedFriendsUidList)
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
        getUserFriends(
            user,
            { friendsList ->
                val friendsLocations = mutableMapOf<User, Location?>()
                val tasks = friendsList.map { friend ->
                    db.collection(usersCollectionPath)
                        .document(friend.uid)
                        .get()
                        .addOnSuccessListener { document ->
                            val location = document.getGeoPoint("location")?.let {
                                Location("").apply {
                                    latitude = it.latitude
                                    longitude = it.longitude
                                }
                            }
                            friendsLocations[friend] = location
                        }
                }
                Tasks.whenAllSuccess<Void>(tasks)
                    .addOnSuccessListener { onSuccess(friendsLocations) }
                    .addOnFailureListener { onFailure(it) }
            },
            onFailure
        )
    }

    override suspend fun getLocationSharingStatus(): Boolean {
        val document = db.collection(usersCollectionPath).document("userId").get().await()
        return document.getBoolean("locationSharingEnabled") ?: false
    }

    override suspend fun setLocationSharingStatus(enabled: Boolean) {
        db.collection(usersCollectionPath).document("userId").update("locationSharingEnabled", enabled).await()
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
                    val document = db.collection(usersCollectionPath).document(uid).get().await()
                    val user = helper.documentSnapshotToUser(document, null)
                    userList.add(user)
                } catch (e: Exception) {
                    onFailure(e)
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
        val geoPoint = document.getGeoPoint("lastKnownLocation")
        val location = geoPoint?.let { GeoLocation(it.latitude, it.longitude) } ?: GeoLocation(0.0, 0.0)
        return User(
            uid = document.data?.get("uid").toString(),
            firstName = document.data?.get("firstName").toString(),
            lastName = document.data?.get("lastName").toString(),
            phoneNumber = document.data?.get("phoneNumber").toString(),
            emailAddress = document.data?.get("emailAddress").toString(),
            profilePicture = profilePicture,
            lastKnownLocation = location
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