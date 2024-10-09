package com.swent.suddenbump.model

import android.os.Looper
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
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
import com.google.firebase.storage.FirebaseStorage
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import junit.framework.TestCase.fail
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class UserRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestoreStorage: FirebaseStorage
  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockUserCollectionReference: CollectionReference
  @Mock private lateinit var mockEmailCollectionReference: CollectionReference
  @Mock private lateinit var mockUserDocumentReference: DocumentReference
  @Mock private lateinit var mockEmailDocumentReference: DocumentReference
  @Mock private lateinit var mockUserDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockEmailDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockUserQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockFirebaseAuth: FirebaseAuth
  @Mock private lateinit var mockFirebaseUser: FirebaseUser
  @Mock private lateinit var firebaseAuthMockStatic: MockedStatic<FirebaseAuth>
  @Mock private lateinit var mockTaskVoid: Task<Void>

  private lateinit var userRepositoryFirestore: UserRepositoryFirestore

  private val user =
      User(
          uid = "1",
          firstName = "Martin",
          lastName = "Vetterli",
          profilePicture = Icons.Outlined.AccountCircle,
          phoneNumber = "00112",
          emailAddress = "user@email.com")

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    userRepositoryFirestore = UserRepositoryFirestore(mockFirestore)

    `when`(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    `when`(mockUserCollectionReference.document(any())).thenReturn(mockUserDocumentReference)
    `when`(mockUserCollectionReference.document()).thenReturn(mockUserDocumentReference)
    `when`(mockUserDocumentReference.set(any())).thenReturn(mockTaskVoid)
    `when`(mockTaskVoid.addOnSuccessListener(any())).thenReturn(mockTaskVoid)
    `when`(mockTaskVoid.addOnCompleteListener(any())).thenReturn(mockTaskVoid)
    `when`(mockTaskVoid.addOnFailureListener(any())).thenReturn(mockTaskVoid)

    `when`(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
    `when`(mockUserDocumentSnapshot.data)
        .thenReturn(
            mapOf(
                "uid" to "1",
                "firstName" to "Martin",
                "lastName" to "Vetterli",
                "profilePicture" to Icons.Outlined.AccountCircle,
                "phoneNumber" to "00112",
                "emailAddress" to "user@email.com"))

    `when`(mockFirestore.collection("Emails")).thenReturn(mockEmailCollectionReference)
    `when`(mockEmailCollectionReference.document(any())).thenReturn(mockEmailDocumentReference)
    `when`(mockEmailCollectionReference.document()).thenReturn(mockEmailDocumentReference)
    `when`(mockEmailDocumentReference.get()).thenReturn(Tasks.forResult(mockEmailDocumentSnapshot))
    `when`(mockEmailDocumentSnapshot.data).thenReturn(mapOf("uid" to "1"))

    firebaseAuthMockStatic
        .`when`<FirebaseAuth> { FirebaseAuth.getInstance() }
        .thenReturn(mockFirebaseAuth)
    `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

    `when`(mockFirebaseUser.uid).thenReturn("1")
    `when`(mockFirebaseUser.email).thenReturn("alexandre.carel@epfl.ch")
    `when`(mockFirebaseUser.displayName).thenReturn("Alexandre Carel")
  }

  @Test
  fun getNewUid() {
    val value_uid = "1"
    `when`(mockUserDocumentReference.id).thenReturn(value_uid)
    val uid = userRepositoryFirestore.getNewUid()
    assert(uid == value_uid)
  }

  @After
  fun tearDown() {
    // Release the mockStatic object after each test to prevent memory leaks
    firebaseAuthMockStatic.close()
  }

  /**
   * This test verifies that when fetching a User, the Firestore `get()` is called on the collection
   * reference and not the document reference.
   */
  @Test
  fun getUserAccount_callsDocuments() {
    // Ensure that mockUserQuerySnapshot is properly initialized and mocked
    `when`(mockUserCollectionReference.get()).thenReturn(Tasks.forResult(mockUserQuerySnapshot))

    // Ensure the QuerySnapshot returns a list of mock DocumentSnapshots
    `when`(mockUserQuerySnapshot.documents).thenReturn(listOf())

    // Call the method under test
    userRepositoryFirestore.getUserAccount(
        onSuccess = {

          // Do nothing; we just want to verify that the 'documents' field was accessed
        },
        onFailure = { fail("Failure callback should not be called") })

    // Verify that the 'documents' field was accessed
    verify(timeout(100)) { (mockUserQuerySnapshot).documents }
  }

  /**
   * This test verifies that when we create a new User account, the Firestore `set()` is called on
   * the document reference. This does NOT CHECK the actual data being added
   */
  @Test
  fun createUserAccount_shouldCallFirestoreCollection() {
    // This test verifies that when we create a new User account, the Firestore `collection()` method is
    // called.
    userRepositoryFirestore.createUserAccount(user, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    // Ensure Firestore collection method was called to reference the "ToDos" collection
    verify(mockUserDocumentReference).set(any())
  }
}
