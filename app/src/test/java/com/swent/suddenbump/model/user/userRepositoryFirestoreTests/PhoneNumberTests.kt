package com.swent.suddenbump.model.user.userRepositoryFirestoreTests

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
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
import com.swent.suddenbump.model.image.ImageRepository
import com.swent.suddenbump.model.user.SharedPreferencesManager
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import com.swent.suddenbump.model.user.UserRepositoryFirestoreHelper
import com.swent.suddenbump.worker.WorkerScheduler
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
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PhoneNumberTests {

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
}
