package com.swent.suddenbump.model.user.userRepositoryFirestoreTests

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.suddenbump.model.user.SharedPreferencesManager
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import com.swent.suddenbump.worker.WorkerScheduler
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class UserStatusTests {

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

  @Mock private lateinit var mockFirebaseUser: FirebaseUser

  @Mock private lateinit var mockTaskVoid: Task<Void>

  @Mock private lateinit var sharedPreferences: SharedPreferences
  @Mock private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

  private lateinit var userRepositoryFirestore: UserRepositoryFirestore

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

    `when`(mockFirebaseUser.uid).thenReturn("1")
    `when`(mockFirebaseUser.email).thenReturn("alexandre.carel@epfl.ch")
    `when`(mockFirebaseUser.displayName).thenReturn("Alexandre Carel")

    // Mock behavior of the editor
    `when`(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor)
    `when`(sharedPreferencesEditor.putString(anyString(), anyString()))
        .thenReturn(sharedPreferencesEditor)
  }

  @Test
  fun updateUserStatus_shouldCallOnSuccessWhenUpdateIsSuccessful() {
    // Arrange
    val uid = "test_user_id"
    val isOnline = true

    // Mock the Firestore and its references
    whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    whenever(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)

    // Mock a successful update task
    val mockUpdateTask = Tasks.forResult<Void>(null)
    whenever(mockUserDocumentReference.update("isOnline", isOnline)).thenReturn(mockUpdateTask)

    var onSuccessCalled = false
    var onFailureCalled = false

    // Act
    userRepositoryFirestore.updateUserStatus(
        uid = uid,
        status = isOnline,
        onSuccess = { onSuccessCalled = true },
        onFailure = { onFailureCalled = true })

    // Ensure all posted runnables on the main thread have been executed
    shadowOf(Looper.getMainLooper()).idle()

    // Assert
    assertTrue("Expected onSuccess to be called", onSuccessCalled)
    assertFalse("Expected onFailure not to be called", onFailureCalled)
    verify(mockUserDocumentReference).update("isOnline", isOnline)
  }

  @Test
  fun updateUserStatus_shouldCallOnFailureWhenUpdateFails() {
    // Arrange
    val uid = "test_user_id"
    val isOnline = false
    val exception = Exception("Failed to update status")

    whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    whenever(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)

    // Mock a failing update task
    val failingTask = Tasks.forException<Void>(exception)
    whenever(mockUserDocumentReference.update("isOnline", isOnline)).thenReturn(failingTask)

    var onSuccessCalled = false
    var onFailureCalled = false
    var caughtException: Exception? = null

    // Act
    userRepositoryFirestore.updateUserStatus(
        uid = uid,
        status = isOnline,
        onSuccess = { onSuccessCalled = true },
        onFailure = {
          onFailureCalled = true
          caughtException = it
        })

    // Ensure all posted runnables on the main thread have been executed
    shadowOf(Looper.getMainLooper()).idle()
    // Assert
    assertFalse(onSuccessCalled)
    assertTrue(onFailureCalled)
    assertNotNull(caughtException)
    assertEquals("Failed to update status", caughtException?.message)
    verify(mockUserDocumentReference).update("isOnline", isOnline)
  }

  @Test
  fun getUserStatus_shouldReturnTrueWhenUserIsOnline() {
    // Arrange
    val uid = "test_user_id"
    whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    whenever(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)

    val mockSnapshot = mock(DocumentSnapshot::class.java)
    whenever(mockSnapshot.exists()).thenReturn(true)
    whenever(mockSnapshot.getBoolean("isOnline")).thenReturn(true)
    whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockSnapshot))

    var onSuccessCalled = false
    var onFailureCalled = false
    var isOnlineResult: Boolean? = null

    // Act
    userRepositoryFirestore.getUserStatus(
        uid = uid,
        onSuccess = {
          onSuccessCalled = true
          isOnlineResult = it
        },
        onFailure = { onFailureCalled = true })

    // Ensure all posted runnables on the main thread have been executed
    shadowOf(Looper.getMainLooper()).idle()
    // Assert
    assertTrue(onSuccessCalled)
    assertFalse(onFailureCalled)
    assertTrue(isOnlineResult == true)
    verify(mockUserDocumentReference).get()
  }

  @Test
  fun getUserStatus_shouldReturnFalseWhenUserIsOffline() {
    // Arrange
    val uid = "test_user_id"
    whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    whenever(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)

    val mockSnapshot = mock(DocumentSnapshot::class.java)
    whenever(mockSnapshot.exists()).thenReturn(true)
    whenever(mockSnapshot.getBoolean("isOnline")).thenReturn(false)
    whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockSnapshot))

    var onSuccessCalled = false
    var onFailureCalled = false
    var isOnlineResult: Boolean? = null

    // Act
    userRepositoryFirestore.getUserStatus(
        uid = uid,
        onSuccess = {
          onSuccessCalled = true
          isOnlineResult = it
        },
        onFailure = { onFailureCalled = true })

    // Ensure all posted runnables on the main thread have been executed
    shadowOf(Looper.getMainLooper()).idle()
    // Assert
    assertTrue(onSuccessCalled)
    assertFalse(onFailureCalled)
    assertFalse(isOnlineResult == true)
    verify(mockUserDocumentReference).get()
  }

  @Test
  fun getUserStatus_shouldCallOnFailureWhenUserNotFound() {
    // Arrange
    val uid = "non_existent_user"
    whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    whenever(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)

    // Mock snapshot that doesn't exist
    val mockSnapshot = mock(DocumentSnapshot::class.java)
    whenever(mockSnapshot.exists()).thenReturn(false)
    whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forResult(mockSnapshot))

    var onSuccessCalled = false
    var onFailureCalled = false
    var caughtException: Exception? = null

    // Act
    userRepositoryFirestore.getUserStatus(
        uid = uid,
        onSuccess = { onSuccessCalled = true },
        onFailure = {
          onFailureCalled = true
          caughtException = it
        })

    // Ensure all posted runnables on the main thread have been executed
    shadowOf(Looper.getMainLooper()).idle()
    // Assert
    assertFalse(onSuccessCalled)
    assertTrue(onFailureCalled)
    assertNotNull(caughtException)
    assertEquals("User not found", caughtException?.message)
    verify(mockUserDocumentReference).get()
  }

  @Test
  fun getUserStatus_shouldCallOnFailureWhenFirestoreFails() {
    // Arrange
    val uid = "test_user_id"
    val exception = Exception("Firestore error")

    whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
    whenever(mockUserCollectionReference.document(uid)).thenReturn(mockUserDocumentReference)
    whenever(mockUserDocumentReference.get()).thenReturn(Tasks.forException(exception))

    var onSuccessCalled = false
    var onFailureCalled = false
    var caughtException: Exception? = null

    // Act
    userRepositoryFirestore.getUserStatus(
        uid = uid,
        onSuccess = { onSuccessCalled = true },
        onFailure = {
          onFailureCalled = true
          caughtException = it
        })

    // Ensure all posted runnables on the main thread have been executed
    shadowOf(Looper.getMainLooper()).idle()
    // Assert
    assertFalse(onSuccessCalled)
    assertTrue(onFailureCalled)
    assertNotNull(caughtException)
    assertEquals("Firestore error", caughtException?.message)
    verify(mockUserDocumentReference).get()
  }
}
