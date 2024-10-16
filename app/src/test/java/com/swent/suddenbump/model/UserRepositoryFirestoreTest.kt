package com.swent.suddenbump.model

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
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.swent.suddenbump.model.location.Location
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import junit.framework.TestCase.fail
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
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

    @Mock
    private lateinit var mockFirestoreStorage: FirebaseStorage
    @Mock
    private lateinit var mockFirestore: FirebaseFirestore
    @Mock
    private lateinit var mockUserCollectionReference: CollectionReference
    @Mock
    private lateinit var mockEmailCollectionReference: CollectionReference
    @Mock
    private lateinit var mockUserDocumentReference: DocumentReference
    @Mock
    private lateinit var mockEmailDocumentReference: DocumentReference
    @Mock
    private lateinit var mockUserDocumentSnapshot: DocumentSnapshot
    @Mock
    private lateinit var mockEmailDocumentSnapshot: DocumentSnapshot
    @Mock
    private lateinit var mockUserQuerySnapshot: QuerySnapshot
    @Mock
    private lateinit var mockEmailQuerySnapshot: QuerySnapshot
    @Mock
    private lateinit var mockFirebaseAuth: FirebaseAuth
    @Mock
    private lateinit var mockFirebaseUser: FirebaseUser
    @Mock
    private lateinit var firebaseAuthMockStatic: MockedStatic<FirebaseAuth>
    @Mock
    private lateinit var mockTaskVoid: Task<Void>

    private lateinit var userRepositoryFirestore: UserRepositoryFirestore

    private val location = Location(0.0, 0.0)
    private val user =
        User(
            uid = "1",
            firstName = "Alexandre",
            lastName = "Carel",
            phoneNumber = "+33 6 59 20 70 02",
            null,
            emailAddress = "alexandre.carel@epfl.ch"
        )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        System.setProperty("mockito.verbose", "true")

        // Initialize Firebase if necessary
        if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
            FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        }

        userRepositoryFirestore = UserRepositoryFirestore(mockFirestore)

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
        `when`(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
        `when`(mockUserDocumentReference.id).thenReturn("1")
        `when`(mockUserDocumentSnapshot.data)
            .thenReturn(
                mapOf(
                    "uid" to "1",
                    "firstName" to "Alexandre",
                    "lastName" to "Carel",
                    "phoneNumber" to "+33 6 59 20 70 02",
                    "emailAddress" to "alexandre.carel@epfl.ch"
                )
            )

        `when`(mockFirestore.collection("Emails")).thenReturn(mockEmailCollectionReference)
        `when`(mockEmailCollectionReference.document(any())).thenReturn(mockEmailDocumentReference)
        `when`(mockEmailCollectionReference.document()).thenReturn(mockEmailDocumentReference)
        `when`(mockEmailDocumentReference.id).thenReturn("alexandre.carel@epfl.ch")
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
        `when`(mockUserDocumentReference.id).thenReturn("1")
        val uid = userRepositoryFirestore.getNewUid()
        assert(uid == "1")
    }

    @After
    fun tearDown() {
        // Release the mockStatic object after each test to prevent memory leaks
        firebaseAuthMockStatic.close()
    }

    @Test
    fun verifyNoAccountExists_callsDocuments() {

        `when`(mockEmailCollectionReference.get()).thenReturn(Tasks.forResult(mockEmailQuerySnapshot))
        `when`(mockUserQuerySnapshot.documents).thenReturn(listOf())

        `when`(mockEmailDocumentReference.get()).thenReturn(
            Tasks.forResult(
                mockEmailDocumentSnapshot
            )
        )

        userRepositoryFirestore.verifyNoAccountExists(
            user.emailAddress,
            onSuccess = {},
            onFailure = { fail("Failure callback should not be called") })

        verify(timeout(100)) { (mockUserQuerySnapshot).documents }
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

    /**
     * This test verifies that when fetching a User, the Firestore `get()` is called on the collection
     * reference and not the document reference.
     */
    @Test
    fun getUserAccount_callsDocuments() {
        `when`(mockUserCollectionReference.get()).thenReturn(Tasks.forResult(mockUserQuerySnapshot))
        `when`(mockUserQuerySnapshot.documents).thenReturn(listOf())

        `when`(mockEmailCollectionReference.get()).thenReturn(Tasks.forResult(mockEmailQuerySnapshot))
        `when`(mockUserQuerySnapshot.documents).thenReturn(listOf())

        `when`(mockEmailDocumentReference.get()).thenReturn(
            Tasks.forResult(
                mockEmailDocumentSnapshot
            )
        )

        userRepositoryFirestore.getUserAccount(
            onSuccess = {}, onFailure = { fail("Failure callback should not be called") })

        verify(timeout(100)) { (mockUserQuerySnapshot).documents }
    }

    /**
     * This test verifies that when we create a new User account, the Firestore `set()` is called on
     * the document reference. This does NOT CHECK the actual data being added
     */
    @Test
    fun updateUserAccount_shouldCallFirestoreCollection() {
        userRepositoryFirestore.updateUserAccount(user, onSuccess = {}, onFailure = {})

        shadowOf(Looper.getMainLooper()).idle()

        verify(mockUserDocumentReference).set(any())
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
     * This test verifies that when fetching a User, the Firestore `get()` is called on the collection
     * reference and not the document reference.
     */
    @Test
    fun getUserFriends_callsDocuments() {
        `when`(mockUserCollectionReference.get()).thenReturn(Tasks.forResult(mockUserQuerySnapshot))
        `when`(mockUserQuerySnapshot.documents).thenReturn(listOf())

        userRepositoryFirestore.getUserFriends(
            user = user,
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
            user = user, friendsList = listOf(user), onSuccess = {}, onFailure = {})

        shadowOf(Looper.getMainLooper()).idle()

        verify(mockUserDocumentReference).update(anyString(), any())
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
            user = user,
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
            user = user, blockedFriendsList = listOf(user), onSuccess = {}, onFailure = {})

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

        userRepositoryFirestore.updateLocation(
            user = user, location = location, onSuccess = {}, onFailure = {})

        shadowOf(Looper.getMainLooper()).idle()

        verify(mockUserDocumentReference).update(anyString(), any())
    }
}
