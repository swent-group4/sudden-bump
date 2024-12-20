package com.swent.suddenbump.model.user.userRepositoryFirestoreTests

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Looper
import android.os.Looper.getMainLooper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.swent.suddenbump.model.image.ImageRepository
import com.swent.suddenbump.model.user.SharedPreferencesManager
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import com.swent.suddenbump.model.user.UserRepositoryFirestoreHelper
import com.swent.suddenbump.worker.WorkerScheduler
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class UserFriendsRequestsTests {

  @Mock private lateinit var mockFirestore: FirebaseFirestore

  @Mock private lateinit var mockSharedPreferencesManager: SharedPreferencesManager

  @Mock private lateinit var mockWorkerScheduler: WorkerScheduler

  @Mock private lateinit var mockUserCollectionReference: CollectionReference

  @Mock private lateinit var mockEmailCollectionReference: CollectionReference

  @Mock private lateinit var mockPhoneCollectionReference: CollectionReference

  @Mock private lateinit var mockUserDocumentReference: DocumentReference

  @Mock private lateinit var mockEmailDocumentReference: DocumentReference

  @Mock private lateinit var mockPhoneDocumentReference: DocumentReference

  @Mock private lateinit var mockUserDocumentSnapshot: DocumentSnapshot

  @Mock private lateinit var mockEmailDocumentSnapshot: DocumentSnapshot

  @Mock private lateinit var mockPhoneDocumentSnapshot: DocumentSnapshot

  @Mock private lateinit var mockPhoneQuerySnapshot: QuerySnapshot

  @Mock private lateinit var mockFirebaseAuth: FirebaseAuth

  @Mock private lateinit var mockFirebaseUser: FirebaseUser

  @Mock private lateinit var firebaseAuthMockStatic: MockedStatic<FirebaseAuth>

  @Mock private lateinit var mockTaskVoid: Task<Void>

  @Mock private lateinit var mockImageRepository: ImageRepository

  @Mock private lateinit var mockFriendDocumentReference: DocumentReference

  @Mock private lateinit var mockFriendDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var sharedPreferences: SharedPreferences
  @Mock private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

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
          phoneNumber = "+33659207002",
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

    val mockContext = mock(Context::class.java)
    `when`(mockContext.getSharedPreferences(anyString(), eq(Context.MODE_PRIVATE)))
        .thenReturn(sharedPreferences)

    `when`(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor)
    `when`(sharedPreferencesEditor.putString(anyString(), anyString()))
        .thenReturn(sharedPreferencesEditor)
    `when`(sharedPreferencesEditor.putBoolean(anyString(), anyBoolean()))
        .thenReturn(sharedPreferencesEditor)

    `when`(mockSharedPreferencesManager.saveString(anyString(), anyString())).thenAnswer {
        invocation ->
      val key = invocation.arguments[0] as String
      val value = invocation.arguments[1] as String
      sharedPreferencesEditor.putString(key, value).apply()
    }

    `when`(mockSharedPreferencesManager.saveBoolean(anyString(), anyBoolean())).thenAnswer {
        invocation ->
      val key = invocation.arguments[0] as String
      val value = invocation.arguments[1] as Boolean
      sharedPreferencesEditor.putBoolean(key, value).apply()
    }

    userRepositoryFirestore =
        UserRepositoryFirestore(mockFirestore, mockSharedPreferencesManager, mockWorkerScheduler)

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
    `when`(mockUserDocumentReference.update(any())).thenReturn(mockTaskVoid)
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

    `when`(mockFirestore.collection("Phones")).thenReturn(mockPhoneCollectionReference)
    `when`(mockPhoneCollectionReference.document(any())).thenReturn(mockPhoneDocumentReference)
    `when`(mockPhoneCollectionReference.document()).thenReturn(mockPhoneDocumentReference)
    `when`(mockPhoneDocumentReference.id).thenReturn("+33659207002")
    `when`(mockPhoneDocumentSnapshot.data).thenReturn(mapOf("uid" to "1"))

    firebaseAuthMockStatic
        .`when`<FirebaseAuth> { FirebaseAuth.getInstance() }
        .thenReturn(mockFirebaseAuth)
    `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

    `when`(mockFirebaseUser.uid).thenReturn("1")
    `when`(mockFirebaseUser.email).thenReturn("alexandre.carel@epfl.ch")
    `when`(mockFirebaseUser.displayName).thenReturn("Alexandre Carel")

    // Mock behavior of the editor
    `when`(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor)
    `when`(sharedPreferencesEditor.putString(anyString(), anyString()))
        .thenReturn(sharedPreferencesEditor)
  }

  @After
  fun tearDown() {
    // Release the mockStatic object after each test to prevent memory leaks
    firebaseAuthMockStatic.close()
    Mockito.reset(
        mockFirestore,
        mockTaskVoid,
        mockUserCollectionReference,
        mockUserDocumentReference,
        mockImageRepository,
        mockUserDocumentSnapshot,
        mockEmailCollectionReference,
        mockEmailDocumentReference,
        mockEmailDocumentSnapshot,
        mockFirebaseAuth,
        mockFirebaseUser,
        mockFriendDocumentReference,
        mockFriendDocumentSnapshot,
        mockPhoneCollectionReference,
        mockPhoneDocumentReference,
        mockPhoneDocumentSnapshot,
        mockPhoneQuerySnapshot)
  }

  @Test
  fun getUserFriendRequestsReturnsCorrectUserListOnSuccess() {
    // Mock user
    val friend1 =
        User(
            uid = "123",
            firstName = "John",
            lastName = "Doe",
            phoneNumber = "123456789",
            profilePicture = null,
            emailAddress = "john.doe@example.com",
            lastKnownLocation = location)

    val friend2 =
        User(
            uid = "456",
            firstName = "John",
            lastName = "Doe",
            phoneNumber = "123456789",
            profilePicture = null,
            emailAddress = "john.doe@example.com",
            lastKnownLocation = location)

    // Mock friend request UIDs
    val friendRequestsUidList = listOf(friend1.uid, friend2.uid)
    val friend1Snapshot = mock(DocumentSnapshot::class.java)
    val friend2Snapshot = mock(DocumentSnapshot::class.java)

    `when`(mockUserDocumentSnapshot.data)
        .thenReturn(mapOf("friendRequests" to friendRequestsUidList))

    // Mock user document reference
    val friend1DocumentRef = mock(DocumentReference::class.java)

    `when`(mockFirestore.collection("Users").document(friend1.uid)).thenReturn(friend1DocumentRef)
    `when`(friend1DocumentRef.get()).thenReturn(Tasks.forResult(friend1Snapshot))

    val friend2DocumentRef = mock(DocumentReference::class.java)

    `when`(mockFirestore.collection("Users").document(friend2.uid)).thenReturn(friend2DocumentRef)
    `when`(friend2DocumentRef.get()).thenReturn(Tasks.forResult(friend2Snapshot))

    `when`(friend1Snapshot.exists()).thenReturn(true)
    `when`(friend2Snapshot.exists()).thenReturn(true)

    val helper = mock(UserRepositoryFirestoreHelper::class.java)
    `when`(helper.documentSnapshotToUser(friend1Snapshot, null)).thenReturn(friend1)
    `when`(helper.documentSnapshotToUser(friend2Snapshot, null)).thenReturn(friend2)

    // Inject mockHelper into userRepository
    val helperField = UserRepositoryFirestore::class.java.getDeclaredField("helper")
    helperField.isAccessible = true
    helperField.set(userRepositoryFirestore, helper)

    // Prepare the tasks for whenAllSuccess
    val friendTasks = listOf(friend1DocumentRef.get(), friend2DocumentRef.get())

    // Mock Tasks.whenAllSuccess
    val whenAllSuccessTask = Tasks.whenAllSuccess<DocumentSnapshot>(friendTasks)

    // Since whenAllSuccess is final and cannot be mocked directly, we simulate its behavior
    `when`(Tasks.whenAllSuccess<DocumentSnapshot>(friendTasks))
        .thenReturn(Tasks.forResult(listOf(friend1Snapshot, friend2Snapshot)))

    // Invoke the method
    userRepositoryFirestore.getUserFriendRequests(
        user.uid,
        onSuccess = { result ->
          // Verify the result
          assert(result.size == 2)
          assert(result.contains(friend1))
          assert(result.contains(friend2))
        },
        onFailure = { assert(false) { "Should not fail on success" } })
  }

  @Test
  fun getUserFriendRequests_ShouldSkipInvalidDocuments_WhenSomeTasksSucceed() {
    // Arrange
    val friendRequestsUidList = listOf("5678", "91011")
    val friend1DocumentSnapshot = mock(DocumentSnapshot::class.java)
    val friend2DocumentSnapshot = mock(DocumentSnapshot::class.java)

    whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    whenever(mockUserCollectionReference.document(user.uid)).thenReturn(mockUserDocumentReference)
    whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
    whenever(mockUserDocumentSnapshot.data)
        .thenReturn(mapOf("friendRequests" to friendRequestsUidList))

    whenever(mockUserCollectionReference.document("5678").get())
        .thenReturn(Tasks.forResult(friend1DocumentSnapshot))
    whenever(mockUserCollectionReference.document("91011").get())
        .thenReturn(Tasks.forResult(friend2DocumentSnapshot))

    val friend1 =
        User("5678", "Friend1", "Test", "+1234567891", null, "friend1@example.com", location)

    whenever(friend1DocumentSnapshot.data).thenReturn(helper.userToMapOf(friend1))
    whenever(friend2DocumentSnapshot.data).thenReturn(null)

    val onSuccessMock = mock<(List<User>) -> Unit>()

    // Act
    userRepositoryFirestore.getUserFriendRequests(
        uid = user.uid,
        onSuccess = { func -> assert(func.size == 1 && func[0].uid == "5678") },
        onFailure = { fail("onFailure should not be called") })
  }

  @Test
  fun getUserFriendRequests_ShouldReturnEmptyList_WhenNoFriendRequestsExist() {
    whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    whenever(mockUserCollectionReference.document(user.uid)).thenReturn(mockUserDocumentReference)
    whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
    whenever(mockUserDocumentSnapshot.data)
        .thenReturn(mapOf("friendRequests" to emptyList<String>()))

    // Act
    userRepositoryFirestore.getUserFriendRequests(
        uid = user.uid,
        onSuccess = { friendRequestsList ->
          // Assert
          assertTrue(friendRequestsList.isEmpty())
        },
        onFailure = { fail("Expected onSuccess to be called") })
  }

  @Test
  fun getUserFriendRequests_ShouldCallOnFailure_WhenFirestoreFails() {
    // Arrange
    val exception = Exception("Firestore error")

    whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    whenever(mockUserCollectionReference.document(user.uid)).thenReturn(mockUserDocumentReference)
    whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forException(exception))

    val onFailureMock = mock<(Exception) -> Unit>()

    // Act
    userRepositoryFirestore.getUserFriendRequests(
        uid = user.uid,
        onSuccess = { fail("Expected onFailure to be called") },
        onFailure = onFailureMock)

    // Assert
    shadowOf(getMainLooper()).idle()
    verify(onFailureMock).invoke(eq(exception))
  }

  @Test
  fun deleteFriendRequest_shouldRemoveFriendRequest() {
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
    val mockFriendDocumentSnapshot = mock(DocumentSnapshot::class.java)

    // Mock the data returned by the document snapshots
    `when`(mockUserDocumentSnapshot.data).thenReturn(mapOf("friendRequests" to listOf(friend.uid)))
    `when`(mockFriendDocumentSnapshot.data)
        .thenReturn(mapOf("sentFriendRequests" to listOf(user.uid)))

    // Mock the Firestore document references
    val mockFriendDocumentReference = mock(DocumentReference::class.java)

    // Mock the Firestore collection reference
    `when`(mockUserCollectionReference.document(user.uid)).thenReturn(mockUserDocumentReference)
    `when`(mockUserCollectionReference.document(friend.uid)).thenReturn(mockFriendDocumentReference)

    // Mock the get() method to return the document snapshots
    `when`(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
    `when`(mockFriendDocumentReference.get())
        .thenReturn(Tasks.forResult(mockFriendDocumentSnapshot))

    // Mock the update() method to return a successful task
    `when`(mockUserDocumentReference.update(anyString(), any())).thenReturn(Tasks.forResult(null))
    `when`(mockFriendDocumentReference.update(anyString(), any())).thenReturn(Tasks.forResult(null))

    // Call the deleteFriendRequest method
    userRepositoryFirestore.deleteFriendRequest(
        user.uid,
        friend.uid,
        {
          // Verify the updates to the user document
          verify(mockUserDocumentReference).update("friendRequests", emptyList<String>())

          // Verify the updates to the friend document
          verify(mockFriendDocumentReference).update("sentFriendRequests", emptyList<String>())
        },
        { fail("onFailure should not be called") })
  }

  @Test
  fun deleteFriendRequest_updateFriendSentFriendRequestsFails() {
    val user =
        User(
            uid = "1",
            firstName = "Alexandre",
            lastName = "Carel",
            phoneNumber = "+33 6 59 20 70 02",
            profilePicture = null,
            emailAddress = "alexandre.carel@epfl.ch",
            lastKnownLocation = location)
    val friend =
        User(
            uid = "2",
            firstName = "Jane",
            lastName = "Doe",
            phoneNumber = "+41 00 000 00 02",
            profilePicture = null,
            emailAddress = "jane.doe@example.com",
            lastKnownLocation = location)

    // Mock the data returned by the document snapshots
    `when`(mockUserDocumentSnapshot.data).thenReturn(mapOf("friendRequests" to listOf(friend.uid)))
    `when`(mockFriendDocumentSnapshot.data)
        .thenReturn(mapOf("sentFriendRequests" to listOf(user.uid)))

    val mockFriendDocumentReference = mock(DocumentReference::class.java)

    `when`(mockUserCollectionReference.document(user.uid)).thenReturn(mockUserDocumentReference)
    `when`(mockUserCollectionReference.document(friend.uid)).thenReturn(mockFriendDocumentReference)

    // Mock the get() method to return the document snapshots
    `when`(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
    `when`(mockFriendDocumentReference.get())
        .thenReturn(Tasks.forResult(mockFriendDocumentSnapshot))

    // Mock the update() method on user document to return a successful task
    `when`(mockUserDocumentReference.update(anyString(), any())).thenReturn(Tasks.forResult(null))
    // Mock the update() method on friend document to return a failed task
    `when`(mockFriendDocumentReference.update(anyString(), any()))
        .thenReturn(Tasks.forException(Exception("Update friend's sentFriendRequests failed")))

    var onFailureCalled = false
    var failureException: Exception? = null

    // Call the deleteFriendRequest method
    userRepositoryFirestore.deleteFriendRequest(
        uid = user.uid,
        fid = friend.uid,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { exception ->
          onFailureCalled = true
          failureException = exception
        })

    // Ensure all asynchronous operations complete
    shadowOf(Looper.getMainLooper()).idle()

    // Verify that the update on friend's sentFriendRequests was attempted
    verify(mockFriendDocumentReference).update(eq("sentFriendRequests"), any())
    // Verify that onFailure was called
    assertTrue(onFailureCalled)
    assertNotNull(failureException)
    assertEquals("Update friend's sentFriendRequests failed", failureException?.message)
  }

  // New test case to verify that onSuccess is called upon successful deletion
  @Test
  fun deleteFriendRequest_successfulCompletion_callsOnSuccess() {
    val user =
        User(
            uid = "1",
            firstName = "Alexandre",
            lastName = "Carel",
            phoneNumber = "+33 6 59 20 70 02",
            profilePicture = null,
            emailAddress = "alexandre.carel@epfl.ch",
            lastKnownLocation = location)
    val friend =
        User(
            uid = "2",
            firstName = "Jane",
            lastName = "Doe",
            phoneNumber = "+41 00 000 00 02",
            profilePicture = null,
            emailAddress = "jane.doe@example.com",
            lastKnownLocation = location)

    val mockFriendDocumentSnapshot = mock(DocumentSnapshot::class.java)

    // Mock the data returned by the document snapshots
    `when`(mockUserDocumentSnapshot.data).thenReturn(mapOf("friendRequests" to listOf(friend.uid)))
    `when`(mockFriendDocumentSnapshot.data)
        .thenReturn(mapOf("sentFriendRequests" to listOf(user.uid)))

    val mockFriendDocumentReference = mock(DocumentReference::class.java)

    `when`(mockUserCollectionReference.document(user.uid)).thenReturn(mockUserDocumentReference)
    `when`(mockUserCollectionReference.document(friend.uid)).thenReturn(mockFriendDocumentReference)

    // Mock the get() and update() methods to return successful tasks
    `when`(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
    `when`(mockFriendDocumentReference.get())
        .thenReturn(Tasks.forResult(mockFriendDocumentSnapshot))
    `when`(mockUserDocumentReference.update(anyString(), any())).thenReturn(Tasks.forResult(null))
    `when`(mockFriendDocumentReference.update(anyString(), any())).thenReturn(Tasks.forResult(null))

    var onSuccessCalled = false
    var onFailureCalled = false

    // Call the deleteFriendRequest method
    userRepositoryFirestore.deleteFriendRequest(
        uid = user.uid,
        fid = friend.uid,
        onSuccess = {
          // Success callback
          onSuccessCalled = true
        },
        onFailure = {
          // Failure callback
          onFailureCalled = true
        })

    // Ensure all asynchronous operations complete
    shadowOf(Looper.getMainLooper()).idle()

    // Verify that onSuccess was called and onFailure was not
    assertTrue(onSuccessCalled)
    assertFalse(onFailureCalled)
  }

  // New test case to cover the failure of updating user's friendRequests
  @Test
  fun deleteFriendRequest_updateUserFriendRequestsFails() {
    val user =
        User(
            uid = "1",
            firstName = "Alexandre",
            lastName = "Carel",
            phoneNumber = "+33 6 59 20 70 02",
            profilePicture = null,
            emailAddress = "alexandre.carel@epfl.ch",
            lastKnownLocation = location)
    val friend =
        User(
            uid = "2",
            firstName = "Jane",
            lastName = "Doe",
            phoneNumber = "+41 00 000 00 02",
            profilePicture = null,
            emailAddress = "jane.doe@example.com",
            lastKnownLocation = location)

    // Mock the data returned by the document snapshot
    `when`(mockUserDocumentSnapshot.data).thenReturn(mapOf("friendRequests" to listOf(friend.uid)))

    // Mock the get() method to return the document snapshot
    `when`(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))

    // Mock the update() method on user document to return a failed task
    `when`(mockUserDocumentReference.update(anyString(), any()))
        .thenReturn(Tasks.forException(Exception("Update user friendRequests failed")))

    var onFailureCalled = false
    var failureException: Exception? = null

    // Call the deleteFriendRequest method
    userRepositoryFirestore.deleteFriendRequest(
        uid = user.uid,
        fid = friend.uid,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { exception ->
          onFailureCalled = true
          failureException = exception
        })

    // Ensure all asynchronous operations complete
    shadowOf(Looper.getMainLooper()).idle()

    // Verify that the update on user's friendRequests was attempted
    verify(mockUserDocumentReference).update(eq("friendRequests"), any())
    // Verify that onFailure was called
    assertTrue(onFailureCalled)
    assertNotNull(failureException)
    assertEquals("Update user friendRequests failed", failureException?.message)
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

    val mockFriendDocumentSnapshot = mock(DocumentSnapshot::class.java)

    // Mock the data returned by the document snapshots
    `when`(mockFriendDocumentSnapshot.data).thenReturn(mapOf("friendRequests" to listOf<String>()))
    `when`(mockUserDocumentSnapshot.data)
        .thenReturn(mapOf("sentFriendRequests" to listOf<String>()))

    val mockFriendDocumentReference = mock(DocumentReference::class.java)

    `when`(mockUserCollectionReference.document(user.uid)).thenReturn(mockUserDocumentReference)
    `when`(mockUserCollectionReference.document(friend.uid)).thenReturn(mockFriendDocumentReference)

    // Mock the get() method to return the document snapshots
    `when`(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
    `when`(mockFriendDocumentReference.get())
        .thenReturn(Tasks.forResult(mockFriendDocumentSnapshot))

    // Mock the update() method to return a successful task
    `when`(mockUserDocumentReference.update(anyString(), any())).thenReturn(Tasks.forResult(null))
    `when`(mockFriendDocumentReference.update(anyString(), any())).thenReturn(Tasks.forResult(null))

    // Call the createFriendRequest method
    userRepositoryFirestore.createFriendRequest(
        user.uid,
        friend.uid,
        {
          // Verify the updates to the friend document
          verify(mockFriendDocumentReference).update("friendRequests", listOf(user.uid))

          // Verify the updates to the user document
          verify(mockUserDocumentReference).update("sentFriendRequests", listOf(friend.uid))
        },
        { fail("onFailure should not be called") })
  }
}
