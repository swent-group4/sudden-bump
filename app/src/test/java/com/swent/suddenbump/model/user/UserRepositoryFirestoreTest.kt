package com.swent.suddenbump.model.user

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Looper
import android.os.Looper.getMainLooper
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
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
import com.google.firebase.firestore.Transaction
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.swent.suddenbump.model.image.ImageRepository
import java.util.concurrent.CountDownLatch
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
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
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
class UserRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore

  @Mock private lateinit var mockUserCollectionReference: CollectionReference

  @Mock private lateinit var mockEmailCollectionReference: CollectionReference

  @Mock private lateinit var mockPhoneCollectionReference: CollectionReference

  @Mock private lateinit var mockUserDocumentReference: DocumentReference

  @Mock private lateinit var mockEmailDocumentReference: DocumentReference

  @Mock private lateinit var mockPhoneDocumentReference: DocumentReference

  @Mock private lateinit var mockUserDocumentSnapshot: DocumentSnapshot

  @Mock private lateinit var mockEmailDocumentSnapshot: DocumentSnapshot

  @Mock private lateinit var mockPhoneDocumentSnapshot: DocumentSnapshot

  @Mock private lateinit var mockedFriendsDocumentSnapshot: DocumentSnapshot

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
  @Mock private lateinit var transaction: Transaction
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

    // Mock the context to return the mocked SharedPreferences
    val mockContext = mock(Context::class.java)
    `when`(mockContext.getSharedPreferences(anyString(), eq(Context.MODE_PRIVATE)))
        .thenReturn(sharedPreferences)

    userRepositoryFirestore = UserRepositoryFirestore(mockFirestore, mockContext)

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
  fun initShouldCallImageRepositoryInitAndInvokeOnSuccess() {
    // Arrange
    val mockImageRepository = mock(ImageRepository::class.java)
    val userRepositoryFirestore = UserRepositoryFirestore(mockFirestore, mock(Context::class.java))

    // Use reflection to inject the mock ImageRepository
    val imageRepositoryField =
        UserRepositoryFirestore::class.java.getDeclaredField("imageRepository")
    imageRepositoryField.isAccessible = true
    imageRepositoryField.set(userRepositoryFirestore, mockImageRepository)

    var onSuccessCalled = false

    // Act
    userRepositoryFirestore.init {
      onSuccessCalled = true // This should be set to true if onSuccess is called
    }

    // Assert
    verify(mockImageRepository).init(any()) // Verify that imageRepository.init() was called
    assertTrue(onSuccessCalled) // Verify that the onSuccess callback was invoked
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

  @Test
  fun verifyNoAccountExistsSuccessfulWithEmailNotInResultCallsOnSuccessTrue() {
    // Arrange
    val emailAddress = "test@example.com"
    val documents = listOf<DocumentSnapshot>() // No emails in the result
    val mockTask = mock(Task::class.java) as Task<QuerySnapshot>
    val mockQuerySnapshot = mock(QuerySnapshot::class.java)

    `when`(mockTask.addOnCompleteListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnCompleteListener<QuerySnapshot>
      listener.onComplete(mockTask)
      mockTask
    }
    `when`(mockTask.addOnFailureListener(any())).thenReturn(mockTask)
    `when`(mockTask.isSuccessful).thenReturn(true)
    `when`(mockTask.result).thenReturn(mockQuerySnapshot)
    `when`(mockQuerySnapshot.documents).thenReturn(documents)

    `when`(mockEmailCollectionReference.get()).thenReturn(mockTask)

    // Act
    userRepositoryFirestore.verifyNoAccountExists(
        emailAddress = emailAddress,
        onSuccess = { result ->
          // Assert
          assertTrue(result) // Email not in the result
        },
        onFailure = { fail("Failure callback should not be called") })
  }

  @Test
  fun verifyNoAccountExistsFailureCallsOnFailure() {
    // Arrange
    val emailAddress = "test@example.com"
    val exception = Exception("Query failed")
    val mockTask = mock(Task::class.java) as Task<QuerySnapshot>

    `when`(mockTask.addOnCompleteListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnCompleteListener<QuerySnapshot>
      listener.onComplete(mockTask)
      mockTask
    }
    `when`(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnFailureListener
      listener.onFailure(exception)
      mockTask
    }
    `when`(mockTask.isSuccessful).thenReturn(false)
    `when`(mockTask.exception).thenReturn(exception)

    `when`(mockEmailCollectionReference.get()).thenReturn(mockTask)

    // Act
    userRepositoryFirestore.verifyNoAccountExists(
        emailAddress = emailAddress,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { error ->
          // Assert
          assertEquals("Query failed", error.message)
        })
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
    val userRepositoryFirestore = UserRepositoryFirestore(mockFirestore, mock(Context::class.java))
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

    // Inject mock ImageRepository into UserRepositoryFirestore via reflection
    val userRepositoryFirestore = UserRepositoryFirestore(mockFirestore, mock(Context::class.java))
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
    val userDocumentSnapshot = mock(DocumentSnapshot::class.java)
    val emailDocumentSnapshot = mock(DocumentSnapshot::class.java)
    val mockImageRepository = mock(ImageRepository::class.java)

    // Mock Firestore interactions
    whenever(mockFirestore.collection("Emails")).thenReturn(mockEmailCollectionReference)
    whenever(mockEmailCollectionReference.document(email)).thenReturn(mockEmailDocumentReference)
    whenever(mockEmailDocumentReference.get()).thenReturn(Tasks.forResult(emailDocumentSnapshot))
    whenever(emailDocumentSnapshot.data).thenReturn(mapOf("uid" to uid))

    whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    whenever(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)
    whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(userDocumentSnapshot))
    whenever(userDocumentSnapshot.data)
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
    whenever(helper.documentSnapshotToUser(eq(userDocumentSnapshot), eq(profilePicture)))
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

    // Mock ImageRepository behavior
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(ImageBitmap) -> Unit>(1)
          onSuccess(profilePicture) // Simulate image download success
          null
        }
        .`when`(mockImageRepository)
        .downloadImage(eq(profilePicturePath), any(), any())

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
    verify(mockImageRepository).downloadImage(eq(profilePicturePath), any(), any())
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
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(exception) // Simulate failure
          null
        }
        .`when`(mockImageRepository)
        .downloadImage(eq(profilePicturePath), any(), any())

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

    // Ensure all asynchronous operations complete
    shadowOf(Looper.getMainLooper()).idle()

    // Assert
    assertTrue(onFailureCalled)
    assertEquals("Image download failed", failureException?.message)
    verify(mockImageRepository).downloadImage(eq(profilePicturePath), any(), any())
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
    val userDocumentSnapshot = mock(DocumentSnapshot::class.java)
    val emailDocumentSnapshot = mock(DocumentSnapshot::class.java)
    val mockImageRepository = mock(ImageRepository::class.java)

    // Mock Firestore interactions
    whenever(mockFirestore.collection("Emails")).thenReturn(mockEmailCollectionReference)
    whenever(mockEmailCollectionReference.document(email)).thenReturn(mockEmailDocumentReference)
    whenever(mockEmailDocumentReference.get()).thenReturn(Tasks.forResult(emailDocumentSnapshot))
    whenever(emailDocumentSnapshot.data).thenReturn(mapOf("uid" to uid))

    whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    whenever(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)
    whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(userDocumentSnapshot))
    whenever(userDocumentSnapshot.data)
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
    whenever(helper.documentSnapshotToUser(eq(userDocumentSnapshot), eq(profilePicture)))
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

    // Mock ImageRepository behavior
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(ImageBitmap) -> Unit>(1)
          onSuccess(profilePicture) // Simulate image download success
          null
        }
        .`when`(mockImageRepository)
        .downloadImage(eq(profilePicturePath), any(), any())

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
    verify(mockImageRepository).downloadImage(eq(profilePicturePath), any(), any())
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

    // Mock ImageRepository to simulate failure
    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(exception) // Simulate failure
          null
        }
        .`when`(mockImageRepository)
        .downloadImage(eq(profilePicturePath), any(), any())

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
    verify(mockImageRepository).downloadImage(eq(profilePicturePath), any(), any())
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

    // Inject mock ImageRepository into UserRepositoryFirestore via reflection
    val userRepositoryFirestore = UserRepositoryFirestore(mockFirestore, mock(Context::class.java))
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

    // Inject mock ImageRepository into UserRepositoryFirestore via reflection
    val userRepositoryFirestore = UserRepositoryFirestore(mockFirestore, mock(Context::class.java))
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

    // Mock user document snapshot
    val userSnapshot = mock(DocumentSnapshot::class.java)
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
    val userDocumentSnapshot = mock(DocumentSnapshot::class.java)
    val friend1DocumentSnapshot = mock(DocumentSnapshot::class.java)
    val friend2DocumentSnapshot = mock(DocumentSnapshot::class.java)

    whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    whenever(mockUserCollectionReference.document(user.uid)).thenReturn(mockUserDocumentReference)
    whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(userDocumentSnapshot))
    whenever(userDocumentSnapshot.data).thenReturn(mapOf("friendRequests" to friendRequestsUidList))

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
    // Arrange
    val userDocumentSnapshot = mock(DocumentSnapshot::class.java)

    whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    whenever(mockUserCollectionReference.document(user.uid)).thenReturn(mockUserDocumentReference)
    whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(userDocumentSnapshot))
    whenever(userDocumentSnapshot.data).thenReturn(mapOf("friendRequests" to emptyList<String>()))

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
    val userDocumentSnapshot = mock(DocumentSnapshot::class.java)
    val friendDocumentSnapshot = mock(DocumentSnapshot::class.java)

    // Mock the data returned by the document snapshots
    `when`(userDocumentSnapshot.data).thenReturn(mapOf("friendRequests" to listOf(friend.uid)))
    `when`(friendDocumentSnapshot.data).thenReturn(mapOf("sentFriendRequests" to listOf(user.uid)))

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
    val userRepository = UserRepositoryFirestore(mockFirestore, mock(Context::class.java))

    // Call the deleteFriendRequest method
    userRepository.deleteFriendRequest(
        user.uid,
        friend.uid,
        {
          // Verify the updates to the user document
          verify(userDocumentReference).update("friendRequests", emptyList<String>())

          // Verify the updates to the friend document
          verify(friendDocumentReference).update("sentFriendRequests", emptyList<String>())
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

    // Mock the Firestore document snapshots
    val userDocumentSnapshot = mock(DocumentSnapshot::class.java)
    val friendDocumentSnapshot = mock(DocumentSnapshot::class.java)

    // Mock the data returned by the document snapshots
    `when`(userDocumentSnapshot.data).thenReturn(mapOf("friendRequests" to listOf(friend.uid)))
    `when`(friendDocumentSnapshot.data).thenReturn(mapOf("sentFriendRequests" to listOf(user.uid)))

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

    // Mock the update() method on user document to return a successful task
    `when`(userDocumentReference.update(anyString(), any())).thenReturn(Tasks.forResult(null))
    // Mock the update() method on friend document to return a failed task
    `when`(friendDocumentReference.update(anyString(), any()))
        .thenReturn(Tasks.forException(Exception("Update friend's sentFriendRequests failed")))

    // Create the UserRepositoryFirestore instance
    val userRepository = UserRepositoryFirestore(mockFirestore, mock(Context::class.java))

    var onFailureCalled = false
    var failureException: Exception? = null

    // Call the deleteFriendRequest method
    userRepository.deleteFriendRequest(
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
    verify(friendDocumentReference).update(eq("sentFriendRequests"), any())
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

    // Mock the Firestore document snapshots
    val userDocumentSnapshot = mock(DocumentSnapshot::class.java)
    val friendDocumentSnapshot = mock(DocumentSnapshot::class.java)

    // Mock the data returned by the document snapshots
    `when`(userDocumentSnapshot.data).thenReturn(mapOf("friendRequests" to listOf(friend.uid)))
    `when`(friendDocumentSnapshot.data).thenReturn(mapOf("sentFriendRequests" to listOf(user.uid)))

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

    // Mock the get() and update() methods to return successful tasks
    `when`(userDocumentReference.get()).thenReturn(Tasks.forResult(userDocumentSnapshot))
    `when`(friendDocumentReference.get()).thenReturn(Tasks.forResult(friendDocumentSnapshot))
    `when`(userDocumentReference.update(anyString(), any())).thenReturn(Tasks.forResult(null))
    `when`(friendDocumentReference.update(anyString(), any())).thenReturn(Tasks.forResult(null))

    // Create the UserRepositoryFirestore instance
    val userRepository = UserRepositoryFirestore(mockFirestore, mock(Context::class.java))

    var onSuccessCalled = false
    var onFailureCalled = false

    // Call the deleteFriendRequest method
    userRepository.deleteFriendRequest(
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

    // Mock the Firestore document snapshot
    val userDocumentSnapshot = mock(DocumentSnapshot::class.java)

    // Mock the data returned by the document snapshot
    `when`(userDocumentSnapshot.data).thenReturn(mapOf("friendRequests" to listOf(friend.uid)))

    // Mock the Firestore document reference
    val userDocumentReference = mock(DocumentReference::class.java)

    // Mock the Firestore collection reference
    val userCollectionReference = mock(CollectionReference::class.java)
    `when`(userCollectionReference.document(user.uid)).thenReturn(userDocumentReference)

    // Mock the Firestore instance
    val mockFirestore = mock(FirebaseFirestore::class.java)
    `when`(mockFirestore.collection("Users")).thenReturn(userCollectionReference)

    // Mock the get() method to return the document snapshot
    `when`(userDocumentReference.get()).thenReturn(Tasks.forResult(userDocumentSnapshot))

    // Mock the update() method on user document to return a failed task
    `when`(userDocumentReference.update(anyString(), any()))
        .thenReturn(Tasks.forException(Exception("Update user friendRequests failed")))

    // Create the UserRepositoryFirestore instance
    val userRepository = UserRepositoryFirestore(mockFirestore, mock(Context::class.java))

    var onFailureCalled = false
    var failureException: Exception? = null

    // Call the deleteFriendRequest method
    userRepository.deleteFriendRequest(
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
    verify(userDocumentReference).update(eq("friendRequests"), any())
    // Verify that onFailure was called
    assertTrue(onFailureCalled)
    assertNotNull(failureException)
    assertEquals("Update user friendRequests failed", failureException?.message)
  }

  @Test
  fun getRecommendedFriends_shouldReturnRecommendedFriends() {
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
    val nonFriend =
        User(
            "3",
            "John",
            "Smith",
            "+44 00 000 00 03",
            null,
            "john.smith@example.com",
            lastKnownLocation = location)
    val blockedUser =
        User(
            "4",
            "Blocked",
            "User",
            "+44 00 000 00 04",
            null,
            "blocked.user@example.com",
            lastKnownLocation = location)
    val userWhoBlockedMe =
        User(
            "5",
            "Blocker",
            "User",
            "+44 00 000 00 05",
            null,
            "blocker.user@example.com",
            lastKnownLocation = location)

    // Mock the Firestore document snapshots
    val userDocumentSnapshot = mock(DocumentSnapshot::class.java)
    val allUsersQuerySnapshot = mock(QuerySnapshot::class.java)
    val friendDocumentSnapshot = mock(DocumentSnapshot::class.java)
    val nonFriendDocumentSnapshot = mock(DocumentSnapshot::class.java)
    val blockedUserDocumentSnapshot = mock(DocumentSnapshot::class.java)
    val userWhoBlockedMeDocumentSnapshot = mock(DocumentSnapshot::class.java)

    // Mock the data returned by the document snapshots
    `when`(userDocumentSnapshot.data)
        .thenReturn(
            mapOf("friendsList" to listOf(friend.uid), "blockedList" to listOf(blockedUser.uid)))
    `when`(friendDocumentSnapshot.data)
        .thenReturn(mapOf("uid" to friend.uid, "blockedList" to emptyList<String>()))
    `when`(nonFriendDocumentSnapshot.data)
        .thenReturn(mapOf("uid" to nonFriend.uid, "blockedList" to emptyList<String>()))
    `when`(blockedUserDocumentSnapshot.data)
        .thenReturn(mapOf("uid" to blockedUser.uid, "blockedList" to emptyList<String>()))
    `when`(userWhoBlockedMeDocumentSnapshot.data)
        .thenReturn(mapOf("uid" to userWhoBlockedMe.uid, "blockedList" to listOf(user.uid)))

    // Set document IDs
    `when`(friendDocumentSnapshot.id).thenReturn(friend.uid)
    `when`(nonFriendDocumentSnapshot.id).thenReturn(nonFriend.uid)
    `when`(blockedUserDocumentSnapshot.id).thenReturn(blockedUser.uid)
    `when`(userWhoBlockedMeDocumentSnapshot.id).thenReturn(userWhoBlockedMe.uid)

    // Mock the Firestore document references
    val userDocumentReference = mock(DocumentReference::class.java)
    val userCollectionReference = mock(CollectionReference::class.java)

    // Mock the Firestore instance
    val mockFirestore = mock(FirebaseFirestore::class.java)
    `when`(mockFirestore.collection("Users")).thenReturn(userCollectionReference)
    `when`(userCollectionReference.document(user.uid)).thenReturn(userDocumentReference)

    // Mock the get() method to return the document snapshots
    `when`(userDocumentReference.get()).thenReturn(Tasks.forResult(userDocumentSnapshot))
    `when`(userCollectionReference.get()).thenReturn(Tasks.forResult(allUsersQuerySnapshot))
    `when`(allUsersQuerySnapshot.documents)
        .thenReturn(
            listOf(
                friendDocumentSnapshot,
                nonFriendDocumentSnapshot,
                blockedUserDocumentSnapshot,
                userWhoBlockedMeDocumentSnapshot))

    // Create the UserRepositoryFirestore instance
    val userRepository = UserRepositoryFirestore(mockFirestore, mock(Context::class.java))

    // Call the getRecommendedFriends method
    userRepository.getRecommendedFriends(
        user.uid,
        { recommendedFriends ->
          // Verify the recommended friends list
          assertEquals(1, recommendedFriends.size)
          assertEquals(nonFriend.uid, recommendedFriends[0].user.uid)

          // Verify that blocked users and users who blocked me are not in the list
          assertFalse(recommendedFriends.any { it.user.uid == blockedUser.uid })
          assertFalse(recommendedFriends.any { it.user.uid == userWhoBlockedMe.uid })
          // Verify that existing friends are not in the list
          assertFalse(recommendedFriends.any { it.user.uid == friend.uid })
        },
        { fail("onFailure should not be called") })
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
    val userRepository = UserRepositoryFirestore(mockFirestore, mock(Context::class.java))

    // Call the createFriend method
    userRepository.createFriend(
        user.uid,
        friend.uid,
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
  fun createFriend_shouldNotWork() {
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
                "sentFriendRequests" to listOf()))

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
    val userRepository = UserRepositoryFirestore(mockFirestore, mock(Context::class.java))

    // Call the createFriend method
    userRepository.createFriend(
        user.uid,
        friend.uid,
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
  fun createFriendFailureWhenGetUserDocumentFails() {
    val friend =
        User(
            "2",
            "Jane",
            "Doe",
            "+41 00 000 00 02",
            null,
            "jane.doe@example.com",
            lastKnownLocation = location)
    // Arrange
    val exception = Exception("Failed to get user document")
    `when`(mockUserDocumentReference.get()).thenReturn(Tasks.forException(exception))

    // Act
    val latch = CountDownLatch(1)
    var successCalled = false
    var failureCalled = false
    var failureException: Exception? = null

    userRepositoryFirestore.createFriend(
        user.uid,
        friend.uid,
        onSuccess = {
          successCalled = true
          latch.countDown()
        },
        onFailure = { exception ->
          failureCalled = true
          failureException = exception
          latch.countDown()
        })

    shadowOf(Looper.getMainLooper()).idle() // Ensure all asynchronous operations complete

    // Assert
    assertFalse(successCalled)
    assertTrue(failureCalled)
    assertNotNull(failureException)
    assertEquals("Failed to get user document", failureException?.message)
  }

  @Test
  fun createFriendFailureWhenUpdatingUserDocumentFails() {
    val friend =
        User(
            "2",
            "Jane",
            "Doe",
            "+41 00 000 00 02",
            null,
            "jane.doe@example.com",
            lastKnownLocation = location)
    // Arrange
    val exception = Exception("Failed to update user document")
    val userData =
        mapOf(
            "friendsList" to listOf<String>(),
            "friendRequests" to listOf(friend.uid),
            "sentFriendRequests" to listOf<String>())

    `when`(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
    `when`(mockUserDocumentSnapshot.data).thenReturn(userData)
    `when`(mockUserDocumentReference.update(anyString(), any()))
        .thenReturn(Tasks.forException(exception))

    // Act
    val latch = CountDownLatch(1)
    var successCalled = false
    var failureCalled = false
    var failureException: Exception? = null

    userRepositoryFirestore.createFriend(
        user.uid,
        friend.uid,
        onSuccess = {
          successCalled = true
          latch.countDown()
        },
        onFailure = { exception ->
          failureCalled = true
          failureException = exception
          latch.countDown()
        })

    shadowOf(Looper.getMainLooper()).idle() // Ensure all asynchronous operations complete

    // Assert
    assertFalse(successCalled)
    assertTrue(failureCalled)
    assertNotNull(failureException)
    assertEquals("Failed to update user document", failureException?.message)
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
    val userRepository = UserRepositoryFirestore(mockFirestore, mock(Context::class.java))

    // Call the createFriendRequest method
    userRepository.createFriendRequest(
        user.uid,
        friend.uid,
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
   * This test validates the `getUserFriends` function of `UserRepositoryFirestore`. It ensures that
   * when the user's friends are fetched, the appropriate image repository function
   * (`downloadImage`) is called.
   *
   * We are mocking Firestore calls, the Task API, and the ImageBitmap repository to simulate the
   * entire flow without requiring actual network or database calls.
   */
  @Test
  fun getUserFriends_callsImageRepository() {
    mockStatic(Tasks::class.java).use { mockedStatic ->
      // The UID of the friend we are testing for
      val friendUid = "123"

      // Mocking Firestore references
      val mockedFriendsDocumentReference = mock(DocumentReference::class.java)

      // Simulating the path of the profile picture of the user
      val profilePicturePath = "gs://sudden-bump-swent.appspot.com/profilePictures/${user.uid}.jpeg"
      val profilePicture = mock(ImageBitmap::class.java) // Mocking the ImageBitmap object
      val mockTask =
          mock(Task::class.java) as Task<DocumentSnapshot> // Mock task for fetching user document
      val mockTaskFriends =
          mock(Task::class.java)
              as Task<DocumentSnapshot> // Mock task for fetching friend's document
      val mockTaskList =
          mock(Task::class.java)
              as Task<List<DocumentSnapshot>> // Mock task for fetching a list of documents
      val mockHelper = mock(UserRepositoryFirestoreHelper::class.java)
      val mockImage = mock(ImageBitmap::class.java)

      // Setting up mocks for Firestore interactions
      `when`(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
      `when`(mockUserCollectionReference.document(user.uid)).thenReturn(mockUserDocumentReference)
      `when`(mockUserCollectionReference.document(friendUid))
          .thenReturn(mockedFriendsDocumentReference)
      `when`(mockUserDocumentReference.get()).thenReturn(mockTask) // Fetch the user's document
      `when`(mockTask.isSuccessful).thenReturn(true) // Simulate a successful task
      `when`(mockTask.result)
          .thenReturn(mockUserDocumentSnapshot) // Return a mocked snapshot of the user document
      `when`(mockTask.addOnFailureListener(any()))
          .thenReturn(mockTask) // Add failure listener (we won't use it)
      `when`(mockUserDocumentSnapshot.data)
          .thenReturn(mapOf("friendsList" to listOf(friendUid))) // Mocking the friends list data

      // Setting up mock for fetching the friend's document
      `when`(mockedFriendsDocumentReference.get()).thenReturn(mockTaskFriends)
      `when`(mockTaskFriends.isSuccessful).thenReturn(true) // Simulate a successful task for friend
      `when`(mockTaskFriends.result)
          .thenReturn(mockedFriendsDocumentSnapshot) // Return a mocked snapshot for friend document

      // Mock the data of the friend's document
      `when`(mockedFriendsDocumentSnapshot.data)
          .thenReturn(
              mapOf(
                  "uid" to user.uid,
                  "firstName" to user.firstName,
                  "lastName" to user.lastName,
                  "phoneNumber" to user.phoneNumber,
                  "emailAddress" to user.emailAddress))

      // Answering the call to addOnSuccessListener and simulating success with the mock data
      doAnswer { invocation ->
            val listener = invocation.arguments[0] as OnSuccessListener<DocumentSnapshot>
            listener.onSuccess(
                mockUserDocumentSnapshot) // Call onSuccess with the mock user document snapshot
            mockTask // Return the mock task
          }
          .`when`(mockTask)
          .addOnSuccessListener(any()) // Mocking the success listener being added for this task

      // Mocking the downloadImage function in the image repository
      doAnswer { invocation ->
            val pathArgument = invocation.getArgument<String>(0)
            val onSuccess = invocation.getArgument<(ImageBitmap) -> Unit>(1)
            onSuccess(profilePicture) // Simulate the image download success, providing a mocked
            // ImageBitmap
            null // No return needed
          }
          .`when`(mockImageRepository)
          .downloadImage(eq(profilePicturePath), any(), any())

      // Mocking Tasks.whenAllSuccess to handle multiple tasks
      `when`(Tasks.whenAllSuccess<DocumentSnapshot>(any<List<Task<DocumentSnapshot>>>()))
          .thenReturn(mockTaskList)

      val mockedList = mock(List::class.java) as List<DocumentSnapshot>
      val mockedIterator = mock(Iterator::class.java) as Iterator<DocumentSnapshot>
      `when`(mockedList.iterator()).thenReturn(mockedIterator)
      `when`(mockedIterator.hasNext()).thenReturn(true, false)
      `when`(mockedIterator.next()).thenReturn(mockedFriendsDocumentSnapshot)

      // Answering the call to addOnSuccessListener on the list of tasks (for friends)
      doAnswer { invocation ->
            val listener = invocation.arguments[0] as OnSuccessListener<List<DocumentSnapshot>>
            listener.onSuccess(mockedList) // Simulate success with a list of friend
            // documents
            mockTaskList // Return the mock task list
          }
          .`when`(mockTaskList)
          .addOnSuccessListener(any()) // Mock the success listener for this task list

      // Simulate no failure listener being called for the mockTaskList
      doAnswer { mockTaskList }.`when`(mockTaskList).addOnFailureListener(any())

      // Access the private field for imageRepository in the UserRepositoryFirestore class
      val imageRepositoryField =
          UserRepositoryFirestore::class.java.getDeclaredField("imageRepository")
      imageRepositoryField.isAccessible = true // Make the field accessible
      imageRepositoryField.set(
          userRepositoryFirestore, mockImageRepository) // Inject the mock image repository

      val helperField = UserRepositoryFirestore::class.java.getDeclaredField("helper")
      helperField.isAccessible = true
      helperField.set(userRepositoryFirestore, mockHelper)

      doAnswer { invocation ->
            val documentArgument = invocation.arguments[0] as DocumentSnapshot
            val profilePictureArgument = invocation.arguments[1] as ImageBitmap?
            UserRepositoryFirestoreHelper()
                .documentSnapshotToUser(documentArgument, profilePictureArgument)
          }
          .`when`(mockHelper)
          .documentSnapshotToUser(any(), any())

      doAnswer { invocation ->
            val uidArgument = invocation.arguments[0] as String
            val referenceArgument = invocation.arguments[1] as StorageReference
            UserRepositoryFirestoreHelper().uidToProfilePicturePath(uidArgument, referenceArgument)
          }
          .`when`(mockHelper)
          .uidToProfilePicturePath(any(), any())

      doAnswer { invocation ->
            val onSuccess = invocation.getArgument<(ImageBitmap) -> Unit>(1)
            onSuccess(mockImage)
          }
          .`when`(mockImageRepository)
          .downloadImageAsync(any(), any(), any())

      // Call the method under test: getUserFriends
      userRepositoryFirestore.getUserFriends(
          uid = user.uid,
          onSuccess = {}, // Simulate success callback
          onFailure = {
            fail("Failure callback should not be called")
          }) // Fail if the failure callback is called

      // Verify that the image repository's downloadImage function was called
      verify(mockImageRepository).downloadImageAsync(any(), any(), any())
      verify(mockHelper).documentSnapshotToUser(any(), any())
      verify(mockedList).size
    }
  }

  /**
   * This test validates the `getUserFriends` function of `UserRepositoryFirestore`. It ensures that
   * when the user's friends are fetched, the appropriate image repository function
   * (`downloadImage`) is called.
   *
   * We are mocking Firestore calls, the Task API, and the ImageBitmap repository to simulate the
   * entire flow without requiring actual network or database calls.
   */
  @Test
  fun getUserFriends_failsCallsSize() {
    mockStatic(Tasks::class.java).use { mockedStatic ->
      // The UID of the friend we are testing for
      val friendUid = "123"

      // Mocking Firestore references
      val mockedFriendsDocumentReference = mock(DocumentReference::class.java)

      // Simulating the path of the profile picture of the user
      val profilePicturePath = "gs://sudden-bump-swent.appspot.com/profilePictures/${user.uid}.jpeg"
      val profilePicture = mock(ImageBitmap::class.java) // Mocking the ImageBitmap object
      val mockTask =
          mock(Task::class.java) as Task<DocumentSnapshot> // Mock task for fetching user document
      val mockTaskFriends =
          mock(Task::class.java)
              as Task<DocumentSnapshot> // Mock task for fetching friend's document
      val mockTaskList =
          mock(Task::class.java)
              as Task<List<DocumentSnapshot>> // Mock task for fetching a list of documents
      val mockHelper = mock(UserRepositoryFirestoreHelper::class.java)
      val mockImage = mock(ImageBitmap::class.java)

      // Setting up mocks for Firestore interactions
      `when`(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
      `when`(mockUserCollectionReference.document(user.uid)).thenReturn(mockUserDocumentReference)
      `when`(mockUserCollectionReference.document(friendUid))
          .thenReturn(mockedFriendsDocumentReference)
      `when`(mockUserDocumentReference.get()).thenReturn(mockTask) // Fetch the user's document
      `when`(mockTask.isSuccessful).thenReturn(true) // Simulate a successful task
      `when`(mockTask.result)
          .thenReturn(mockUserDocumentSnapshot) // Return a mocked snapshot of the user document
      `when`(mockTask.addOnFailureListener(any()))
          .thenReturn(mockTask) // Add failure listener (we won't use it)
      `when`(mockUserDocumentSnapshot.data)
          .thenReturn(mapOf("friendsList" to listOf(friendUid))) // Mocking the friends list data

      // Setting up mock for fetching the friend's document
      `when`(mockedFriendsDocumentReference.get()).thenReturn(mockTaskFriends)
      `when`(mockTaskFriends.isSuccessful).thenReturn(true) // Simulate a successful task for friend
      `when`(mockTaskFriends.result)
          .thenReturn(mockedFriendsDocumentSnapshot) // Return a mocked snapshot for friend document

      // Mock the data of the friend's document
      `when`(mockedFriendsDocumentSnapshot.data)
          .thenReturn(
              mapOf(
                  "uid" to user.uid,
                  "firstName" to user.firstName,
                  "lastName" to user.lastName,
                  "phoneNumber" to user.phoneNumber,
                  "emailAddress" to user.emailAddress))

      // Answering the call to addOnSuccessListener and simulating success with the mock data
      doAnswer { invocation ->
            val listener = invocation.arguments[0] as OnSuccessListener<DocumentSnapshot>
            listener.onSuccess(
                mockUserDocumentSnapshot) // Call onSuccess with the mock user document snapshot
            mockTask // Return the mock task
          }
          .`when`(mockTask)
          .addOnSuccessListener(any()) // Mocking the success listener being added for this task

      // Mocking the downloadImage function in the image repository
      doAnswer { invocation ->
            val pathArgument = invocation.getArgument<String>(0)
            val onSuccess = invocation.getArgument<(ImageBitmap) -> Unit>(1)
            onSuccess(profilePicture) // Simulate the image download success, providing a mocked
            // ImageBitmap
            null // No return needed
          }
          .`when`(mockImageRepository)
          .downloadImage(eq(profilePicturePath), any(), any())

      // Mocking Tasks.whenAllSuccess to handle multiple tasks
      `when`(Tasks.whenAllSuccess<DocumentSnapshot>(any<List<Task<DocumentSnapshot>>>()))
          .thenReturn(mockTaskList)

      val mockedList = mock(List::class.java) as List<DocumentSnapshot>
      val mockedIterator = mock(Iterator::class.java) as Iterator<DocumentSnapshot>
      `when`(mockedList.iterator()).thenReturn(mockedIterator)
      `when`(mockedIterator.hasNext()).thenReturn(true, false)
      `when`(mockedIterator.next()).thenReturn(mockedFriendsDocumentSnapshot)

      // Answering the call to addOnSuccessListener on the list of tasks (for friends)
      doAnswer { invocation ->
            val listener = invocation.arguments[0] as OnSuccessListener<List<DocumentSnapshot>>
            listener.onSuccess(mockedList) // Simulate success with a list of friend
            // documents
            mockTaskList // Return the mock task list
          }
          .`when`(mockTaskList)
          .addOnSuccessListener(any()) // Mock the success listener for this task list

      // Simulate no failure listener being called for the mockTaskList
      doAnswer { mockTaskList }.`when`(mockTaskList).addOnFailureListener(any())

      // Access the private field for imageRepository in the UserRepositoryFirestore class
      val imageRepositoryField =
          UserRepositoryFirestore::class.java.getDeclaredField("imageRepository")
      imageRepositoryField.isAccessible = true // Make the field accessible
      imageRepositoryField.set(
          userRepositoryFirestore, mockImageRepository) // Inject the mock image repository

      val helperField = UserRepositoryFirestore::class.java.getDeclaredField("helper")
      helperField.isAccessible = true
      helperField.set(userRepositoryFirestore, mockHelper)

      doAnswer { invocation ->
            val documentArgument = invocation.arguments[0] as DocumentSnapshot
            val profilePictureArgument = invocation.arguments[1] as ImageBitmap?
            UserRepositoryFirestoreHelper()
                .documentSnapshotToUser(documentArgument, profilePictureArgument)
          }
          .`when`(mockHelper)
          .documentSnapshotToUser(any(), any())

      doAnswer { invocation ->
            val uidArgument = invocation.arguments[0] as String
            val referenceArgument = invocation.arguments[1] as StorageReference
            UserRepositoryFirestoreHelper().uidToProfilePicturePath(uidArgument, referenceArgument)
          }
          .`when`(mockHelper)
          .uidToProfilePicturePath(any(), any())

      doAnswer { invocation ->
            val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
            onFailure(Exception("Mocked Exception"))
          }
          .`when`(mockImageRepository)
          .downloadImageAsync(any(), any(), any())

      // Call the method under test: getUserFriends
      userRepositoryFirestore.getUserFriends(
          uid = user.uid,
          onSuccess = {}, // Simulate success callback
          onFailure = {
            fail("Failure callback should not be called")
          }) // Fail if the failure callback is called

      // Verify that the image repository's downloadImage function was called
      verify(mockedList).size
    }
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
        uid = user.uid,
        onSuccess = {},
        onFailure = { fail("Failure callback should not be called") })

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
        uid = user.uid, friendsList = listOf(user), onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockUserDocumentReference).update(anyString(), any())
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

    // Mock the Firestore document snapshots
    val currentUserSnapshot = mock(DocumentSnapshot::class.java)
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

    `when`(currentUserSnapshot.data).thenReturn(currentUserData)
    `when`(blockedUserSnapshot.data).thenReturn(blockedUserData)

    // Mock the Firestore document references
    val currentUserRef = mock(DocumentReference::class.java)
    val blockedUserRef = mock(DocumentReference::class.java)

    // Mock the Firestore collection reference
    val userCollectionReference = mock(CollectionReference::class.java)
    `when`(userCollectionReference.document(currentUser.uid)).thenReturn(currentUserRef)
    `when`(userCollectionReference.document(blockedUser.uid)).thenReturn(blockedUserRef)

    // Mock the Firestore instance
    val mockFirestore = mock(FirebaseFirestore::class.java)
    `when`(mockFirestore.collection("Users")).thenReturn(userCollectionReference)

    // Mock transaction behavior
    `when`(transaction.get(currentUserRef)).thenReturn(currentUserSnapshot)
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

    // Create the UserRepositoryFirestore instance
    val userRepository = UserRepositoryFirestore(mockFirestore, mock(Context::class.java))

    var onSuccessCalled = false
    var onFailureCalled = false

    // Call blockUser
    userRepository.blockUser(
        currentUser,
        blockedUser,
        onSuccess = { onSuccessCalled = true },
        onFailure = { onFailureCalled = true })

    // Ensure all asynchronous operations complete
    shadowOf(Looper.getMainLooper()).idle()

    // Verify the transaction updates
    verify(transaction).update(eq(currentUserRef), eq("blockedList"), eq(listOf(blockedUser.uid)))
    verify(transaction).update(eq(currentUserRef), eq("friendsList"), eq(emptyList<String>()))
    verify(transaction).update(eq(currentUserRef), eq("friendRequests"), eq(emptyList<String>()))
    verify(transaction)
        .update(eq(currentUserRef), eq("sentFriendRequests"), eq(emptyList<String>()))

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

    // Mock the Firestore collection reference
    val userCollectionReference = mock(CollectionReference::class.java)
    `when`(userCollectionReference.document(any())).thenReturn(mock(DocumentReference::class.java))

    // Mock the Firestore instance
    val mockFirestore = mock(FirebaseFirestore::class.java)
    `when`(mockFirestore.collection("Users")).thenReturn(userCollectionReference)

    // Mock runTransaction to fail
    val exception = Exception("Transaction failed")
    `when`(mockFirestore.runTransaction<Void>(any())).thenReturn(Tasks.forException(exception))

    // Create the UserRepositoryFirestore instance
    val userRepository = UserRepositoryFirestore(mockFirestore, mock(Context::class.java))

    var onSuccessCalled = false
    var onFailureCalled = false
    var failureException: Exception? = null

    // Call blockUser
    userRepository.blockUser(
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

  /**
   * This test verifies that when fetching a new user's Location, the Firestore `set()` is called on
   * the document reference. This does NOT CHECK the actual data being added
   */
  @Test
  fun setLocation_shouldCallFirestoreCollection() {
    `when`(mockUserDocumentReference.update(anyString(), any())).thenReturn(mockTaskVoid)

    userRepositoryFirestore.updateUserLocation(
        uid = user.uid, location = location.value, onSuccess = {}, onFailure = {})

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

    val userFriends = MutableStateFlow<List<User>>(emptyList())

    val location1 = mock(Location::class.java)
    val snapshot1 = mock(DocumentSnapshot::class.java)
    val snapshot2 = mock(DocumentSnapshot::class.java)

    // Mock the snapshot locations
    whenever(snapshot1.get("lastKnownLocation")).thenReturn(location1)
    whenever(snapshot2.get("lastKnownLocation")).thenReturn(null)

    // Mock the user repository to return the snapshots
    val userRepositoryFirestore = mock(UserRepositoryFirestore::class.java)
    whenever(userRepositoryFirestore.getUserFriends(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<User>) -> Unit>(1)
      val result = listOf(friend1, friend2)
      onSuccess(result)
    }

    // Define the expected list
    val expectedList = listOf(friend1, friend2)

    // When
    userRepositoryFirestore.getUserFriends(
        user.uid,
        onSuccess = { friends ->
          // Update the state with the locations of friends
          userFriends.value = friends
          assertEquals(expectedList, friends)
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
    whenever(snapshot1.get("lastKnownLocation")).thenReturn(mock(Location::class.java))
    whenever(snapshot2.get("lastKnownLocation")).thenReturn(null)

    // When
    userRepositoryFirestore.getUserFriends(
        user.uid,
        { friends ->
          // Then
          assert(friends == emptyList<User>())
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

  // inspired by Ilyas's tests for verifyNoAccountExists
  @Test
  fun verifyUnusedPhoneNumberCallsDocuments() {

    `when`(mockPhoneCollectionReference.get()).thenReturn(Tasks.forResult(mockPhoneQuerySnapshot))
    `when`(mockUserQuerySnapshot.documents).thenReturn(listOf())

    `when`(mockPhoneDocumentReference.get()).thenReturn(Tasks.forResult(mockPhoneDocumentSnapshot))

    userRepositoryFirestore.verifyUnusedPhoneNumber(
        user.phoneNumber,
        onSuccess = {},
        onFailure = { fail("Failure callback should not be called") })

    verify(timeout(100)) { (mockUserQuerySnapshot).documents }
  }

  // inspired by Ilyas's tests for verifyNoAccountExists
  @Test
  fun verifyUnusedPhoneNumberSuccessfulWithPhoneNotInResultCallsOnSuccessTrue() {
    // Arrange
    val phoneNumber = "+41791234567"
    val documents = listOf<DocumentSnapshot>() // No emails in the result
    val mockTask = mock(Task::class.java) as Task<QuerySnapshot>
    val mockQuerySnapshot = mock(QuerySnapshot::class.java)

    `when`(mockTask.addOnCompleteListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnCompleteListener<QuerySnapshot>
      listener.onComplete(mockTask)
      mockTask
    }
    `when`(mockTask.addOnFailureListener(any())).thenReturn(mockTask)
    `when`(mockTask.isSuccessful).thenReturn(true)
    `when`(mockTask.result).thenReturn(mockQuerySnapshot)
    `when`(mockQuerySnapshot.documents).thenReturn(documents)

    `when`(mockPhoneCollectionReference.get()).thenReturn(mockTask)

    // Act
    userRepositoryFirestore.verifyUnusedPhoneNumber(
        phoneNumber = phoneNumber,
        onSuccess = { result -> assertTrue(result) },
        onFailure = { fail("Failure callback should not be called") })
  }

  // inspired by Ilyas's tests for verifyNoAccountExists
  @Test
  fun verifyUnusedPhoneNumberFailureCallsOnFailure() {
    // Arrange
    val phoneNumber = "+41791234567"
    val exception = Exception("Query failed")
    val mockTask = mock(Task::class.java) as Task<QuerySnapshot>

    `when`(mockTask.addOnCompleteListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnCompleteListener<QuerySnapshot>
      listener.onComplete(mockTask)
      mockTask
    }
    `when`(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnFailureListener
      listener.onFailure(exception)
      mockTask
    }
    `when`(mockTask.isSuccessful).thenReturn(false)
    `when`(mockTask.exception).thenReturn(exception)

    `when`(mockPhoneCollectionReference.get()).thenReturn(mockTask)

    // Act
    userRepositoryFirestore.verifyUnusedPhoneNumber(
        phoneNumber = phoneNumber,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { error ->
          // Assert
          assertEquals("Query failed", error.message)
        })
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

    // Mock the Firestore document snapshots
    val userDocumentSnapshot = mock(DocumentSnapshot::class.java)
    val friendDocumentSnapshot = mock(DocumentSnapshot::class.java)

    // Mock the data returned by the document snapshots
    `when`(userDocumentSnapshot.data).thenReturn(mapOf("locationSharedWith" to emptyList<String>()))
    `when`(friendDocumentSnapshot.data).thenReturn(mapOf("locationSharedBy" to emptyList<String>()))

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
    val userRepository = UserRepositoryFirestore(mockFirestore, mock(Context::class.java))

    // Call the deleteFriendRequest method
    userRepository.shareLocationWithFriend(
        user.uid,
        friend.uid,
        {
          // Verify the updates to the user document
          verify(userDocumentReference).update("locationSharedWith", listOf(friend.uid))

          // Verify the updates to the friend document
          verify(friendDocumentReference).update("locationSharedBy", listOf(user.uid))
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

  @Test
  fun saveNotifiedFriends_shouldSaveListAsJsonString() {
    val friendsUID = listOf("user1", "user2", "user3")
    val gson = Gson()
    val expectedJson = gson.toJson(friendsUID)

    // Act
    userRepositoryFirestore.saveNotifiedFriends(friendsUID)

    // Verify JSON string is saved in SharedPreferences
    verify(sharedPreferencesEditor).putString("notified_friends", expectedJson)
    verify(sharedPreferencesEditor).apply()
  }

  @Test
  fun getSavedAlreadyNotifiedFriends_shouldReturnSavedList() {
    val friendsUID = listOf("user1", "user2", "user3")
    val gson = Gson()
    val jsonString = gson.toJson(friendsUID)

    // Mock SharedPreferences to return the JSON string
    `when`(sharedPreferences.getString("notified_friends", null)).thenReturn(jsonString)

    // Act
    val result = userRepositoryFirestore.getSavedAlreadyNotifiedFriends()

    // Assert the result matches the expected list
    assertEquals(friendsUID, result)
  }

  @Test
  fun getSavedAlreadyNotifiedFriends_shouldReturnEmptyListWhenNoData() {
    // Mock SharedPreferences to return null
    `when`(sharedPreferences.getString("notified_friends", null)).thenReturn(null)

    // Act
    val result = userRepositoryFirestore.getSavedAlreadyNotifiedFriends()

    // Assert the result is an empty list
    assertEquals(emptyList<String>(), result)
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
}
