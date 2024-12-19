package com.swent.suddenbump.model.user.userRepositoryFirestoreTests

import android.content.Context
import android.content.SharedPreferences
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
import com.swent.suddenbump.model.image.ImageRepository
import com.swent.suddenbump.model.user.SharedPreferencesManager
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import com.swent.suddenbump.model.user.UserRepositoryFirestoreHelper
import com.swent.suddenbump.worker.WorkerScheduler
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import kotlinx.coroutines.flow.MutableStateFlow
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
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ShareLocationTests {

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

  @Mock private lateinit var mockFriendDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var sharedPreferences: SharedPreferences
  @Mock private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

  private lateinit var userRepositoryFirestore: UserRepositoryFirestore

  val snapshot1: DocumentSnapshot = mock(DocumentSnapshot::class.java)
  val snapshot2: DocumentSnapshot = mock(DocumentSnapshot::class.java)

  private val location =
      MutableStateFlow(
          Location("mock_provider").apply {
            latitude = 0.0
            longitude = 0.0
          })
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
  fun getSharedByFriends_callsDocumentSnapshotToUserList() {
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
    `when`(mockUserDocumentSnapshot.data).thenReturn(mapOf("locationSharedBy" to listOf(user)))

    userRepositoryFirestore.getSharedByFriends(
        uid = user.uid,
        onSuccess = {},
        onFailure = { fail("Failure callback should not be called") })

    verify(mocked).addOnSuccessListener(any())
  }

  @Test
  fun getSharedWithFriends_callsDocumentSnapshotToUserList() {
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
    `when`(mockUserDocumentSnapshot.data).thenReturn(mapOf("locationSharedWith" to listOf(user)))

    userRepositoryFirestore.getSharedWithFriends(
        uid = user.uid,
        onSuccess = {},
        onFailure = { fail("Failure callback should not be called") })

    verify(mocked).addOnSuccessListener(any())
  }

  @Test
  fun shareLocationWithFriendCallsFirestore() {
    `when`(mockUserCollectionReference.get()).thenReturn(Tasks.forResult(mockUserQuerySnapshot))
    `when`(mockUserQuerySnapshot.documents).thenReturn(listOf())

    `when`(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))

    userRepositoryFirestore.shareLocationWithFriend(
        user.uid,
        "1",
        onSuccess = {},
        onFailure = { fail("Failure callback should not be called") })

    verify(timeout(100)) { (mockUserQuerySnapshot).documents }
  }

  @Test
  fun shareLocationWithFriendSucceeds() {
    val user =
        User(
            "1",
            "Deyan",
            "Marinov",
            "+33613507628",
            null,
            "deyan.marinov@epfl.ch",
            lastKnownLocation = location)
    val friend =
        User(
            "2",
            "Dan-Costin",
            "Ruicu",
            "+41774618762",
            null,
            "dan-costin.ruicu@epfl.ch",
            lastKnownLocation = location)

    // Mock the data returned by the document snapshots
    `when`(mockUserDocumentSnapshot.data)
        .thenReturn(mapOf("locationSharedWith" to emptyList<String>()))
    `when`(mockFriendDocumentSnapshot.data)
        .thenReturn(mapOf("locationSharedBy" to emptyList<String>()))

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
    userRepositoryFirestore.shareLocationWithFriend(
        user.uid,
        friend.uid,
        {
          // Verify the updates to the user document
          verify(mockUserDocumentReference).update("locationSharedWith", listOf(friend.uid))

          // Verify the updates to the friend document
          verify(mockFriendDocumentReference).update("locationSharedBy", listOf(user.uid))
        },
        { fail("onFailure should not be called") })
  }

  @Test
  fun shareLocationWithFriendSuccessfullySharesLocation() {
    val uid = "user123"
    val fid = "friend123"
    val sharedFriendsUidList = emptyList<String>()
    val sharedByFriendsUidList = emptyList<String>()

    val mockUidDocumentReference: DocumentReference = mock()
    val mockFidDocumentReference: DocumentReference = mock()

    val mockUidDocumentSnapshot: DocumentSnapshot = mock()
    val mockFidDocumentSnapshot: DocumentSnapshot = mock()

    val mockSuccessTask: Task<Void> = Tasks.forResult(null)

    `when`(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    `when`(mockUserCollectionReference.document(uid)).thenReturn(mockUidDocumentReference)
    `when`(mockUserCollectionReference.document(fid)).thenReturn(mockFidDocumentReference)

    `when`(mockUidDocumentReference.get()).thenReturn(Tasks.forResult(mockUidDocumentSnapshot))
    `when`(mockUidDocumentSnapshot.data)
        .thenReturn(mapOf("locationSharedWith" to sharedFriendsUidList))

    `when`(mockFidDocumentReference.get()).thenReturn(Tasks.forResult(mockFidDocumentSnapshot))
    `when`(mockFidDocumentSnapshot.data)
        .thenReturn(mapOf("locationSharedBy" to sharedByFriendsUidList))

    `when`(mockUidDocumentReference.update(eq("locationSharedWith"), any()))
        .thenReturn(mockSuccessTask)
    `when`(mockFidDocumentReference.update(eq("locationSharedBy"), any()))
        .thenReturn(mockSuccessTask)

    var successCalled = false
    userRepositoryFirestore.shareLocationWithFriend(
        uid, fid, { successCalled = true }, { fail("Should not fail") })

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(successCalled)
    verify(mockUidDocumentReference).update(eq("locationSharedWith"), eq(listOf(fid)))
    verify(mockFidDocumentReference).update(eq("locationSharedBy"), eq(listOf(uid)))
  }

  @Test
  fun shareLocationWithFriendFailsIfFriendAlreadyHasAccess() {
    val uid = "user123"
    val fid = "friend123"
    val sharedFriendsUidList = listOf(fid)
    val sharedByFriendsUidList = listOf(uid)

    val mockUidDocumentReference: DocumentReference = mock()
    val mockFidDocumentReference: DocumentReference = mock()

    val mockUidDocumentSnapshot: DocumentSnapshot = mock()
    val mockFidDocumentSnapshot: DocumentSnapshot = mock()

    `when`(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    `when`(mockUserCollectionReference.document(uid)).thenReturn(mockUidDocumentReference)
    `when`(mockUserCollectionReference.document(fid)).thenReturn(mockFidDocumentReference)

    `when`(mockUidDocumentReference.get()).thenReturn(Tasks.forResult(mockUidDocumentSnapshot))
    `when`(mockUidDocumentSnapshot.data)
        .thenReturn(mapOf("locationSharedWith" to sharedFriendsUidList))

    `when`(mockFidDocumentReference.get()).thenReturn(Tasks.forResult(mockFidDocumentSnapshot))
    `when`(mockFidDocumentSnapshot.data)
        .thenReturn(mapOf("locationSharedBy" to sharedByFriendsUidList))

    var errorCalled = false
    userRepositoryFirestore.shareLocationWithFriend(
        uid, fid, { fail("Should not succeed") }, { errorCalled = true })

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(errorCalled)
    verify(mockUidDocumentReference, never()).update(anyString(), any())
    verify(mockFidDocumentReference, never()).update(anyString(), any())
  }

  @Test
  fun shareLocationWithFriendFailsWhenFriendAlreadyHasAccess() {
    val uid = "user123"
    val fid = "friend123"
    val sharedFriendsUidList = listOf(fid)
    val sharedByFriendsUidList = listOf(uid)

    `when`(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    `when`(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)
    `when`(mockUserCollectionReference.document(fid)).thenReturn(mockUserDocumentReference)
    `when`(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
    `when`(mockUserDocumentSnapshot.data)
        .thenReturn(
            mapOf(
                "locationSharedWith" to sharedFriendsUidList,
                "locationSharedBy" to sharedByFriendsUidList))

    userRepositoryFirestore.shareLocationWithFriend(uid, fid, {}, {})

    verify(mockUserDocumentReference, never()).update(anyString(), any())
  }

  @Test
  fun stopSharingLocationWithFriendSuccessfullyStopsSharing() {
    val uid = "user123"
    val fid = "friend123"
    val sharedFriendsUidList = listOf(fid)
    val sharedByFriendsUidList = listOf(uid)

    val mockUidDocumentReference: DocumentReference = mock()
    val mockFidDocumentReference: DocumentReference = mock()

    val mockUidDocumentSnapshot: DocumentSnapshot = mock()
    val mockFidDocumentSnapshot: DocumentSnapshot = mock()

    val mockSuccessTask: Task<Void> = Tasks.forResult(null)

    `when`(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    `when`(mockUserCollectionReference.document(uid)).thenReturn(mockUidDocumentReference)
    `when`(mockUserCollectionReference.document(fid)).thenReturn(mockFidDocumentReference)

    `when`(mockUidDocumentReference.get()).thenReturn(Tasks.forResult(mockUidDocumentSnapshot))
    `when`(mockUidDocumentSnapshot.data)
        .thenReturn(mapOf("locationSharedWith" to sharedFriendsUidList))

    `when`(mockFidDocumentReference.get()).thenReturn(Tasks.forResult(mockFidDocumentSnapshot))
    `when`(mockFidDocumentSnapshot.data)
        .thenReturn(mapOf("locationSharedBy" to sharedByFriendsUidList))

    `when`(mockUidDocumentReference.update(eq("locationSharedWith"), any()))
        .thenReturn(mockSuccessTask)
    `when`(mockFidDocumentReference.update(eq("locationSharedBy"), any()))
        .thenReturn(mockSuccessTask)

    userRepositoryFirestore.stopSharingLocationWithFriend(uid, fid, {}, {})

    // Ensure all async tasks complete
    shadowOf(Looper.getMainLooper()).idle()

    verify(mockUidDocumentReference).update(eq("locationSharedWith"), eq(listOf<String>()))
    verify(mockFidDocumentReference).update(eq("locationSharedBy"), eq(listOf<String>()))
  }

  @Test
  fun stopSharingLocationWithFriendFailsWhenFriendDoesNotHaveAccess() {
    val uid = "user123"
    val fid = "friend123"
    val sharedFriendsUidList = listOf<String>()
    val sharedByFriendsUidList = listOf<String>()

    `when`(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    `when`(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)
    `when`(mockUserCollectionReference.document(fid)).thenReturn(mockUserDocumentReference)
    `when`(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
    `when`(mockUserDocumentSnapshot.data)
        .thenReturn(
            mapOf(
                "locationSharedWith" to sharedFriendsUidList,
                "locationSharedBy" to sharedByFriendsUidList))

    userRepositoryFirestore.stopSharingLocationWithFriend(uid, fid, {}, {})

    shadowOf(Looper.getMainLooper()).idle()
    verify(mockUserDocumentReference, never()).update(anyString(), any())
  }

  @Test
  fun getSharedByFriendsReturnsCorrectList() {
    val uid = "user123"
    val sharedByFriendsUidList = listOf("friend123", "friend456")

    `when`(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    `when`(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)
    `when`(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
    `when`(mockUserDocumentSnapshot.data)
        .thenReturn(mapOf("locationSharedBy" to sharedByFriendsUidList))

    userRepositoryFirestore.getSharedByFriends(uid, {}, {})

    verify(mockUserDocumentReference).get()
  }

  @Test
  fun getSharedByFriendsSuccessfullyRetrievesUsers() {
    // Mock friends
    val friend1 =
        User(
            uid = "friend1",
            firstName = "Alice",
            lastName = "Smith",
            phoneNumber = "123456789",
            profilePicture = null,
            emailAddress = "alice.smith@example.com",
            lastKnownLocation = location)

    val friend2 =
        User(
            uid = "friend2",
            firstName = "Bob",
            lastName = "Brown",
            phoneNumber = "987654321",
            profilePicture = null,
            emailAddress = "bob.brown@example.com",
            lastKnownLocation = location)

    // Mock sharedBy UIDs
    val sharedByUidList = listOf(friend1.uid, friend2.uid)

    // Mock document snapshots for each friend
    val userSnapshot = mock(DocumentSnapshot::class.java)
    val friend1Snapshot = mock(DocumentSnapshot::class.java)
    val friend2Snapshot = mock(DocumentSnapshot::class.java)

    `when`(mockUserDocumentSnapshot.data).thenReturn(mapOf("locationSharedBy" to sharedByUidList))

    // Mock document references for each friend
    val friend1DocumentRef = mock(DocumentReference::class.java)
    val friend2DocumentRef = mock(DocumentReference::class.java)

    `when`(mockFirestore.collection("Users").document(friend1.uid)).thenReturn(friend1DocumentRef)
    `when`(friend1DocumentRef.get()).thenReturn(Tasks.forResult(friend1Snapshot))

    `when`(mockFirestore.collection("Users").document(friend2.uid)).thenReturn(friend2DocumentRef)
    `when`(friend2DocumentRef.get()).thenReturn(Tasks.forResult(friend2Snapshot))

    `when`(friend1Snapshot.exists()).thenReturn(true)
    `when`(friend2Snapshot.exists()).thenReturn(true)

    // Mock documentSnapshotToUser behavior
    val helper = mock(UserRepositoryFirestoreHelper::class.java)
    `when`(helper.documentSnapshotToUser(friend1Snapshot, null)).thenReturn(friend1)
    `when`(helper.documentSnapshotToUser(friend2Snapshot, null)).thenReturn(friend2)

    // Inject the mock helper into the repository
    val helperField = UserRepositoryFirestore::class.java.getDeclaredField("helper")
    helperField.isAccessible = true
    helperField.set(userRepositoryFirestore, helper)

    // Prepare tasks for whenAllSuccess
    val friendTasks = listOf(friend1DocumentRef.get(), friend2DocumentRef.get())

    // Mock Tasks.whenAllSuccess behavior
    `when`(Tasks.whenAllSuccess<DocumentSnapshot>(friendTasks))
        .thenReturn(Tasks.forResult(listOf(friend1Snapshot, friend2Snapshot)))

    // Invoke the method under test
    userRepositoryFirestore.getSharedByFriends(
        uid = "user123",
        onSuccess = { result ->
          // Verify the result
          assert(result.size == 2)
          assert(result.contains(friend1))
          assert(result.contains(friend2))
        },
        onFailure = { assert(false) { "Should not fail on success" } })
  }

  @Test
  fun getSharedByFriendsReturnsEmptyListWhenNoData() {
    val uid = "user123"

    // Mock Firestore document snapshot for the user
    val mockUserSnapshot = mock(DocumentSnapshot::class.java)
    `when`(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserSnapshot))

    // Mock the data to simulate no "locationSharedBy" field
    `when`(mockUserSnapshot.data).thenReturn(emptyMap())

    userRepositoryFirestore.getSharedByFriends(
        uid = uid,
        onSuccess = { result ->
          // Assert that the result is an empty list
          assertNotNull(result)
          assertTrue(result.isEmpty())
        },
        onFailure = { fail("Should not fail when no data is available") })

    // Ensure all async tasks complete
    shadowOf(Looper.getMainLooper()).idle()

    // Verify that the Firestore document reference's `get` method was called
    verify(mockUserDocumentReference).get()
  }

  @Test
  fun getSharedByFriendsFailsWhenFirestoreFails() {
    val uid = "user123"
    val exception = Exception("Firestore error")

    // Mock Firestore collection and document references
    `when`(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    `when`(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)

    // Mock a failing Firestore Task
    `when`(mockUserDocumentReference.get()).thenReturn(Tasks.forException(exception))

    var failureResult: Exception? = null

    userRepositoryFirestore.getSharedByFriends(
        uid, { fail("Should not succeed") }, { failureResult = it })

    shadowOf(Looper.getMainLooper()).idle()

    assertNotNull(failureResult)
    assertEquals(exception.message, failureResult!!.message)
    verify(mockUserDocumentReference).get()
  }

  @Test
  fun getSharedWithFriendsReturnsCorrectList() {
    val uid = "user123"
    val sharedWithFriendsUidList = listOf("friend123", "friend456")

    `when`(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    `when`(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)
    `when`(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
    `when`(mockUserDocumentSnapshot.data)
        .thenReturn(mapOf("locationSharedWith" to sharedWithFriendsUidList))

    userRepositoryFirestore.getSharedWithFriends(uid, {}, {})

    verify(mockUserDocumentReference).get()
  }

  @Test
  fun getSharedWithFriendsSuccessfullyRetrievesUsers() {
    // Mock friends
    val friend1 =
        User(
            uid = "friend1",
            firstName = "Alice",
            lastName = "Smith",
            phoneNumber = "123456789",
            profilePicture = null,
            emailAddress = "alice.smith@example.com",
            lastKnownLocation = location)

    val friend2 =
        User(
            uid = "friend2",
            firstName = "Bob",
            lastName = "Brown",
            phoneNumber = "987654321",
            profilePicture = null,
            emailAddress = "bob.brown@example.com",
            lastKnownLocation = location)

    // Mock sharedWith UIDs
    val sharedWithUidList = listOf(friend1.uid, friend2.uid)

    // Mock document snapshots for each friend
    val friend1Snapshot = mock(DocumentSnapshot::class.java)
    val friend2Snapshot = mock(DocumentSnapshot::class.java)

    `when`(mockUserDocumentSnapshot.data)
        .thenReturn(mapOf("locationSharedWith" to sharedWithUidList))

    // Mock document references for each friend
    val friend1DocumentRef = mock(DocumentReference::class.java)
    val friend2DocumentRef = mock(DocumentReference::class.java)

    `when`(mockFirestore.collection("Users").document(friend1.uid)).thenReturn(friend1DocumentRef)
    `when`(friend1DocumentRef.get()).thenReturn(Tasks.forResult(friend1Snapshot))

    `when`(mockFirestore.collection("Users").document(friend2.uid)).thenReturn(friend2DocumentRef)
    `when`(friend2DocumentRef.get()).thenReturn(Tasks.forResult(friend2Snapshot))

    `when`(friend1Snapshot.exists()).thenReturn(true)
    `when`(friend2Snapshot.exists()).thenReturn(true)

    // Mock documentSnapshotToUser behavior
    val helper = mock(UserRepositoryFirestoreHelper::class.java)
    `when`(helper.documentSnapshotToUser(friend1Snapshot, null)).thenReturn(friend1)
    `when`(helper.documentSnapshotToUser(friend2Snapshot, null)).thenReturn(friend2)

    // Inject the mock helper into the repository
    val helperField = UserRepositoryFirestore::class.java.getDeclaredField("helper")
    helperField.isAccessible = true
    helperField.set(userRepositoryFirestore, helper)

    // Prepare tasks for whenAllSuccess
    val friendTasks = listOf(friend1DocumentRef.get(), friend2DocumentRef.get())

    // Mock Tasks.whenAllSuccess behavior
    `when`(Tasks.whenAllSuccess<DocumentSnapshot>(friendTasks))
        .thenReturn(Tasks.forResult(listOf(friend1Snapshot, friend2Snapshot)))

    // Invoke the method under test
    userRepositoryFirestore.getSharedWithFriends(
        uid = "user123",
        onSuccess = { result ->
          // Verify the result
          assert(result.size == 2)
          assert(result.contains(friend1))
          assert(result.contains(friend2))
        },
        onFailure = { assert(false) { "Should not fail on success" } })
  }

  @Test
  fun getSharedWithFriendsReturnsEmptyListWhenNoData() {
    val uid = "user123"

    // Mock document snapshot for the user
    val mockUserSnapshot = mock(DocumentSnapshot::class.java)
    `when`(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserSnapshot))

    // Mock the data to simulate no "locationSharedWith" field
    `when`(mockUserSnapshot.data).thenReturn(emptyMap())

    userRepositoryFirestore.getSharedWithFriends(
        uid = uid,
        onSuccess = { result ->
          assertNotNull(result)
          assertTrue(result.isEmpty())
        },
        onFailure = { fail("Should not fail when no data is available") })

    shadowOf(Looper.getMainLooper()).idle()
  }
}
