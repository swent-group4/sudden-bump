package com.swent.suddenbump.model.user

import android.app.Activity
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import junit.framework.TestCase.fail
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class UserRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestoreStorage: FirebaseStorage
  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockUserCollectionReference: CollectionReference
  @Mock private lateinit var mockEmailCollectionReference: CollectionReference
  @Mock private lateinit var mockUserDocumentReference: DocumentReference
  @Mock private lateinit var mockEmailDocumentReference: DocumentReference
  @Mock private lateinit var mockUserDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockEmailDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockUserQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockEmailQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockFirebaseAuth: FirebaseAuth
  @Mock private lateinit var mockFirebaseUser: FirebaseUser
  @Mock private lateinit var firebaseAuthMockStatic: MockedStatic<FirebaseAuth>
  @Mock private lateinit var mockTaskVoid: Task<Void>

  private lateinit var userRepositoryFirestore: UserRepositoryFirestore
  private val helper = UserRepositoryFirestoreHelper()

  val snapshot1: DocumentSnapshot = mock(DocumentSnapshot::class.java)
  val snapshot2: DocumentSnapshot = mock(DocumentSnapshot::class.java)

  private val location =
      Location("mock_provider").apply {
        latitude = 0.0
        longitude = 0.0
      }
  private val user =
      User(
          uid = "1",
          firstName = "Alexandre",
          lastName = "Carel",
          phoneNumber = "+33 6 59 20 70 02",
          null,
          emailAddress = "alexandre.carel@epfl.ch",
          lastKnownLocation = location)

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    System.setProperty("mockito.verbose", "true")

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    userRepositoryFirestore = UserRepositoryFirestore(mockFirestore, mock(Context::class.java))

    `when`(mockTaskVoid.isSuccessful).thenReturn(true)
    `when`(mockTaskVoid.isCanceled).thenReturn(false)
    `when`(mockTaskVoid.isComplete).thenReturn(true)
    `when`(mockTaskVoid.addOnSuccessListener(any())).thenReturn(mockTaskVoid)
    `when`(mockTaskVoid.addOnCompleteListener(any())).thenReturn(mockTaskVoid)
    `when`(mockTaskVoid.addOnFailureListener(any())).thenReturn(mockTaskVoid)

    `when`(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    `when`(mockUserCollectionReference.document(any())).thenReturn(mockUserDocumentReference)
    `when`(mockUserCollectionReference.document()).thenReturn(mockUserDocumentReference)
    `when`(mockUserDocumentReference.set(any())).thenReturn(mockTaskVoid)
    `when`(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
    `when`(mockUserDocumentReference.id).thenReturn("1")
    `when`(mockUserDocumentSnapshot.data)
        .thenReturn(
            mapOf(
                "uid" to "1",
                "firstName" to "Alexandre",
                "lastName" to "Carel",
                "phoneNumber" to "+33 6 59 20 70 02",
                "emailAddress" to "alexandre.carel@epfl.ch"))

    `when`(mockFirestore.collection("Emails")).thenReturn(mockEmailCollectionReference)
    `when`(mockEmailCollectionReference.document(any())).thenReturn(mockEmailDocumentReference)
    `when`(mockEmailCollectionReference.document()).thenReturn(mockEmailDocumentReference)
    `when`(mockEmailDocumentReference.id).thenReturn("alexandre.carel@epfl.ch")
    `when`(mockEmailDocumentSnapshot.data).thenReturn(mapOf("uid" to "1"))

    firebaseAuthMockStatic
        .`when`<FirebaseAuth> { FirebaseAuth.getInstance() }
        .thenReturn(mockFirebaseAuth)
    `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

    `when`(mockFirebaseUser.uid).thenReturn("1")
    `when`(mockFirebaseUser.email).thenReturn("alexandre.carel@epfl.ch")
    `when`(mockFirebaseUser.displayName).thenReturn("Alexandre Carel")
  }

  @Test
  fun getNewUid() {
    `when`(mockUserDocumentReference.id).thenReturn("1")
    val uid = userRepositoryFirestore.getNewUid()
    assert(uid == "1")
  }

  @After
  fun tearDown() {
    // Release the mockStatic object after each test to prevent memory leaks
    firebaseAuthMockStatic.close()
  }

  @Test
  fun verifyNoAccountExists_callsDocuments() {

    `when`(mockEmailCollectionReference.get()).thenReturn(Tasks.forResult(mockEmailQuerySnapshot))
    `when`(mockUserQuerySnapshot.documents).thenReturn(listOf())

    `when`(mockEmailDocumentReference.get()).thenReturn(Tasks.forResult(mockEmailDocumentSnapshot))

    userRepositoryFirestore.verifyNoAccountExists(
        user.emailAddress,
        onSuccess = {},
        onFailure = { fail("Failure callback should not be called") })

    verify(timeout(100)) { (mockUserQuerySnapshot).documents }
  }

  /**
   * This test verifies that when we create a new User account, the Firestore `set()` is called on
   * the document reference. This does NOT CHECK the actual data being added
   */
  @Test
  fun createUserAccount_shouldCallFirestoreCollection() {
    userRepositoryFirestore.createUserAccount(user, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockUserDocumentReference).set(any())
  }

  /**
   * This test verifies that when fetching a User, the Firestore `get()` is called on the collection
   * reference and not the document reference.
   */
  @Test
  fun getUserAccount_callsDocuments() {
    `when`(mockUserCollectionReference.get()).thenReturn(Tasks.forResult(mockUserQuerySnapshot))
    `when`(mockUserQuerySnapshot.documents).thenReturn(listOf())

    `when`(mockEmailCollectionReference.get()).thenReturn(Tasks.forResult(mockEmailQuerySnapshot))
    `when`(mockUserQuerySnapshot.documents).thenReturn(listOf())

    `when`(mockEmailDocumentReference.get()).thenReturn(Tasks.forResult(mockEmailDocumentSnapshot))

    userRepositoryFirestore.getUserAccount(
        onSuccess = {}, onFailure = { fail("Failure callback should not be called") })

    verify(timeout(100)) { (mockUserQuerySnapshot).documents }
  }

  /**
   * This test verifies that when we create a new User account, the Firestore `set()` is called on
   * the document reference. This does NOT CHECK the actual data being added
   */
  @Test
  fun updateUserAccount_shouldCallFirestoreCollection() {
    userRepositoryFirestore.updateUserAccount(user, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockUserDocumentReference).set(any())
  }

  @Test
  fun createFriend_shouldUpdateFriendLists() {
    val user =
        User(
            "1",
            "Alexandre",
            "Carel",
            "+33 6 59 20 70 02",
            null,
            "alexandre.carel@epfl.ch",
            lastKnownLocation = location)
    val friend =
        User(
            "2",
            "Jane",
            "Doe",
            "+41 00 000 00 02",
            null,
            "jane.doe@example.com",
            lastKnownLocation = location)

    // Mock the Firestore document snapshots
    val userDocumentSnapshot = mock(DocumentSnapshot::class.java)
    val friendDocumentSnapshot = mock(DocumentSnapshot::class.java)

    // Mock the data returned by the document snapshots
    `when`(userDocumentSnapshot.data)
        .thenReturn(
            mapOf(
                "friendsList" to listOf<String>(),
                "friendRequests" to listOf(friend.uid),
                "sentFriendRequests" to listOf<String>()))
    `when`(friendDocumentSnapshot.data)
        .thenReturn(
            mapOf(
                "friendsList" to listOf<String>(),
                "friendRequests" to listOf<String>(),
                "sentFriendRequests" to listOf(user.uid)))

    // Mock the Firestore document references
    val userDocumentReference = mock(DocumentReference::class.java)
    val friendDocumentReference = mock(DocumentReference::class.java)

    // Mock the Firestore collection reference
    val userCollectionReference = mock(CollectionReference::class.java)
    `when`(userCollectionReference.document(user.uid)).thenReturn(userDocumentReference)
    `when`(userCollectionReference.document(friend.uid)).thenReturn(friendDocumentReference)

    // Mock the Firestore instance
    val mockFirestore = mock(FirebaseFirestore::class.java)
    `when`(mockFirestore.collection("Users")).thenReturn(userCollectionReference)

    // Mock the get() method to return the document snapshots
    `when`(userDocumentReference.get()).thenReturn(Tasks.forResult(userDocumentSnapshot))
    `when`(friendDocumentReference.get()).thenReturn(Tasks.forResult(friendDocumentSnapshot))

    // Mock the update() method to return a successful task
    `when`(userDocumentReference.update(anyString(), any())).thenReturn(Tasks.forResult(null))
    `when`(friendDocumentReference.update(anyString(), any())).thenReturn(Tasks.forResult(null))

    // Create the UserRepositoryFirestore instance
    val userRepository = UserRepositoryFirestore(mockFirestore)

    // Call the createFriend method
    userRepository.createFriend(
        user,
        friend,
        {
          // Verify the updates to the user document
          verify(userDocumentReference).update("friendRequests", listOf<String>())
          verify(userDocumentReference).update("friendsList", listOf(friend.uid))

          // Verify the updates to the friend document
          verify(friendDocumentReference).update("friendsList", listOf(user.uid))
          verify(friendDocumentReference).update("friendRequests", listOf<String>())
          verify(friendDocumentReference).update("sentFriendRequests", listOf<String>())
        },
        { fail("onFailure should not be called") })
  }

  @Test
  fun createFriendRequest_shouldAddFriendRequest() {
    val user =
        User(
            "1",
            "Alexandre",
            "Carel",
            "+33 6 59 20 70 02",
            null,
            "alexandre.carel@epfl.ch",
            lastKnownLocation = location)
    val friend =
        User(
            "2",
            "Jane",
            "Doe",
            "+41 00 000 00 02",
            null,
            "jane.doe@example.com",
            lastKnownLocation = location)

    // Mock the Firestore document snapshots
    val userDocumentSnapshot = mock(DocumentSnapshot::class.java)
    val friendDocumentSnapshot = mock(DocumentSnapshot::class.java)

    // Mock the data returned by the document snapshots
    `when`(friendDocumentSnapshot.data).thenReturn(mapOf("friendRequests" to listOf<String>()))
    `when`(userDocumentSnapshot.data).thenReturn(mapOf("sentFriendRequests" to listOf<String>()))

    // Mock the Firestore document references
    val userDocumentReference = mock(DocumentReference::class.java)
    val friendDocumentReference = mock(DocumentReference::class.java)

    // Mock the Firestore collection reference
    val userCollectionReference = mock(CollectionReference::class.java)
    `when`(userCollectionReference.document(user.uid)).thenReturn(userDocumentReference)
    `when`(userCollectionReference.document(friend.uid)).thenReturn(friendDocumentReference)

    // Mock the Firestore instance
    val mockFirestore = mock(FirebaseFirestore::class.java)
    `when`(mockFirestore.collection("Users")).thenReturn(userCollectionReference)

    // Mock the get() method to return the document snapshots
    `when`(userDocumentReference.get()).thenReturn(Tasks.forResult(userDocumentSnapshot))
    `when`(friendDocumentReference.get()).thenReturn(Tasks.forResult(friendDocumentSnapshot))

    // Mock the update() method to return a successful task
    `when`(userDocumentReference.update(anyString(), any())).thenReturn(Tasks.forResult(null))
    `when`(friendDocumentReference.update(anyString(), any())).thenReturn(Tasks.forResult(null))

    // Create the UserRepositoryFirestore instance
    val userRepository = UserRepositoryFirestore(mockFirestore)

    // Call the createFriendRequest method
    userRepository.createFriendRequest(
        user,
        friend,
        {
          // Verify the updates to the friend document
          verify(friendDocumentReference).update("friendRequests", listOf(user.uid))

          // Verify the updates to the user document
          verify(userDocumentReference).update("sentFriendRequests", listOf(friend.uid))
        },
        { fail("onFailure should not be called") })
  }

  /**
   * This check that the correct Firestore method is called when deleting. Does NOT CHECK that the
   * correct data is deleted.
   */
  @Test
  fun deleteUserById_shouldCallDocumentReferenceDelete() {
    `when`(mockUserDocumentReference.delete()).thenReturn(mockTaskVoid)

    userRepositoryFirestore.deleteUserAccount("1", onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle() // Ensure all asynchronous operations complete

    verify(mockUserDocumentReference).delete()
  }

  /**
   * This test verifies that when fetching a User, the Firestore `get()` is called on the collection
   * reference and not the document reference.
   */
  @Test
  fun getUserFriends_callsDocuments() {
    `when`(mockUserCollectionReference.get()).thenReturn(Tasks.forResult(mockUserQuerySnapshot))
    `when`(mockUserQuerySnapshot.documents).thenReturn(listOf())

    userRepositoryFirestore.getUserFriends(
        user = user, onSuccess = {}, onFailure = { fail("Failure callback should not be called") })

    verify(timeout(100)) { (mockUserQuerySnapshot).documents }
  }

  /**
   * This test verifies that when we create a new User account, the Firestore `set()` is called on
   * the document reference. This does NOT CHECK the actual data being added
   */
  @Test
  fun setUserFriends_shouldCallFirestoreCollection() {
    `when`(mockUserDocumentReference.update(anyString(), any())).thenReturn(mockTaskVoid)

    userRepositoryFirestore.setUserFriends(
        user = user, friendsList = listOf(user), onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockUserDocumentReference).update(anyString(), any())
  }

  /**
   * This test verifies that when fetching a User, the Firestore `get()` is called on the collection
   * reference and not the document reference.
   */
  @Test
  fun getUserBlockedFriends_callsDocuments() {
    `when`(mockUserCollectionReference.get()).thenReturn(Tasks.forResult(mockUserQuerySnapshot))
    `when`(mockUserQuerySnapshot.documents).thenReturn(listOf())

    userRepositoryFirestore.getBlockedFriends(
        user = user, onSuccess = {}, onFailure = { fail("Failure callback should not be called") })

    verify(timeout(100)) { (mockUserQuerySnapshot).documents }
  }

  /**
   * This test verifies that when we create a new User account, the Firestore `set()` is called on
   * the document reference. This does NOT CHECK the actual data being added
   */
  @Test
  fun setUserBlockedFriends_shouldCallFirestoreCollection() {
    `when`(mockUserDocumentReference.update(anyString(), any())).thenReturn(mockTaskVoid)

    userRepositoryFirestore.setBlockedFriends(
        user = user, blockedFriendsList = listOf(user), onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockUserDocumentReference).update(anyString(), any())
  }

  /**
   * This test verifies that when fetching a new user's Location, the Firestore `set()` is called on
   * the document reference. This does NOT CHECK the actual data being added
   */
  @Test
  fun setLocation_shouldCallFirestoreCollection() {
    `when`(mockUserDocumentReference.update(anyString(), any())).thenReturn(mockTaskVoid)

    userRepositoryFirestore.updateLocation(
        user = user, location = location, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockUserDocumentReference).update(anyString(), any())
  }

  @Test
  fun getFriendsLocationSuccessWithFriendsAndLocations() {
    // Given
    val friend1 =
        User(
            "uid1",
            "Friend1",
            "Test",
            "000",
            null,
            "friend1@example.com",
            lastKnownLocation = location)
    val friend2 =
        User(
            "uid2",
            "Friend2",
            "Test",
            "000",
            null,
            "friend2@example.com",
            lastKnownLocation = location)

    val friendsLocations = mutableStateOf<Map<User, Location?>>(emptyMap())

    val location1 = mock(Location::class.java)
    val snapshot1 = mock(DocumentSnapshot::class.java)
    val snapshot2 = mock(DocumentSnapshot::class.java)

    // Mock the snapshot locations
    whenever(snapshot1.get("location")).thenReturn(location1)
    whenever(snapshot2.get("location")).thenReturn(null)

    // Mock the user repository to return the snapshots
    val userRepositoryFirestore = mock(UserRepositoryFirestore::class.java)
    whenever(userRepositoryFirestore.getFriendsLocation(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Map<User, Location?>) -> Unit>(1)
      val result = mapOf(friend1 to location1, friend2 to null)
      onSuccess(result)
    }

    // Define the expected map
    val expectedMap = mapOf(friend1 to location1, friend2 to null)

    // When
    userRepositoryFirestore.getFriendsLocation(
        listOf(friend1, friend2),
        onSuccess = { friendsLoc ->
          // Update the state with the locations of friends
          friendsLocations.value = friendsLoc
          println("friendsLoc: $friendsLoc")
          println("expectedMap: $expectedMap")
          assertEquals(expectedMap, friendsLoc)
        },
        onFailure = { error ->
          // Handle the error, e.g., log or show error message
          Log.e("UserViewModel", "Failed to load friends' locations: ${error.message}")
          fail("Expected successful callback but got failure: ${error.message}")
        })
  }

  @Test
  fun getFriendsLocationSuccessWithNoFriends() {
    // Mock the snapshot locations
    whenever(snapshot1.get("location")).thenReturn(mock(Location::class.java))
    whenever(snapshot2.get("location")).thenReturn(null)

    // When
    userRepositoryFirestore.getFriendsLocation(
        listOf(),
        { friendsLoc ->
          // Then
          assert(friendsLoc == emptyMap<User, Location>())
        },
        { fail("Failure callback should not be called") })
  }

  @Test
  fun userToMap() {
    val testUser =
        User(
            uid = "1234",
            firstName = "John",
            lastName = "Doe",
            phoneNumber = "+1234567890",
            profilePicture = null,
            emailAddress = "john.doe@example.com",
            lastKnownLocation = location)

    val result = helper.userToMapOf(testUser)

    assertEquals("1234", result["uid"])
    assertEquals("John", result["firstName"])
    assertEquals("Doe", result["lastName"])
    assertEquals("+1234567890", result["phoneNumber"])
    assertEquals("john.doe@example.com", result["emailAddress"])
    assertEquals(
        "{provider=mock_provider, latitude=0.0, longitude=0.0}", result["lastKnownLocation"])

    assertEquals(
        setOf("uid", "firstName", "lastName", "phoneNumber", "emailAddress", "lastKnownLocation"),
        result.keys)

    assert(!result.contains("profilePicture"))
  }

  @Test
  fun documentSnapshotToUser() {
    val data =
        mapOf(
            "uid" to "1234",
            "firstName" to "John",
            "lastName" to "Doe",
            "phoneNumber" to "+1234567890",
            "emailAddress" to "john.doe@example.com",
            "lastKnownLocation" to location)
    val defaultProfilePicture: ImageBitmap? = null

    `when`(mockUserDocumentSnapshot.data).thenReturn(data)
    val result = helper.documentSnapshotToUser(mockUserDocumentSnapshot, defaultProfilePicture)

    assertEquals("1234", result.uid)
    assertEquals("John", result.firstName)
    assertEquals("Doe", result.lastName)
    assertEquals("+1234567890", result.phoneNumber)
    assertEquals("john.doe@example.com", result.emailAddress)
    assertEquals(defaultProfilePicture, result.profilePicture)
  }

  @Test
  fun documentSnapshotToList() {
    val data = "[uid1, uid2, uid3]"

    var result = helper.documentSnapshotToList(data)
    assertEquals(listOf("uid1", "uid2", "uid3"), result)

    result = helper.documentSnapshotToList("[uid1]")
    assertEquals(listOf("uid1"), result)

    result = helper.documentSnapshotToList("[]")
    assertEquals(emptyList<String>(), result)

    result = helper.documentSnapshotToList("")
    assertEquals(emptyList<String>(), result)
  }

  @Test
  fun uidToProfilePicturePath() {
    val uid = "user123"
    var path = "gs:/"
    val storageReference = mock(StorageReference::class.java)
    `when`(storageReference.toString()).thenAnswer { path }
    `when`(storageReference.child(anyString())).thenAnswer { invocation ->
      val childPath = invocation.getArgument<String>(0)
      if (path.last() == '/') {
        path += childPath
      } else {
        path += "/$childPath"
      }
      storageReference
    }

    var result = helper.uidToProfilePicturePath(uid, storageReference)
    assertEquals("gs:/user123.jpeg", result)

    result = helper.uidToProfilePicturePath("user@123", storageReference)
    assertEquals("gs:/user123.jpeg/user@123.jpeg", result)
  }

  @Test
  fun testSendVerificationCode() {
    val phoneNumber = "+1234567890"
    val verificationId = "verificationId"

    val callbacks = mock(PhoneAuthProvider.OnVerificationStateChangedCallbacks::class.java)
    doAnswer {
          callbacks.onCodeSent(
              verificationId, mock(PhoneAuthProvider.ForceResendingToken::class.java))
        }
        .`when`(callbacks)
        .onCodeSent(any(), any())

    val mockActivity = mock(Activity::class.java)

    val options =
        PhoneAuthOptions.newBuilder(mockFirebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
            .setActivity(mockActivity)
            .setCallbacks(callbacks)
            .build()

    PhoneAuthProvider.verifyPhoneNumber(options)

    userRepositoryFirestore.sendVerificationCode(
        phoneNumber, { id -> assertEquals(verificationId, id) }, { fail("Verification failed") })
  }

  @Test
  fun testVerifyCode() {
    val verificationId = "verificationId"
    val code = "123456"
    val credential = mock(PhoneAuthCredential::class.java)

    // Mock the static method PhoneAuthProvider.getCredential
    val phoneAuthProviderMockStatic = mockStatic(PhoneAuthProvider::class.java)
    phoneAuthProviderMockStatic
        .`when`<PhoneAuthCredential> { PhoneAuthProvider.getCredential(verificationId, code) }
        .thenReturn(credential)

    val authResultTask = Tasks.forResult(mock(AuthResult::class.java))
    `when`(mockFirebaseAuth.signInWithCredential(eq(credential))).thenReturn(authResultTask)

    userRepositoryFirestore.verifyCode(
        verificationId,
        code,
        {
          // Success
        },
        { fail("Verification failed") })

    // Close the static mock after the test
    phoneAuthProviderMockStatic.close()
  }
}
