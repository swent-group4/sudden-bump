package com.swent.suddenbump.model.user.userRepositoryFirestoreTests

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Looper
import android.util.Log
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
import com.google.firebase.firestore.Transaction
import com.google.firebase.storage.StorageReference
import com.swent.suddenbump.model.image.ImageRepository
import com.swent.suddenbump.model.user.SharedPreferencesManager
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import com.swent.suddenbump.model.user.UserRepositoryFirestoreHelper
import com.swent.suddenbump.worker.WorkerScheduler
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
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class UserFriendsTests {

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

  @Test
  fun getUserFriendsLocationSuccessWithFriendsAndLocations() {
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
  fun getUserFriendsLocationSuccessWithNoFriends() {
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
    val mockFriendDocumentSnapshot = mock(DocumentSnapshot::class.java)

    // Mock the data returned by the document snapshots
    `when`(mockUserDocumentSnapshot.data)
        .thenReturn(
            mapOf(
                "friendsList" to listOf<String>(),
                "friendRequests" to listOf(friend.uid),
                "sentFriendRequests" to listOf<String>()))
    `when`(mockFriendDocumentSnapshot.data)
        .thenReturn(
            mapOf(
                "friendsList" to listOf<String>(),
                "friendRequests" to listOf<String>(),
                "sentFriendRequests" to listOf(user.uid)))

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

    // Call the createFriend method
    userRepositoryFirestore.createFriend(
        user.uid,
        friend.uid,
        {
          // Verify the updates to the user document
          verify(mockUserDocumentReference).update("friendRequests", listOf<String>())
          verify(mockUserDocumentReference).update("friendsList", listOf(friend.uid))

          // Verify the updates to the friend document
          verify(mockFriendDocumentReference).update("friendsList", listOf(user.uid))
          verify(mockFriendDocumentReference).update("friendRequests", listOf<String>())
          verify(mockFriendDocumentReference).update("sentFriendRequests", listOf<String>())
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

    val mockFriendDocumentSnapshot = mock(DocumentSnapshot::class.java)

    // Mock the data returned by the document snapshots
    `when`(mockUserDocumentSnapshot.data)
        .thenReturn(
            mapOf(
                "friendsList" to listOf<String>(),
                "friendRequests" to listOf(friend.uid),
                "sentFriendRequests" to listOf<String>()))
    `when`(mockFriendDocumentSnapshot.data)
        .thenReturn(
            mapOf(
                "friendsList" to listOf<String>(),
                "friendRequests" to listOf<String>(),
                "sentFriendRequests" to listOf()))

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

    // Call the createFriend method
    userRepositoryFirestore.createFriend(
        user.uid,
        friend.uid,
        {
          // Verify the updates to the user document
          verify(mockUserDocumentReference).update("friendRequests", listOf<String>())
          verify(mockUserDocumentReference).update("friendsList", listOf(friend.uid))

          // Verify the updates to the friend document
          verify(mockFriendDocumentReference).update("friendsList", listOf(user.uid))
          verify(mockFriendDocumentReference).update("friendRequests", listOf<String>())
          verify(mockFriendDocumentReference).update("sentFriendRequests", listOf<String>())
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
  fun deleteFriend_shouldCallOnSuccessWhenTransactionSucceeds() {
    // Arrange
    val currentUserId = "currentUserId"
    val friendUserId = "friendUserId"

    val currentUserRef = mock(DocumentReference::class.java)
    val friendUserRef = mock(DocumentReference::class.java)
    val currentUserSnapshot = mock(DocumentSnapshot::class.java)
    val friendUserSnapshot = mock(DocumentSnapshot::class.java)

    // Mock initial lists
    val currentUserData = mapOf("friendsList" to listOf(friendUserId))
    val friendUserData = mapOf("friendsList" to listOf(currentUserId))

    `when`(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    `when`(mockUserCollectionReference.document(currentUserId)).thenReturn(currentUserRef)
    `when`(mockUserCollectionReference.document(friendUserId)).thenReturn(friendUserRef)

    `when`(transaction.get(currentUserRef)).thenReturn(currentUserSnapshot)
    `when`(transaction.get(friendUserRef)).thenReturn(friendUserSnapshot)

    `when`(currentUserSnapshot.data).thenReturn(currentUserData)
    `when`(friendUserSnapshot.data).thenReturn(friendUserData)

    // Mock runTransaction to succeed
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

    // Act
    userRepositoryFirestore.deleteFriend(
        currentUserId = currentUserId,
        friendUserId = friendUserId,
        onSuccess = { onSuccessCalled = true },
        onFailure = { onFailureCalled = true })

    shadowOf(Looper.getMainLooper()).idle()

    // Assert
    assertTrue(onSuccessCalled)
    assertFalse(onFailureCalled)

    // Verify that transaction updates were attempted
    verify(transaction).get(currentUserRef)
    verify(transaction).get(friendUserRef)

    // Both should have removed each other from their friends list
    verify(transaction).update(currentUserRef, "friendsList", emptyList<String>())
    verify(transaction).update(friendUserRef, "friendsList", emptyList<String>())
  }

  @Test
  fun deleteFriend_shouldCallOnFailureWhenTransactionFails() {
    // Arrange
    val currentUserId = "currentUserId"
    val friendUserId = "friendUserId"
    val exception = Exception("Transaction failed")

    `when`(mockFirestore.runTransaction<Void>(any())).thenReturn(Tasks.forException(exception))

    var onSuccessCalled = false
    var onFailureCalled = false
    var caughtException: Exception? = null

    // Act
    userRepositoryFirestore.deleteFriend(
        currentUserId = currentUserId,
        friendUserId = friendUserId,
        onSuccess = { onSuccessCalled = true },
        onFailure = {
          onFailureCalled = true
          caughtException = it
        })

    shadowOf(Looper.getMainLooper()).idle()

    // Assert
    assertFalse(onSuccessCalled)
    assertTrue(onFailureCalled)
    assertNotNull(caughtException)
    assertEquals("Transaction failed", caughtException?.message)
  }
}
