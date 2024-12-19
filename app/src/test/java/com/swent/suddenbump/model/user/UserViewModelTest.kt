package com.swent.suddenbump.model.user

import android.location.Location
import android.os.Looper.getMainLooper
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.chat.Message
import com.swent.suddenbump.model.direction.Route
import com.swent.suddenbump.ui.navigation.NavigationActions
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class UserViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var userRepository: UserRepository
    private lateinit var userViewModel: UserViewModel
    private lateinit var chatRepository: ChatRepository
    private lateinit var mockUserDocumentReference: DocumentReference
    private lateinit var mockUserDocumentSnapshot: DocumentSnapshot
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockUserQuerySnapshot: QuerySnapshot
    private lateinit var mockUserCollectionReference: CollectionReference
    private lateinit var mockPhoneCollectionReference: CollectionReference
    private lateinit var mockEmailCollectionReference: CollectionReference
    private lateinit var mockChatCollectionReference: CollectionReference
    private lateinit var mockQuerySnapshot: QuerySnapshot
    private lateinit var mockQuery: Query


    private val exception = Exception()
    private val location =
        MutableStateFlow(
            Location("mock_provider").apply {
                latitude = 0.0
                longitude = 0.0
            })
    private val user =
        User(
            "2",
            "Martin",
            "Vetterli",
            "+41 00 000 00 01",
            null,
            "martin.vetterli@epfl.ch",
            location
        )

    private val testDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        userRepository = mock(UserRepository::class.java)
        chatRepository = mock(ChatRepository::class.java)
        userViewModel = UserViewModel(userRepository, chatRepository)
        mockUserDocumentReference = mock(DocumentReference::class.java)
        mockUserDocumentSnapshot = mock(DocumentSnapshot::class.java)
        mockFirestore = mock(FirebaseFirestore::class.java)
        mockUserQuerySnapshot = mock(QuerySnapshot::class.java)
        mockUserCollectionReference = mock(CollectionReference::class.java)
        mockPhoneCollectionReference = mock(CollectionReference::class.java)
        mockEmailCollectionReference = mock(CollectionReference::class.java)
        mockChatCollectionReference = mock(CollectionReference::class.java)
        mockQuery = mock(Query::class.java)
        mockQuerySnapshot = mock(QuerySnapshot::class.java)
        Dispatchers.setMain(testDispatcher)

        val config = Configuration.Builder().setMinimumLoggingLevel(android.util.Log.DEBUG).build()

        WorkManagerTestInitHelper.initializeTestWorkManager(
            ApplicationProvider.getApplicationContext(), config
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        WorkManager.getInstance(ApplicationProvider.getApplicationContext()).cancelAllWork()
    }

    @Test
    fun getUserStatus_shouldCallOnSuccessWhenUserIsOnline() = runTest {
        val uid = "test_user_id"
        val isOnline = true

        // Mock repository getUserStatus to call onSuccess with `true`
        doAnswer { invocation ->
            val onSuccess = invocation.getArgument<(Boolean) -> Unit>(1)
            onSuccess(isOnline)
            null
        }
            .whenever(userRepository)
            .getUserStatus(eq(uid), any(), any())

        var onSuccessCalled = false
        var resultIsOnline: Boolean? = null
        var onFailureCalled = false

        userViewModel.getUserStatus(
            uid = uid,
            onSuccess = {
                onSuccessCalled = true
                resultIsOnline = it
            },
            onFailure = { onFailureCalled = true })

        // Assertions
        verify(userRepository).getUserStatus(eq(uid), any(), any())
        assert(onSuccessCalled)
        assert(!onFailureCalled)
        assert(resultIsOnline == true)
    }

    @Test
    fun getUserStatus_shouldCallOnSuccessWhenUserIsOffline() = runTest {
        val uid = "test_user_id"
        val isOnline = false

        doAnswer { invocation ->
            val onSuccess = invocation.getArgument<(Boolean) -> Unit>(1)
            onSuccess(isOnline)
            null
        }
            .whenever(userRepository)
            .getUserStatus(eq(uid), any(), any())

        var onSuccessCalled = false
        var resultIsOnline: Boolean? = null
        var onFailureCalled = false

        userViewModel.getUserStatus(
            uid = uid,
            onSuccess = {
                onSuccessCalled = true
                resultIsOnline = it
            },
            onFailure = { onFailureCalled = true })

        verify(userRepository).getUserStatus(eq(uid), any(), any())
        assert(onSuccessCalled)
        assert(!onFailureCalled)
        assert(resultIsOnline == false)
    }

    @Test
    fun deleteFriend_success() = runTest {
        // Arrange
        val currentUser =
            User(
                uid = "currentUserId",
                firstName = "Current",
                lastName = "User",
                phoneNumber = "+41 00 000 00 01",
                profilePicture = null,
                emailAddress = "current.user@example.com",
                lastKnownLocation = MutableStateFlow(Location("mock_provider"))
            )

        val friend =
            User(
                uid = "friendUserId",
                firstName = "Friend",
                lastName = "User",
                phoneNumber = "+41 00 000 00 02",
                profilePicture = null,
                emailAddress = "friend.user@example.com",
                lastKnownLocation = MutableStateFlow(Location("mock_provider"))
            )

        // Set current user in the ViewModel
        userViewModel.setUser(currentUser, onSuccess = {}, onFailure = {})

        // Add the friend to the user's friends list
        userViewModel.setUserFriends(friendsList = listOf(friend), onSuccess = {}, onFailure = {})

        // Mock repository.deleteFriend to call onSuccess
        doAnswer { invocation ->
            val onSuccess = invocation.getArgument<() -> Unit>(2)
            onSuccess()
            null
        }
            .whenever(userRepository)
            .deleteFriend(eq(currentUser.uid), eq(friend.uid), any(), any())

        // Mock repository.stopSharingLocationWithFriend to call onSuccess
        doAnswer { invocation ->
            val onSuccess = invocation.getArgument<() -> Unit>(2)
            onSuccess()
            null
        }
            .whenever(userRepository)
            .stopSharingLocationWithFriend(eq(currentUser.uid), eq(friend.uid), any(), any())
        doAnswer { invocation ->
            val onSuccess = invocation.getArgument<() -> Unit>(2)
            onSuccess()
            null
        }
            .whenever(userRepository)
            .stopSharingLocationWithFriend(eq(friend.uid), eq(currentUser.uid), any(), any())

        var onSuccessCalled = false
        var onFailureCalled = false

        // Act
        userViewModel.deleteFriend(
            user = currentUser,
            friend = friend,
            onSuccess = { onSuccessCalled = true },
            onFailure = { onFailureCalled = true })

        // Assert
        verify(userRepository).deleteFriend(eq(currentUser.uid), eq(friend.uid), any(), any())
        verify(userRepository)
            .stopSharingLocationWithFriend(eq(currentUser.uid), eq(friend.uid), any(), any())
        verify(userRepository)
            .stopSharingLocationWithFriend(eq(friend.uid), eq(currentUser.uid), any(), any())
        assertTrue("onSuccess should be called", onSuccessCalled)
        assertFalse("onFailure should not be called", onFailureCalled)
        assertFalse(
            "Friend should be removed from user's friends list",
            userViewModel.getUserFriends().value.contains(friend)
        )
    }

    @Test
    fun unsendFriendRequest_shouldRemoveFriendFromSentRequestsOnSuccess() = runTest {
        // Arrange
        val currentUser =
            User(
                uid = "currentUserId",
                firstName = "Current",
                lastName = "User",
                phoneNumber = "+41 00 000 00 01",
                profilePicture = null,
                emailAddress = "current.user@example.com",
                lastKnownLocation = MutableStateFlow(Location("mock_provider"))
            )

        val friend =
            User(
                uid = "friendUserId",
                firstName = "Friend",
                lastName = "User",
                phoneNumber = "+41 00 000 00 02",
                profilePicture = null,
                emailAddress = "friend.user@example.com",
                lastKnownLocation = MutableStateFlow(Location("mock_provider"))
            )

        // Set the current user in the ViewModel
        userViewModel.setUser(currentUser, onSuccess = {}, onFailure = {})

        // Initially, add the friend to the sent friend requests
        userViewModel.sendFriendRequest(currentUser, friend, onSuccess = {}, onFailure = {})
        assertTrue(userViewModel.getSentFriendRequests().value.contains(friend))

        // Mock repository.deleteFriendRequest to call onSuccess
        doAnswer { invocation ->
            val onSuccess = invocation.getArgument<() -> Unit>(2)
            onSuccess()
            null
        }
            .whenever(userRepository)
            .deleteFriendRequest(eq(friend.uid), eq(currentUser.uid), any(), any())

        var onSuccessCalled = false
        var onFailureCalled = false

        // Act
        userViewModel.unsendFriendRequest(
            user = currentUser,
            friend = friend,
            onSuccess = { onSuccessCalled = true },
            onFailure = { onFailureCalled = true })

        // Assert
        verify(userRepository).deleteFriendRequest(
            eq(friend.uid),
            eq(currentUser.uid),
            any(),
            any()
        )
        assertTrue(onSuccessCalled)
        assertFalse(onFailureCalled)
        assertFalse(userViewModel.getSentFriendRequests().value.contains(friend))
    }

    @Test
    fun unsendFriendRequest_shouldCallOnFailureOnError() = runTest {
        // Arrange
        val currentUser =
            User(
                uid = "currentUserId",
                firstName = "Current",
                lastName = "User",
                phoneNumber = "+41 00 000 00 01",
                profilePicture = null,
                emailAddress = "current.user@example.com",
                lastKnownLocation = MutableStateFlow(Location("mock_provider"))
            )

        val friend =
            User(
                uid = "friendUserId",
                firstName = "Friend",
                lastName = "User",
                phoneNumber = "+41 00 000 00 02",
                profilePicture = null,
                emailAddress = "friend.user@example.com",
                lastKnownLocation = MutableStateFlow(Location("mock_provider"))
            )

        // Set the current user in the ViewModel
        userViewModel.setUser(currentUser, onSuccess = {}, onFailure = {})

        // Initially, add the friend to the sent friend requests
        userViewModel.sendFriendRequest(currentUser, friend, onSuccess = {}, onFailure = {})
        assertTrue(userViewModel.getSentFriendRequests().value.contains(friend))

        val exception = Exception("Failed to unsend request")

        // Mock repository.deleteFriendRequest to call onFailure
        doAnswer { invocation ->
            val onFailure = invocation.getArgument<(Exception) -> Unit>(3)
            onFailure(exception)
            null
        }
            .whenever(userRepository)
            .deleteFriendRequest(eq(friend.uid), eq(currentUser.uid), any(), any())

        var onSuccessCalled = false
        var onFailureCalled = false
        var caughtException: Exception? = null

        // Act
        userViewModel.unsendFriendRequest(
            user = currentUser,
            friend = friend,
            onSuccess = { onSuccessCalled = true },
            onFailure = {
                onFailureCalled = true
                caughtException = it
            })

        // Assert
        verify(userRepository).deleteFriendRequest(
            eq(friend.uid),
            eq(currentUser.uid),
            any(),
            any()
        )
        assertFalse(onSuccessCalled)
        assertTrue(onFailureCalled)
        assertEquals(exception, caughtException)
        // The friend should still be in the sent requests since it failed
        assertTrue(userViewModel.getSentFriendRequests().value.contains(friend))
    }

    @Test
    fun deleteFriend_failure() = runTest {
        // Arrange
        val currentUser =
            User(
                uid = "currentUserId",
                firstName = "Current",
                lastName = "User",
                phoneNumber = "+41 00 000 00 01",
                profilePicture = null,
                emailAddress = "current.user@example.com",
                lastKnownLocation = MutableStateFlow(Location("mock_provider"))
            )

        val friend =
            User(
                uid = "friendUserId",
                firstName = "Friend",
                lastName = "User",
                phoneNumber = "+41 00 000 00 02",
                profilePicture = null,
                emailAddress = "friend.user@example.com",
                lastKnownLocation = MutableStateFlow(Location("mock_provider"))
            )

        // Set current user in the ViewModel
        userViewModel.setUser(currentUser, onSuccess = {}, onFailure = {})

        // Add the friend to the user's friends list
        userViewModel.setUserFriends(friendsList = listOf(friend), onSuccess = {}, onFailure = {})

        val exception = Exception("Delete friend failed")

        // Mock repository.deleteFriend to call onFailure
        doAnswer { invocation ->
            val onFailure = invocation.getArgument<(Exception) -> Unit>(3)
            onFailure(exception)
            null
        }
            .whenever(userRepository)
            .deleteFriend(eq(currentUser.uid), eq(friend.uid), any(), any())

        var onSuccessCalled = false
        var onFailureCalled = false
        var caughtException: Exception? = null

        // Act
        userViewModel.deleteFriend(
            user = currentUser,
            friend = friend,
            onSuccess = { onSuccessCalled = true },
            onFailure = {
                onFailureCalled = true
                caughtException = it
            })

        // Assert
        verify(userRepository).deleteFriend(eq(currentUser.uid), eq(friend.uid), any(), any())
        assertFalse("onSuccess should not be called", onSuccessCalled)
        assertTrue("onFailure should be called", onFailureCalled)
        assertEquals(
            "Should have caught the correct exception",
            "Delete friend failed",
            caughtException?.message
        )
        assertTrue(
            "Friend should still be in user's friends list after failure",
            userViewModel.getUserFriends().value.contains(friend)
        )
    }

    @Test
    fun getUserStatus_shouldCallOnFailureWhenRepositoryFails() = runTest {
        val uid = "test_user_id"
        val exception = Exception("Firestore error")

        doAnswer { invocation ->
            val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
            onFailure(exception)
            null
        }
            .whenever(userRepository)
            .getUserStatus(eq(uid), any(), any())

        var onSuccessCalled = false
        var onFailureCalled = false
        var caughtException: Exception? = null

        userViewModel.getUserStatus(
            uid = uid,
            onSuccess = { onSuccessCalled = true },
            onFailure = {
                onFailureCalled = true
                caughtException = it
            })

        verify(userRepository).getUserStatus(eq(uid), any(), any())
        assert(!onSuccessCalled)
        assert(onFailureCalled)
        assert(caughtException == exception)
    }

    @Test
    fun updateUserStatus_shouldCallOnSuccessWhenUpdateIsSuccessful() = runTest {
        val uid = "test_user_id"
        val isOnline = true

        doAnswer { invocation ->
            val onSuccess = invocation.getArgument<() -> Unit>(2)
            onSuccess()
            null
        }
            .whenever(userRepository)
            .updateUserStatus(eq(uid), eq(isOnline), any(), any())

        var onSuccessCalled = false
        var onFailureCalled = false

        userViewModel.updateUserStatus(
            uid = uid,
            status = isOnline,
            onSuccess = { onSuccessCalled = true },
            onFailure = { onFailureCalled = true })

        verify(userRepository).updateUserStatus(eq(uid), eq(isOnline), any(), any())
        assert(onSuccessCalled)
        assert(!onFailureCalled)
        // Optionally verify the view model's internal state if it updates anything related to isOnline
    }

    @Test
    fun updateUserStatus_shouldCallOnFailureWhenUpdateFails() = runTest {
        val uid = "test_user_id"
        val isOnline = false
        val exception = Exception("Failed to update status")

        doAnswer { invocation ->
            val onFailure = invocation.getArgument<(Exception) -> Unit>(3)
            onFailure(exception)
            null
        }
            .whenever(userRepository)
            .updateUserStatus(eq(uid), eq(isOnline), any(), any())

        var onSuccessCalled = false
        var onFailureCalled = false
        var caughtException: Exception? = null

        userViewModel.updateUserStatus(
            uid = uid,
            status = isOnline,
            onSuccess = { onSuccessCalled = true },
            onFailure = { error ->
                onFailureCalled = true
                caughtException = error
            })

        verify(userRepository).updateUserStatus(eq(uid), eq(isOnline), any(), any())
        assert(!onSuccessCalled)
        assert(onFailureCalled)
        assert(caughtException == exception)
    }

    @Test
    fun setCurrentUser() {
        val user2 =
            User(
                "2222",
                "Martin",
                "Vetterli",
                "+41 00 000 00 01",
                null,
                "martin.vetterli@epfl.ch",
                location
            )

        doAnswer { invocationOnMock ->
            val onSuccess = invocationOnMock.getArgument<(User) -> Unit>(0)
            val onFailure = invocationOnMock.getArgument<(Exception) -> Unit>(1)
            onSuccess(user2)
            onFailure(exception)
        }
            .whenever(userRepository)
            .getUserAccount(any(), any())

        doAnswer { invocationOnMock ->
            val onSuccess = invocationOnMock.getArgument<(List<User>) -> Unit>(1)
            val onFailure = invocationOnMock.getArgument<(Exception) -> Unit>(2)
            onSuccess(listOf(user2))
            onFailure(exception)
        }
            .whenever(userRepository)
            .getUserFriends(any(), any(), any())

        doAnswer { invocationOnMock ->
            val onSuccess = invocationOnMock.getArgument<(List<User>) -> Unit>(1)
            val onFailure = invocationOnMock.getArgument<(Exception) -> Unit>(2)
            onSuccess(listOf(user2))
            onFailure(exception)
        }
            .whenever(userRepository)
            .getBlockedFriends(any(), any(), any())

        userViewModel.setCurrentUser()

        verify(userRepository).getUserAccount(any(), any())
        verify(userRepository).getUserFriends(any(), any(), any())
        verify(userRepository).getUserFriendRequests(any(), any(), any())
        verify(userRepository).getSentFriendRequests(any(), any(), any())
        verify(userRepository).getBlockedFriends(any(), any(), any())
        verify(userRepository).getRecommendedFriends(any(), any(), any())

        assertThat(userViewModel.getCurrentUser().value.uid, `is`(user2.uid))
        assert(userViewModel.getUserFriends().value.map { it.uid }.contains(user2.uid))
        assert(userViewModel.getBlockedFriends().value.map { it.uid }.contains(user2.uid))
    }

    @Test
    fun setCurrentUserUid() {
        val user2 =
            User(
                "2222",
                "Martin",
                "Vetterli",
                "+41 00 000 00 01",
                null,
                "martin.vetterli@epfl.ch",
                location
            )

        doAnswer { invocationOnMock ->
            val uid = invocationOnMock.getArgument<String>(0)
            val onSuccess = invocationOnMock.getArgument<(User) -> Unit>(1)
            val onFailure = invocationOnMock.getArgument<(Exception) -> Unit>(2)
            uid
            onSuccess(user2)
            onFailure(exception)
        }
            .whenever(userRepository)
            .getUserAccount(any(), any(), any())

        doAnswer { invocationOnMock ->
            val onSuccess = invocationOnMock.getArgument<(List<User>) -> Unit>(1)
            val onFailure = invocationOnMock.getArgument<(Exception) -> Unit>(2)
            onSuccess(listOf(user2))
            onFailure(exception)
        }
            .whenever(userRepository)
            .getUserFriends(any(), any(), any())

        doAnswer { invocationOnMock ->
            val onSuccess = invocationOnMock.getArgument<(List<User>) -> Unit>(1)
            val onFailure = invocationOnMock.getArgument<(Exception) -> Unit>(2)
            onSuccess(listOf(user2))
            onFailure(exception)
        }
            .whenever(userRepository)
            .getBlockedFriends(any(), any(), any())

        userViewModel.setCurrentUser("2222", {}, {})

        verify(userRepository).getUserAccount(any(), any(), any())
        verify(userRepository).getUserFriends(any(), any(), any())
        verify(userRepository).getBlockedFriends(any(), any(), any())
        verify(userRepository).getUserFriendRequests(any(), any(), any())
        verify(userRepository).getSentFriendRequests(any(), any(), any())
        verify(userRepository).getRecommendedFriends(any(), any(), any())

        assertThat(userViewModel.getCurrentUser().value.uid, `is`(user2.uid))
        assert(userViewModel.getUserFriends().value.map { it.uid }.contains(user2.uid))
        assert(userViewModel.getBlockedFriends().value.map { it.uid }.contains(user2.uid))
    }

    @Test
    fun setUser() {
        val user2 =
            User(
                "2222",
                "Martin",
                "Vetterli",
                "+41 00 000 00 01",
                null,
                "martin.vetterli@epfl.ch",
                location
            )

        userViewModel.setUser(user2, {}, {})

        verify(userRepository).updateUserAccount(any(), any(), any())
        assertThat(userViewModel.getCurrentUser().value, `is`(user2))
    }

    @Test
    fun verifyNoAccountExists() {
        userViewModel.verifyNoAccountExists(user.emailAddress, {}, {})
        verify(userRepository).verifyNoAccountExists(any(), any(), any())
    }

    @Test
    fun createUserAccount() {
        userViewModel.createUserAccount(user, {}, {})
        verify(userRepository).createUserAccount(any(), any(), any())
    }

    @Test
    fun getCurrentUser() {
        assertThat(userViewModel.getCurrentUser().value.uid, `is`(user.uid))
        assertThat(userViewModel.getCurrentUser().value.emailAddress, `is`(user.emailAddress))
        assertThat(userViewModel.getCurrentUser().value.lastName, `is`(user.lastName))
        assertThat(userViewModel.getCurrentUser().value.firstName, `is`(user.firstName))
        assertThat(userViewModel.getCurrentUser().value.phoneNumber, `is`(user.phoneNumber))
    }

    @Test
    fun setUserFriends() {
        val user2 =
            User(
                "2222",
                "Martin",
                "Vetterli",
                "+41 00 000 00 01",
                null,
                "martin.vetterli@epfl.ch",
                location
            )

        userViewModel.setUserFriends(friendsList = listOf(user), onSuccess = {}, onFailure = {})
        assert(userViewModel.getUserFriends().value.map { it.uid }.contains(user.uid))

        userViewModel.setUserFriends(friendsList = listOf(user2), onSuccess = {}, onFailure = {})
        assert(userViewModel.getUserFriends().value.map { it.uid }.contains(user2.uid))
    }

    @Test
    fun acceptFriendRequest() {
        val user =
            User(
                "1",
                "Martin",
                "Vetterli",
                "+41 00 000 00 01",
                null,
                "martin.vetterli@epfl.ch",
                location
            )
        val friend =
            User("2", "Jane", "Doe", "+41 00 000 00 02", null, "jane.doe@example.com", location)

        // Mock the repository methods
        doAnswer { invocation ->
            val onSuccess = invocation.getArgument<() -> Unit>(2)
            onSuccess()
            null
        }
            .whenever(userRepository)
            .createFriend(any(), any(), any(), any())

        // Call the method
        userViewModel.acceptFriendRequest(user, friend, {}, {})

        // Verify the repository interactions
        verify(userRepository).createFriend(eq(user.uid), eq(friend.uid), any(), any())

        // Verify the state updates
        assert(userViewModel.getUserFriends().value.contains(friend))
        assert(!userViewModel.getUserFriendRequests().value.contains(friend))
    }

    @Test
    fun declineFriendRequest_shouldRemoveFriendRequest() {
        val user =
            User(
                "1",
                "Alexandre",
                "Carel",
                "+33 6 59 20 70 02",
                null,
                "alexandre.carel@epfl.ch",
                lastKnownLocation = location
            )
        val friend =
            User(
                "2",
                "Jane",
                "Doe",
                "+41 00 000 00 02",
                null,
                "jane.doe@example.com",
                lastKnownLocation = location
            )

        // Mock the repository method
        doAnswer { invocation ->
            val onSuccess = invocation.getArgument<() -> Unit>(2)
            onSuccess()
            null
        }
            .whenever(userRepository)
            .deleteFriendRequest(any(), any(), any(), any())

        // Call the method
        userViewModel.declineFriendRequest(user, friend, {}, {})

        // Verify the repository interaction
        verify(userRepository).deleteFriendRequest(eq(user.uid), eq(friend.uid), any(), any())

        // Verify the state update
        assert(!userViewModel.getUserFriendRequests().value.map { it.uid }.contains(friend.uid))
    }

    @Test
    fun sendFriendRequest() {
        val user =
            User(
                "1",
                "Martin",
                "Vetterli",
                "+41 00 000 00 01",
                null,
                "martin.vetterli@epfl.ch",
                location
            )
        val friend =
            User("2", "Jane", "Doe", "+41 00 000 00 02", null, "jane.doe@example.com", location)

        // Mock the repository methods
        doAnswer { invocation ->
            val onSuccess = invocation.getArgument<() -> Unit>(2)
            onSuccess()
            null
        }
            .whenever(userRepository)
            .createFriendRequest(any(), any(), any(), any())

        // Call the method
        userViewModel.sendFriendRequest(user, friend, {}, {})

        // Verify the repository interactions
        verify(userRepository).createFriendRequest(eq(user.uid), eq(friend.uid), any(), any())

        // Verify the state updates
        assert(userViewModel.getSentFriendRequests().value.contains(friend))
    }

    @Test
    fun testBlockUser() = runTest {
        val user =
            User(
                "1",
                "John",
                "Doe",
                "+1234567890",
                null,
                "john.doe@example.com",
                MutableStateFlow(Location("mock_provider"))
            )
        val blockedUser =
            User(
                "2",
                "Jane",
                "Doe",
                "+0987654321",
                null,
                "jane.doe@example.com",
                MutableStateFlow(Location("mock_provider"))
            )

        var onSuccessCalled = false
        var onFailureCalled = false

        doAnswer {
            val onSuccess = it.getArgument<() -> Unit>(2)
            onSuccess()
            null
        }
            .`when`(userRepository)
            .blockUser(any(), any(), any(), any())

        userViewModel.blockUser(
            user, blockedUser, { onSuccessCalled = true }, { onFailureCalled = true })

        verify(userRepository).blockUser(eq(user), eq(blockedUser), any(), any())
        assert(onSuccessCalled)
        assert(!onFailureCalled)
        assert(userViewModel.getBlockedFriends().value.contains(blockedUser))
        assert(!userViewModel.getUserFriends().value.contains(blockedUser))
        assert(!userViewModel.getUserFriendRequests().value.contains(blockedUser))
        assert(!userViewModel.getSentFriendRequests().value.contains(blockedUser))
    }

    @Test
    fun setBlockedFriends() {
        val user2 =
            User(
                "2222",
                "Martin",
                "Vetterli",
                "+41 00 000 00 01",
                null,
                "martin.vetterli@epfl.ch",
                location
            )

        userViewModel.setBlockedFriends(
            blockedFriendsList = listOf(user), onSuccess = {}, onFailure = {})
        assert(userViewModel.getBlockedFriends().value.map { it.uid }.contains(user.uid))

        userViewModel.setBlockedFriends(
            blockedFriendsList = listOf(user2), onSuccess = {}, onFailure = {})
        assert(userViewModel.getBlockedFriends().value.map { it.uid }.contains(user2.uid))
    }

    @Test
    fun getLocation() {
        val location =
            Location("mock_provider").apply {
                latitude = 0.0
                longitude = 0.0
            }

        assertThat(userViewModel.getLocation().value.latitude, `is`(0.0))
        assertThat(userViewModel.getLocation().value.longitude, `is`(0.0))
    }

    @Test
    fun updateLocation() {
        val mockLocation = Mockito.mock(Location::class.java)

        // Set up the mock to return specific values
        Mockito.`when`(mockLocation.latitude).thenReturn(1.0)
        Mockito.`when`(mockLocation.longitude).thenReturn(1.0)

        userViewModel.updateLocation(location = mockLocation, onSuccess = {}, onFailure = {})
        verify(userRepository).updateUserLocation(any(), any(), any(), any())
        assertThat(userViewModel.getLocation().value.latitude, `is`(1.0))
        assertThat(userViewModel.getLocation().value.longitude, `is`(1.0))
    }

    @Test
    fun getNewUid() {
        `when`(userRepository.getNewUid()).thenReturn("uid")
        assertThat(userViewModel.getNewUid(), `is`("uid"))
    }

    @Test
    fun loadFriends_success() {

        val friendLocation =
            MutableStateFlow(
                Location("mock_provider").apply {
                    latitude = 1.0
                    longitude = 1.0
                })

        // Arrange
        val friend =
            User(
                "2",
                "Jane",
                "Doe",
                "+41 00 000 00 02",
                null,
                "jane.doe@example.com",
                friendLocation
            )
        val friendsList = listOf(friend)

        // Mock the repository method to call the onSuccess callback with the friendsMap
        whenever(userRepository.getUserFriends(any(), any(), any())).thenAnswer {
            val onSuccess = it.getArgument<(List<User>) -> Unit>(1)
            onSuccess(friendsList)
        }

        userViewModel.setUser(user.copy(uid = "1"), {}, {})

        // Act
        runBlocking { userViewModel.loadFriends() }

        // Assert
        assertThat(userViewModel.getUserFriends().value, `is`(friendsList))
        verify(userRepository).getUserFriends(any(), any(), any())
    }

    @Test
    fun loadFriendsLocations_failure() {
        // Arrange
        val errorMessage = "Failed to fetch friends' locations"
        val exception = Exception(errorMessage)
        val userRepository: UserRepository = mock() // Mock the UserRepository
        userViewModel = UserViewModel(userRepository, chatRepository) // Instantiate the ViewModel

        userViewModel.setUser(user.copy(uid = "1"), {}, {})

        // Mock repository method to simulate a failure
        whenever(userRepository.getUserFriends(any(), any(), any())).thenAnswer {
            val onFailure = it.getArgument<(Exception) -> Unit>(2)
            onFailure(exception)
        }

        // Act
        userViewModel.loadFriends()
        testDispatcher.scheduler.advanceUntilIdle() // Ensure coroutines complete

        // Assert
        verify(userRepository).getUserFriends(any(), any(), any())
        // Additional checks can be added to validate that the error state is handled properly
    }

    @Test
    fun getRelativeDistance_knownFriendLocation() {
        // Arrange
        userViewModel.setUser(user, {}, {})
        val friendLocation =
            MutableStateFlow(
                Location("mock_provider").apply {
                    latitude = 1.0
                    longitude = 1.0
                })
        val friend =
            User(
                "2",
                "Jane",
                "Doe",
                "+41 00 000 00 02",
                null,
                "jane.doe@example.com",
                friendLocation
            )
        val friendsMap = mapOf(friend to friendLocation.value)

        // Mock repository method for loading friend locations
        whenever(userRepository.getUserFriends(any(), any(), any())).thenAnswer {
            val onSuccess = it.getArgument<(Map<User, Location?>) -> Unit>(1)
            onSuccess(friendsMap)
        }

        // Update the user location
        userViewModel.updateLocation(friend, friendLocation.value, onSuccess = {}, onFailure = {})

        // Act
        val distance = userViewModel.getRelativeDistance(friend)

        // Assert
        //        assertThat(distance,
        // `is`(user.lastKnownLocation.value.distanceTo(friendLocation.value)))
        assertThat(distance, `is`(Float.MAX_VALUE))
    }

    @Test
    fun getRelativeDistance_unknownFriendLocation() {
        // Arrange
        userViewModel.updateLocation(location = location.value, onSuccess = {}, onFailure = {})

        val friend =
            User("2", "Jane", "Doe", "+41 00 000 00 02", null, "jane.doe@example.com", location)

        // Act
        val distance = userViewModel.getRelativeDistance(friend)

        // Assert
        assertThat(distance, `is`(Float.MAX_VALUE))
    }

    @Test
    fun getRelativeDistance_noUserLocation() {
        // Arrange
        val friendLocation =
            MutableStateFlow(
                Location("mock_provider").apply {
                    latitude = 1.0
                    longitude = 1.0
                })
        val friend =
            User(
                "2",
                "Jane",
                "Doe",
                "+41 00 000 00 02",
                null,
                "jane.doe@example.com",
                friendLocation
            )

        userViewModel.updateLocation(friend, friendLocation.value, onSuccess = {}, onFailure = {})

        // Act
        val distance = userViewModel.getRelativeDistance(friend)

        // Assert
        assertThat(distance, `is`(Float.MAX_VALUE))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_sendMessage_success() = runTest {
        // Arrange
        val currentUser =
            User(
                uid = "currentUserId",
                firstName = "Current",
                lastName = "User",
                phoneNumber = "+41 00 000 00 01",
                profilePicture = null,
                emailAddress = "current.user@example.com",
                lastKnownLocation = location
            )

        val friend =
            User(
                uid = "friendUserId",
                firstName = "Friend",
                lastName = "User",
                phoneNumber = "+41 00 000 00 02",
                profilePicture = null,
                emailAddress = "friend.user@example.com",
                lastKnownLocation = location
            )

        val chatId = "chat456"
        val messageContent = "Hello"

        val messagesFlow = MutableSharedFlow<List<Message>>()

        // Initialiser le userViewModel avec le currentUser
        userViewModel.setUser(currentUser, {}, {})

        // Définir les amis de l'utilisateur
        userViewModel.setUserFriends(friendsList = listOf(friend), onSuccess = {}, onFailure = {})

        // Mock chatRepository.getOrCreateChat pour retourner chatId
        whenever(chatRepository.getOrCreateChat(friend.uid, currentUser.uid)).thenReturn(chatId)

        // Mock chatRepository.getMessages pour retourner un Flow
        whenever(chatRepository.getMessages(chatId)).thenReturn(messagesFlow)

        // Mock chatRepository.sendMessage
        whenever(chatRepository.sendMessage(chatId, messageContent, currentUser, friend))
            .thenReturn(Unit)

        // Act
        userViewModel.getOrCreateChat(friend.uid)
        advanceUntilIdle()

        userViewModel.sendMessage(messageContent, user = friend)
        advanceUntilIdle()

        // Assert
        verify(chatRepository).sendMessage(chatId, messageContent, currentUser, friend)
    }

    @Test
    fun testSendVerificationCode() {
        val phoneNumber = "+1234567890"
        val verificationId = "verificationId"
        val observer = mock(Observer::class.java) as Observer<String>

        userViewModel.verificationStatus.observeForever(observer)

        doAnswer {
            val onSuccess = it.getArgument<(String) -> Unit>(1)
            onSuccess(verificationId)
        }
            .`when`(userRepository)
            .sendVerificationCode(eq(phoneNumber), any(), any())

        userViewModel.sendVerificationCode(phoneNumber)

        verify(observer).onChanged("Code Sent")
    }

    @Test
    fun testVerifyCode() {
        val verificationId = "verificationId"
        val code = "123456"
        val observer = mock(Observer::class.java) as Observer<String>

        (userViewModel.verificationId as MutableLiveData).postValue(verificationId)
        userViewModel.verificationStatus.observeForever(observer)

        doAnswer {
            val onSuccess = it.getArgument<() -> Unit>(2)
            onSuccess()
        }
            .`when`(userRepository)
            .verifyCode(eq(verificationId), eq(code), any(), any())

        userViewModel.verifyCode(code)

        verify(observer).onChanged("Phone Verified")
    }

    @Test
    fun verifyUnusedPhoneNumberCallsOnSuccessWhenPhoneNumberIsUnused() {
        // Arrange
        val phoneNumber = "+41791234567"
        val onSuccess = mock<(Boolean) -> Unit>()
        val onFailure = mock<(Exception) -> Unit>()

        doAnswer { invocation ->
            val successCallback = invocation.getArgument<(Boolean) -> Unit>(1)
            successCallback(true) // Simulate that the phone number is unused
            null
        }
            .whenever(userRepository)
            .verifyUnusedPhoneNumber(eq(phoneNumber), any(), any())

        // Act
        userViewModel.verifyUnusedPhoneNumber(phoneNumber, onSuccess, onFailure)

        // Assert
        verify(onSuccess).invoke(true) // Verify onSuccess was called with true
        verifyNoInteractions(onFailure) // Verify onFailure was not called
    }

    @Test
    fun verifyUnusedPhoneNumberInvokesOnSuccessWithFalseWhenPhoneNumberIsInUse() {
        // Arrange
        val phoneNumber = "1234567890"
        val onSuccess = mock<(Boolean) -> Unit>()
        val onFailure = mock<(Exception) -> Unit>()

        // Mock repository behavior
        doAnswer { invocation ->
            val successCallback = invocation.arguments[1] as (Boolean) -> Unit
            successCallback(false) // Simulate phone number being in use
            null
        }
            .whenever(userRepository)
            .verifyUnusedPhoneNumber(eq(phoneNumber), any(), any())

        // Act
        userViewModel.verifyUnusedPhoneNumber(phoneNumber, onSuccess, onFailure)

        // Assert
        verify(onSuccess).invoke(false) // Verify onSuccess(false) was called
        verify(onFailure, never()).invoke(any()) // Verify onFailure was never called
    }

    @Test
    fun verifyUnusedPhoneNumberInvokesOnFailureWhenAnExceptionOccurs() {
        // Arrange
        val phoneNumber = "1234567890"
        val onSuccess = mock<(Boolean) -> Unit>()
        val onFailure = mock<(Exception) -> Unit>()
        val exception = Exception("Test Exception")

        // Mock repository behavior
        doAnswer { invocation ->
            val failureCallback = invocation.arguments[2] as (Exception) -> Unit
            failureCallback(exception) // Simulate an exception
            null
        }
            .whenever(userRepository)
            .verifyUnusedPhoneNumber(eq(phoneNumber), any(), any())

        // Act
        userViewModel.verifyUnusedPhoneNumber(phoneNumber, onSuccess, onFailure)

        // Assert
        verify(onFailure).invoke(exception) // Verify onFailure(exception) was called
        verify(onSuccess, never()).invoke(any()) // Verify onSuccess was never called
    }

    @Test
    fun shareLocationWithFriendSuccessfullySharesLocation() {
        val uid = "user123"
        val friend =
            User(
                "friend123",
                "Jane",
                "Doe",
                "+41 00 000 00 02",
                null,
                "jane.doe@example.com",
                location
            )

        doAnswer { invocation ->
            val onSuccess = invocation.getArgument<() -> Unit>(2)
            onSuccess()
            null
        }
            .whenever(userRepository)
            .shareLocationWithFriend(eq(uid), eq(friend.uid), any(), any())

        userViewModel.shareLocationWithFriend(uid, friend, {}, {})

        verify(userRepository).shareLocationWithFriend(eq(uid), eq(friend.uid), any(), any())
        assert(userViewModel.locationSharedWith.value.contains(friend))
    }

    @Test
    fun stopSharingLocationWithFriendSuccessfullyStopsSharing() {
        val uid = "user123"
        val friend =
            User(
                "friend123",
                "Jane",
                "Doe",
                "+41 00 000 00 02",
                null,
                "jane.doe@example.com",
                location
            )

        doAnswer { invocation ->
            val onSuccess = invocation.getArgument<() -> Unit>(2)
            onSuccess()
            null
        }
            .whenever(userRepository)
            .stopSharingLocationWithFriend(eq(uid), eq(friend.uid), any(), any())

        userViewModel.stopSharingLocationWithFriend(uid, friend, {}, {})

        verify(userRepository).stopSharingLocationWithFriend(eq(uid), eq(friend.uid), any(), any())
        assert(!userViewModel.locationSharedWith.value.contains(friend))
    }

    @Test
    fun testUnblockUser_Success() = runTest {
        // Arrange
        val currentUser =
            User(
                "current_id",
                "Current",
                "User",
                "+41789123450",
                null,
                "current.user@example.com",
                MutableStateFlow(Location("mock_provider"))
            )
        val blockedUser =
            User(
                "blocked_id",
                "Blocked",
                "User",
                "+41789123456",
                null,
                "blocked.user@example.com",
                MutableStateFlow(Location("mock_provider"))
            )

        // Set current user
        userViewModel.setUser(currentUser, {}, {})

        // Mock setBlockedFriends behavior
        doAnswer { invocation ->
            val uid = invocation.getArgument<String>(0)
            val blockedList = invocation.getArgument<List<User>>(1)
            val onSuccess = invocation.getArgument<() -> Unit>(2)
            onSuccess()
            null
        }
            .whenever(userRepository)
            .setBlockedFriends(any(), any(), any(), any())

        // Add user to blocked list initially
        userViewModel.setBlockedFriends(
            blockedFriendsList = listOf(blockedUser), onSuccess = {}, onFailure = {})

        var onSuccessCalled = false
        var onFailureCalled = false

        // Mock unblockUser behavior
        doAnswer { invocation ->
            val onSuccess = invocation.getArgument<() -> Unit>(2)
            onSuccess()
            null
        }
            .whenever(userRepository)
            .unblockUser(any(), any(), any(), any())

        // Act
        userViewModel.unblockUser(
            blockedUser = blockedUser,
            onSuccess = { onSuccessCalled = true },
            onFailure = { onFailureCalled = true })

        advanceUntilIdle()

        // Assert
        verify(userRepository).unblockUser(eq(currentUser.uid), eq(blockedUser.uid), any(), any())
        assert(onSuccessCalled)
        assert(!onFailureCalled)
        assert(!userViewModel.getBlockedFriends().value.contains(blockedUser))
    }

    @Test
    fun testUnblockUser_Failure() = runTest {
        // Arrange
        val currentUser =
            User(
                "current_id",
                "Current",
                "User",
                "+41789123450",
                null,
                "current.user@example.com",
                MutableStateFlow(Location("mock_provider"))
            )
        val blockedUser =
            User(
                "blocked_id",
                "Blocked",
                "User",
                "+41789123456",
                null,
                "blocked.user@example.com",
                MutableStateFlow(Location("mock_provider"))
            )
        val error = Exception("Failed to unblock user")

        // Set current user
        userViewModel.setUser(currentUser, {}, {})

        // Mock setBlockedFriends behavior
        doAnswer { invocation ->
            val uid = invocation.getArgument<String>(0)
            val blockedList = invocation.getArgument<List<User>>(1)
            val onSuccess = invocation.getArgument<() -> Unit>(2)
            onSuccess()
            null
        }
            .whenever(userRepository)
            .setBlockedFriends(any(), any(), any(), any())

        // Add user to blocked list initially
        userViewModel.setBlockedFriends(
            blockedFriendsList = listOf(blockedUser), onSuccess = {}, onFailure = {})

        var onSuccessCalled = false
        var onFailureCalled = false
        var failureException: Exception? = null

        // Mock unblockUser behavior to fail
        doAnswer { invocation ->
            val onFailure = invocation.getArgument<(Exception) -> Unit>(3)
            onFailure(error)
            null
        }
            .whenever(userRepository)
            .unblockUser(any(), any(), any(), any())

        // Act
        userViewModel.unblockUser(
            blockedUser = blockedUser,
            onSuccess = { onSuccessCalled = true },
            onFailure = { e ->
                onFailureCalled = true
                failureException = e
            })

        advanceUntilIdle()

        // Assert
        verify(userRepository).unblockUser(eq(currentUser.uid), eq(blockedUser.uid), any(), any())
        assert(!onSuccessCalled)
        assert(onFailureCalled)
        assertEquals(error, failureException)
        // User should still be in blocked list since unblock failed
        assert(userViewModel.getBlockedFriends().value.contains(blockedUser))
    }

    @Test
    fun logoutShouldCallRepositoryAndResetViewModelState() = runTest {
        // Mock repository behavior
        `when`(userRepository.logoutUser()).then {}

        // Call the logout method
        userViewModel.logout()

        // Verify repository's logoutUser() is called
        verify(userRepository).logoutUser()

        // Assert that all ViewModel fields are reset correctly
        with(userViewModel) {
            assertEquals(userDummy2, getCurrentUser().value)
            assertTrue(messages.value.isEmpty())
            assertTrue(getUserFriends().value.isEmpty())
            assertTrue(getUserFriendRequests().value.isEmpty())
            assertTrue(getSentFriendRequests().value.isEmpty())
            assertTrue(getUserRecommendedFriends().value.isEmpty())
            assertTrue(getBlockedFriends().value.isEmpty())
            assertTrue(locationSharedWith.value.isEmpty())
            assertEquals("", phoneNumber.value)
            assertTrue(chatSummaries.value.isEmpty())
            assertEquals("", verificationId.value)
            assertEquals("", verificationStatus.value)
            assertEquals(userDummy1, getSelectedContact().value)
        }
    }

    @Test
    fun saveRadiusCallsRepositoryWithCorrectValue() {
        // Arrange
        val radius = 10.5f

        // Act
        userViewModel.saveRadius(radius)

        // Assert
        verify(userRepository).saveRadius(radius)
    }

    @Test
    fun saveNotificationStatusCallsRepositoryWithCorrectValue() {
        // Arrange
        val notificationStatus = true

        // Act
        userViewModel.saveNotificationStatus(notificationStatus)

        // Assert
        verify(userRepository).saveNotificationStatus(notificationStatus)
    }

    @Test
    fun deleteUserAccount_successfulAllSteps_callsOnSuccess() {
        // Arrange
        val uid = "test_uid"
        val phoneNumber = "+123456789"
        val emailAddress = "test@example.com"

        // Mock user data
        val userData = mapOf("phoneNumber" to phoneNumber, "emailAddress" to emailAddress)
        whenever(mockUserDocumentReference.get()).thenReturn(
            Tasks.forResult(
                mockUserDocumentSnapshot
            )
        )
        whenever(mockUserDocumentSnapshot.data).thenReturn(userData)

        // Mock getBlockedBy to return a list of users who blocked `uid`
        val blockingUserUid = "blocking_user"
        val blockingUserDocSnapshot = mock(DocumentSnapshot::class.java)
        whenever(blockingUserDocSnapshot.data).thenReturn(
            mapOf(
                "uid" to blockingUserUid,
                "blockedList" to listOf(uid) // contains the user to delete
            )
        )

        val mockUserDocumentReference = mock(DocumentReference::class.java)
        whenever(mockUserCollectionReference.document(eq(uid))).thenReturn(mockUserDocumentReference)

        // Then mock the delete operation:
        whenever(mockUserDocumentReference.delete()).thenReturn(Tasks.forResult(null))

        // Mock Tasks.whenAllSuccess for getBlockedBy logic
        whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
        whenever(mockUserCollectionReference.get()).thenReturn(Tasks.forResult(mockUserQuerySnapshot))
        whenever(mockUserQuerySnapshot.documents).thenReturn(listOf(blockingUserDocSnapshot))

        // When calling getBlockedBy, we'll simulate that it returns one blocking user
        // The unblock operation requires updating that user's blockedList
        val blockingUserRef = mock(DocumentReference::class.java)
        whenever(mockUserCollectionReference.document(blockingUserUid)).thenReturn(blockingUserRef)
        whenever(blockingUserRef.get()).thenReturn(Tasks.forResult(blockingUserDocSnapshot))
        // After removing uid from blockedList
        whenever(
            blockingUserRef.update(
                "blockedList",
                emptyList<String>()
            )
        ).thenReturn(Tasks.forResult(null))

        // Mock delete from Phones and Emails
        whenever(
            mockPhoneCollectionReference.document(phoneNumber).delete()
        ).thenReturn(Tasks.forResult(null))
        whenever(
            mockEmailCollectionReference.document(emailAddress).delete()
        ).thenReturn(Tasks.forResult(null))

        // Mock chat deletion logic: no chats or successful deletion
        whenever(mockChatCollectionReference.whereArrayContains(anyString(), eq(uid))).thenReturn(
            mockQuery
        )
        whenever(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
        // no chats to delete
        whenever(mockQuerySnapshot.documents).thenReturn(emptyList())

        // Mock final user doc deletion
        whenever(mockUserDocumentReference.delete()).thenReturn(Tasks.forResult(null))

        var onSuccessCalled = false
        var onFailureCalled = false

        // Act
        userRepository.deleteUserAccount(
            uid = uid,
            onSuccess = { onSuccessCalled = true },
            onFailure = { onFailureCalled = true }
        )

        shadowOf(getMainLooper()).idle() // Ensure all async tasks complete

        // Assert
        assertTrue("Expected onSuccess to be called", onSuccessCalled)
        assertFalse("Expected onFailure not to be called", onFailureCalled)
        verify(mockUserDocumentReference).delete()
    }

    @Test
    fun deleteUserAccount_failureOnUnblockOperation_callsOnFailure() {
        // Arrange
        val uid = "test_uid"
        val phoneNumber = "+123456789"
        val emailAddress = "test@example.com"

        val userData = mapOf("phoneNumber" to phoneNumber, "emailAddress" to emailAddress)
        whenever(mockUserDocumentReference.get()).thenReturn(
            Tasks.forResult(
                mockUserDocumentSnapshot
            )
        )
        whenever(mockUserDocumentSnapshot.data).thenReturn(userData)

        // getBlockedBy returns one blocking user
        val blockingUserUid = "blocking_user"
        val blockingUserDocSnapshot = mock(DocumentSnapshot::class.java)
        whenever(blockingUserDocSnapshot.data).thenReturn(
            mapOf(
                "uid" to blockingUserUid,
                "blockedList" to listOf(uid)
            )
        )

        whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
        whenever(mockUserCollectionReference.get()).thenReturn(Tasks.forResult(mockUserQuerySnapshot))
        whenever(mockUserQuerySnapshot.documents).thenReturn(listOf(blockingUserDocSnapshot))

        val blockingUserRef = mock(DocumentReference::class.java)
        whenever(mockUserCollectionReference.document(blockingUserUid)).thenReturn(blockingUserRef)
        whenever(blockingUserRef.get()).thenReturn(Tasks.forResult(blockingUserDocSnapshot))

        // Fail to update the blockingUser's blockedList
        val unblockException = Exception("Failed to unblock user")
        whenever(blockingUserRef.update("blockedList", emptyList<String>()))
            .thenReturn(Tasks.forException(unblockException))

        var onSuccessCalled = false
        var onFailureCalled = false
        var capturedException: Exception? = null
        doAnswer { invocation ->
            val onFailureCallback = invocation.getArgument<(Exception) -> Unit>(3)
            onFailureCallback(Exception("Simulated unblock failure"))
            null
        }.whenever(userRepository).unblockUser(any(), any(), any(), any())

        // Act
        userRepository.deleteUserAccount(
            uid = uid,
            onSuccess = { onSuccessCalled = true },
            onFailure = {
                onFailureCalled = true
                capturedException = it
            }
        )

        shadowOf(getMainLooper()).idle() // Execute async tasks

        // Assert
        assertFalse("onSuccess should not be called if unblock fails", onSuccessCalled)
        assertTrue("onFailure should be called if unblock fails", onFailureCalled)
        assertEquals("Failed to unblock user", capturedException?.message)
        verify(mockUserDocumentReference, never()).delete() // user doc shouldn't be deleted
    }

    @Test
    fun deleteUserAccount_failureOnUserDocDeletion_callsOnFailure() {
        // Arrange
        val uid = "test_uid"
        val phoneNumber = "+123456789"
        val emailAddress = "test@example.com"

        val userData = mapOf("phoneNumber" to phoneNumber, "emailAddress" to emailAddress)
        whenever(mockUserDocumentReference.get()).thenReturn(
            Tasks.forResult(
                mockUserDocumentSnapshot
            )
        )
        whenever(mockUserDocumentSnapshot.data).thenReturn(userData)

        // No blocking users
        whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
        whenever(mockUserCollectionReference.get()).thenReturn(Tasks.forResult(mockUserQuerySnapshot))
        whenever(mockUserQuerySnapshot.documents).thenReturn(emptyList()) // no one blocked this user

        // Mock deletion of phone and email to succeed
        whenever(
            mockPhoneCollectionReference.document(phoneNumber).delete()
        ).thenReturn(Tasks.forResult(null))
        whenever(
            mockEmailCollectionReference.document(emailAddress).delete()
        ).thenReturn(Tasks.forResult(null))

        // Mock no chats
        whenever(mockChatCollectionReference.whereArrayContains(anyString(), eq(uid))).thenReturn(
            mockQuery
        )
        whenever(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
        whenever(mockQuerySnapshot.documents).thenReturn(emptyList())

        // Fail on user doc deletion
        val exception = Exception("Failed to delete user doc")
        whenever(mockUserDocumentReference.delete()).thenReturn(Tasks.forException(exception))

        var onSuccessCalled = false
        var onFailureCalled = false
        var capturedException: Exception? = null

        // Act
        userRepository.deleteUserAccount(
            uid = uid,
            onSuccess = { onSuccessCalled = true },
            onFailure = {
                onFailureCalled = true
                capturedException = it
            }
        )

        shadowOf(getMainLooper()).idle()

        // Assert
        assertFalse(onSuccessCalled)
        assertTrue(onFailureCalled)
        assertEquals("Failed to delete user doc", capturedException?.message)
    }

    @Test
    fun deleteUserAccount_noPhoneOrEmailNoBlockers_callsOnSuccess() {
        // Arrange
        val uid = "test_uid"
        val userData = emptyMap<String, Any>() // no phoneNumber or emailAddress
        whenever(mockUserDocumentReference.get()).thenReturn(
            Tasks.forResult(
                mockUserDocumentSnapshot
            )
        )
        whenever(mockUserDocumentSnapshot.data).thenReturn(userData)

        // No blocking users
        whenever(mockFirestore.collection("Users")).thenReturn(mockUserCollectionReference)
        whenever(mockUserCollectionReference.get()).thenReturn(Tasks.forResult(mockUserQuerySnapshot))
        whenever(mockUserQuerySnapshot.documents).thenReturn(emptyList())

        // No phone or email deletion needed

        // No chats
        whenever(mockChatCollectionReference.whereArrayContains(anyString(), eq(uid))).thenReturn(
            mockQuery
        )
        whenever(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
        whenever(mockQuerySnapshot.documents).thenReturn(emptyList())

        // user doc deletion succeeds
        whenever(mockUserDocumentReference.delete()).thenReturn(Tasks.forResult(null))

        var onSuccessCalled = false
        var onFailureCalled = false

        // Act
        userRepository.deleteUserAccount(
            uid = uid,
            onSuccess = { onSuccessCalled = true },
            onFailure = { onFailureCalled = true }
        )

        shadowOf(getMainLooper()).idle()

        // Assert
        assertTrue(onSuccessCalled)
        assertFalse(onFailureCalled)
        verify(mockUserDocumentReference).delete()
    }
    @Test
    fun deleteUserAccount_successfulDeletion() = runTest {
        // Arrange
        val currentUser = User(
            uid = "testUserId",
            firstName = "Test",
            lastName = "User",
            phoneNumber = "+41 00 000 00 01",
            profilePicture = null,
            emailAddress = "test.user@example.com",
            lastKnownLocation = MutableStateFlow(Location("mock_provider"))
        )

        // Mocking a navigation actions instance
        val navigationActions = mock(NavigationActions::class.java)

        // Mock some friends and requests to be cleaned up before deletion
        val friend1 = User(
            uid = "friend1",
            firstName = "Friend",
            lastName = "One",
            phoneNumber = "+41 00 000 00 02",
            profilePicture = null,
            emailAddress = "friend.one@example.com",
            lastKnownLocation = MutableStateFlow(Location("mock_provider"))
        )
        val friend2 = User(
            uid = "friend2",
            firstName = "Friend",
            lastName = "Two",
            phoneNumber = "+41 00 000 00 03",
            profilePicture = null,
            emailAddress = "friend.two@example.com",
            lastKnownLocation = MutableStateFlow(Location("mock_provider"))
        )

        val requestFrom = User(
            uid = "requestFrom",
            firstName = "Requester",
            lastName = "Inbound",
            phoneNumber = "+41 00 000 00 04",
            profilePicture = null,
            emailAddress = "requester.inbound@example.com",
            lastKnownLocation = MutableStateFlow(Location("mock_provider"))
        )

        val requestTo = User(
            uid = "requestTo",
            firstName = "Requested",
            lastName = "Outbound",
            phoneNumber = "+41 00 000 00 05",
            profilePicture = null,
            emailAddress = "requested.outbound@example.com",
            lastKnownLocation = MutableStateFlow(Location("mock_provider"))
        )

        // Set the current user in the ViewModel
        userViewModel.setUser(currentUser, {}, {})

        // Populate friends and requests
        (userViewModel.getUserFriends() as MutableStateFlow).value = listOf(friend1, friend2)
        (userViewModel.getUserFriendRequests() as MutableStateFlow).value = listOf(requestFrom)
        (userViewModel.getSentFriendRequests() as MutableStateFlow).value = listOf(requestTo)

        // Mock repository.deleteFriend calls to succeed
        whenever(userRepository.deleteFriend(eq(currentUser.uid), any(), any(), any())).thenAnswer {
            val onSuccess = it.arguments[2] as () -> Unit
            onSuccess()
            null
        }

        // Mock repository.deleteFriendRequest calls to succeed
        whenever(userRepository.deleteFriendRequest(any(), any(), any(), any())).thenAnswer {
            val onSuccess = it.arguments[2] as () -> Unit
            onSuccess()
            null
        }

        // Mock repository.deleteUserAccount call to succeed
        whenever(userRepository.deleteUserAccount(eq(currentUser.uid), any(), any())).thenAnswer {
            val onSuccess = it.arguments[1] as () -> Unit
            onSuccess()
            null
        }

        // Act
        userViewModel.deleteUserAccount(navigationActions)

        // Assert
        // Check that logout was called and user state was reset
        assertEquals(userViewModel.getCurrentUser().value.uid, userViewModel.userDummy2.uid)
        assertTrue(userViewModel.getUserFriends().value.isEmpty())
        assertTrue(userViewModel.getUserFriendRequests().value.isEmpty())
        assertTrue(userViewModel.getSentFriendRequests().value.isEmpty())

        // Verify that each friend was attempted to be removed
        verify(userRepository).deleteFriend(eq(currentUser.uid), eq(friend1.uid), any(), any())
        verify(userRepository).deleteFriend(eq(currentUser.uid), eq(friend2.uid), any(), any())

        // Verify that the requests were cleaned up
        verify(userRepository).deleteFriendRequest(eq(currentUser.uid), eq(requestFrom.uid), any(), any())
        verify(userRepository).deleteFriendRequest(eq(requestTo.uid), eq(currentUser.uid), any(), any())

        // Verify that the user account deletion was called
        verify(userRepository).deleteUserAccount(eq(currentUser.uid), any(), any())

    }

    @Test
    fun deleteUserAccount_partialCleanupFailureStillDeletesAccount() = runTest {
        // Arrange
        val currentUser = User(
            uid = "testUserId",
            firstName = "Test",
            lastName = "User",
            phoneNumber = "+41 00 000 00 01",
            profilePicture = null,
            emailAddress = "test.user@example.com",
            lastKnownLocation = MutableStateFlow(Location("mock_provider"))
        )

        // Mock navigation actions
        val navigationActions = mock(NavigationActions::class.java)

        val friend = User(
            uid = "friendFail",
            firstName = "Friend",
            lastName = "Fail",
            phoneNumber = "+41 00 000 00 02",
            profilePicture = null,
            emailAddress = "friend.fail@example.com",
            lastKnownLocation = MutableStateFlow(Location("mock_provider"))
        )

        userViewModel.setUser(currentUser, {}, {})
        (userViewModel.getUserFriends() as MutableStateFlow).value = listOf(friend)

        // Mock a failure in deleting a friend
        val deletionException = Exception("Failed to delete friend")

        whenever(userRepository.deleteFriend(eq(currentUser.uid), eq(friend.uid), any(), any())).thenAnswer {
            val onFailure = it.arguments[3] as (Exception) -> Unit
            onFailure(deletionException)
            null
        }

        // Mock repository.deleteUserAccount call to succeed
        whenever(userRepository.deleteUserAccount(eq(currentUser.uid), any(), any())).thenAnswer {
            val onSuccess = it.arguments[1] as () -> Unit
            onSuccess()
            null
        }

        // Act
        userViewModel.deleteUserAccount(navigationActions)

        // Assert
        // Even if friend deletion fails, we still call deleteUserAccount
        verify(userRepository).deleteUserAccount(eq(currentUser.uid), any(), any())


        // User should be logged out
        assertEquals(userViewModel.getCurrentUser().value.uid, userViewModel.userDummy2.uid)
        assertTrue(userViewModel.getUserFriends().value.isEmpty())
    }

}
