package com.swent.suddenbump.model.user

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.chat.ChatRepositoryFirestore
import com.swent.suddenbump.model.chat.ChatSummary
import com.swent.suddenbump.model.chat.Message
import com.swent.suddenbump.model.image.ImageBitMapIO
import com.swent.suddenbump.worker.WorkerScheduler.scheduleLocationUpdateWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel class for managing user-related data and operations. It acts as a bridge between the UI
 * and the underlying UserRepository and ChatRepository, handling tasks such as user authentication,
 * friend management, chat operations, and location updates.
 *
 * @property repository The UserRepository instance used for managing user data.
 * @property chatRepository The ChatRepository instance used for managing chats and messages.
 */
class UserViewModel(
    private val repository: UserRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

  private val logTag = "UserViewModel"
  private val _chatSummaries = MutableStateFlow<List<ChatSummary>>(emptyList())
  val chatSummaries: Flow<List<ChatSummary>> = _chatSummaries.asStateFlow()
  private val profilePicture = ImageBitMapIO()
  val friendsLocations = mutableStateOf<Map<User, Location?>>(emptyMap())

  val locationDummy =
      MutableStateFlow(
          Location("dummy").apply {
            latitude = 0.0 // Set latitude
            longitude = 0.0 // Set longitude
          })

  private val _user: MutableStateFlow<User> =
      MutableStateFlow(
          User(
              "1",
              "Martin",
              "Vetterli",
              "+41 00 000 00 01",
              null,
              "martin.vetterli@epfl.ch",
              locationDummy))

  private val _userFriendRequests: MutableStateFlow<List<User>> =
      MutableStateFlow(listOf(_user.value))
  private val _sentFriendRequests: MutableStateFlow<List<User>> =
      MutableStateFlow(listOf(_user.value))
  private val _userFriends: MutableStateFlow<List<User>> = MutableStateFlow(listOf(_user.value))
  private val _recommendedFriends: MutableStateFlow<List<User>> =
      MutableStateFlow(listOf(_user.value))
  private val _blockedFriends: MutableStateFlow<List<User>> = MutableStateFlow(listOf(_user.value))
  private val _userProfilePictureChanging: MutableStateFlow<Boolean> = MutableStateFlow(false)
  private val _selectedContact: MutableStateFlow<User> = MutableStateFlow(_user.value)

  // LiveData for verification status
  private val _verificationStatus = MutableLiveData<String>()
  val verificationStatus: LiveData<String> = _verificationStatus

  // LiveData for phone number
  private val _phoneNumber = MutableLiveData<String>()
  val phoneNumber: LiveData<String> = _phoneNumber

  // LiveData for verification ID
  private val _verificationId = MutableLiveData<String>()
  val verificationId: LiveData<String> = _verificationId

  /** Initializes the ViewModel by setting up the repository. */
  init {
    repository.init { Log.i(logTag, "Repository successfully initialized!") }
  }

  /**
   * Factory method for creating a UserViewModel instance with the required dependencies.
   *
   * @param context The application context used to initialize the repositories.
   * @return A ViewModelProvider.Factory for creating UserViewModel instances.
   */
  companion object {
    fun provideFactory(context: Context): ViewModelProvider.Factory {
      return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          val userRepository = UserRepositoryFirestore(Firebase.firestore, context)
          return UserViewModel(userRepository, ChatRepositoryFirestore(Firebase.firestore)) as T
        }
      }
    }
  }

  /**
   * Sets the current authenticated user in the ViewModel and fetches related data such as friends,
   * friend requests, and recommendations.
   */
  fun setCurrentUser() {
    repository.getUserAccount(
        onSuccess = { user ->
          _user.value = user
          saveUserLoginStatus(_user.value.uid)
          scheduleLocationUpdateWorker(getApplicationContext(), _user.value.uid)
          Log.d(logTag, "User set 1: ${_user.value}")
          repository.getUserFriends(
              user = _user.value,
              onSuccess = { friendsList ->
                Log.d(logTag, friendsList.toString())
                _userFriends.value = friendsList
                repository.getBlockedFriends(
                    user = _user.value,
                    onSuccess = { blockedFriendsList ->
                      _blockedFriends.value = blockedFriendsList
                      // Start updating locations after friends are loaded
                      startUpdatingFriendsLocations()
                    },
                    onFailure = { e -> Log.e(logTag, e.toString()) })
              },
              onFailure = { e -> Log.e(logTag, e.toString()) })
          repository.getSentFriendRequests(
              user = _user.value,
              onSuccess = { sentRequestsList -> _sentFriendRequests.value = sentRequestsList },
              onFailure = { e -> Log.e(logTag, e.toString()) })
          repository.getUserFriendRequests(
              user = _user.value,
              onSuccess = { friendRequestsList -> _userFriendRequests.value = friendRequestsList },
              onFailure = { e -> Log.e(logTag, e.toString()) })
          repository.getRecommendedFriends(
              user = _user.value,
              onSuccess = { recommendedFriendsList ->
                _recommendedFriends.value = recommendedFriendsList
              },
              onFailure = { e -> Log.e(logTag, e.toString()) })
        },
        onFailure = { e -> Log.e(logTag, e.toString()) })
  }

  /**
   * Sets the current user by UID and fetches related data.
   *
   * @param uid The unique identifier of the user to set.
   * @param onSuccess Called when the operation succeeds.
   * @param onFailure Called with an exception if the operation fails.
   */
  fun setCurrentUser(uid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    repository.getUserAccount(
        uid,
        onSuccess = {
          _user.value = it
          repository.getUserFriends(
              user = _user.value,
              onSuccess = { friendsList ->
                Log.i(logTag, friendsList.toString())
                _userFriends.value = friendsList
                repository.getBlockedFriends(
                    user = _user.value,
                    onSuccess = { blockedFriendsList ->
                      _blockedFriends.value = blockedFriendsList
                      onSuccess()
                    },
                    onFailure = { e -> Log.e(logTag, e.toString()) })
              },
              onFailure = { e -> Log.e(logTag, e.toString()) })
          repository.getSentFriendRequests(
              user = _user.value,
              onSuccess = { sentRequestsList -> _sentFriendRequests.value = sentRequestsList },
              onFailure = { e -> Log.e(logTag, e.toString()) })
          repository.getUserFriendRequests(
              user = _user.value,
              onSuccess = { friendRequestsList -> _userFriendRequests.value = friendRequestsList },
              onFailure = { e -> Log.e(logTag, e.toString()) })
          repository.getRecommendedFriends(
              user = _user.value,
              onSuccess = { recommendedFriendsList ->
                _recommendedFriends.value = recommendedFriendsList
              },
              onFailure = { e -> Log.e(logTag, e.toString()) })
        },
        onFailure)
  }

  /**
   * Updates the current user with the given User object.
   *
   * @param user The updated User object.
   * @param onSuccess Called when the update succeeds.
   * @param onFailure Called with an exception if the update fails.
   */
  fun setUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    _user.value = user
    repository.updateUserAccount(user, onSuccess, onFailure)
  }

  /**
   * Verifies if no account exists for the given email address.
   *
   * @param emailAddress The email address to verify.
   * @param onSuccess Called with `true` if no account exists, `false` otherwise.
   * @param onFailure Called with an exception if the operation fails.
   */
  fun verifyNoAccountExists(
      emailAddress: String,
      onSuccess: (Boolean) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.verifyNoAccountExists(emailAddress, onSuccess, onFailure)
  }

  /**
   * Creates a new user account.
   *
   * @param user The User object containing the account details.
   * @param onSuccess Called when the account is successfully created.
   * @param onFailure Called with an exception if the operation fails.
   */
  fun createUserAccount(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    _user.value = user
    repository.createUserAccount(user, onSuccess, onFailure)
  }

  /**
   * Accepts a friend request and updates the friend lists.
   *
   * @param user The current user accepting the request.
   * @param friend The user who sent the friend request.
   * @param onSuccess Called when the operation succeeds.
   * @param onFailure Called with an exception if the operation fails.
   */
  fun acceptFriendRequest(
      user: User = _user.value,
      friend: User,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.createFriend(user, friend, onSuccess, onFailure)
    _userFriendRequests.value = _userFriendRequests.value.minus(friend)
    _userFriends.value = _userFriends.value.plus(friend)
  }

  /**
   * Declines a friend request and removes it from the pending requests.
   *
   * @param user The current user declining the request.
   * @param friend The user whose request is being declined.
   * @param onSuccess Called when the operation succeeds.
   * @param onFailure Called with an exception if the operation fails.
   */
  fun declineFriendRequest(
      user: User = _user.value,
      friend: User,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.deleteFriendRequest(user, friend, onSuccess, onFailure)
    _userFriendRequests.value = _userFriendRequests.value.minus(friend)
  }

  /**
   * Sends a friend request to another user.
   *
   * @param user The current user sending the request.
   * @param friend The user receiving the friend request.
   * @param onSuccess Called when the operation succeeds.
   * @param onFailure Called with an exception if the operation fails.
   */
  fun sendFriendRequest(
      user: User = _user.value,
      friend: User,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.createFriendRequest(user, friend, onSuccess, onFailure)
    _sentFriendRequests.value = _sentFriendRequests.value.plus(friend)
  }

  /**
   * Retrieves the current authenticated user's details as a StateFlow.
   *
   * @return A StateFlow containing the current User object.
   */
  fun getCurrentUser(): StateFlow<User> {
    return _user.asStateFlow()
  }

  /**
   * Retrieves the current user's friends as a StateFlow.
   *
   * @return A StateFlow containing a list of the user's friends.
   */
  fun getUserFriends(): StateFlow<List<User>> {
    return _userFriends.asStateFlow()
  }

  /**
   * Retrieves the user's friend requests as a StateFlow.
   *
   * @return A StateFlow representing the user's friend requests.
   */
  fun getUserFriendRequests(): StateFlow<List<User>> {
    return _userFriendRequests.asStateFlow()
  }

  /**
   * Updates the user's friends list in the repository.
   *
   * @param user The user whose friends list is being updated.
   * @param friendsList The updated list of friends.
   * @param onSuccess Called when the update is successful.
   * @param onFailure Called with an exception if the update fails.
   */
  fun setUserFriends(
      user: User = _user.value,
      friendsList: List<User>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    _userFriends.value = friendsList
    repository.setUserFriends(user, friendsList, onSuccess, onFailure)
  }

  /**
   * Retrieves the user's recommended friends as a StateFlow.
   *
   * @return A StateFlow containing a list of recommended friends.
   */
  fun getUserRecommendedFriends(): StateFlow<List<User>> {
    return _recommendedFriends.asStateFlow()
  }

  /**
   * Retrieves the user's sent friend requests as a StateFlow.
   *
   * @return A StateFlow containing a list of sent friend requests.
   */
  fun getSentFriendRequests(): StateFlow<List<User>> {
    return _sentFriendRequests.asStateFlow()
  }

  /**
   * Retrieves the user's blocked friends as a StateFlow.
   *
   * @return A StateFlow containing a list of blocked friends.
   */
  fun getBlockedFriends(): StateFlow<List<User>> {
    return _blockedFriends.asStateFlow()
  }

  /**
   * Updates the user's blocked friends list in the repository.
   *
   * @param user The user whose blocked friends list is being updated.
   * @param blockedFriendsList The updated list of blocked friends.
   * @param onSuccess Called when the update is successful.
   * @param onFailure Called with an exception if the update fails.
   */
  fun setBlockedFriends(
      user: User = _user.value,
      blockedFriendsList: List<User>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    _blockedFriends.value = blockedFriendsList
    repository.setBlockedFriends(user, blockedFriendsList, onSuccess, onFailure)
  }

  /**
   * Retrieves the user's location as a StateFlow.
   *
   * @return A StateFlow containing the user's last known location.
   */
  fun getLocation(): StateFlow<Location> {
    return _user.value.lastKnownLocation.asStateFlow()
  }

  /**
   * Updates the user's location in the repository.
   *
   * @param user The user whose location is being updated.
   * @param location The new location.
   * @param onSuccess Called when the update is successful.
   * @param onFailure Called with an exception if the update fails.
   */
  fun updateLocation(
      user: User = _user.value,
      location: Location,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    _user.value.lastKnownLocation.value = location
    repository.updateLocation(user, location, onSuccess, onFailure)
  }

  /**
   * Updates the user's last activity timestamp in the repository.
   *
   * @param user The user whose timestamp is being updated.
   * @param timestamp The new timestamp.
   * @param onSuccess Called when the update is successful.
   * @param onFailure Called with an exception if the update fails.
   */
  fun updateTimestamp(
      user: User = _user.value,
      timestamp: Timestamp,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.updateTimestamp(user, timestamp, onSuccess, onFailure)
  }

  // Define DistanceRange enum
  enum class DistanceRange(val label: String, val minDistance: Float, val maxDistance: Float) {
    WITHIN_5KM("Within 5km", 0f, 5000f),
    WITHIN_10KM("Within 10km", 5000f, 10000f),
    WITHIN_20KM("Within 20km", 10000f, 20000f),
    FURTHER("Further", 20000f, Float.MAX_VALUE)
  }

  private val _friendsGroupedByDistance =
      MutableStateFlow<Map<DistanceRange, List<User>>>(emptyMap())
  val friendsGroupedByDistance: StateFlow<Map<DistanceRange, List<User>>> =
      _friendsGroupedByDistance.asStateFlow()

  fun startUpdatingFriendsLocations() {
    viewModelScope.launch {
      while (true) {
        loadFriendsLocations()
        updateFriendsGroupedByDistance()
        delay(5000L) // Refresh every 5 seconds
      }
    }
  }

  private fun updateFriendsGroupedByDistance() {
    val userLocation = _user.value.lastKnownLocation.value

    val groupedFriends =
        friendsLocations.value
            .mapNotNull { (friend, location) ->
              val friendLocation = location
              if (friendLocation != null && userLocation != locationDummy.value) {
                val distance = userLocation.distanceTo(friendLocation)
                friend to distance
              } else {
                null
              }
            }
            .groupBy { (_, distance) ->
              DistanceRange.values().first {
                distance >= it.minDistance && distance <= it.maxDistance
              }
            }
            .mapValues { entry -> entry.value.map { it.first } }

    _friendsGroupedByDistance.value = groupedFriends
  }

  fun loadFriendsLocations() {
    try {
      repository.getFriendsLocation(
          _userFriends.value,
          onSuccess = { friendsLoc ->
            // Update the state with the locations of friends
            friendsLocations.value = friendsLoc
            Log.d("FriendsMarkers", "On success load Friends Locations ${friendsLocations.value}")
          },
          onFailure = { error ->
            // Handle the error, e.g., log or show error message
            Log.e("UserViewModel", "Failed to load friends' locations: ${error.message}")
          })
    } catch (e: Exception) {
      Log.e("UserViewModel", e.toString())
    }
  }

  /**
   * Calculates the relative distance between the current user and a friend.
   *
   * @param friend The friend whose distance is being calculated.
   * @return The distance in meters, or Float.MAX_VALUE if locations are unavailable.
   */
  fun getRelativeDistance(friend: User): Float {
    val userLocation = _user.value.lastKnownLocation.value
    val friendLocation = friendsLocations.value[friend]

    if (userLocation == locationDummy.value || friendLocation == null) {
      return Float.MAX_VALUE
    }
    return userLocation.distanceTo(friendLocation)
  }

  /**
   * Checks if any friends are within a specified radius.
   *
   * @param radius The radius in meters.
   * @return True if any friends are within the radius, false otherwise.
   */
  fun isFriendsInRadius(radius: Int): Boolean {
    loadFriendsLocations()
    friendsLocations.value.values.forEach { friendLocation ->
      if (friendLocation != null) {
        Log.d(
            "FriendsRadius",
            "Friends Locations: ${_user.value.lastKnownLocation.value.distanceTo(friendLocation)}")
        if (_user.value.lastKnownLocation.value.distanceTo(friendLocation) <= radius) {
          return true
        }
      }
    }
    return false
  }

  /**
   * Retrieves a new unique identifier.
   *
   * @return A new unique identifier as a String.
   */
  fun getNewUid(): String {
    return repository.getNewUid()
  }

  /**
   * Saves the user's login status.
   *
   * @param userId The user's unique identifier.
   */
  fun saveUserLoginStatus(userId: String) {
    repository.saveLoginStatus(userId)
  }

  /**
   * Retrieves the saved unique identifier.
   *
   * @return The saved unique identifier as a String.
   */
  fun getSavedUid(): String {
    return repository.getSavedUid()
  }

  /**
   * Checks if the user is logged in.
   *
   * @return True if the user is logged in, false otherwise.
   */
  fun isUserLoggedIn(): Boolean {
    return repository.isUserLoggedIn()
  }

  /** Logs out the current user. */
  fun logout() {
    repository.logoutUser()
  }

  private val _messages = MutableStateFlow<List<Message>>(emptyList())
  val messages: Flow<List<Message>> = _messages

  private var chatId: String? = null

  private var isGettingChatId = false

  var user: User? = null
  private val userId: String?
    get() = user?.uid

  /**
   * Retrieves or creates a chat with a friend.
   *
   * @param friendId The friend's unique identifier.
   */
  fun getOrCreateChat(friendId: String) =
      viewModelScope.launch {
        if (!isGettingChatId) {
          isGettingChatId = true
          chatId = chatRepository.getOrCreateChat(friendId, _user.value.uid)
          isGettingChatId = false
          chatRepository.getMessages(chatId!!).collect { messages -> _messages.value = messages }
        }
      }

  /**
   * Sends a new message to a chat.
   *
   * @param messageContent The content of the message.
   * @param user The user sending the message.
   */
  fun sendMessage(messageContent: String, user: User) {
    viewModelScope.launch {
      if (chatId != null) chatRepository.sendMessage(chatId!!, messageContent, user)
    }
  }

  /**
   * Sends a verification code to the specified phone number.
   *
   * @param phoneNumber The phone number to verify.
   */
  fun sendVerificationCode(phoneNumber: String) {
    _phoneNumber.value = phoneNumber
    repository.sendVerificationCode(
        phoneNumber,
        onSuccess = { verificationId ->
          _verificationId.postValue(verificationId) // Store the verification ID
          _verificationStatus.postValue("Code Sent")
        },
        onFailure = { _verificationStatus.postValue("Failed to send code: ${it.message}") })
  }

  /**
   * Verifies the provided code using the verification ID.
   *
   * @param code The verification code to validate.
   */
  fun verifyCode(code: String) {
    val verificationIdValue = _verificationId.value
    if (verificationIdValue != null) {
      repository.verifyCode(
          verificationIdValue,
          code,
          onSuccess = { _verificationStatus.postValue("Phone Verified") },
          onFailure = { _verificationStatus.postValue("Verification failed: ${it.message}") })
    } else {
      _verificationStatus.postValue("Verification ID is missing.")
    }
  }

  /**
   * Retrieves the selected contact as a StateFlow.
   *
   * @return A StateFlow containing the selected User object.
   */
  fun getSelectedContact(): StateFlow<User> {
    return _selectedContact.asStateFlow()
  }

  /**
   * Sets the selected contact.
   *
   * @param user The User object representing the selected contact.
   */
  fun setSelectedContact(user: User) {
    _selectedContact.value = user
  }
}
