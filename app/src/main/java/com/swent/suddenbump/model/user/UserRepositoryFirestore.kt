package com.swent.suddenbump.model.user

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.swent.suddenbump.MainActivity
import com.swent.suddenbump.model.image.ImageRepository
import com.swent.suddenbump.model.image.ImageRepositoryFirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

/**
 * A Firebase Firestore-backed implementation of the UserRepository interface, managing user
 * accounts, friend relationships, and profile pictures, as well as sending and verifying phone
 * verification codes.
 *
 * @property db The Firestore database instance used to perform CRUD operations.
 * @property context Application context, used for accessing shared preferences.
 */
class UserRepositoryFirestore(private val db: FirebaseFirestore, private val context: Context) :
    UserRepository {

    private val logTag = "UserRepositoryFirestore"
    private val helper = UserRepositoryFirestoreHelper()

    private val usersCollectionPath = "Users"
    private val emailCollectionPath = "Emails"
    private val phoneCollectionPath = "Phones"

    private val storage = Firebase.storage("gs://sudden-bump-swent.appspot.com")
    private val profilePicturesRef: StorageReference = storage.reference.child("profilePictures")

    override val imageRepository: ImageRepository = ImageRepositoryFirebaseStorage(storage)

    private lateinit var verificationId: String

    private val sharedPreferences =
        context.getSharedPreferences("SuddenBumpLocalDB", Context.MODE_PRIVATE)

    /**
     * Initializes the repository by setting up the image repository.
     *
     * @param onSuccess Called when initialization is successful.
     */
    override fun init(onSuccess: () -> Unit) {
        imageRepository.init(onSuccess)
        onSuccess()
    }

    /**
     * Generates a new unique user ID.
     *
     * @return The generated user ID as a String.
     */
    override fun getNewUid(): String {
        return db.collection(usersCollectionPath).document().id
    }

    /**
     * Verifies if no account exists with the given email address.
     *
     * @param emailAddress The email address to verify.
     * @param onSuccess Called with `true` if no account exists, `false` otherwise.
     * @param onFailure Called with an exception if the check fails.
     */
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

    /**
     * Verifies if the given phone number is not associated with any account.
     *
     * @param phoneNumber The phone number to verify.
     * @param onSuccess Called with `true` if the phone number is not associated with any account,
     *   `false` otherwise.
     * @param onFailure Called with an exception if the check fails.
     */
    override fun verifyUnusedPhoneNumber(
        phoneNumber: String,
        onSuccess: (Boolean) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(phoneCollectionPath)
            .get()
            .addOnFailureListener { onFailure(it) }
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    val resultPhone = result.result.documents.map { it.id }
                    onSuccess(!resultPhone.contains(phoneNumber))
                } else {
                    result.exception?.let { onFailure(it) }
                }
            }
    }

    /**
     * Creates a new user account with the given User object and uploads profile picture if available.
     *
     * @param user The User object containing user information.
     * @param onSuccess Called when the account is successfully created.
     * @param onFailure Called with an exception if account creation fails.
     */
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
                    db.collection(phoneCollectionPath)
                        .document(user.phoneNumber)
                        .set(mapOf("uid" to user.uid))
                        .addOnFailureListener { onFailure(it) }
                } else {
                    result.exception?.let { onFailure(it) }
                }
            }
    }

    /**
     * Retrieves the current authenticated user's account details.
     *
     * @param onSuccess Called with the User object if retrieval succeeds.
     * @param onFailure Called with an exception if retrieval fails.
     */
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

    /**
     * Retrieves another user's account details by user ID.
     *
     * @param uid The user ID of the account to retrieve.
     * @param onSuccess Called with the User object if retrieval succeeds.
     * @param onFailure Called with an exception if retrieval fails.
     */
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

    /**
     * Updates the account details of the given User object in the database.
     *
     * @param user The User object containing updated information.
     * @param onSuccess Called when the account update is successful.
     * @param onFailure Called with an exception if the update fails.
     */
    override fun updateUserAccount(
        user: User,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(usersCollectionPath)
            .document(user.uid)
            .update(helper.userToMapOf(user))
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

    /**
     * Deletes a user account by user ID.
     *
     * @param uid The user ID of the account to delete.
     * @param onSuccess Called when the account deletion is successful.
     * @param onFailure Called with an exception if deletion fails.
     */
    override fun deleteUserAccount(
        uid: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(usersCollectionPath)
            .document(uid)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    /**
     * Retrieves friend requests received by the specified user.
     *
     * @param uid The user id whose friend requests are being retrieved.
     * @param onSuccess Called with a list of Users who sent friend requests if retrieval succeeds.
     * @param onFailure Called with an exception if retrieval fails.
     */
    override fun getUserFriendRequests(
        uid: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(usersCollectionPath)
            .document(uid)
            .get()
            .addOnFailureListener { e -> onFailure(e) }
            .addOnSuccessListener { result ->
                val friendRequestsUidList =
                    result.data?.get("friendRequests") as? List<String> ?: emptyList()
                if (friendRequestsUidList.isEmpty()) {
                    onSuccess(emptyList())
                } else {
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
    }

    /**
     * Retrieves friend requests sent by the specified user.
     *
     * @param uid The user id who sent the friend requests.
     * @param onSuccess Called with a list of Users who received friend requests from the user.
     * @param onFailure Called with an exception if retrieval fails.
     */
    override fun getSentFriendRequests(
        uid: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(usersCollectionPath)
            .document(uid)
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

    /**
     * Adds a friend to the specified user's friend list, removing any pending friend requests between
     * them.
     *
     * @param uid The user id who is adding a friend.
     * @param fid The friend id being added.
     * @param onSuccess Called when the friend addition is successful.
     * @param onFailure Called with an exception if the operation fails.
     */
    override fun createFriend(
        uid: String,
        fid: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Update the user document to remove the friend from the friendRequests or sentFriendRequest
        // list and add them to the friends list
        db.collection(usersCollectionPath)
            .document(uid)
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

                when (fid) {
                    in mutableFriendRequestsUidList -> {
                        createFriendHelper(
                            mutableFriendRequestsUidList,
                            mutableFriendsUidList,
                            uid,
                            fid,
                            "friendRequests",
                            onSuccess,
                            onFailure
                        )
                    }

                    in sentFriendRequestsUidList -> {
                        createFriendHelper(
                            mutableFriendRequestsUidList,
                            mutableFriendsUidList,
                            uid,
                            fid,
                            "sentFriendRequests",
                            onSuccess,
                            onFailure
                        )
                    }

                    else -> {
                        onFailure(Exception("Friend request not found"))
                    }
                }
            }

        // Update the friend document to add the user to the friends list
        db.collection(usersCollectionPath)
            .document(fid)
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

                mutableFriendsUidList.add(uid)
                mutableFriendsRequestList.remove(uid)
                mutableFriendsSentRequestList.remove(uid)
                db.collection(usersCollectionPath)
                    .document(fid)
                    .update("friendsList", mutableFriendsUidList)
                    .addOnFailureListener { e -> onFailure(e) }
                    .addOnSuccessListener {
                        db.collection(usersCollectionPath)
                            .document(fid)
                            .update("friendRequests", mutableFriendsRequestList)
                            .addOnFailureListener { e -> onFailure(e) }
                            .addOnSuccessListener {
                                db.collection(usersCollectionPath)
                                    .document(fid)
                                    .update("sentFriendRequests", mutableFriendsSentRequestList)
                                    .addOnFailureListener { e -> onFailure(e) }
                                    .addOnSuccessListener {
                                        db.collection(usersCollectionPath)
                                            .document(fid)
                                            .update("friendsList", mutableFriendsUidList)
                                            .addOnFailureListener { e -> onFailure(e) }
                                            .addOnSuccessListener { onSuccess() }
                                    }
                            }
                    }
            }
    }

    override fun createFriendHelper(
        friendRequestsUidList: MutableList<String>,
        friendsUidList: MutableList<String>,
        uid: String,
        fid: String,
        updateField: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        friendRequestsUidList.remove(fid)
        friendsUidList.add(fid)
        db.collection(usersCollectionPath)
            .document(uid)
            .update(updateField, friendRequestsUidList)
            .addOnFailureListener { e -> onFailure(e) }
            .addOnSuccessListener {
                db.collection(usersCollectionPath)
                    .document(uid)
                    .update("friendsList", friendsUidList)
                    .addOnFailureListener { e -> onFailure(e) }
                    .addOnSuccessListener { onSuccess() }
            }
    }

    /**
     * Sends a friend request from the specified user to the target friend.
     *
     * @param uid The user id sending the friend request.
     * @param fid The target friend id who will receive the request.
     * @param onSuccess Called when the friend request is successfully sent.
     * @param onFailure Called with an exception if the request fails.
     */
    override fun createFriendRequest(
        uid: String,
        fid: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(usersCollectionPath)
            .document(fid)
            .get()
            .addOnFailureListener { e -> onFailure(e) }
            .addOnSuccessListener { result ->
                val friendRequestsUidList =
                    result.data?.get("friendRequests") as? List<String> ?: emptyList()

                db.collection(usersCollectionPath)
                    .document(uid)
                    .get()
                    .addOnFailureListener { e -> onFailure(e) }
                    .addOnSuccessListener { userResult ->
                        val sentFriendRequestsUidList =
                            userResult.data?.get("sentFriendRequests") as? List<String>
                                ?: emptyList()
                        val mutableFriendRequestsUidList = friendRequestsUidList.toMutableList()
                        val mutableSentFriendRequestsUidList =
                            sentFriendRequestsUidList.toMutableList()

                        if (uid !in mutableFriendRequestsUidList) {
                            mutableFriendRequestsUidList.add(uid)
                            mutableSentFriendRequestsUidList.add(fid)
                            db.collection(usersCollectionPath)
                                .document(fid)
                                .update("friendRequests", mutableFriendRequestsUidList)
                                .addOnFailureListener { e -> onFailure(e) }
                                .addOnSuccessListener {
                                    db.collection(usersCollectionPath)
                                        .document(uid)
                                        .update(
                                            "sentFriendRequests",
                                            mutableSentFriendRequestsUidList
                                        )
                                        .addOnFailureListener { e -> onFailure(e) }
                                        .addOnSuccessListener { onSuccess() }
                                }
                        } else {
                            onFailure(Exception("Friend request already exists"))
                        }
                    }
            }
    }

    /**
     * Deletes a friend request from the specified user to the target friend.
     *
     * @param uid The user id deleting the friend request.
     * @param fid The friend id whose request is being deleted.
     * @param onSuccess Called when the friend request is successfully deleted.
     * @param onFailure Called with an exception if deletion fails.
     */
    override fun deleteFriendRequest(
        uid: String,
        fid: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Remove the friendId from the user's friendRequests list
        db.collection(usersCollectionPath)
            .document(uid)
            .get()
            .addOnFailureListener { e -> onFailure(e) }
            .addOnSuccessListener { result ->
                val friendRequestsUidList =
                    result.data?.get("friendRequests") as? List<String> ?: emptyList()
                val mutableFriendRequestsUidList = friendRequestsUidList.toMutableList()

                if (fid in mutableFriendRequestsUidList) {
                    mutableFriendRequestsUidList.remove(fid)
                    db.collection(usersCollectionPath)
                        .document(uid)
                        .update("friendRequests", mutableFriendRequestsUidList)
                        .addOnFailureListener { e -> onFailure(e) }
                        .addOnSuccessListener {
                            // Remove the userId from the friend's sentFriendRequests list
                            db.collection(usersCollectionPath)
                                .document(fid)
                                .get()
                                .addOnFailureListener { e -> onFailure(e) }
                                .addOnSuccessListener { friendResult ->
                                    val sentFriendRequestsUidList =
                                        friendResult.data?.get("sentFriendRequests") as? List<String>
                                            ?: emptyList()
                                    val mutableSentFriendRequestsUidList =
                                        sentFriendRequestsUidList.toMutableList()

                                    if (uid in mutableSentFriendRequestsUidList) {
                                        mutableSentFriendRequestsUidList.remove(uid)
                                        db.collection(usersCollectionPath)
                                            .document(fid)
                                            .update(
                                                "sentFriendRequests",
                                                mutableSentFriendRequestsUidList
                                            )
                                            .addOnFailureListener { e -> onFailure(e) }
                                            .addOnSuccessListener { onSuccess() }
                                    } else {
                                        onFailure(
                                            Exception("User ID not found in friend's sentFriendRequests list")
                                        )
                                    }
                                }
                        }
                } else {
                    onFailure(Exception("Friend ID not found in user's friendRequests list"))
                }
            }
    }

    /**
     * Sets the list of sent friend requests for the specified user.
     *
     * @param uid The user id whose sent friend requests are being set.
     * @param friendRequestsList A list of User objects representing the sent friend requests.
     * @param onSuccess Called when the list is successfully updated.
     * @param onFailure Called with an exception if the update fails.
     */
    override fun setSentFriendRequests(
        uid: String,
        friendRequestsList: List<User>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val friendRequestsUidList = friendRequestsList.map { it.uid }
        db.collection(usersCollectionPath)
            .document(uid)
            .update("sentFriendRequests", friendRequestsUidList)
            .addOnFailureListener { onFailure(it) }
            .addOnSuccessListener { onSuccess() }
    }

    /**
     * Sets the list of received friend requests for the specified user.
     *
     * @param uid The user id whose received friend requests are being set.
     * @param friendRequestsList A list of User objects representing the received friend requests.
     * @param onSuccess Called when the list is successfully updated.
     * @param onFailure Called with an exception if the update fails.
     */
    override fun setUserFriendRequests(
        uid: String,
        friendRequestsList: List<User>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val friendRequestsUidList = friendRequestsList.map { it.uid }
        db.collection(usersCollectionPath)
            .document(uid)
            .update("friendRequests", friendRequestsUidList)
            .addOnFailureListener { onFailure(it) }
            .addOnSuccessListener { onSuccess() }
    }

    /**
     * Retrieves a list of friends for the specified user.
     *
     * @param uid The user id whose friends are being retrieved.
     * @param onSuccess Called with a list of User objects representing the user's friends if
     *   retrieval succeeds.
     * @param onFailure Called with an exception if retrieval fails.
     */
    override fun getUserFriends(
        uid: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(usersCollectionPath)
            .document(uid)
            .get()
            .addOnFailureListener { e -> onFailure(e) }
            .addOnSuccessListener { result ->
                val friendsUidList = result.data?.get("friendsList") as? List<String> ?: emptyList()
                if (friendsUidList.isEmpty()) {
                    onSuccess(emptyList())
                    return@addOnSuccessListener
                }

                val tasks =
                    friendsUidList.map { uid ->
                        db.collection(usersCollectionPath).document(uid).get()
                    }

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

    /**
     * Sets the list of friends for the specified user.
     *
     * @param uid The user id whose friends list is being set.
     * @param friendsList A list of User objects representing the user's friends.
     * @param onSuccess Called when the list is successfully updated.
     * @param onFailure Called with an exception if the update fails.
     */
    override fun setUserFriends(
        uid: String,
        friendsList: List<User>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val friendsUidList = friendsList.map { it.uid }
        db.collection(usersCollectionPath)
            .document(uid)
            .update("friendsList", friendsUidList)
            .addOnFailureListener { onFailure(it) }
            .addOnSuccessListener { onSuccess() }
    }

    /**
     * Retrieves a list of recommended friends for the specified user, excluding current friends.
     *
     * @param uid The user id requesting friend recommendations.
     * @param onSuccess Called with a list of recommended friends if retrieval succeeds.
     * @param onFailure Called with an exception if retrieval fails.
     */
    override fun getRecommendedFriends(
        uid: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Fetch the user's friends list from the database
        db.collection(usersCollectionPath)
            .document(uid)
            .get()
            .addOnFailureListener { onFailure(it) }
            .addOnSuccessListener { userDocument ->
                val friendsUidList =
                    userDocument.data?.get("friendsList") as? List<String> ?: emptyList()

                // Fetch all users from the database
                db.collection(usersCollectionPath)
                    .get()
                    .addOnFailureListener { onFailure(it) }
                    .addOnSuccessListener { result ->
                        val allUsers =
                            result.documents.mapNotNull { helper.documentSnapshotToUser(it, null) }
                        val recommendedFriends =
                            allUsers.filter { it.uid !in friendsUidList && it.uid != uid }
                        onSuccess(recommendedFriends)
                    }
            }
    }

    /**
     * Retrieves a list of blocked users for the specified user.
     *
     * @param uid The user id whose blocked users list is being retrieved.
     * @param onSuccess Called with a list of blocked User objects if retrieval succeeds.
     * @param onFailure Called with an exception if retrieval fails.
     */
    override fun getBlockedFriends(
        uid: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(usersCollectionPath)
            .document(uid)
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

    /**
     * Sets the list of blocked users for the specified user.
     *
     * @param uid The user id whose blocked list is being set.
     * @param blockedFriendsList A list of User objects representing blocked users.
     * @param onSuccess Called when the list is successfully updated.
     * @param onFailure Called with an exception if the update fails.
     */
    override fun setBlockedFriends(
        uid: String,
        blockedFriendsList: List<User>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(usersCollectionPath)
            .document(uid)
            .update("blockedFriendsList", blockedFriendsList)
            .addOnFailureListener { onFailure(it) }
            .addOnSuccessListener { onSuccess() }
    }

    /**
     * Updates the last known location of the specified user.
     *
     * @param uid The user id whose location is being updated.
     * @param location The new Location object to update.
     * @param onSuccess Called when the location is successfully updated.
     * @param onFailure Called with an exception if the update fails.
     */
    override fun updateUserLocation(
        uid: String,
        location: Location,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(usersCollectionPath)
            .document(uid)
            .update("lastKnownLocation", helper.locationToString(location))
            .addOnFailureListener { onFailure(it) }
            .addOnSuccessListener { onSuccess() }
    }

    /**
     * Updates the timestamp for the specified user.
     *
     * @param uid The user id whose timestamp is being updated.
     * @param timestamp The new Timestamp to update.
     * @param onSuccess Called when the timestamp is successfully updated.
     * @param onFailure Called with an exception if the update fails.
     */
    override fun updateTimestamp(
        uid: String,
        timestamp: Timestamp,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(usersCollectionPath)
            .document(uid)
            .update("timestamp", timestamp)
            .addOnFailureListener { onFailure(it) }
            .addOnSuccessListener { onSuccess() }
    }

    override fun isFriendsInRadius(
        userLocation: Location,
        friends: List<User>,
        radius: Double,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        var inRadius = false
        friends.forEach { friend ->
            inRadius = inRadius || userLocation.distanceTo(friend.lastKnownLocation.value) < radius
        }
        if (inRadius) {
            onSuccess()
        } else {
            onFailure(Exception("No friends in radius"))
        }
    }

    /**
     * Sends a phone number verification code to the given phone number.
     *
     * @param phoneNumber The phone number to verify.
     * @param onSuccess Called with the verification ID or code if verification is successful.
     * @param onFailure Called with an exception if verification fails.
     */
    override fun sendVerificationCode(
        phoneNumber: String,
        onSuccess: (String) -> Unit, // Change to accept verification ID
        onFailure: (Exception) -> Unit
    ) {
        val options =
            PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber(phoneNumber) // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(MainActivity()) // Activity for callback binding
                .setCallbacks(
                    object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            // Auto-retrieval or instant verification succeeded
                            onSuccess(
                                credential.smsCode ?: "Auto-verified"
                            ) // Return SMS code if available
                        }

                        override fun onVerificationFailed(e: FirebaseException) {
                            Log.e(
                                "PhoneAuth",
                                "Verification failed: ${e.localizedMessage}, Cause: ${e.cause}"
                            )
                            onFailure(e)
                        }

                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            // Save verification ID and resending token so we can use them later
                            this@UserRepositoryFirestore.verificationId = verificationId
                            onSuccess(verificationId) // Return verification ID
                        }
                    })
                .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * Verifies the code entered by the user.
     *
     * @param verificationId The verification ID associated with the code.
     * @param code The verification code provided by the user.
     * @param onSuccess Called when code verification is successful.
     * @param onFailure Called with an exception if verification fails.
     */
    override fun verifyCode(
        verificationId: String,
        code: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()
            } else {
                task.exception?.let { onFailure(it) }
            }
        }
    }

    /**
     * Saves the login status of a user in shared preferences.
     *
     * @param uid The unique identifier of the user to save.
     */
    override fun saveLoginStatus(uid: String) {
        with(sharedPreferences.edit()) {
            putBoolean("isLoggedIn", true)
            putString("userId", uid)
            apply()
        }
    }

    /**
     * Retrieves the saved user ID from shared preferences.
     *
     * @return The saved user ID as a String, or an empty string if no user is logged in.
     */
    override fun getSavedUid(): String {
        return sharedPreferences.getString("userId", null) ?: ""
    }

    /**
     * Checks if a user is currently logged in based on shared preferences.
     *
     * @return `true` if the user is logged in, `false` otherwise.
     */
    override fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    /**
     * Logs out the current user by updating shared preferences to remove login status and user ID.
     */
    override fun logoutUser() {
        with(sharedPreferences.edit()) {
            putBoolean("isLoggedIn", false)
            putString("userId", null)
            apply()
        }
    }

    /**
     * Converts a JSON list of user IDs into a list of User objects by fetching their details from
     * Firestore.
     *
     * @param uidJsonList The JSON-formatted String containing user IDs.
     * @param onFailure Called with an exception if any user data retrieval fails.
     * @return A list of User objects corresponding to the IDs in the input JSON.
     */
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

/**
 * Helper class for UserRepositoryFirestore, providing methods for converting User objects to
 * Firestore data maps, and utilities for parsing and converting location data.
 */
internal class UserRepositoryFirestoreHelper {
    /**
     * Converts a User object into a map representation suitable for Firestore.
     *
     * @param user The User object to convert.
     * @return A map of user data fields.
     */
    fun userToMapOf(user: User): Map<String, String> {
        return mapOf(
            "uid" to user.uid,
            "firstName" to user.firstName,
            "lastName" to user.lastName,
            "phoneNumber" to user.phoneNumber,
            "emailAddress" to user.emailAddress,
            "lastKnownLocation" to locationToString(user.lastKnownLocation.value)
        )
    }

    /**
     * Converts a Location object into a String format suitable for Firestore storage.
     *
     * @param lastKnownLocation The Location object to convert.
     * @return The location as a formatted String.
     */
    fun locationToString(lastKnownLocation: Location?): String {
        return if (lastKnownLocation != null) {
            "{" +
                    "provider=" +
                    lastKnownLocation.provider +
                    ", latitude=" +
                    lastKnownLocation.latitude.toString() +
                    ", longitude=" +
                    lastKnownLocation.longitude.toString() +
                    "}"
        } else "{" + "provider= " + "latitude= " + ", " + "longitude= " + "}"
    }

    /**
     * Parses a location String back into a Location object.
     *
     * @param mapAttributes The formatted String containing location details.
     * @return The reconstructed Location object.
     */
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

    /**
     * Converts a DocumentSnapshot from Firestore into a User object.
     *
     * @param document The DocumentSnapshot to convert.
     * @param profilePicture The user's profile picture as an ImageBitmap, if available.
     * @return The reconstructed User object.
     */
    fun documentSnapshotToUser(document: DocumentSnapshot, profilePicture: ImageBitmap?): User {
        val lastKnownLocationString = document.data?.get("lastKnownLocation")?.toString()
        val lastKnownLocation =
            if (!lastKnownLocationString.isNullOrEmpty()) {
                try {
                    locationParser(lastKnownLocationString)
                } catch (e: Exception) {
                    Log.e("UserRepositoryFirestoreHelper", "Error parsing location: ", e)
                    null
                }
            } else {
                null
            }
        return User(
            uid = document.data?.get("uid").toString(),
            firstName = document.data?.get("firstName").toString(),
            lastName = document.data?.get("lastName").toString(),
            phoneNumber = document.data?.get("phoneNumber").toString(),
            emailAddress = document.data?.get("emailAddress").toString(),
            profilePicture = profilePicture,
            lastKnownLocation = MutableStateFlow(lastKnownLocation ?: Location("")),
        )
    }

    /**
     * Parses a JSON list of user IDs from a String and returns them as a list.
     *
     * @param uidJsonList The JSON-formatted String containing user IDs.
     * @return A list of user IDs.
     */
    fun documentSnapshotToList(uidJsonList: String): List<String> {
        val uidList = uidJsonList.trim('[', ']').split(", ").filter { it.isNotBlank() }
        return uidList
    }

    /**
     * Constructs the storage path for a user's profile picture.
     *
     * @param uid The user ID.
     * @param reference The root StorageReference for profile pictures.
     * @return The complete path to the profile picture.
     */
    fun uidToProfilePicturePath(uid: String, reference: StorageReference): String {
        return reference.child("$uid.jpeg").toString()
    }
}
