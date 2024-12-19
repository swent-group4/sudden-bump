package com.swent.suddenbump.model.user.userRepositoryFirestoreTests

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Looper
import androidx.compose.ui.graphics.ImageBitmap
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
import com.google.firebase.storage.StorageReference
import com.swent.suddenbump.model.image.ImageRepository
import com.swent.suddenbump.model.user.SharedPreferencesManager
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import com.swent.suddenbump.model.user.UserRepositoryFirestoreHelper
import com.swent.suddenbump.worker.WorkerScheduler
import junit.framework.TestCase.assertTrue
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.isAccessible
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
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class MiscellaneousTests {

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

  @Mock private lateinit var mockedFriendsDocumentSnapshot: DocumentSnapshot

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

  /**
   * This test verifies that when fetching a new user's Location, the Firestore `set()` is called on
   * the document reference. This does NOT CHECK the actual data being added
   */
  @Test
  fun setLocation_shouldCallFirestoreCollection() {
    `when`(mockUserDocumentReference.update(anyString(), any())).thenReturn(mockTaskVoid)

    userRepositoryFirestore.updateUserLocation(
        uid = user.uid, location = location, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockUserDocumentReference).update(anyString(), any())
  }

  @Test
  fun getNewUid() {
    `when`(mockUserDocumentReference.id).thenReturn("1")
    val uid = userRepositoryFirestore.getNewUid()
    assert(uid == "1")
  }

  @Test
  fun initShouldCallImageRepositoryInitAndInvokeOnSuccess() {
    // Arrange
    val mockImageRepository = mock(ImageRepository::class.java)

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
  fun documentSnapshotToUserListWorks() {
    mockStatic(Tasks::class.java).use { mockedStatic ->
      val profilePicture = mock(ImageBitmap::class.java)
      val mockImage = mock(ImageBitmap::class.java)
      val mockTaskList = mock(Task::class.java) as Task<List<DocumentSnapshot>>

      // Mock the data of the friend's document
      `when`(mockedFriendsDocumentSnapshot.data)
          .thenReturn(
              mapOf(
                  "uid" to user.uid,
                  "firstName" to user.firstName,
                  "lastName" to user.lastName,
                  "phoneNumber" to user.phoneNumber,
                  "emailAddress" to user.emailAddress))

      val uidJsonList = user.uid
      val uidList = listOf(user.uid)
      // Mock documentSnapshotToUser behavior
      val helperMocked = mock(UserRepositoryFirestoreHelper::class.java)
      `when`(helperMocked.documentSnapshotToList(uidJsonList)).thenReturn(uidList)
      // Inject the mock helper into the repository
      val helperField = UserRepositoryFirestore::class.java.getDeclaredField("helper")
      helperField.isAccessible = true
      helperField.set(userRepositoryFirestore, helper)

      // Access the private field for imageRepository in the UserRepositoryFirestore class
      val imageRepositoryField =
          UserRepositoryFirestore::class.java.getDeclaredField("imageRepository")
      imageRepositoryField.isAccessible = true // Make the field accessible
      imageRepositoryField.set(
          userRepositoryFirestore, mockImageRepository) // Inject the mock image repository

      // Mock Task
      val mocked = mock(Task::class.java) as Task<DocumentSnapshot>

      `when`(mockUserCollectionReference.document(any())).thenReturn(mockUserDocumentReference)
      `when`(mockUserDocumentReference.get()).thenReturn(mocked)
      `when`(mocked.isSuccessful).thenReturn(true)
      `when`(mocked.isCanceled).thenReturn(false)
      `when`(mocked.result).thenReturn(mockUserDocumentSnapshot)
      `when`(mocked.addOnFailureListener(any())).thenReturn(mocked)
      doAnswer { invocation ->
            val listener = invocation.arguments[0] as OnSuccessListener<DocumentSnapshot>
            listener.onSuccess(mockUserDocumentSnapshot)
            mocked
          }
          .`when`(mocked)
          .addOnSuccessListener(any())
      `when`(mocked.result).thenReturn(mockUserDocumentSnapshot)

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

      doAnswer { invocation ->
            val onSuccess = invocation.getArgument<(ImageBitmap) -> Unit>(1)
            onSuccess(mockImage)
          }
          .`when`(mockImageRepository)
          .downloadImageAsync(any(), any(), any())

      val urfClass = userRepositoryFirestore::class
      val documentSnapshotToUserListFunction =
          urfClass.declaredFunctions.first { it.name == "documentSnapshotToUserList" }
      documentSnapshotToUserListFunction.isAccessible = true
      documentSnapshotToUserListFunction.call(
          userRepositoryFirestore, uidJsonList, { it: List<User> -> println(it) })

      verify(mockImageRepository).downloadImageAsync(any(), any(), any())
    }
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
  fun logoutUserShouldUnscheduleLocationUpdatesAndClearPreferences() {
    // Act
    userRepositoryFirestore.logoutUser()

    // Assert
    // Verify that the location update worker was unscheduled
    verify(mockWorkerScheduler).unscheduleLocationUpdateWorker()

    // Verify that shared preferences were cleared
    verify(mockSharedPreferencesManager).clearPreferences()
  }
}
