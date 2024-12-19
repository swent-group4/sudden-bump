package com.swent.suddenbump.model.user.userRepositoryFirestoreTests
import android.content.Context
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
import com.google.firebase.storage.StorageReference
import com.swent.suddenbump.model.user.SharedPreferencesManager
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import com.swent.suddenbump.worker.WorkerScheduler
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.fail
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DeleteUserAccountTests {

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
    @Mock private lateinit var mockFirebaseAuth: FirebaseAuth
    @Mock private lateinit var mockFirebaseUser: FirebaseUser
    @Mock private lateinit var mockStorageReference: StorageReference
    @Mock private lateinit var mockProfilePicRef: StorageReference
    @Mock private lateinit var mockTaskVoid: Task<Void>

    @Mock private lateinit var firebaseAuthMockStatic: MockedStatic<FirebaseAuth>


    private lateinit var userRepositoryFirestore: UserRepositoryFirestore
    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        firebaseAuthMockStatic = mockStatic(FirebaseAuth::class.java)
        whenever(FirebaseAuth.getInstance()).thenReturn(mockFirebaseAuth)
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn("some_uid")

        // Mock Firestore
        whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
        whenever(mockUserCollectionReference.document(anyString())).thenReturn(mockUserDocumentReference)

        whenever(mockFirestore.collection("Emails")).thenReturn(mockEmailCollectionReference)
        whenever(mockEmailCollectionReference.document(anyString())).thenReturn(mockEmailDocumentReference)

        whenever(mockFirestore.collection("Phones")).thenReturn(mockPhoneCollectionReference)
        whenever(mockPhoneCollectionReference.document(anyString())).thenReturn(mockPhoneDocumentReference)

        // etc. pour tous vos besoins

        // Enfin, vous pouvez instancier votre UserRepositoryFirestore
        userRepositoryFirestore = UserRepositoryFirestore(mockFirestore, mockSharedPreferencesManager, mockWorkerScheduler)
    }

    @After
    fun tearDown() {
        firebaseAuthMockStatic.close()
    }
    @Test
    fun deleteUserAccountSuccess() {
        val uid = "test_user_id"
        val userData = mapOf(
            "phoneNumber" to "+1234567890",
            "emailAddress" to "test@example.com"
        )

        // On mock le get() du user
        whenever(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)
        whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
        whenever(mockUserDocumentSnapshot.data).thenReturn(userData)

        // Mock suppression email/phone
        val deleteSuccessTask = Tasks.forResult<Void>(null)
        whenever(mockEmailCollectionReference.document("test@example.com").delete()).thenReturn(deleteSuccessTask)
        whenever(mockPhoneCollectionReference.document("+1234567890").delete()).thenReturn(deleteSuccessTask)

        // Mock suppression photo de profil
        whenever(mockProfilePicRef.delete()).thenReturn(deleteSuccessTask)

        // Mock suppression user doc
        whenever(mockUserDocumentReference.delete()).thenReturn(deleteSuccessTask)

        var onSuccessCalled = false
        var onFailureCalled = false

        userRepositoryFirestore.deleteUserAccount(uid,
            onSuccess = { onSuccessCalled = true },
            onFailure = { onFailureCalled = true }
        )

        // Vérifications
        assertTrue(onSuccessCalled)
        assertFalse(onFailureCalled)

        verify(mockUserDocumentReference).get()
        verify(mockEmailCollectionReference.document("test@example.com")).delete()
        verify(mockPhoneCollectionReference.document("+1234567890")).delete()
        verify(mockProfilePicRef).delete()
        verify(mockUserDocumentReference).delete()
    }

    @Test
    fun deleteUserAccountUserNotFound() {
        val uid = "not_existing"
        val exception = Exception("User not found")

        whenever(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)
        whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forException(exception))

        var onSuccessCalled = false
        var onFailureCalled = false
        var caughtException: Exception? = null

        userRepositoryFirestore.deleteUserAccount(uid,
            onSuccess = { onSuccessCalled = true },
            onFailure = {
                onFailureCalled = true
                caughtException = it
            }
        )

        assertFalse(onSuccessCalled)
        assertTrue(onFailureCalled)
        assertEquals("User not found", caughtException?.message)
        verify(mockUserDocumentReference).get()
        verify(mockUserDocumentReference, never()).delete()
    }

    @Test
    fun deleteUserAccountFailsOnEmailDelete() {
        val uid = "test_user_id"
        val userData = mapOf(
            "phoneNumber" to "+1234567890",
            "emailAddress" to "test@example.com"
        )
        val exception = Exception("Failed to delete email doc")

        whenever(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)
        whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
        whenever(mockUserDocumentSnapshot.data).thenReturn(userData)

        // fail sur la suppression de l'email
        whenever(mockEmailCollectionReference.document("test@example.com").delete()).thenReturn(Tasks.forException(exception))

        var onSuccessCalled = false
        var onFailureCalled = false
        var caughtException: Exception? = null

        userRepositoryFirestore.deleteUserAccount(uid,
            onSuccess = { onSuccessCalled = true },
            onFailure = {
                onFailureCalled = true
                caughtException = it
            }
        )

        assertFalse(onSuccessCalled)
        assertTrue(onFailureCalled)
        assertEquals("Failed to delete email doc", caughtException?.message)
        verify(mockUserDocumentReference).get()
        verify(mockEmailCollectionReference.document("test@example.com")).delete()
        verify(mockUserDocumentReference, never()).delete()
    }

    @Test
    fun deleteUserAccountFailsOnUserDocDelete() {
        val uid = "test_user_id"
        val userData = mapOf(
            "phoneNumber" to "+1234567890",
            "emailAddress" to "test@example.com"
        )
        val exception = Exception("Failed to delete user doc")

        whenever(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)
        whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
        whenever(mockUserDocumentSnapshot.data).thenReturn(userData)

        // Succès sur phone/email/photo
        whenever(mockEmailCollectionReference.document("test@example.com").delete()).thenReturn(Tasks.forResult(null))
        whenever(mockPhoneCollectionReference.document("+1234567890").delete()).thenReturn(Tasks.forResult(null))
        whenever(mockProfilePicRef.delete()).thenReturn(Tasks.forResult(null))

        // Echoue sur la suppression du user doc
        whenever(mockUserDocumentReference.delete()).thenReturn(Tasks.forException(exception))

        var onSuccessCalled = false
        var onFailureCalled = false
        var caughtException: Exception? = null

        userRepositoryFirestore.deleteUserAccount(uid,
            onSuccess = { onSuccessCalled = true },
            onFailure = {
                onFailureCalled = true
                caughtException = it
            }
        )

        assertFalse(onSuccessCalled)
        assertTrue(onFailureCalled)
        assertEquals("Failed to delete user doc", caughtException?.message)
        verify(mockUserDocumentReference).get()
        verify(mockEmailCollectionReference.document("test@example.com")).delete()
        verify(mockPhoneCollectionReference.document("+1234567890")).delete()
        verify(mockProfilePicRef).delete()
        verify(mockUserDocumentReference).delete()
    }

    @Test
    fun deleteUserAccountNoPhoneOrEmail() {
        val uid = "test_user_id"
        val userData = mapOf(
            "phoneNumber" to "",
            "emailAddress" to ""
        )

        whenever(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)
        whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockUserDocumentSnapshot))
        whenever(mockUserDocumentSnapshot.data).thenReturn(userData)

        val deleteSuccessTask = Tasks.forResult<Void>(null)
        whenever(mockProfilePicRef.delete()).thenReturn(deleteSuccessTask)
        whenever(mockUserDocumentReference.delete()).thenReturn(deleteSuccessTask)

        var onSuccessCalled = false
        var onFailureCalled = false

        userRepositoryFirestore.deleteUserAccount(uid,
            onSuccess = { onSuccessCalled = true },
            onFailure = { onFailureCalled = true }
        )

        assertTrue(onSuccessCalled)
        assertFalse(onFailureCalled)

        verify(mockUserDocumentReference).get()
        // Pas d'appel sur les emails ou phones
        verify(mockUserDocumentReference).delete()
    }
}
