package com.swent.suddenbump.model.user.userRepositoryFirestoreTests

import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.OnSuccessListener
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
import com.google.firebase.firestore.Transaction
import com.swent.suddenbump.model.image.ImageRepository
import com.swent.suddenbump.model.user.SharedPreferencesManager
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import com.swent.suddenbump.worker.WorkerScheduler
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class BlockedUserTests {

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

  @Mock private lateinit var mockUserQuerySnapshot: QuerySnapshot

  @Mock private lateinit var mockPhoneQuerySnapshot: QuerySnapshot

  @Mock private lateinit var mockFirebaseAuth: FirebaseAuth

  @Mock private lateinit var mockFirebaseUser: FirebaseUser

  @Mock private lateinit var firebaseAuthMockStatic: MockedStatic<FirebaseAuth>

  @Mock private lateinit var mockTaskVoid: Task<Void>

  @Mock private lateinit var mockImageRepository: ImageRepository

  @Mock private lateinit var mockFriendDocumentReference: DocumentReference

  @Mock private lateinit var mockContext: Context

  @Mock private lateinit var mockFriendDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var transaction: Transaction

  private lateinit var userRepositoryFirestore: UserRepositoryFirestore

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

    mockContext = mock(Context::class.java)

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
  fun blockUser_shouldUpdateAllLists() {
    val currentUser =
        User(
            "1",
            "Alexandre",
            "Carel",
            "+33 6 59 20 70 02",
            null,
            "alexandre.carel@epfl.ch",
            lastKnownLocation = location)
    val blockedUser =
        User(
            "2",
            "Jane",
            "Doe",
            "+41 00 000 00 02",
            null,
            "jane.doe@example.com",
            lastKnownLocation = location)

    val blockedUserSnapshot = mock(DocumentSnapshot::class.java)

    // Mock the initial data for both users
    val currentUserData =
        mutableMapOf<String, Any>(
            "blockedList" to listOf<String>(),
            "friendsList" to listOf(blockedUser.uid),
            "friendRequests" to listOf<String>(),
            "sentFriendRequests" to listOf<String>())
    val blockedUserData =
        mutableMapOf<String, Any>(
            "friendsList" to listOf(currentUser.uid),
            "friendRequests" to listOf<String>(),
            "sentFriendRequests" to listOf<String>())
    `when`(mockUserDocumentSnapshot.data).thenReturn(currentUserData)
    `when`(blockedUserSnapshot.data).thenReturn(blockedUserData)

    val blockedUserRef = mock(DocumentReference::class.java)
    `when`(mockUserCollectionReference.document(currentUser.uid))
        .thenReturn(mockUserDocumentReference)
    `when`(mockUserCollectionReference.document(blockedUser.uid)).thenReturn(blockedUserRef)

    // Mock transaction behavior
    `when`(transaction.get(mockUserDocumentReference)).thenReturn(mockUserDocumentSnapshot)
    `when`(transaction.get(blockedUserRef)).thenReturn(blockedUserSnapshot)

    // Mock runTransaction
    `when`(mockFirestore.runTransaction<Void>(any())).thenAnswer { invocation ->
      val transactionFunction = invocation.arguments[0]
      when (transactionFunction) {
        is com.google.firebase.firestore.Transaction.Function<*> -> {
          transactionFunction.apply(transaction)
        }
      }
      Tasks.forResult(null)
    }

    var onSuccessCalled = false
    var onFailureCalled = false

    // Call blockUser
    userRepositoryFirestore.blockUser(
        currentUser,
        blockedUser,
        onSuccess = { onSuccessCalled = true },
        onFailure = { onFailureCalled = true })

    // Ensure all asynchronous operations complete
    shadowOf(Looper.getMainLooper()).idle()

    // Verify the transaction updates
    verify(transaction)
        .update(eq(mockUserDocumentReference), eq("blockedList"), eq(listOf(blockedUser.uid)))
    verify(transaction)
        .update(eq(mockUserDocumentReference), eq("friendsList"), eq(emptyList<String>()))
    verify(transaction)
        .update(eq(mockUserDocumentReference), eq("friendRequests"), eq(emptyList<String>()))
    verify(transaction)
        .update(eq(mockUserDocumentReference), eq("sentFriendRequests"), eq(emptyList<String>()))

    verify(transaction).update(eq(blockedUserRef), eq("friendsList"), eq(emptyList<String>()))
    verify(transaction).update(eq(blockedUserRef), eq("friendRequests"), eq(emptyList<String>()))
    verify(transaction)
        .update(eq(blockedUserRef), eq("sentFriendRequests"), eq(emptyList<String>()))

    // Verify callbacks
    assertTrue(onSuccessCalled)
    assertFalse(onFailureCalled)
  }

  @Test
  fun blockUser_whenTransactionFails_shouldCallOnFailure() {
    val currentUser =
        User(
            "1",
            "Alexandre",
            "Carel",
            "+33 6 59 20 70 02",
            null,
            "alexandre.carel@epfl.ch",
            lastKnownLocation = location)
    val blockedUser =
        User(
            "2",
            "Jane",
            "Doe",
            "+41 00 000 00 02",
            null,
            "jane.doe@example.com",
            lastKnownLocation = location)

    // Mock runTransaction to fail
    val exception = Exception("Transaction failed")
    `when`(mockFirestore.runTransaction<Void>(any())).thenReturn(Tasks.forException(exception))

    var onSuccessCalled = false
    var onFailureCalled = false
    var failureException: Exception? = null

    // Call blockUser
    userRepositoryFirestore.blockUser(
        currentUser,
        blockedUser,
        onSuccess = { onSuccessCalled = true },
        onFailure = {
          onFailureCalled = true
          failureException = it
        })

    // Ensure all asynchronous operations complete
    shadowOf(Looper.getMainLooper()).idle()

    // Verify callbacks
    assertFalse(onSuccessCalled)
    assertTrue(onFailureCalled)
    assertEquals(exception, failureException)
  }

  @Test
  fun unblockUser_Success() {
    // Mock document references
    val currentUserRef = mock(DocumentReference::class.java)
    val currentUserSnapshot = mock(DocumentSnapshot::class.java)

    // Mock initial blocked list
    val blockedList = listOf("blocked_user_id")
    `when`(currentUserSnapshot.get("blockedList")).thenReturn(blockedList)

    // Mock Firestore interactions
    `when`(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    `when`(mockUserCollectionReference.document("current_user_id")).thenReturn(currentUserRef)
    `when`(currentUserRef.get()).thenReturn(Tasks.forResult(currentUserSnapshot))
    `when`(currentUserRef.update("blockedList", emptyList<String>()))
        .thenReturn(Tasks.forResult(null))

    var onSuccessCalled = false
    var onFailureCalled = false

    // Call unblockUser
    userRepositoryFirestore.unblockUser(
        "current_user_id",
        "blocked_user_id",
        onSuccess = { onSuccessCalled = true },
        onFailure = { onFailureCalled = true })

    // Ensure all asynchronous operations complete
    shadowOf(Looper.getMainLooper()).idle()

    // Verify success
    assertTrue(onSuccessCalled)
    assertFalse(onFailureCalled)
    verify(currentUserRef).update("blockedList", emptyList<String>())
  }

  @Test
  fun unblockUser_FailureOnGet() {
    // Mock document references
    val currentUserRef = mock(DocumentReference::class.java)
    val exception = Exception("Failed to get document")

    // Mock Firestore interactions with failure
    `when`(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    `when`(mockUserCollectionReference.document("current_user_id")).thenReturn(currentUserRef)
    `when`(currentUserRef.get()).thenReturn(Tasks.forException(exception))

    var onSuccessCalled = false
    var onFailureCalled = false
    var failureException: Exception? = null

    // Call unblockUser
    userRepositoryFirestore.unblockUser(
        "current_user_id",
        "blocked_user_id",
        onSuccess = { onSuccessCalled = true },
        onFailure = { e ->
          onFailureCalled = true
          failureException = e
        })

    // Ensure all asynchronous operations complete
    shadowOf(Looper.getMainLooper()).idle()

    // Verify failure
    assertFalse(onSuccessCalled)
    assertTrue(onFailureCalled)
    assertEquals(exception, failureException)
  }

  @Test
  fun unblockUser_FailureOnUpdate() {
    // Mock document references
    val currentUserRef = mock(DocumentReference::class.java)
    val currentUserSnapshot = mock(DocumentSnapshot::class.java)
    val exception = Exception("Failed to update document")

    // Mock initial blocked list
    val blockedList = listOf("blocked_user_id")
    `when`(currentUserSnapshot.get("blockedList")).thenReturn(blockedList)

    // Mock Firestore interactions with update failure
    `when`(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    `when`(mockUserCollectionReference.document("current_user_id")).thenReturn(currentUserRef)
    `when`(currentUserRef.get()).thenReturn(Tasks.forResult(currentUserSnapshot))
    `when`(currentUserRef.update("blockedList", emptyList<String>()))
        .thenReturn(Tasks.forException(exception))

    var onSuccessCalled = false
    var onFailureCalled = false
    var failureException: Exception? = null

    // Call unblockUser
    userRepositoryFirestore.unblockUser(
        "current_user_id",
        "blocked_user_id",
        onSuccess = { onSuccessCalled = true },
        onFailure = { e ->
          onFailureCalled = true
          failureException = e
        })

    // Ensure all asynchronous operations complete
    shadowOf(Looper.getMainLooper()).idle()

    // Verify failure
    assertFalse(onSuccessCalled)
    assertTrue(onFailureCalled)
    assertEquals(exception, failureException)
  }

  /**
   * This test verifies that when we create a new User account, the Firestore `set()` is called on
   * the document reference. This does NOT CHECK the actual data being added
   */
  @Test
  fun setUserBlockedFriends_shouldCallFirestoreCollection() {
    `when`(mockUserDocumentReference.update(anyString(), any())).thenReturn(mockTaskVoid)

    userRepositoryFirestore.setBlockedFriends(
        uid = user.uid, blockedFriendsList = listOf(user), onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockUserDocumentReference).update(anyString(), any())
  }

  @Test
  fun getUserBlockedFriends_callsDocumentSnapshotToUserList() {
    val mocked = mock(Task::class.java) as Task<DocumentSnapshot>
    `when`(mockUserCollectionReference.document(any())).thenReturn(mockUserDocumentReference)
    `when`(mockUserDocumentReference.get()).thenReturn(mocked)
    `when`(mocked.isSuccessful).thenReturn(true)
    `when`(mocked.isCanceled).thenReturn(false)
    `when`(mocked.addOnFailureListener(any())).thenReturn(mocked)
    doAnswer { invocation ->
          val listener = invocation.arguments[0] as OnSuccessListener<DocumentSnapshot>
          listener.onSuccess(mockUserDocumentSnapshot)
          mocked
        }
        .`when`(mocked)
        .addOnSuccessListener(any())
    `when`(mocked.result).thenReturn(mockUserDocumentSnapshot)
    `when`(mockUserDocumentSnapshot.data).thenReturn(mapOf("blockedList" to listOf(user)))

    userRepositoryFirestore.getBlockedFriends(
        uid = user.uid,
        onSuccess = {},
        onFailure = { fail("Failure callback should not be called") })

    verify(mocked).addOnSuccessListener(any())
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
        uid = user.uid,
        onSuccess = {},
        onFailure = { fail("Failure callback should not be called") })

    verify(timeout(100)) { (mockUserQuerySnapshot).documents }
  }
}
