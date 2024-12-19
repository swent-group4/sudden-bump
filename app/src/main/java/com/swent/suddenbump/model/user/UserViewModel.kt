package com.swent.suddenbump.model.user

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.swent.suddenbump.BuildConfig
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.chat.ChatRepositoryFirestore
import com.swent.suddenbump.model.chat.ChatSummary
import com.swent.suddenbump.model.chat.Message
import com.swent.suddenbump.network.RetrofitInstance
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import com.swent.suddenbump.ui.utils.isRunningTest
import com.swent.suddenbump.worker.WorkerScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Enumeration for distance categories used to group friends. */
enum class DistanceCategory(val maxDistance: Float, val title: String) {
  WITHIN_5KM(5000f, "Within 5km"),
  WITHIN_10KM(10000f, "Within 10km"),
  WITHIN_20KM(20000f, "Within 20km"),
  FURTHER(Float.MAX_VALUE, "Further")
}

/** ViewModel class to manage user-related data and operations. */
open class UserViewModel(
    private val repository: UserRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

  private val logTag = "UserViewModel"
  private val _chatSummaries = MutableStateFlow<List<ChatSummary>>(emptyList())
  val chatSummaries: StateFlow<List<ChatSummary>> = _chatSummaries.asStateFlow()

  private val locationDummy =
      MutableStateFlow(
          Location("dummy").apply {
            latitude = 0.0 // Latitude fictive
            longitude = 0.0 // Longitude fictive
          })
  val testUser =
      User(
          "h33lbxyJpcj4OZQAA4QM",
          "Martin",
          "Vetterli",
          "+1234567890",
          null,
          "martin.vetterli@epfl.ch",
          locationDummy)

  val userDummy1 =
      User(
          "1",
          "Martin",
          "Vetterli",
          "+41 00 000 00 01",
          null,
          "martin.vetterli@epfl.ch",
          locationDummy)

  val userDummy2 =
      User(
          "2",
          "Martin",
          "Vetterli",
          "+41 00 000 00 01",
          null,
          "martin.vetterli@epfl.ch",
          locationDummy)

  private val _user: MutableStateFlow<User> = MutableStateFlow(userDummy2)

  private val _userFriendRequests: MutableStateFlow<List<User>> =
      MutableStateFlow(listOf(userDummy1))
  private val _sentFriendRequests: MutableStateFlow<List<User>> =
      MutableStateFlow(listOf(userDummy1))
  private val _userFriends: MutableStateFlow<List<User>> =
      if (!isRunningTest()) MutableStateFlow(emptyList()) else MutableStateFlow(listOf(testUser))
  private val _recommendedFriends: MutableStateFlow<List<UserWithFriendsInCommon>> =
      if (!isRunningTest()) MutableStateFlow(emptyList())
      else MutableStateFlow(listOf(UserWithFriendsInCommon(userDummy1, 1)))
  private val _blockedFriends: MutableStateFlow<List<User>> = MutableStateFlow(listOf(userDummy1))
  private val _userProfilePictureChanging: MutableStateFlow<Boolean> = MutableStateFlow(false)
  private val _selectedContact: MutableStateFlow<User> = MutableStateFlow(userDummy1)
  private val _friendIsOnline: MutableStateFlow<Boolean> = MutableStateFlow(false)
  val friendIsOnline = _friendIsOnline.asStateFlow()

  private val _statusMessage = MutableLiveData<String>()
  val statusMessage: LiveData<String>
    get() = _statusMessage

  private val _locationSharedWith: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())
  val locationSharedWith: StateFlow<List<User>> = _locationSharedWith.asStateFlow()

  // LiveData for verification status
  private val _verificationStatus = MutableLiveData<String>()
  val verificationStatus: LiveData<String> = _verificationStatus

  // LiveData for phone number
  private val _phoneNumber = MutableLiveData<String>()
  val phoneNumber: LiveData<String> = _phoneNumber

  // LiveData for verification ID
  private val _verificationId = MutableLiveData<String>()
  val verificationId: LiveData<String> = _verificationId

  val groupedFriends: StateFlow<Map<DistanceCategory, List<Pair<User, Float>>>?> =
      combine(_user, _userFriends) { user, friends ->
            // Only compute grouped friends if friends are loaded
            if (friends.isNotEmpty()) {
              val friendsWithDistances =
                  friends.mapNotNull { friend ->
                    val distance = getRelativeDistance(friend)
                    if (distance != Float.MAX_VALUE) {
                      friend to distance
                    } else {
                      null
                    }
                  }

              friendsWithDistances.groupBy { (_, distance) ->
                when {
                  distance <= DistanceCategory.WITHIN_5KM.maxDistance -> DistanceCategory.WITHIN_5KM
                  distance <= DistanceCategory.WITHIN_10KM.maxDistance ->
                      DistanceCategory.WITHIN_10KM
                  distance <= DistanceCategory.WITHIN_20KM.maxDistance ->
                      DistanceCategory.WITHIN_20KM
                  else -> DistanceCategory.FURTHER
                }
              }
            } else {
              null // Indicate that friends are not yet loaded
            }
          }
          .stateIn(viewModelScope, SharingStarted.Eagerly, null)

  /** Initializes the ViewModel by configuring the repository. */
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
      val appContext = context.applicationContext

      return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          val preferencesManager = SharedPreferencesManager(appContext)
          val workerScheduler = WorkerScheduler(appContext)
          val userRepository =
              UserRepositoryFirestore(Firebase.firestore, preferencesManager, workerScheduler)
          val chatRepository = ChatRepositoryFirestore(Firebase.firestore)

          return UserViewModel(userRepository, chatRepository) as T
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
          scheduleWorker(_user.value.uid)
          repository.getUserFriends(
              uid = _user.value.uid,
              onSuccess = { friendsList ->
                Log.d(logTag, friendsList.toString())
                _userFriends.value = friendsList
                repository.getBlockedFriends(
                    uid = _user.value.uid,
                    onSuccess = { blockedFriendsList ->
                      _blockedFriends.value = blockedFriendsList
                    },
                    onFailure = { e -> Log.e(logTag, e.toString()) })
              },
              onFailure = { e -> Log.e(logTag, e.toString()) })
          repository.getSentFriendRequests(
              uid = _user.value.uid,
              onSuccess = { sentRequestsList -> _sentFriendRequests.value = sentRequestsList },
              onFailure = { e -> Log.e(logTag, e.toString()) })
          repository.getUserFriendRequests(
              uid = _user.value.uid,
              onSuccess = { friendRequestsList -> _userFriendRequests.value = friendRequestsList },
              onFailure = { e -> Log.e(logTag, e.toString()) })
          repository.getRecommendedFriends(
              uid = _user.value.uid,
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
    scheduleWorker(uid)
    repository.getUserAccount(
        uid,
        onSuccess = {
          _user.value = it
          repository.getUserFriends(
              uid = _user.value.uid,
              onSuccess = { friendsList ->
                Log.i(logTag, friendsList.toString())
                _userFriends.value = friendsList
                repository.getBlockedFriends(
                    uid = _user.value.uid,
                    onSuccess = { blockedFriendsList ->
                      _blockedFriends.value = blockedFriendsList
                      onSuccess()
                    },
                    onFailure = { e -> Log.e(logTag, e.toString()) })
              },
              onFailure = { e -> Log.e(logTag, e.toString()) })
          repository.getSentFriendRequests(
              uid = _user.value.uid,
              onSuccess = { sentRequestsList -> _sentFriendRequests.value = sentRequestsList },
              onFailure = { e -> Log.e(logTag, e.toString()) })
          repository.getUserFriendRequests(
              uid = _user.value.uid,
              onSuccess = { friendRequestsList -> _userFriendRequests.value = friendRequestsList },
              onFailure = { e -> Log.e(logTag, e.toString()) })
          repository.getRecommendedFriends(
              uid = _user.value.uid,
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

  fun verifyUnusedPhoneNumber(
      phoneNumber: String,
      onSuccess: (Boolean) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.verifyUnusedPhoneNumber(phoneNumber, onSuccess, onFailure)
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
    repository.createFriend(user.uid, friend.uid, onSuccess, onFailure)
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
    repository.deleteFriendRequest(user.uid, friend.uid, onSuccess, onFailure)
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
    repository.createFriendRequest(user.uid, friend.uid, onSuccess, onFailure)
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
    repository.setUserFriends(user.uid, friendsList, onSuccess, onFailure)
  }

  fun getUserRecommendedFriends(): StateFlow<List<UserWithFriendsInCommon>> {
    return _recommendedFriends.asStateFlow()
  }

  fun getSentFriendRequests(): StateFlow<List<User>> {
    return _sentFriendRequests.asStateFlow()
  }

  fun deleteFriend(
      user: User = _user.value,
      friend: User,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.deleteFriend(
        currentUserId = user.uid,
        friendUserId = friend.uid,
        onSuccess = {
          try {
            stopSharingLocationWithFriend(user.uid, friend, onSuccess, onFailure)
          } catch (e: Exception) {
            Log.e(logTag, "Your location is not shared with this friend")
          }
          try {
            stopSharingLocationWithFriend(friend.uid, user, onSuccess, onFailure)
          } catch (e: Exception) {
            Log.e(logTag, "This friend is not sharing location to you")
          }
          // Update local state
          _userFriends.value = _userFriends.value.filter { it.uid != friend.uid }
          onSuccess()
        },
        onFailure = { e -> onFailure(e) })
  }

  fun blockUser(
      user: User = _user.value,
      blockedUser: User,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    _blockedFriends.value = _blockedFriends.value.plus(blockedUser)
    _userFriends.value = _userFriends.value.minus(blockedUser)
    _userFriendRequests.value = _userFriendRequests.value.minus(blockedUser)
    _sentFriendRequests.value = _sentFriendRequests.value.minus(blockedUser)
    return repository.blockUser(user, blockedUser, onSuccess, onFailure)
  }

  fun getBlockedFriends(): StateFlow<List<User>> {
    return _blockedFriends.asStateFlow()
  }

  fun setBlockedFriends(
      user: User = _user.value,
      blockedFriendsList: List<User>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    _blockedFriends.value = blockedFriendsList
    repository.setBlockedFriends(user.uid, blockedFriendsList, onSuccess, onFailure)
  }

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
    repository.updateUserLocation(user.uid, location, onSuccess, onFailure)
  }

  /**
   * Retrieves the online status of a user from the repository.
   *
   * @param uid The unique identifier of the user.
   * @param onSuccess Called with the online status if retrieval is successful.
   * @param onFailure Called with an exception if retrieval fails.
   */
  fun getUserStatus(uid: String, onSuccess: (Boolean) -> Unit, onFailure: (Exception) -> Unit) {
    repository.getUserStatus(uid, onSuccess, onFailure)
  }

  /**
   * Updates the user's online status in the repository.
   *
   * @param uid The unique identifier of the user.
   * @param status The new online status of the user.
   * @param onSuccess Called when the update is successful.
   * @param onFailure Called with an exception if the update fails.
   */
  fun updateUserStatus(
      uid: String,
      status: Boolean,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    _friendIsOnline.value = status
    repository.updateUserStatus(uid, status, onSuccess, onFailure)
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
    repository.updateTimestamp(user.uid, timestamp, onSuccess, onFailure)
  }

  fun loadFriends() {
    if (_user.value.uid != userDummy2.uid) {
      repository.getUserFriends(
          uid = _user.value.uid,
          onSuccess = { friends ->
            // Update the state with the locations of friends
            _userFriends.value = friends
            Log.d("UserViewModel", "On success load Friends ${_userFriends.value}")
          },
          onFailure = { error ->
            // Handle the error, e.g., log or show error message
            Log.e("UserViewModel", "Failed to load friends' : ${error.message}")
          })
      Log.d("UserViewModel", "Loading sharedLocationWith...")
      repository.getSharedWithFriends(
          uid = _user.value.uid,
          onSuccess = { list ->
            _locationSharedWith.value = list
            Log.d("UserViewModel", "Successfully loaded sharedLocationWith: $list")
          },
          onFailure = { error ->
            Log.e("UserViewModel", "Failed to load sharedLocationWith : ${error.message}")
          })
    }
  }

  fun getSelectedContact(): StateFlow<User> {
    return _selectedContact.asStateFlow()
  }

  fun setSelectedContact(user: User) {
    _selectedContact.value = user
  }

  fun getRelativeDistance(friend: User): Float {
    val userLocation = _user.value.lastKnownLocation.value
    val friendLocation = friend.lastKnownLocation.value
    if (userLocation == locationDummy.value || friendLocation == locationDummy.value) {
      return Float.MAX_VALUE
    }
    return userLocation.distanceTo(friendLocation)
  }

  /** Cache to store fetched locations */
  private val locationCache = mutableMapOf<String, String>()

  /** Fetches the city and country for a given location */
  suspend fun getCityAndCountry(location: StateFlow<Location>): String {
    val latLng = "${location.value.latitude},${location.value.longitude}"

    // Check cache first
    locationCache[latLng]?.let {
      return it
    }

    return withContext(Dispatchers.IO) {
      try {
        val response =
            RetrofitInstance.geocodingApi.reverseGeocode(
                latlng = latLng,
                apiKey =
                    BuildConfig.MAPS_API_KEY // Replace with method to securely retrieve API key
                )

        if (response.status == "OK" && response.results.isNotEmpty()) {
          val addressComponents = response.results[0].address_components

          val city =
              addressComponents
                  .firstOrNull { component -> component.types.contains("locality") }
                  ?.long_name

          val country =
              addressComponents
                  .firstOrNull { component -> component.types.contains("country") }
                  ?.long_name

          val cityCountry = listOfNotNull(city, country).joinToString(", ")

          // Save to cache
          locationCache[latLng] = cityCountry

          cityCountry
        } else {
          Log.e("UserViewModel", "Geocoding API error: ${response.status}")
          "Unknown Location"
        }
      } catch (e: Exception) {
        Log.e("UserViewModel", "Error fetching location: ${e.message}")
        "Unknown Location"
      }
    }
  }

  fun isFriendsInRadius(radius: Int): Boolean {
    loadFriends()
    _userFriends.value.forEach { friend ->
      if (_user.value.lastKnownLocation.value.distanceTo(friend.lastKnownLocation.value) <=
          radius) {
        return true
      }
    }
    return false
  }

  /**
   * Retrieves a new unique identifier (UID).
   *
   * @return A new UID as a String.
   */
  fun getNewUid(): String {
    return repository.getNewUid()
  }

  /**
   * Saves the user's login status.
   *
   * @param userId The unique identifier of the user.
   */
  fun saveUserLoginStatus(userId: String) {
    repository.saveLoginStatus(userId)
  }

  /**
   * Retrieves the saved UID of the user.
   *
   * @return The saved UID as a String.
   */
  fun getSavedUid(): String {
    return repository.getSavedUid()
  }

  /**
   * Checks if the user is logged in.
   *
   * @return True if the user is logged in, false otherwise.
   */
  fun saveRadius(radius: Float) {
    repository.saveRadius(radius)
  }

  fun getSavedRadius(): Float {
    return repository.getSavedRadius()
  }

  fun saveNotificationStatus(status: Boolean) {
    repository.saveNotificationStatus(status)
  }

  fun getSavedNotificationStatus(): Boolean {
    return repository.getSavedNotificationStatus()
  }

  fun isUserLoggedIn(): Boolean {
    return repository.isUserLoggedIn()
  }

  /** Logs out the current user and resets the ViewModel state. */
  fun logout() {
    repository.logoutUser()
    _user.value = userDummy2
    _messages.value = emptyList()
    _userFriends.value = emptyList()
    _userFriendRequests.value = emptyList()
    _sentFriendRequests.value = emptyList()
    _recommendedFriends.value = emptyList()
    _blockedFriends.value = emptyList()
    _locationSharedWith.value = emptyList()
    _phoneNumber.value = ""
    _chatSummaries.value = emptyList()
    _verificationId.value = ""
    _verificationStatus.value = ""
    _userProfilePictureChanging.value = false
    _selectedContact.value = userDummy1
  }

  fun deleteUserAccount(navigationActions: NavigationActions) {
    val currentUser = _user.value
    val uidToDelete = currentUser.uid

    // Capture these lists before logout since logout clears them
    val friends = _userFriends.value.toList()
    val receivedRequests = _userFriendRequests.value.toList()
    val sentRequests = _sentFriendRequests.value.toList()

    logout()
    navigationActions.navigateTo(Route.AUTH)

    try {
      // Remove from friends
      for (friend in friends) {
        deleteFriend(
            user = currentUser,
            friend = friend,
            onSuccess = {},
            onFailure = { e -> Log.e(logTag, "Failed to delete friend: ${e.message}") })
      }

      // Decline received requests
      for (requester in receivedRequests) {
        declineFriendRequest(
            user = currentUser,
            friend = requester,
            onSuccess = { {} },
            onFailure = { e -> Log.e(logTag, "Failed to decline friend request: ${e.message}") })
      }

      // Unsend sent requests
      for (requestedFriend in sentRequests) {
        unsendFriendRequest(
            user = currentUser,
            friend = requestedFriend,
            onSuccess = {},
            onFailure = { e -> Log.e(logTag, "Failed to unsend friend request: ${e.message}") })
      }

      // 3. After all references are removed, delete the user account
      repository.deleteUserAccount(
          uid = uidToDelete,
          onSuccess = {
            Log.i(logTag, "User account successfully deleted!")
            // Optionally show a UI message or navigate
          },
          onFailure = { exception ->
            Log.e(logTag, "Failed to delete user account: ${exception.message}")
          })
    } catch (e: Exception) {
      Log.e(logTag, "Error during user account deletion cleanup: ${e.message}")
      // Even if cleanup failed, we attempt to delete the user account
      repository.deleteUserAccount(
          uid = uidToDelete,
          onSuccess = { Log.i(logTag, "User account deleted with partial cleanup!") },
          onFailure = { exception ->
            Log.e(logTag, "Failed to delete user account after error: ${exception.message}")
          })
    }
  }

  private fun finalizeAccountDeletion(uid: String) {
    // First logout the user and clear local state
    logout()

    // Now call the repository method to delete the account from Firestore and related data
    repository.deleteUserAccount(
        uid = uid,
        onSuccess = {
          // Show a success message
          _statusMessage.postValue("User account successfully deleted!")
        },
        onFailure = { exception ->
          // Handle the failure
          _statusMessage.postValue("Failed to delete user account: ${exception.message}")
        })
  }

  private val _messages = MutableStateFlow<List<Message>>(emptyList())
  val messages: StateFlow<List<Message>> = _messages

  private var chatId: String? = null
  private var chatFriendId: String? = null

  private var isGettingChatId = false

  var user: User? = null
  private val userId: String?
    get() = user?.uid

  fun getOrCreateChat(friendId: String) =
      viewModelScope.launch {
        if (!isGettingChatId) {
          isGettingChatId = true
          chatFriendId = friendId
          chatId = chatRepository.getOrCreateChat(friendId, _user.value.uid)
          isGettingChatId = false
          chatRepository.getMessages(chatId!!).collect { messages -> _messages.value = messages }
        }
      }

  fun getChatSummaries() =
      viewModelScope.launch {
        chatRepository.getChatSummaries(_user.value.uid).collect { list ->
          _chatSummaries.value = list
        }
      }

  fun deleteAllMessages() {
    viewModelScope.launch {
      chatId?.let {
        chatRepository.deleteAllMessages(_user.value.uid)
        _messages.value = emptyList()
      }
    }
  }

  // Send a new message and add it to Firestore
  fun sendMessage(messageContent: String, user: User) {
    viewModelScope.launch {
      Log.d("Chat", "INSIDE FUN")
      if (chatId != null)
          chatRepository.sendMessage(
              chatId!!,
              messageContent,
              _user.value,
              _userFriends.value.first { it.uid == chatFriendId })
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
          _verificationId.postValue(verificationId) // Stocke l'ID de vérification
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
   * Shares the user's location with a friend.
   *
   * @param uid The user ID of the person sharing their location.
   * @param friend The User object representing the friend with whom the location is being shared.
   * @param onSuccess Called when the location is successfully shared.
   * @param onFailure Called with an exception if the sharing fails.
   */
  fun shareLocationWithFriend(
      uid: String,
      friend: User,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    _locationSharedWith.value = _locationSharedWith.value.plus(friend)
    repository.shareLocationWithFriend(uid, friend.uid, onSuccess, onFailure)
  }

  /**
   * Stops sharing the user's location with a friend.
   *
   * @param uid The user ID of the person stopping the sharing.
   * @param friend The User object representing the friend with whom the location sharing is being
   *   stopped.
   * @param onSuccess Called when the location sharing is successfully stopped.
   * @param onFailure Called with an exception if the stopping fails.
   */
  fun stopSharingLocationWithFriend(
      uid: String,
      friend: User,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    _locationSharedWith.value.forEach {
      if (it.uid == friend.uid) {
        _locationSharedWith.value = _locationSharedWith.value.minus(it)
      }
    }
    repository.stopSharingLocationWithFriend(uid, friend.uid, onSuccess, onFailure)
  }

  /**
   * Retrieves the list of friends who have shared their location with the user.
   *
   * @param uid The user ID of the person retrieving the list.
   * @param onSuccess Called with a list of User objects if retrieval succeeds.
   * @param onFailure Called with an exception if retrieval fails.
   */
  fun getLocationSharedBy(
      uid: String,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.getSharedByFriends(uid, onSuccess, onFailure)
  }

  /**
   * Un-send the friend request sent to the user's friend.
   *
   * @param user The user of the person unsending the request
   * @param friend The friend to which the user is undoing the request
   * @param onSuccess Called with a list of User objects if retrieval succeeds.
   * @param onFailure Called with an exception if retrieval fails.
   */
  fun unsendFriendRequest(
      user: User = _user.value,
      friend: User,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.deleteFriendRequest(
        uid = friend.uid, // the one who originally received the request
        fid = user.uid, // the one who originally sent the request
        onSuccess = {
          _sentFriendRequests.value = _sentFriendRequests.value.minus(friend)
          onSuccess()
        },
        onFailure = onFailure)
  }

  fun unblockUser(
      blockedUser: User,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    viewModelScope.launch {
      repository.unblockUser(
          currentUserId = _user.value.uid,
          blockedUserId = blockedUser.uid,
          onSuccess = {
            // Remove user from blocked list in local state
            _blockedFriends.value = _blockedFriends.value.filter { it.uid != blockedUser.uid }
            onSuccess()
          },
          onFailure = onFailure)
    }
  }

  fun scheduleWorker(uid: String) {
    repository.scheduleWorker(uid)
  }
}
