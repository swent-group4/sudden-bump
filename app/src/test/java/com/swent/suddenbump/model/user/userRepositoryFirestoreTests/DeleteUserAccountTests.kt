package com.swent.suddenbump.model.user.userRepositoryFirestoreTests

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Looper
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
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.StorageReference
import com.swent.suddenbump.model.image.ImageRepository
import com.swent.suddenbump.model.user.SharedPreferencesManager
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import com.swent.suddenbump.worker.WorkerScheduler
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert
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
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class DeleteUserAccountTests {

  @Mock private lateinit var mockFirestore: FirebaseFirestore

  @Mock private lateinit var mockSharedPreferencesManager: SharedPreferencesManager

  @Mock private lateinit var mockWorkerScheduler: WorkerScheduler

  @Mock private lateinit var mockUserCollectionReference: CollectionReference

  @Mock private lateinit var mockEmailCollectionReference: CollectionReference

  @Mock private lateinit var mockPhoneCollectionReference: CollectionReference

  @Mock private lateinit var mockChatCollectionReference: CollectionReference

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
    `when`(
            mockContext.getSharedPreferences(
                anyString(), org.mockito.kotlin.eq(Context.MODE_PRIVATE)))
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
    `when`(mockTaskVoid.addOnSuccessListener(org.mockito.kotlin.any())).thenReturn(mockTaskVoid)
    `when`(mockTaskVoid.addOnCompleteListener(org.mockito.kotlin.any())).thenReturn(mockTaskVoid)
    `when`(mockTaskVoid.addOnFailureListener(org.mockito.kotlin.any())).thenReturn(mockTaskVoid)

    `when`(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    `when`(mockUserCollectionReference.document(org.mockito.kotlin.any()))
        .thenReturn(mockUserDocumentReference)
    `when`(mockUserCollectionReference.document()).thenReturn(mockUserDocumentReference)
    `when`(mockUserDocumentReference.set(org.mockito.kotlin.any())).thenReturn(mockTaskVoid)
    `when`(mockUserDocumentReference.update(org.mockito.kotlin.any())).thenReturn(mockTaskVoid)
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
    `when`(mockEmailCollectionReference.document(org.mockito.kotlin.any()))
        .thenReturn(mockEmailDocumentReference)
    `when`(mockEmailCollectionReference.document()).thenReturn(mockEmailDocumentReference)
    `when`(mockEmailDocumentReference.id).thenReturn("alexandre.carel@epfl.ch")
    `when`(mockEmailDocumentSnapshot.data).thenReturn(mapOf("uid" to "1"))

    `when`(mockFirestore.collection("Phones")).thenReturn(mockPhoneCollectionReference)
    `when`(mockPhoneCollectionReference.document(org.mockito.kotlin.any()))
        .thenReturn(mockPhoneDocumentReference)
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
  fun deleteUserAccount_allDeletionsSuccessful_callsOnSuccess() {
    // Arrange
    val uid = "test_uid"
    val phoneNumber = "+123456789"
    val emailAddress = "test@example.com"

    // Mock user data
    val userData = mapOf("phoneNumber" to phoneNumber, "emailAddress" to emailAddress)
    whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
    whenever(mockUserDocumentSnapshot.data).thenReturn(userData)

    // Mock deletion tasks for phone and email documents
    val deletePhoneTask = Tasks.forResult<Void>(null)
    val deleteEmailTask = Tasks.forResult<Void>(null)
    whenever(mockPhoneCollectionReference.document(phoneNumber).delete())
        .thenReturn(deletePhoneTask)
    whenever(mockEmailCollectionReference.document(emailAddress).delete())
        .thenReturn(deleteEmailTask)

    // Mock profile picture deletion
    val profilePicturesRefField =
        UserRepositoryFirestore::class.java.getDeclaredField("profilePicturesRef")
    profilePicturesRefField.isAccessible = true
    val mockProfilePicturesRef = mock(StorageReference::class.java)
    profilePicturesRefField.set(userRepositoryFirestore, mockProfilePicturesRef)
    val mockProfilePictureRef = mock(StorageReference::class.java)
    whenever(mockProfilePicturesRef.child("$uid.jpeg")).thenReturn(mockProfilePictureRef)
    whenever(mockProfilePictureRef.delete()).thenReturn(Tasks.forResult<Void>(null))

    // Mock chats query and deletion
    val chatsCollectionRef = mock(CollectionReference::class.java)
    whenever(mockFirestore.collection("chats")).thenReturn(chatsCollectionRef)
    val chatsQuery = mock(Query::class.java)
    whenever(chatsCollectionRef.whereArrayContains("participants", uid)).thenReturn(chatsQuery)

    val chatSnapshot = mock(QuerySnapshot::class.java)
    whenever(chatsQuery.get()).thenReturn(Tasks.forResult(chatSnapshot))
    whenever(chatSnapshot.documents).thenReturn(emptyList()) // no chats to delete

    // Mock user document deletion
    whenever(mockUserDocumentReference.delete()).thenReturn(Tasks.forResult<Void>(null))

    var onSuccessCalled = false
    var onFailureCalled = false

    // Act
    userRepositoryFirestore.deleteUserAccount(
        uid = uid, onSuccess = { onSuccessCalled = true }, onFailure = { onFailureCalled = true })

    // Assert
    shadowOf(Looper.getMainLooper()).idle()
    assertTrue(onSuccessCalled)
    assertFalse(onFailureCalled)

    org.mockito.kotlin.verify(mockUserDocumentReference).get()
    org.mockito.kotlin.verify(mockPhoneCollectionReference.document(phoneNumber)).delete()
    org.mockito.kotlin.verify(mockEmailCollectionReference.document(emailAddress)).delete()
    org.mockito.kotlin.verify(mockUserDocumentReference).delete()
  }

  @Test
  fun deleteUserAccount_failureOnGettingUserDoc_callsOnFailure() {

    whenever(mockFirestore.collection("chats")).thenReturn(mockChatCollectionReference)
    // Arrange
    val uid = "test_uid"
    val exception = Exception("User not found")

    whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forException(exception))

    var onSuccessCalled = false
    var onFailureCalled = false
    var capturedException: Exception? = null

    // Act
    userRepositoryFirestore.deleteUserAccount(
        uid = uid,
        onSuccess = { onSuccessCalled = true },
        onFailure = {
          onFailureCalled = true
          capturedException = it
        })

    // Assert
    shadowOf(Looper.getMainLooper()).idle()
    assertFalse(onSuccessCalled)
    assertTrue(onFailureCalled)
    Assert.assertEquals("User not found", capturedException?.message)
  }

  @Test
  fun deleteUserAccount_failureOnDeletingPhoneDocument_callsOnFailure() {
    var onSuccessCalled = false
    var onFailureCalled = false
    var capturedException: Exception? = null

    val mockDeleteDocument = mock(DocumentReference::class.java)
    val mockQuery = mock(Query::class.java)
    val mockQuerySnapshot = mock(Task::class.java) as Task<QuerySnapshot>
    val mockException = mock(Exception::class.java)

    `when`(mockException.message).thenReturn("Failed to delete phone doc")

    whenever(mockFirestore.collection("chats")).thenReturn(mockChatCollectionReference)
    `when`(mockEmailCollectionReference.document(anyString())).thenReturn(mockDeleteDocument)
    `when`(mockDeleteDocument.delete()).thenReturn(mockTaskVoid)
    `when`(mockChatCollectionReference.whereArrayContains(anyString(), org.mockito.kotlin.any()))
        .thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(mockQuerySnapshot)
    //        `when`(mockQuerySnapshot.isSuccessful).thenReturn(false)
    doAnswer { invocation -> mockQuerySnapshot }
        .`when`(mockQuerySnapshot)
        .addOnSuccessListener(org.mockito.kotlin.any())
    doAnswer { invocation ->
          onFailureCalled = true
          capturedException = mockException
          mockQuerySnapshot
        }
        .`when`(mockQuerySnapshot)
        .addOnFailureListener(org.mockito.kotlin.any())

    // Arrange
    val uid = "test_uid"
    val phoneNumber = "+123456789"
    val emailAddress = "test@example.com"

    val userData = mapOf("phoneNumber" to phoneNumber, "emailAddress" to emailAddress)
    whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
    whenever(mockUserDocumentSnapshot.data).thenReturn(userData)

    // Simulate failure on phone doc delete
    val exception = Exception("Failed to delete phone doc")
    whenever(mockPhoneCollectionReference.document(phoneNumber).delete())
        .thenReturn(Tasks.forException(exception))

    // Act
    userRepositoryFirestore.deleteUserAccount(
        uid = uid,
        onSuccess = { onSuccessCalled = true },
        onFailure = {
          onFailureCalled = true
          capturedException = it
        })

    // Assert
    shadowOf(Looper.getMainLooper()).idle()
    assertFalse(onSuccessCalled)
    assertTrue(onFailureCalled)
    Assert.assertEquals("Failed to delete phone doc", capturedException?.message)
    org.mockito.kotlin.verify(mockPhoneCollectionReference.document(phoneNumber)).delete()
  }

  @Test
  fun deleteUserAccount_failureOnDeletingChat_callsOnFailure() {
    // Arrange
    val uid = "test_uid"
    // Suppose these are the user data fields from getUserDocumentSnapshot
    val phoneNumber = "+123456789"
    val emailAddress = "test@example.com"

    // Mock references for phone and email documents to return a valid Task<Void>
    val mockPhoneDocRef = mock(DocumentReference::class.java)
    val mockEmailDocRef = mock(DocumentReference::class.java)
    val mockProfilePicturesRef = mock(StorageReference::class.java)
    val profilePicturesRefField =
        UserRepositoryFirestore::class.java.getDeclaredField("profilePicturesRef")
    profilePicturesRefField.isAccessible = true
    profilePicturesRefField.set(userRepositoryFirestore, mockProfilePicturesRef)
    val mockProfilePictureRef = mock(StorageReference::class.java)
    whenever(mockProfilePicturesRef.child("$uid.jpeg")).thenReturn(mockProfilePictureRef)
    whenever(mockProfilePictureRef.delete()).thenReturn(Tasks.forResult(null))

    whenever(mockPhoneCollectionReference.document(phoneNumber)).thenReturn(mockPhoneDocRef)
    whenever(mockEmailCollectionReference.document(emailAddress)).thenReturn(mockEmailDocRef)

    // Ensure their delete() returns a Task instead of null
    whenever(mockPhoneDocRef.delete()).thenReturn(Tasks.forResult(null))
    whenever(mockEmailDocRef.delete()).thenReturn(Tasks.forResult(null))

    // Mock the user document reference delete as well
    whenever(mockUserDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    // If you're deleting profile pictures from StorageReference, mock that too
    whenever(mockProfilePicturesRef.child("$uid.jpeg")).thenReturn(mockProfilePictureRef)
    whenever(mockProfilePictureRef.delete()).thenReturn(Tasks.forResult(null))

    // If you have chats to delete, mock those DocumentReferences as well
    val mockChatDocRef = mock(DocumentReference::class.java)
    whenever(mockChatDocRef.delete())
        .thenReturn(Tasks.forException(Exception("Failed to delete chat")))
    // Set up the query to return this chatDocRef in the snapshot
    val mockChatQuerySnapshot = mock(QuerySnapshot::class.java)
    val mockChatDocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(mockChatQuerySnapshot.documents).thenReturn(listOf(mockChatDocumentSnapshot))
    `when`(mockChatDocumentSnapshot.reference).thenReturn(mockChatDocRef)

    val chatDocRef = mock(DocumentReference::class.java)
    val chatDocSnapshot = mock(DocumentSnapshot::class.java)
    whenever(chatDocSnapshot.reference).thenReturn(chatDocRef)
    whenever(chatDocRef.delete()).thenReturn(Tasks.forException(Exception("Failed to delete chat")))

    val mockChatsQuery = mock(Query::class.java)
    whenever(mockChatsQuery.get()).thenReturn(Tasks.forResult(mockChatQuerySnapshot))
    whenever(mockChatCollectionReference.whereArrayContains("participants", uid))
        .thenReturn(mockChatsQuery)
  }

  @Test
  fun deleteUserAccount_userDocDeletionFails_callsOnFailure() {
    // Arrange
    val uid = "test_uid"
    val phoneNumber = "+123456789"
    val emailAddress = "test@example.com"

    val userData = mapOf("phoneNumber" to phoneNumber, "emailAddress" to emailAddress)
    whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
    whenever(mockUserDocumentSnapshot.data).thenReturn(userData)

    whenever(mockPhoneCollectionReference.document(phoneNumber).delete())
        .thenReturn(Tasks.forResult(null))
    whenever(mockEmailCollectionReference.document(emailAddress).delete())
        .thenReturn(Tasks.forResult(null))

    // Mock profile picture deletion success
    val profilePicturesRefField =
        UserRepositoryFirestore::class.java.getDeclaredField("profilePicturesRef")
    profilePicturesRefField.isAccessible = true
    val mockProfilePicturesRef = mock(StorageReference::class.java)
    profilePicturesRefField.set(userRepositoryFirestore, mockProfilePicturesRef)
    val mockProfilePictureRef = mock(StorageReference::class.java)
    whenever(mockProfilePicturesRef.child("$uid.jpeg")).thenReturn(mockProfilePictureRef)
    whenever(mockProfilePictureRef.delete()).thenReturn(Tasks.forResult(null))

    // Mock chats query with no chats
    val chatsCollectionRef = mock(CollectionReference::class.java)
    whenever(mockFirestore.collection("chats")).thenReturn(chatsCollectionRef)
    val chatsQuery = mock(Query::class.java)
    whenever(chatsCollectionRef.whereArrayContains("participants", uid)).thenReturn(chatsQuery)
    val chatSnapshot = mock(QuerySnapshot::class.java)
    whenever(chatsQuery.get()).thenReturn(Tasks.forResult(chatSnapshot))
    whenever(chatSnapshot.documents).thenReturn(emptyList())

    // Simulate failure on deleting user doc
    val exception = Exception("Failed to delete user doc")
    whenever(mockUserDocumentReference.delete()).thenReturn(Tasks.forException(exception))

    var onSuccessCalled = false
    var onFailureCalled = false
    var capturedException: Exception? = null

    // Act
    userRepositoryFirestore.deleteUserAccount(
        uid = uid,
        onSuccess = { onSuccessCalled = true },
        onFailure = {
          onFailureCalled = true
          capturedException = it
        })

    // Assert
    shadowOf(Looper.getMainLooper()).idle()
    assertFalse(onSuccessCalled)
    assertTrue(onFailureCalled)
    Assert.assertEquals("Failed to delete user doc", capturedException?.message)
    org.mockito.kotlin.verify(mockUserDocumentReference).delete()
  }
}
