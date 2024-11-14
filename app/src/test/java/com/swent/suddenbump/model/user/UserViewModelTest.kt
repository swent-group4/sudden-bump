package com.swent.suddenbump.model.user

import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.firebase.Timestamp
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.chat.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserViewModelTest {
  @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var chatRepository: ChatRepository

  private val exception = Exception()
  private val location =
      MutableStateFlow(
          Location("mock_provider").apply {
            latitude = 0.0
            longitude = 0.0
          })
  private val user =
      User("1", "Martin", "Vetterli", "+41 00 000 00 01", null, "martin.vetterli@epfl.ch", location)

  private val testDispatcher = StandardTestDispatcher()

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setUp() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
    userRepository = mock(UserRepository::class.java)
    chatRepository = mock(ChatRepository::class.java)
    userViewModel = UserViewModel(userRepository, chatRepository)
    Dispatchers.setMain(testDispatcher)

    val config = Configuration.Builder().setMinimumLoggingLevel(android.util.Log.DEBUG).build()

    WorkManagerTestInitHelper.initializeTestWorkManager(
        ApplicationProvider.getApplicationContext(), config)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @After
  fun tearDown() {
    Dispatchers.resetMain()
    WorkManager.getInstance(ApplicationProvider.getApplicationContext()).cancelAllWork()
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
            location)

    doAnswer { invocationOnMock ->
          val onSuccess = invocationOnMock.getArgument<(User) -> Unit>(0)
          val onFailure = invocationOnMock.getArgument<(Exception) -> Unit>(1)
          onSuccess(user2)
          onFailure(exception)
        }
        .whenever(userRepository)
        .getUserAccount(any(), any())

    doAnswer { invocationOnMock ->
          val user = invocationOnMock.getArgument<User>(0)
          val onSuccess = invocationOnMock.getArgument<(List<User>) -> Unit>(1)
          val onFailure = invocationOnMock.getArgument<(Exception) -> Unit>(2)
          user
          onSuccess(listOf(user2))
          onFailure(exception)
        }
        .whenever(userRepository)
        .getUserFriends(any(), any(), any())

    doAnswer { invocationOnMock ->
          val user = invocationOnMock.getArgument<User>(0)
          val onSuccess = invocationOnMock.getArgument<(List<User>) -> Unit>(1)
          val onFailure = invocationOnMock.getArgument<(Exception) -> Unit>(2)
          user
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
            location)

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
          val user = invocationOnMock.getArgument<User>(0)
          val onSuccess = invocationOnMock.getArgument<(List<User>) -> Unit>(1)
          val onFailure = invocationOnMock.getArgument<(Exception) -> Unit>(2)
          user
          onSuccess(listOf(user2))
          onFailure(exception)
        }
        .whenever(userRepository)
        .getUserFriends(any(), any(), any())

    doAnswer { invocationOnMock ->
          val user = invocationOnMock.getArgument<User>(0)
          val onSuccess = invocationOnMock.getArgument<(List<User>) -> Unit>(1)
          val onFailure = invocationOnMock.getArgument<(Exception) -> Unit>(2)
          user
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
            location)

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
            location)

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
            location)
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
    verify(userRepository).createFriend(eq(user), eq(friend), any(), any())

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
    verify(userRepository).deleteFriendRequest(eq(user), eq(friend), any(), any())

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
            location)
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
    verify(userRepository).createFriendRequest(eq(user), eq(friend), any(), any())

    // Verify the state updates
    assert(userViewModel.getSentFriendRequests().value.contains(friend))
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
            location)

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
    verify(userRepository).updateLocation(any(), any(), any(), any())
    assertThat(userViewModel.getLocation().value.latitude, `is`(1.0))
    assertThat(userViewModel.getLocation().value.longitude, `is`(1.0))
  }

  @Test
  fun getNewUid() {
    `when`(userRepository.getNewUid()).thenReturn("uid")
    assertThat(userViewModel.getNewUid(), `is`("uid"))
  }

  @Test
  fun loadFriendsLocations_success() {
    // Arrange
    val friend =
        User("2", "Jane", "Doe", "+41 00 000 00 02", null, "jane.doe@example.com", location)
    val friendLocation =
        Location("mock_provider").apply {
          latitude = 1.0
          longitude = 1.0
        }
    val friendsMap = mapOf(friend to friendLocation)

    // Mock the repository method to call the onSuccess callback with the friendsMap
    whenever(userRepository.getFriendsLocation(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Map<User, Location?>) -> Unit>(1)
      onSuccess(friendsMap)
    }

    // Act
    runBlocking { userViewModel.loadFriendsLocations() }

    // Assert
    assertThat(userViewModel.friendsLocations.value, `is`(friendsMap))
    verify(userRepository).getFriendsLocation(any(), any(), any())
  }

  @Test
  fun loadFriendsLocations_failure() {
    // Arrange
    val errorMessage = "Failed to fetch friends' locations"
    val exception = Exception(errorMessage)
    val userRepository: UserRepository = mock() // Mock the UserRepository
    userViewModel = UserViewModel(userRepository, chatRepository) // Instantiate the ViewModel

    // Mock repository method to simulate a failure
    whenever(userRepository.getFriendsLocation(any(), any(), any())).thenAnswer {
      val onFailure = it.getArgument<(Exception) -> Unit>(2)
      onFailure(exception)
    }

    // Act
    userViewModel.loadFriendsLocations()
    testDispatcher.scheduler.advanceUntilIdle() // Ensure coroutines complete

    // Assert
    verify(userRepository).getFriendsLocation(any(), any(), any())
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
        User("2", "Jane", "Doe", "+41 00 000 00 02", null, "jane.doe@example.com", friendLocation)
    val friendsMap = mapOf(friend to friendLocation.value)

    // Mock repository method for loading friend locations
    whenever(userRepository.getFriendsLocation(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Map<User, Location?>) -> Unit>(1)
      onSuccess(friendsMap)
    }

    // Update the user location
    userViewModel.updateLocation(friend, friendLocation.value, onSuccess = {}, onFailure = {})

    // Act
    val distance = userViewModel.getRelativeDistance(friend)

    // Assert
    assertThat(distance, `is`(user.lastKnownLocation.value.distanceTo(friendLocation.value)))
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
        User("2", "Jane", "Doe", "+41 00 000 00 02", null, "jane.doe@example.com", friendLocation)

    userViewModel.updateLocation(friend, friendLocation.value, onSuccess = {}, onFailure = {})

    // Act
    val distance = userViewModel.getRelativeDistance(friend)

    // Assert
    assertThat(distance, `is`(Float.MAX_VALUE))
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun test_getOrCreateChat_success() = runTest {
    // Arrange
    val userId = "user123"
    val chatId = "chat456"
    val messages =
        listOf(
            Message("msg1", "user123", "Hello", Timestamp.now(), listOf("user123")),
            Message("msg2", "user456", "Hi", Timestamp.now(), listOf("user456")))

    // Set the user in the viewModel
    userViewModel.user =
        User(
            uid = userId,
            firstName = "Test",
            lastName = "User",
            phoneNumber = "",
            profilePicture = null,
            emailAddress = "",
            location)

    // Mock chatRepository.getOrCreateChat to return chatId
    whenever(chatRepository.getOrCreateChat(userId)).thenReturn(chatId)

    // Mock chatRepository.getMessages to return Flow<List<Message>> of messages
    val messagesFlow = MutableSharedFlow<List<Message>>()
    whenever(chatRepository.getMessages(chatId)).thenReturn(messagesFlow)

    // Act
    userViewModel.getOrCreateChat()
    advanceUntilIdle()

    // Send messages through the flow
    messagesFlow.emit(messages)
    advanceUntilIdle()

    // Collect the messages from userViewModel.messages
    val collectedMessages = mutableListOf<List<Message>>()
    val job = launch { userViewModel.messages.collect { collectedMessages.add(it) } }

    // Allow time for messages to be collected
    advanceUntilIdle()

    // Assert
    assertThat(collectedMessages.last(), `is`(messages))
    verify(chatRepository).getOrCreateChat(userId)
    verify(chatRepository).getMessages(chatId)

    // Clean up
    job.cancel()
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun test_sendMessage_success() = runTest {
    // Arrange
    val userId = "user123"
    val chatId = "chat456"
    val messageContent = "Hello"
    val username = "Test User"

    // Set the user in the viewModel
    userViewModel.user =
        User(
            uid = userId,
            firstName = "Test",
            lastName = "User",
            phoneNumber = "",
            profilePicture = null,
            emailAddress = "",
            location)

    // Mock chatRepository.getOrCreateChat to return chatId
    whenever(chatRepository.getOrCreateChat(userId)).thenReturn(chatId)

    // Mock chatRepository.getMessages to return Flow<List<Message>>
    whenever(chatRepository.getMessages(chatId)).thenReturn(MutableSharedFlow())

    // Mock chatRepository.sendMessage to do nothing
    whenever(chatRepository.sendMessage(chatId, messageContent, username)).thenReturn(Unit)

    // Act
    userViewModel.getOrCreateChat()
    advanceUntilIdle()

    userViewModel.sendMessage(messageContent, username)
    advanceUntilIdle()

    // Assert
    verify(chatRepository).sendMessage(chatId, messageContent, username)
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
}
