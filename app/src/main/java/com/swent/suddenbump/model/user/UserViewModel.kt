package com.swent.suddenbump.model.user

import android.location.Location
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.chat.ChatRepositoryFirestore
import com.swent.suddenbump.model.chat.ChatSummary
import com.swent.suddenbump.model.chat.Message
import com.swent.suddenbump.model.image.ImageBitMapIO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class UserViewModel(
    private val repository: UserRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

  private val logTag = "UserViewModel"
  private val _chatSummaries = MutableStateFlow<List<ChatSummary>>(emptyList())
  val chatSummaries: Flow<List<ChatSummary>> = _chatSummaries.asStateFlow()
  private val profilePicture = ImageBitMapIO()
  val friendsLocations = mutableStateOf<Map<User, Location?>>(emptyMap())

  val locationDummy =
      Location("providerName").apply {
        latitude = 0.0 // Set latitude
        longitude = 0.0 // Set longitude
      }

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
  private val _userLocation: MutableStateFlow<Location> =
      MutableStateFlow(
          Location("mock_provider").apply {
            latitude = 0.0
            longitude = 0.0
          })
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

  init {
    repository.init { Log.i(logTag, "Repository successfully initialized!") }
  }

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserViewModel(
                UserRepositoryFirestore(Firebase.firestore),
                ChatRepositoryFirestore(Firebase.firestore))
                as T
          }
        }
  }

  fun setCurrentUser() {
    repository.getUserAccount(
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

  fun setUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    _user.value = user
    repository.updateUserAccount(user, onSuccess, onFailure)
  }

  /** onSuccess returns TRUE if no account exists */
  fun verifyNoAccountExists(
      emailAddress: String,
      onSuccess: (Boolean) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.verifyNoAccountExists(emailAddress, onSuccess, onFailure)
  }

  fun createUserAccount(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    _user.value = user
    repository.createUserAccount(user, onSuccess, onFailure)
  }

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

  fun declineFriendRequest(
      user: User = _user.value,
      friend: User,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.deleteFriendRequest(user, friend, onSuccess, onFailure)
    _userFriendRequests.value = _userFriendRequests.value.minus(friend)
  }

  fun sendFriendRequest(
      user: User = _user.value,
      friend: User,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.createFriendRequest(user, friend, onSuccess, onFailure)
    _sentFriendRequests.value = _sentFriendRequests.value.plus(friend)
  }

  fun getCurrentUser(): StateFlow<User> {
    return _user.asStateFlow()
  }

  fun getUserFriends(): StateFlow<List<User>> {
    return _userFriends.asStateFlow()
  }

  fun getUserFriendRequests(): StateFlow<List<User>> {
    return _userFriendRequests.asStateFlow()
  }

  fun setUserFriends(
      user: User = _user.value,
      friendsList: List<User>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    _userFriends.value = friendsList
    repository.setUserFriends(user, friendsList, onSuccess, onFailure)
  }

  fun getUserRecommendedFriends(): StateFlow<List<User>> {
    return _recommendedFriends.asStateFlow()
  }

  fun getSentFriendRequests(): StateFlow<List<User>> {
    return _sentFriendRequests.asStateFlow()
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
    repository.setBlockedFriends(user, blockedFriendsList, onSuccess, onFailure)
  }

  fun getLocation(): StateFlow<Location> {
    return _userLocation.asStateFlow()
  }

  fun updateLocation(
      user: User = _user.value,
      location: Location,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    _userLocation.value = location
    repository.updateLocation(user, location, onSuccess, onFailure)
  }

  fun loadFriendsLocations() {
    try {
      Log.i(logTag, "1: ${_userFriends.value.toString()}")
      println("1: ${_userFriends.value.toString()}")
      Log.i(logTag, "2: ${getUserFriends().value.toString()}")
      println("2: ${getUserFriends().value.toString()}")
      repository.getFriendsLocation(
          _userFriends.value,
          onSuccess = { friendsLoc ->
            // Update the state with the locations of friends
            friendsLocations.value = friendsLoc
            Log.d("FriendsMarkers", "On success load Friends Locations ${friendsLocations.value}")
            println("On success load Friends Locations ${friendsLocations.value}")
          },
          onFailure = { error ->
            // Handle the error, e.g., log or show error message
            Log.e("UserViewModel", "Failed to load friends' locations: ${error.message}")
            println("exception1")
          })
    } catch (e: Exception) {
      Log.e("UserViewModel", e.toString())
      println("exception2")
    }
    println("endfunc")
  }

  fun getSelectedContact(): StateFlow<User> {
    return _selectedContact.asStateFlow()
  }

  fun setSelectedContact(user: User) {
    _selectedContact.value = user
  }

  fun getRelativeDistance(friend: User): Float {
    loadFriendsLocations()
    val userLocation = _userLocation.value
    val friendLocation = friendsLocations.value[friend]
    return if (friendLocation != null) {
      userLocation.distanceTo(friendLocation)
    } else {
      Float.MAX_VALUE
    }
  }

  fun isFriendsInRadius(radius: Int): Boolean {
    loadFriendsLocations()
    friendsLocations.value.values.forEach { friendLocation ->
      if (friendLocation != null) {
        Log.d(
            "FriendsRadius", "Friends Locations: ${_userLocation.value.distanceTo(friendLocation)}")
        if (_userLocation.value.distanceTo(friendLocation) <= radius) {
          return true
        }
      }
    }
    return false
  }

  fun getNewUid(): String {
    return repository.getNewUid()
  }

  private val _messages = MutableStateFlow<List<Message>>(emptyList())
  val messages: Flow<List<Message>> = _messages

  private var chatId: String? = null

  private var isGettingChatId = false

  var user: User? = null
  private val userId: String?
    get() = user?.uid

  fun getOrCreateChat(friendId: String) =
      viewModelScope.launch {
        if (!isGettingChatId) {
          isGettingChatId = true
          chatId = chatRepository.getOrCreateChat(friendId, _user.value.uid)
          isGettingChatId = false
          chatRepository.getMessages(chatId!!).collect { messages -> _messages.value = messages }
        }
      }

  // Send a new message and add it to Firestore
  fun sendMessage(messageContent: String, user: User) {
    viewModelScope.launch {
      if (chatId != null) chatRepository.sendMessage(chatId!!, messageContent, user)
    }
  }

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
}
