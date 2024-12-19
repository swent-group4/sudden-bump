import android.location.Location
import android.os.Looper.getMainLooper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.swent.suddenbump.model.image.ImageRepository
import com.swent.suddenbump.model.user.SharedPreferencesManager
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import com.swent.suddenbump.worker.WorkerScheduler
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
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
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class GetAllNonBlockedUsersTests {

  // Reference the same mocks setup from RecommendedFriendsTests.kt
  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockSharedPreferencesManager: SharedPreferencesManager
  @Mock private lateinit var mockWorkerScheduler: WorkerScheduler
  @Mock private lateinit var mockUserCollectionReference: CollectionReference
  @Mock private lateinit var mockUserDocumentReference: DocumentReference
  @Mock private lateinit var mockUserDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockImageRepository: ImageRepository
  @Mock private lateinit var firebaseAuthMockStatic: MockedStatic<FirebaseAuth>
  @Mock private lateinit var mockTaskVoid: Task<Void>

  private lateinit var userRepositoryFirestore: UserRepositoryFirestore
  //
  private val location =
      MutableStateFlow(
          Location("mock_provider").apply {
            latitude = 0.0
            longitude = 0.0
          })

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    userRepositoryFirestore =
        UserRepositoryFirestore(mockFirestore, mockSharedPreferencesManager, mockWorkerScheduler)

    `when`(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    `when`(mockUserCollectionReference.document(any())).thenReturn(mockUserDocumentReference)
    `when`(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
  }

  @After
  fun tearDown() {
    firebaseAuthMockStatic.close()
    Mockito.reset(
        mockFirestore,
        mockTaskVoid,
        mockUserCollectionReference,
        mockUserDocumentReference,
        mockUserDocumentSnapshot,
        mockImageRepository)
  }

  @Test
  fun getAllNonBlockedUsers_shouldReturnNonBlockedUsers() {
    // Create test users
    val currentUser =
        User(
            "1",
            "Current",
            "User",
            "+1234567890",
            null,
            "current.user@example.com",
            lastKnownLocation = location)

    val nonBlockedUser =
        User(
            "2",
            "Non",
            "Blocked",
            "+0987654321",
            null,
            "non.blocked@example.com",
            lastKnownLocation = location)

    val blockedUser =
        User(
            "3",
            "Blocked",
            "User",
            "+1122334455",
            null,
            "blocked.user@example.com",
            lastKnownLocation = location)

    // Mock query snapshot and document snapshots
    val allUsersQuerySnapshot = mock(QuerySnapshot::class.java)
    val nonBlockedUserSnapshot = mock(DocumentSnapshot::class.java)
    val blockedUserSnapshot = mock(DocumentSnapshot::class.java)

    // Mock the current user's document data (containing blocked list)
    `when`(mockUserDocumentSnapshot.data)
        .thenReturn(mapOf("blockedList" to listOf(blockedUser.uid)))

    // Mock the data for other users
    `when`(nonBlockedUserSnapshot.data)
        .thenReturn(
            mapOf(
                "uid" to nonBlockedUser.uid,
                "firstName" to nonBlockedUser.firstName,
                "lastName" to nonBlockedUser.lastName,
                "phoneNumber" to nonBlockedUser.phoneNumber,
                "emailAddress" to nonBlockedUser.emailAddress))
    `when`(blockedUserSnapshot.data)
        .thenReturn(
            mapOf(
                "uid" to blockedUser.uid,
                "firstName" to blockedUser.firstName,
                "lastName" to blockedUser.lastName,
                "phoneNumber" to blockedUser.phoneNumber,
                "emailAddress" to blockedUser.emailAddress))

    // Set document IDs
    `when`(nonBlockedUserSnapshot.id).thenReturn(nonBlockedUser.uid)
    `when`(blockedUserSnapshot.id).thenReturn(blockedUser.uid)

    // Mock the collection query result
    `when`(mockUserCollectionReference.get()).thenReturn(Tasks.forResult(allUsersQuerySnapshot))
    `when`(allUsersQuerySnapshot.documents)
        .thenReturn(listOf(nonBlockedUserSnapshot, blockedUserSnapshot))

    // Call getAllNonBlockedUsers
    userRepositoryFirestore.getAllNonBlockedUsers(
        currentUser.uid,
        { users ->
          // Verify the results
          assertEquals(1, users.size)
          assertEquals(nonBlockedUser.uid, users[0].uid)
          assertFalse(users.any { it.uid == blockedUser.uid })
          assertFalse(users.any { it.uid == currentUser.uid })
        },
        { fail("onFailure should not be called") })
  }

  @Test
  fun getAllNonBlockedUsers_shouldHandleEmptyBlockedList() {
    val currentUser =
        User(
            "1",
            "Current",
            "User",
            "+1234567890",
            null,
            "current.user@example.com",
            lastKnownLocation = location)

    val otherUser =
        User(
            "2",
            "Other",
            "User",
            "+0987654321",
            null,
            "other.user@example.com",
            lastKnownLocation = location)

    // Mock query snapshot and document snapshots
    val allUsersQuerySnapshot = mock(QuerySnapshot::class.java)
    val otherUserSnapshot = mock(DocumentSnapshot::class.java)

    // Mock empty blocked list
    `when`(mockUserDocumentSnapshot.data).thenReturn(mapOf("blockedList" to emptyList<String>()))

    // Mock other user data
    `when`(otherUserSnapshot.data)
        .thenReturn(
            mapOf(
                "uid" to otherUser.uid,
                "firstName" to otherUser.firstName,
                "lastName" to otherUser.lastName,
                "phoneNumber" to otherUser.phoneNumber,
                "emailAddress" to otherUser.emailAddress))
    `when`(otherUserSnapshot.id).thenReturn(otherUser.uid)

    // Mock the collection query result
    `when`(mockUserCollectionReference.get()).thenReturn(Tasks.forResult(allUsersQuerySnapshot))
    `when`(allUsersQuerySnapshot.documents).thenReturn(listOf(otherUserSnapshot))

    userRepositoryFirestore.getAllNonBlockedUsers(
        currentUser.uid,
        { users ->
          assertEquals(1, users.size)
          assertEquals(otherUser.uid, users[0].uid)
        },
        { fail("onFailure should not be called") })
  }

  @Test
  fun getAllNonBlockedUsers_shouldHandleFailure() {
    val exception = Exception("Failed to fetch users")
    var failureException: Exception? = null

    // Create a failed task
    val failedTask = Tasks.forException<DocumentSnapshot>(exception)

    // Mock the document reference to return the failed task
    `when`(mockUserDocumentReference.get()).thenReturn(failedTask)

    // We need to use a CountDownLatch to wait for the async callback
    val latch = CountDownLatch(1)

    userRepositoryFirestore.getAllNonBlockedUsers(
        "1",
        { fail("onSuccess should not be called") },
        { error ->
          failureException = error
          latch.countDown()
        })

    // Execute any pending tasks on the main looper
    shadowOf(getMainLooper()).idle()

    // Wait for the callback to be executed (with a shorter timeout)
    assertTrue("Callback was not executed in time", latch.await(1, TimeUnit.SECONDS))

    assertNotNull("Failure exception should not be null", failureException)
    assertEquals(exception.message, failureException?.message)
  }
}
