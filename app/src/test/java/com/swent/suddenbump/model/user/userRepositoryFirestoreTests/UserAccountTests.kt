package com.swent.suddenbump.model.user.userRepositoryFirestoreTests

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Looper
import androidx.compose.ui.graphics.ImageBitmap
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
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
import com.google.firebase.storage.StorageReference
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
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class UserAccountTests {
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

  @Mock private lateinit var mockEmailQuerySnapshot: QuerySnapshot

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

  /**
   * This test verifies that when fetching a User, the Firestore `get()` is called on the collection
   * reference and not the document reference.
   */
  @Test
  fun getUserAccount1_callsDocuments() {
    `when`(mockUserCollectionReference.get()).thenReturn(Tasks.forResult(mockUserQuerySnapshot))
    `when`(mockUserQuerySnapshot.documents).thenReturn(listOf())

    `when`(mockEmailCollectionReference.get()).thenReturn(Tasks.forResult(mockEmailQuerySnapshot))
    `when`(mockUserQuerySnapshot.documents).thenReturn(listOf())

    `when`(mockEmailDocumentReference.get()).thenReturn(Tasks.forResult(mockEmailDocumentSnapshot))

    userRepositoryFirestore.getUserAccount(
        onSuccess = {}, onFailure = { fail("Failure callback should not be called") })

    verify(timeout(100)) { (mockUserQuerySnapshot).documents }
  }

  @Test
  fun getUserAccount1ShouldFetchUserDocumentAndDownloadImageOnSuccess() {
    // Arrange
    val uid = "1234"
    val email = "test@example.com"
    val profilePicturePath = "profilePictures/$uid.jpeg"
    val profilePicture = mock(ImageBitmap::class.java)
    val mockUserDocumentSnapshot = mock(DocumentSnapshot::class.java)
    val emailDocumentSnapshot = mock(DocumentSnapshot::class.java)

    // Mock Firestore interactions
    whenever(mockFirestore.collection("Emails")).thenReturn(mockEmailCollectionReference)
    whenever(mockEmailCollectionReference.document(email)).thenReturn(mockEmailDocumentReference)
    whenever(mockEmailDocumentReference.get()).thenReturn(Tasks.forResult(emailDocumentSnapshot))
    whenever(emailDocumentSnapshot.data).thenReturn(mapOf("uid" to uid))

    whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    whenever(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)
    whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
    whenever(mockUserDocumentSnapshot.data)
        .thenReturn(
            mapOf(
                "uid" to uid,
                "firstName" to "John",
                "lastName" to "Doe",
                "phoneNumber" to "+1234567890",
                "emailAddress" to email))

    // Mock helper methods
    val helper = mock(UserRepositoryFirestoreHelper::class.java)
    whenever(helper.uidToProfilePicturePath(eq(uid), any())).thenReturn(profilePicturePath)
    whenever(helper.documentSnapshotToUser(eq(mockUserDocumentSnapshot), eq(profilePicture)))
        .thenReturn(
            User(
                uid = uid,
                firstName = "John",
                lastName = "Doe",
                phoneNumber = "+1234567890",
                profilePicture = profilePicture,
                emailAddress = email,
                lastKnownLocation = MutableStateFlow(Location("mock_provider"))))

    // Inject mocked helper into userRepositoryFirestore
    val helperField = UserRepositoryFirestore::class.java.getDeclaredField("helper")
    helperField.isAccessible = true
    helperField.set(userRepositoryFirestore, helper)

    // Mock ImageRepository behavior for async image download
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(ImageBitmap) -> Unit>(1)
          onSuccess(profilePicture) // Simulate successful image download
          null
        }
        .`when`(mockImageRepository)
        .downloadImageAsync(eq(profilePicturePath), any(), any())

    // Inject mockImageRepository into userRepositoryFirestore
    val imageRepositoryField =
        UserRepositoryFirestore::class.java.getDeclaredField("imageRepository")
    imageRepositoryField.isAccessible = true
    imageRepositoryField.set(userRepositoryFirestore, mockImageRepository)

    var onSuccessCalled = false
    var returnedUser: User? = null

    // Act
    userRepositoryFirestore.getUserAccount(
        onSuccess = {
          onSuccessCalled = true
          returnedUser = it
        },
        onFailure = { fail("Failure callback should not be called") })

    // Ensure all asynchronous operations complete
    shadowOf(Looper.getMainLooper()).idle()

    // Assert
    assertTrue(onSuccessCalled)
    assertNotNull(returnedUser)
    assertEquals(uid, returnedUser?.uid)
    assertEquals("John", returnedUser?.firstName)
    assertEquals("Doe", returnedUser?.lastName)
    assertEquals(email, returnedUser?.emailAddress)
    assertEquals(profilePicture, returnedUser?.profilePicture)

    verify(mockEmailDocumentReference).get()
    verify(mockUserDocumentReference).get()
    verify(mockImageRepository).downloadImageAsync(eq(profilePicturePath), any(), any())
  }

  @Test
  fun getUserAccount1ShouldCallOnFailureWhenGetUserFails() {
    // Arrange
    val uid = "1234"
    val email = "test@example.com"
    val exception = Exception("Failed to fetch user document")

    val resultEmailSnapshot = mock(DocumentSnapshot::class.java)
    whenever(resultEmailSnapshot.data).thenReturn(mapOf("uid" to uid))

    // Mock Firestore interactions
    whenever(mockFirestore.collection("Emails").document(email).get())
        .thenReturn(Tasks.forResult(resultEmailSnapshot))
    whenever(mockFirestore.collection("Users").document(uid).get())
        .thenReturn(Tasks.forException(exception))

    var onFailureCalled = false
    var failureException: Exception? = null

    // Act
    userRepositoryFirestore.getUserAccount(
        onSuccess = { fail("Success callback should not be called") },
        onFailure = {
          onFailureCalled = true
          failureException = it
        })

    // Ensure all asynchronous operations complete
    shadowOf(Looper.getMainLooper()).idle()

    // Assert
    assertTrue(onFailureCalled)
    assertEquals(exception, failureException)
    verify(mockFirestore.collection("Users").document(uid)).get()
  }

  @Test
  fun getUserAccount1ShouldCallOnFailureWhenDownloadImageFails() {
    // Arrange
    val uid = "1234"
    val email = "test@example.com"
    val exception = Exception("Image download failed")
    val resultEmailSnapshot = mock(DocumentSnapshot::class.java)
    val resultUserSnapshot = mock(DocumentSnapshot::class.java)
    val profilePicturePath = "profilePictures/$uid.jpeg"

    // Define collection paths
    val emailCollectionPath = "Emails"
    val usersCollectionPath = "Users"

    // Mock Firestore email collection
    whenever(mockFirestore.collection(emailCollectionPath)).thenReturn(mockEmailCollectionReference)
    whenever(mockEmailCollectionReference.document(email)).thenReturn(mockEmailDocumentReference)
    whenever(mockEmailDocumentReference.get()).thenReturn(Tasks.forResult(resultEmailSnapshot))
    whenever(resultEmailSnapshot.data).thenReturn(mapOf("uid" to uid))

    // Mock Firestore user collection
    whenever(mockFirestore.collection(usersCollectionPath)).thenReturn(mockUserCollectionReference)
    whenever(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)
    whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(resultUserSnapshot))
    whenever(resultUserSnapshot.data)
        .thenReturn(
            mapOf(
                "uid" to uid,
                "firstName" to "John",
                "lastName" to "Doe",
                "phoneNumber" to "+1234567890",
                "emailAddress" to email))

    // Mock helper methods
    val helper = mock(UserRepositoryFirestoreHelper::class.java)
    whenever(helper.uidToProfilePicturePath(eq(uid), any())).thenReturn(profilePicturePath)

    // Mock ImageRepository to simulate failure
    doAnswer { invocation ->
          val onFailure =
              invocation.getArgument<(Exception) -> Unit>(2) // Retrieve failure callback
          onFailure.invoke(Exception("Image download failed")) // Trigger failure
          null
        }
        .`when`(mockImageRepository)
        .downloadImageAsync(eq(profilePicturePath), any(), any())

    // Inject mocks into UserRepositoryFirestore
    val helperField = UserRepositoryFirestore::class.java.getDeclaredField("helper")
    helperField.isAccessible = true
    helperField.set(userRepositoryFirestore, helper)

    val imageRepositoryField =
        UserRepositoryFirestore::class.java.getDeclaredField("imageRepository")
    imageRepositoryField.isAccessible = true
    imageRepositoryField.set(userRepositoryFirestore, mockImageRepository)

    var onFailureCalled = false
    var failureException: Exception? = null

    // Act
    userRepositoryFirestore.getUserAccount(
        onSuccess = { fail("onSuccess should not be called") },
        onFailure = { exception ->
          onFailureCalled = true
          failureException = exception
        })

    // Ensure all asynchronous operations in the main looper complete
    shadowOf(Looper.getMainLooper()).idle()

    // Assert
    assertTrue(onFailureCalled)
    assertEquals("Image download failed", failureException?.message)
    verify(mockImageRepository).downloadImageAsync(eq(profilePicturePath), any(), any())
  }

  @Test
  fun getUserAccount2ShouldCallOnFailureWhenRetrievalFails() {
    // Arrange
    val uid = "1234"
    val exception = Exception("User retrieval failed")

    // Inject usersCollectionPath and profilePicturesRef
    val usersCollectionPathField =
        UserRepositoryFirestore::class.java.getDeclaredField("usersCollectionPath")
    usersCollectionPathField.isAccessible = true
    usersCollectionPathField.set(userRepositoryFirestore, "Users")

    val profilePicturesRefField =
        UserRepositoryFirestore::class.java.getDeclaredField("profilePicturesRef")
    profilePicturesRefField.isAccessible = true
    profilePicturesRefField.set(userRepositoryFirestore, mock(StorageReference::class.java))

    // Mock Firestore user collection to simulate failure
    whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    whenever(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)
    whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forException(exception))

    var onFailureCalled = false
    var failureException: Exception? = null

    // Act
    userRepositoryFirestore.getUserAccount(
        uid = uid,
        onSuccess = { fail("onSuccess should not be called") },
        onFailure = { error ->
          onFailureCalled = true
          failureException = error
        })

    // Ensure all asynchronous operations complete
    shadowOf(Looper.getMainLooper()).idle()

    // Assert
    assertTrue(onFailureCalled)
    assertEquals(exception, failureException)
    verify(mockFirestore.collection("Users").document(uid)).get()
  }

  @Test
  fun getUserAccount2ShouldFetchUserDocumentAndDownloadImageOnSuccess() {
    // Arrange
    val uid = "1234"
    val email = "test@example.com"
    val profilePicturePath = "profilePictures/$uid.jpeg"
    val profilePicture = mock(ImageBitmap::class.java)
    val mockUserDocumentSnapshot = mock(DocumentSnapshot::class.java)
    val emailDocumentSnapshot = mock(DocumentSnapshot::class.java)

    // Mock Firestore interactions
    whenever(mockFirestore.collection("Emails")).thenReturn(mockEmailCollectionReference)
    whenever(mockEmailCollectionReference.document(email)).thenReturn(mockEmailDocumentReference)
    whenever(mockEmailDocumentReference.get()).thenReturn(Tasks.forResult(emailDocumentSnapshot))
    whenever(emailDocumentSnapshot.data).thenReturn(mapOf("uid" to uid))

    whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    whenever(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)
    whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
    whenever(mockUserDocumentSnapshot.data)
        .thenReturn(
            mapOf(
                "uid" to uid,
                "firstName" to "John",
                "lastName" to "Doe",
                "phoneNumber" to "+1234567890",
                "emailAddress" to email))

    // Mock helper methods
    val helper = mock(UserRepositoryFirestoreHelper::class.java)
    whenever(helper.uidToProfilePicturePath(eq(uid), any())).thenReturn(profilePicturePath)
    whenever(helper.documentSnapshotToUser(eq(mockUserDocumentSnapshot), eq(profilePicture)))
        .thenReturn(
            User(
                uid = uid,
                firstName = "John",
                lastName = "Doe",
                phoneNumber = "+1234567890",
                profilePicture = profilePicture,
                emailAddress = email,
                lastKnownLocation = MutableStateFlow(Location("mock_provider"))))

    // Inject mocked helper into userRepositoryFirestore
    val helperField = UserRepositoryFirestore::class.java.getDeclaredField("helper")
    helperField.isAccessible = true
    helperField.set(userRepositoryFirestore, helper)

    // Mock ImageRepository behavior for async image download
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(ImageBitmap) -> Unit>(1)
          onSuccess(profilePicture) // Simulate successful image download
          null
        }
        .`when`(mockImageRepository)
        .downloadImageAsync(eq(profilePicturePath), any(), any())

    // Inject mockImageRepository into userRepositoryFirestore
    val imageRepositoryField =
        UserRepositoryFirestore::class.java.getDeclaredField("imageRepository")
    imageRepositoryField.isAccessible = true
    imageRepositoryField.set(userRepositoryFirestore, mockImageRepository)

    var onSuccessCalled = false
    var returnedUser: User? = null

    // Act
    userRepositoryFirestore.getUserAccount(
        uid = uid,
        onSuccess = {
          onSuccessCalled = true
          returnedUser = it
        },
        onFailure = { fail("Failure callback should not be called") })

    // Ensure all asynchronous operations complete
    shadowOf(Looper.getMainLooper()).idle()

    // Assert
    assertTrue(onSuccessCalled)
    assertNotNull(returnedUser)
    assertEquals(uid, returnedUser?.uid)
    assertEquals("John", returnedUser?.firstName)
    assertEquals("Doe", returnedUser?.lastName)
    assertEquals(email, returnedUser?.emailAddress)
    assertEquals(profilePicture, returnedUser?.profilePicture)

    verify(mockUserDocumentReference).get()
    verify(mockImageRepository).downloadImageAsync(eq(profilePicturePath), any(), any())
  }

  @Test
  fun getUserAccount2ShouldCallOnFailureWhenDownloadImageFails() {
    // Arrange
    val uid = "1234"
    val email = "test@example.com"
    val exception = Exception("Image download failed")
    val resultEmailSnapshot = mock(DocumentSnapshot::class.java)
    val resultUserSnapshot = mock(DocumentSnapshot::class.java)
    val profilePicturePath = "profilePictures/$uid.jpeg"

    // Define collection paths
    val emailCollectionPath = "Emails"
    val usersCollectionPath = "Users"

    // Mock Firestore email collection
    whenever(mockFirestore.collection(emailCollectionPath)).thenReturn(mockEmailCollectionReference)
    whenever(mockEmailCollectionReference.document(email)).thenReturn(mockEmailDocumentReference)
    whenever(mockEmailDocumentReference.get()).thenReturn(Tasks.forResult(resultEmailSnapshot))
    whenever(resultEmailSnapshot.data).thenReturn(mapOf("uid" to uid))

    // Mock Firestore user collection
    whenever(mockFirestore.collection(usersCollectionPath)).thenReturn(mockUserCollectionReference)
    whenever(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)
    whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(resultUserSnapshot))
    whenever(resultUserSnapshot.data)
        .thenReturn(
            mapOf(
                "uid" to uid,
                "firstName" to "John",
                "lastName" to "Doe",
                "phoneNumber" to "+1234567890",
                "emailAddress" to email))

    // Mock helper methods
    val helper = mock(UserRepositoryFirestoreHelper::class.java)
    whenever(helper.uidToProfilePicturePath(eq(uid), any())).thenReturn(profilePicturePath)

    // Mock ImageRepository to simulate failure in async image download
    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2) // Failure callback
          onFailure(exception) // Trigger failure
          null
        }
        .`when`(mockImageRepository)
        .downloadImageAsync(eq(profilePicturePath), any(), any())

    // Inject mocks into UserRepositoryFirestore
    val helperField = UserRepositoryFirestore::class.java.getDeclaredField("helper")
    helperField.isAccessible = true
    helperField.set(userRepositoryFirestore, helper)

    val imageRepositoryField =
        UserRepositoryFirestore::class.java.getDeclaredField("imageRepository")
    imageRepositoryField.isAccessible = true
    imageRepositoryField.set(userRepositoryFirestore, mockImageRepository)

    var onFailureCalled = false
    var failureException: Exception? = null

    // Act
    userRepositoryFirestore.getUserAccount(
        uid,
        onSuccess = { fail("onSuccess should not be called") },
        onFailure = { exception ->
          onFailureCalled = true
          failureException = exception
        })

    // Ensure all asynchronous operations complete
    shadowOf(Looper.getMainLooper()).idle()

    // Assert
    assertTrue(onFailureCalled)
    assertEquals("Image download failed", failureException?.message)
    verify(mockImageRepository).downloadImageAsync(eq(profilePicturePath), any(), any())
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

  @Test
  fun deleteUserAccount_shouldCallOnSuccessWhenDeletionSucceeds() {
    // Arrange
    val uid = "test_user_id"
    val mockDeleteTask = Tasks.forResult<Void>(null) // Simulate successful deletion

    whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    whenever(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)
    whenever(mockUserDocumentReference.delete()).thenReturn(mockDeleteTask)

    var onSuccessCalled = false
    var onFailureCalled = false

    // Act
    userRepositoryFirestore.deleteUserAccount(
        uid = uid, onSuccess = { onSuccessCalled = true }, onFailure = { onFailureCalled = true })

    // Ensure tasks have completed
    shadowOf(Looper.getMainLooper()).idle()

    // Assert
    assertTrue("Expected onSuccess to be called", onSuccessCalled)
    assertFalse("Expected onFailure not to be called", onFailureCalled)
    verify(mockUserDocumentReference).delete()
  }

  @Test
  fun deleteUserAccount_shouldCallOnFailureWhenDeletionFails() {
    // Arrange
    val uid = "test_user_id"
    val exception = Exception("Deletion failed")
    val mockDeleteTask = Tasks.forException<Void>(exception) // Simulate failed deletion

    whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    whenever(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)
    whenever(mockUserDocumentReference.delete()).thenReturn(mockDeleteTask)

    var onSuccessCalled = false
    var onFailureCalled = false
    var caughtException: Exception? = null

    // Act
    userRepositoryFirestore.deleteUserAccount(
        uid = uid,
        onSuccess = { onSuccessCalled = true },
        onFailure = {
          onFailureCalled = true
          caughtException = it
        })

    // Ensure tasks have completed
    shadowOf(Looper.getMainLooper()).idle()

    // Assert
    assertFalse("Expected onSuccess not to be called", onSuccessCalled)
    assertTrue("Expected onFailure to be called", onFailureCalled)
    assertNotNull("Exception should be passed to onFailure", caughtException)
    assertEquals("Deletion failed", caughtException?.message)
    verify(mockUserDocumentReference).delete()
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

  @Test
  fun createUserAccountFailsOnInitialFailure() {
    // Arrange
    val exception = Exception("Initial failure")
    val mockTask = mock(Task::class.java) as Task<Void>

    `when`(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnFailureListener
      listener.onFailure(exception)
      mockTask
    }
    `when`(mockTask.addOnCompleteListener(any())).thenReturn(mockTask)

    `when`(mockUserDocumentReference.set(any())).thenReturn(mockTask)

    // Act
    userRepositoryFirestore.createUserAccount(
        user = user,
        onSuccess = { fail("onSuccess should not be called") },
        onFailure = { error ->
          // Assert
          assertEquals("Initial failure", error.message)
        })
  }

  @Test
  fun createUserAccountFailsOnEmailDocumentSet() {
    // Arrange
    val exception = Exception("Failed to set email document")
    val mockTask = mock(Task::class.java) as Task<Void>
    val mockCompleteTask = mock(Task::class.java) as Task<Void>

    `when`(mockTask.addOnFailureListener(any())).thenReturn(mockTask)
    `when`(mockTask.addOnCompleteListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnCompleteListener<Void>
      listener.onComplete(mockTask)
      mockTask
    }
    `when`(mockTask.isSuccessful).thenReturn(true)

    `when`(mockCompleteTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnFailureListener
      listener.onFailure(exception)
      mockCompleteTask
    }

    `when`(mockUserDocumentReference.set(any())).thenReturn(mockTask)
    `when`(mockEmailCollectionReference.document(user.emailAddress).set(any()))
        .thenReturn(mockCompleteTask)

    `when`(mockPhoneDocumentReference.set(any())).thenReturn(mockTask)
    `when`(mockPhoneCollectionReference.document(user.phoneNumber).set(any()))
        .thenReturn(mockCompleteTask)

    // Act
    userRepositoryFirestore.createUserAccount(
        user = user,
        onSuccess = { fail("onSuccess should not be called") },
        onFailure = { error ->
          // Assert
          assertEquals("Failed to set email document", error.message)
        })
  }

  @Test
  fun createUserAccountFailsOnProfilePictureUpload() {
    // Arrange
    val exception = Exception("Profile picture upload failed")
    val mockTask = mock(Task::class.java) as Task<Void>
    val mockCompleteTask = mock(Task::class.java) as Task<Void>
    val mockSuccessTask = mock(Task::class.java) as Task<Void>

    `when`(mockTask.addOnFailureListener(any())).thenReturn(mockTask)
    `when`(mockTask.addOnCompleteListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnCompleteListener<Void>
      listener.onComplete(mockTask)
      mockTask
    }
    `when`(mockTask.isSuccessful).thenReturn(true)

    `when`(mockCompleteTask.addOnFailureListener(any())).thenReturn(mockCompleteTask)
    `when`(mockCompleteTask.addOnSuccessListener(any())).thenReturn(mockCompleteTask)

    `when`(mockUserDocumentReference.set(any())).thenReturn(mockTask)
    `when`(mockEmailCollectionReference.document(user.emailAddress).set(any()))
        .thenReturn(mockCompleteTask)

    `when`(mockUserDocumentReference.set(any())).thenReturn(mockTask)
    `when`(mockPhoneCollectionReference.document(user.phoneNumber).set(any()))
        .thenReturn(mockCompleteTask)

    doAnswer {
          val onFailure = it.getArgument<(Exception) -> Unit>(3)
          onFailure(exception)
        }
        .`when`(mockImageRepository)
        .uploadImage(any(), any(), any(), any())

    // Act
    userRepositoryFirestore.createUserAccount(
        user = user,
        onSuccess = { fail("onSuccess should not be called") },
        onFailure = { error ->
          // Assert
          assertEquals("Profile picture upload failed", error.message)
        })
  }

  @Test
  fun createUserAccountSkipsImageUploadWhenProfilePictureIsNull() {
    // Arrange
    val mockTask = mock(Task::class.java) as Task<Void>
    `when`(mockTask.addOnFailureListener(any())).thenReturn(mockTask)
    `when`(mockTask.addOnCompleteListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnCompleteListener<Void>
      listener.onComplete(mockTask)
      mockTask
    }
    `when`(mockTask.isSuccessful).thenReturn(true)

    `when`(mockUserDocumentReference.set(any())).thenReturn(mockTask)
    `when`(mockEmailCollectionReference.document(user.emailAddress).set(any())).thenReturn(mockTask)

    `when`(mockUserDocumentReference.set(any())).thenReturn(mockTask)
    `when`(mockPhoneCollectionReference.document(user.phoneNumber).set(any())).thenReturn(mockTask)

    // Act
    val userWithoutProfilePicture = user.copy(profilePicture = null)
    userRepositoryFirestore.createUserAccount(
        user = userWithoutProfilePicture,
        onSuccess = { assertTrue(true) },
        onFailure = { fail("onFailure should not be called") })

    // Assert
    verify(mockImageRepository, never()).uploadImage(any(), any(), any(), any())
  }

  @Test
  fun createUserAccountFailsWhenTaskIsNotSuccessful() {
    // Arrange
    val exception = Exception("Task failed")
    val mockTask = mock(Task::class.java) as Task<Void>

    // Mock the task to simulate failure behavior
    `when`(mockTask.addOnFailureListener(any())).thenReturn(mockTask)
    `when`(mockTask.addOnCompleteListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnCompleteListener<Void>
      listener.onComplete(mockTask) // Simulate task completion
      mockTask
    }
    `when`(mockTask.isSuccessful).thenReturn(false) // Simulate task failure
    `when`(mockTask.exception).thenReturn(exception) // Provide the exception

    // Stub Firestore call to return the mocked task
    `when`(mockUserDocumentReference.set(any())).thenReturn(mockTask)

    // Act
    userRepositoryFirestore.createUserAccount(
        user = user,
        onSuccess = { fail("onSuccess should not be called") }, // Should not be called
        onFailure = { error ->
          // Assert
          assertEquals("Task failed", error.message) // Verify the exception message
        })
  }

  @Test
  fun createUserAccountSucceeds() {
    // Arrange
    val mockTask = mock(Task::class.java) as Task<Void>
    val mockCompleteTask = mock(Task::class.java) as Task<Void>

    `when`(mockTask.addOnFailureListener(any())).thenReturn(mockTask)
    `when`(mockTask.addOnCompleteListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnCompleteListener<Void>
      listener.onComplete(mockTask)
      mockTask
    }
    `when`(mockTask.isSuccessful).thenReturn(true)

    `when`(mockCompleteTask.addOnFailureListener(any())).thenReturn(mockCompleteTask)
    `when`(mockCompleteTask.addOnSuccessListener(any())).thenReturn(mockCompleteTask)

    `when`(mockUserDocumentReference.set(any())).thenReturn(mockTask)
    `when`(mockEmailCollectionReference.document(user.emailAddress).set(any()))
        .thenReturn(mockCompleteTask)

    `when`(mockUserDocumentReference.set(any())).thenReturn(mockTask)
    `when`(mockPhoneCollectionReference.document(user.phoneNumber).set(any()))
        .thenReturn(mockCompleteTask)

    doAnswer {
          val onSuccess = it.getArgument<() -> Unit>(2)
          onSuccess()
        }
        .`when`(mockImageRepository)
        .uploadImage(any(), any(), any(), any())

    // Act
    userRepositoryFirestore.createUserAccount(
        user = user,
        onSuccess = {
          // Assert
          verify(mockImageRepository).uploadImage(any(), any(), any(), any())
        },
        onFailure = { fail("onFailure should not be called") })
  }

  @Test
  fun createUserAccountNoUploadWhenProfilePictureIsNull() {
    // Arrange
    val mockUser = user.copy(profilePicture = null)

    // Act
    userRepositoryFirestore.createUserAccount(
        user = mockUser,
        onSuccess = {
          // Assert
          verify(mockImageRepository, never()).uploadImage(any(), any(), any(), any())
        },
        onFailure = { fail("onFailure should not be called when profilePicture is null") })
  }

  @Test
  fun createUserAccountWithProfilePictureUpload() {
    // Arrange
    val profilePicture = mock(ImageBitmap::class.java) // Mock ImageBitmap
    val userWithProfilePicture =
        user.copy(profilePicture = profilePicture) // User with profile picture
    val profilePicturePath =
        "gs://sudden-bump-swent.appspot.com/profilePictures/1.jpeg" // Updated expected path
    val profilePicturesRef = mock(StorageReference::class.java)

    // Mock the StorageReference.child() method
    val childRef = mock(StorageReference::class.java)
    `when`(profilePicturesRef.child(anyString())).thenReturn(childRef)
    `when`(childRef.toString()).thenReturn(profilePicturePath)

    // Mock the helper method to return the actual expected value
    `when`(helper.uidToProfilePicturePath(userWithProfilePicture.uid, profilePicturesRef))
        .thenReturn(profilePicturePath)

    // Mock the ImageRepository's uploadImage method directly
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(2)
          onSuccess() // Simulate a successful upload
        }
        .`when`(mockImageRepository)
        .uploadImage(any(), eq(profilePicturePath), any(), any())

    // Mock Firestore interactions
    `when`(mockUserDocumentReference.set(any())).thenReturn(Tasks.forResult(null))
    `when`(mockEmailCollectionReference.document(userWithProfilePicture.emailAddress).set(any()))
        .thenReturn(Tasks.forResult(null))

    `when`(mockUserDocumentReference.set(any())).thenReturn(Tasks.forResult(null))
    `when`(mockPhoneCollectionReference.document(userWithProfilePicture.phoneNumber).set(any()))
        .thenReturn(Tasks.forResult(null))

    // Inject mock ImageRepository into UserRepositoryFirestore via reflection
    val imageRepositoryField =
        UserRepositoryFirestore::class.java.getDeclaredField("imageRepository")
    imageRepositoryField.isAccessible = true
    imageRepositoryField.set(userRepositoryFirestore, mockImageRepository)

    var onSuccessCalled = false
    var onFailureCalled = false

    // Act
    userRepositoryFirestore.createUserAccount(
        user = userWithProfilePicture,
        onSuccess = { onSuccessCalled = true },
        onFailure = { onFailureCalled = true })

    // Ensure all asynchronous operations are executed
    shadowOf(Looper.getMainLooper()).idle()

    // Assert
    assertTrue("Expected onSuccess to be called", onSuccessCalled)
    assertFalse("Expected onFailure not to be called", onFailureCalled)
    verify(mockImageRepository)
        .uploadImage(any(), eq(profilePicturePath), any(), any()) // Match expected path
  }

  @Test
  fun createUserAccountWithProfilePictureUploadFailure() {
    // Arrange
    val profilePicture = mock(ImageBitmap::class.java) // Mock ImageBitmap
    val userWithProfilePicture =
        user.copy(profilePicture = profilePicture) // User with profile picture
    val profilePicturePath =
        "gs://sudden-bump-swent.appspot.com/profilePictures/1.jpeg" // Updated expected path
    val profilePicturesRef = mock(StorageReference::class.java)

    // Mock the StorageReference.child() method
    val childRef = mock(StorageReference::class.java)
    `when`(profilePicturesRef.child(anyString())).thenReturn(childRef)
    `when`(childRef.toString()).thenReturn(profilePicturePath)

    // Mock the helper method to return the actual expected value
    `when`(helper.uidToProfilePicturePath(userWithProfilePicture.uid, profilePicturesRef))
        .thenReturn(profilePicturePath)

    // Mock the ImageRepository's uploadImage method directly to simulate failure
    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(3)
          onFailure(Exception("Simulated upload failure")) // Simulate a failed upload
        }
        .`when`(mockImageRepository)
        .uploadImage(any(), eq(profilePicturePath), any(), any())

    // Mock Firestore interactions
    `when`(mockUserDocumentReference.set(any())).thenReturn(Tasks.forResult(null))
    `when`(mockEmailCollectionReference.document(userWithProfilePicture.emailAddress).set(any()))
        .thenReturn(Tasks.forResult(null))

    `when`(mockUserDocumentReference.set(any())).thenReturn(Tasks.forResult(null))
    `when`(mockPhoneCollectionReference.document(userWithProfilePicture.phoneNumber).set(any()))
        .thenReturn(Tasks.forResult(null))

    val imageRepositoryField =
        UserRepositoryFirestore::class.java.getDeclaredField("imageRepository")
    imageRepositoryField.isAccessible = true
    imageRepositoryField.set(userRepositoryFirestore, mockImageRepository)

    var onSuccessCalled = false
    var onFailureCalled = false

    // Act
    userRepositoryFirestore.createUserAccount(
        user = userWithProfilePicture,
        onSuccess = { onSuccessCalled = true },
        onFailure = { error ->
          onFailureCalled = true
          assertEquals("Simulated upload failure", error.message) // Ensure correct error message
        })

    // Ensure all asynchronous operations are executed
    shadowOf(Looper.getMainLooper()).idle()

    // Assert
    assertFalse("Expected onSuccess not to be called", onSuccessCalled)
    assertTrue("Expected onFailure to be called", onFailureCalled)
    verify(mockImageRepository)
        .uploadImage(any(), eq(profilePicturePath), any(), any()) // Match expected path
  }

  /**
   * This test verifies that when we create a new User account, the Firestore `set()` is called on
   * the document reference. This does NOT CHECK the actual data being added
   */
  @Test
  fun updateUserAccount_shouldCallFirestoreCollection() {
    userRepositoryFirestore.updateUserAccount(user, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockUserDocumentReference).update(any())
  }

  @Test
  fun updateUserAccount_ShouldCallOnFailure_WhenFirestoreSetFails() {
    // Arrange
    val exception = Exception("Firestore set failed")

    // Mock Firestore setup
    whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    whenever(mockUserCollectionReference.document(user.uid)).thenReturn(mockUserDocumentReference)

    // Create a mock Task to simulate failure and chaining behavior
    val failingTask = mock(Task::class.java) as Task<Void>
    whenever(failingTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val failureListener = invocation.getArgument<OnFailureListener>(0)
      failureListener.onFailure(exception)
      failingTask // Return the task itself to support chaining
    }
    whenever(failingTask.addOnCompleteListener(any())).thenReturn(failingTask)

    whenever(mockUserDocumentReference.update(any())).thenReturn(failingTask)

    val onFailureMock = mock<(Exception) -> Unit>()

    // Act
    userRepositoryFirestore.updateUserAccount(
        user = user,
        onSuccess = { fail("Expected onFailure to be called") },
        onFailure = onFailureMock)

    // Assert
    verify(onFailureMock).invoke(exception) // Verify onFailure is called with the correct exception
    verifyNoInteractions(mockImageRepository) // Ensure no image upload occurs
  }

  @Test
  fun updateUserAccountSucceeds() {
    // Arrange
    val mockTask = mock(Task::class.java) as Task<Void>
    val mockCompleteTask = mock(Task::class.java) as Task<Void>

    `when`(mockTask.addOnFailureListener(any())).thenReturn(mockTask)
    `when`(mockTask.addOnCompleteListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnCompleteListener<Void>
      listener.onComplete(mockTask)
      mockTask
    }
    `when`(mockTask.isSuccessful).thenReturn(true)

    `when`(mockCompleteTask.addOnFailureListener(any())).thenReturn(mockCompleteTask)
    `when`(mockCompleteTask.addOnSuccessListener(any())).thenReturn(mockCompleteTask)

    `when`(mockUserDocumentReference.update(any())).thenReturn(mockTask)
    `when`(mockEmailCollectionReference.document(user.emailAddress).set(any()))
        .thenReturn(mockCompleteTask)

    doAnswer {
          val onSuccess = it.getArgument<() -> Unit>(2)
          onSuccess()
        }
        .`when`(mockImageRepository)
        .uploadImage(any(), any(), any(), any())

    // Act
    userRepositoryFirestore.updateUserAccount(
        user = user,
        onSuccess = {
          // Assert
          verify(mockImageRepository).uploadImage(any(), any(), any(), any())
        },
        onFailure = { fail("onFailure should not be called") })
  }

  @Test
  fun updateUserAccountNoUploadWhenProfilePictureIsNull() {
    // Arrange
    val mockUser = user.copy(profilePicture = null)

    // Act
    userRepositoryFirestore.updateUserAccount(
        user = mockUser,
        onSuccess = {
          // Assert
          verify(mockImageRepository, never()).uploadImage(any(), any(), any(), any())
        },
        onFailure = { fail("onFailure should not be called when profilePicture is null") })
  }

  @Test
  fun updateUserAccountWithProfilePictureUpload() {
    // Arrange
    val profilePicture = mock(ImageBitmap::class.java) // Mock ImageBitmap
    val userWithProfilePicture =
        user.copy(profilePicture = profilePicture) // User with profile picture
    val profilePicturePath =
        "gs://sudden-bump-swent.appspot.com/profilePictures/1.jpeg" // Updated expected path
    val profilePicturesRef = mock(StorageReference::class.java)

    // Mock the StorageReference.child() method
    val childRef = mock(StorageReference::class.java)
    `when`(profilePicturesRef.child(anyString())).thenReturn(childRef)
    `when`(childRef.toString()).thenReturn(profilePicturePath)

    // Mock the helper method to return the actual expected value
    `when`(helper.uidToProfilePicturePath(userWithProfilePicture.uid, profilePicturesRef))
        .thenReturn(profilePicturePath)

    // Mock the ImageRepository's uploadImage method directly
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(2)
          onSuccess() // Simulate a successful upload
        }
        .`when`(mockImageRepository)
        .uploadImage(any(), eq(profilePicturePath), any(), any())

    // Mock Firestore interactions
    `when`(mockUserDocumentReference.update(any())).thenReturn(Tasks.forResult(null))
    `when`(mockEmailCollectionReference.document(userWithProfilePicture.emailAddress).set(any()))
        .thenReturn(Tasks.forResult(null))

    val imageRepositoryField =
        UserRepositoryFirestore::class.java.getDeclaredField("imageRepository")
    imageRepositoryField.isAccessible = true
    imageRepositoryField.set(userRepositoryFirestore, mockImageRepository)

    var onSuccessCalled = false
    var onFailureCalled = false

    // Act
    userRepositoryFirestore.updateUserAccount(
        user = userWithProfilePicture,
        onSuccess = { onSuccessCalled = true },
        onFailure = { onFailureCalled = true })

    // Ensure all asynchronous operations are executed
    shadowOf(Looper.getMainLooper()).idle()

    // Assert
    assertTrue("Expected onSuccess to be called", onSuccessCalled)
    assertFalse("Expected onFailure not to be called", onFailureCalled)
    verify(mockImageRepository)
        .uploadImage(any(), eq(profilePicturePath), any(), any()) // Match expected path
  }

  @Test
  fun updateUserAccountWithProfilePictureUploadFailure() {
    // Arrange
    val profilePicture = mock(ImageBitmap::class.java) // Mock ImageBitmap
    val userWithProfilePicture =
        user.copy(profilePicture = profilePicture) // User with profile picture
    val profilePicturePath =
        "gs://sudden-bump-swent.appspot.com/profilePictures/1.jpeg" // Updated expected path
    val profilePicturesRef = mock(StorageReference::class.java)

    // Mock the StorageReference.child() method
    val childRef = mock(StorageReference::class.java)
    `when`(profilePicturesRef.child(anyString())).thenReturn(childRef)
    `when`(childRef.toString()).thenReturn(profilePicturePath)

    // Mock the helper method to return the actual expected value
    `when`(helper.uidToProfilePicturePath(userWithProfilePicture.uid, profilePicturesRef))
        .thenReturn(profilePicturePath)

    // Mock the ImageRepository's uploadImage method directly to simulate failure
    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(3)
          onFailure(Exception("Simulated upload failure")) // Simulate a failed upload
        }
        .`when`(mockImageRepository)
        .uploadImage(any(), eq(profilePicturePath), any(), any())

    // Mock Firestore interactions
    `when`(mockUserDocumentReference.update(any())).thenReturn(Tasks.forResult(null))
    `when`(mockEmailCollectionReference.document(userWithProfilePicture.emailAddress).set(any()))
        .thenReturn(Tasks.forResult(null))

    val imageRepositoryField =
        UserRepositoryFirestore::class.java.getDeclaredField("imageRepository")
    imageRepositoryField.isAccessible = true
    imageRepositoryField.set(userRepositoryFirestore, mockImageRepository)

    var onSuccessCalled = false
    var onFailureCalled = false

    // Act
    userRepositoryFirestore.updateUserAccount(
        user = userWithProfilePicture,
        onSuccess = { onSuccessCalled = true },
        onFailure = { error ->
          onFailureCalled = true
          assertEquals("Simulated upload failure", error.message) // Ensure correct error message
        })

    // Ensure all asynchronous operations are executed
    shadowOf(Looper.getMainLooper()).idle()

    // Assert
    assertFalse("Expected onSuccess not to be called", onSuccessCalled)
    assertTrue("Expected onFailure to be called", onFailureCalled)
    verify(mockImageRepository)
        .uploadImage(any(), eq(profilePicturePath), any(), any()) // Match expected path
  }

  @Test
  fun updateUserAccountFailsWhenTaskIsNotSuccessful() {
    // Arrange
    val exception = Exception("Task failed")
    val mockTask = mock(Task::class.java) as Task<Void>

    // Mock the task to simulate failure behavior
    `when`(mockTask.addOnFailureListener(any())).thenReturn(mockTask)
    `when`(mockTask.addOnCompleteListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnCompleteListener<Void>
      listener.onComplete(mockTask) // Simulate task completion
      mockTask
    }
    `when`(mockTask.isSuccessful).thenReturn(false) // Simulate task failure
    `when`(mockTask.exception).thenReturn(exception) // Provide the exception

    // Stub Firestore call to return the mocked task
    `when`(mockUserDocumentReference.update(any())).thenReturn(mockTask)

    // Act
    userRepositoryFirestore.updateUserAccount(
        user = user,
        onSuccess = { fail("onSuccess should not be called") }, // Should not be called
        onFailure = { error ->
          // Assert
          assertEquals("Task failed", error.message) // Verify the exception message
        })
  }
}
