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
import com.swent.suddenbump.BuildConfig
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.chat.ChatRepositoryFirestore
import com.swent.suddenbump.model.chat.ChatSummary
import com.swent.suddenbump.model.chat.Message
import com.swent.suddenbump.model.image.ImageBitMapIO
import com.swent.suddenbump.network.RetrofitInstance
import com.swent.suddenbump.ui.utils.isRunningTest
import com.swent.suddenbump.worker.WorkerScheduler.scheduleLocationUpdateWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Enumération pour les catégories de distance utilisées pour regrouper les amis. */
enum class DistanceCategory(val maxDistance: Float, val title: String) {
  WITHIN_5KM(5000f, "Within 5km"),
  WITHIN_10KM(10000f, "Within 10km"),
  WITHIN_20KM(20000f, "Within 20km"),
  FURTHER(Float.MAX_VALUE, "Further")
}

/** Classe ViewModel pour gérer les données et opérations liées à l'utilisateur. */
open class UserViewModel(
    private val repository: UserRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

  private val chatSummaryDummy =
      ChatSummary("1", "message content", "chat456", Timestamp.now(), 0, listOf("1", "2"))

  private val logTag = "UserViewModel"
  private val _chatSummaries = MutableStateFlow<List<ChatSummary>>(emptyList())
  val chatSummaries: StateFlow<List<ChatSummary>> = _chatSummaries.asStateFlow()
  private val profilePicture = ImageBitMapIO()
  val friendsLocations = mutableStateOf<Map<User, Location?>>(emptyMap())

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

  // In UserViewModel

  // Change the type to allow null values
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

  /** Initialise le ViewModel en configurant le dépôt. */
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

  fun getNewUid(): String {
    return repository.getNewUid()
  }

  fun saveUserLoginStatus(userId: String) {
    repository.saveLoginStatus(userId)
  }

  fun getSavedUid(): String {
    return repository.getSavedUid()
  }

  fun isUserLoggedIn(): Boolean {
    return repository.isUserLoggedIn()
  }

  fun logout() {
    repository.logoutUser()
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
}
