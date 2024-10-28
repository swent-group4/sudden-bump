package com.swent.suddenbump.model.user

import android.location.Location
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.chat.Message
import com.swent.suddenbump.model.image.ImageBitMapIO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    private val logTag = "UserViewModel"
    private val profilePicture = ImageBitMapIO()
    val friendsLocations = mutableStateOf<Map<User, Location?>>(emptyMap())

    private val _user: MutableStateFlow<User> = MutableStateFlow(User())
    private val _userFriends: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())
    private val _blockedFriends: MutableStateFlow<List<User>> = MutableStateFlow(listOf(_user.value))
    private val _userLocation: MutableStateFlow<Location> =
        MutableStateFlow(
            Location("mock_provider").apply {
                latitude = 0.0
                longitude = 0.0
            })
    private val _userProfilePictureChanging: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _users: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())

    init {
        repository.init { Log.i(logTag, "Repository successfully initialized!") }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return UserViewModel(UserRepositoryFirestore(Firebase.firestore)) as T
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

    fun getCurrentUser(): StateFlow<User> {
        return _user.asStateFlow()
    }

    fun getUserAccount(uid: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit){
        repository.getUserAccount(uid, onSuccess, onFailure)
    }

    fun getUserFriends(): StateFlow<List<User>> {
        return _userFriends.asStateFlow()
    }

    fun addUserFriend(friend: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        setUserFriends(getCurrentUser().value, _userFriends.value + friend, onSuccess, onFailure)
    }

    private fun setUserFriends(
        user: User = _user.value,
        friendsList: List<User>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        _userFriends.value = friendsList.map {
            it.copy(isFriend = true)
        }.distinct()
        repository.setUserFriends(user, friendsList.distinct(), onSuccess, onFailure)
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
        repository.getFriendsLocation(
            _user.value,
            onSuccess = { friendsLoc ->
                // Update the state with the locations of friends
                friendsLocations.value = friendsLoc
            },
            onFailure = { error ->
                // Handle the error, e.g., log or show error message
                Log.e("UserViewModel", "Failed to load friends' locations: ${error.message}")
            })
    }

    fun getRelativeDistance(friend: User): Float {
        loadFriendsLocations()
        val userLocation = _userLocation.value
        val friendLocation = friendsLocations.value.get(friend)
        return if (friendLocation != null) {
            userLocation.distanceTo(friendLocation)
        } else {
            Float.MAX_VALUE
        }
    }

    fun getNewUid(): String {
        return repository.getNewUid()
    }

    fun getAllUsers(): StateFlow<List<User>> {
        viewModelScope.launch {
            _users.value = repository.getAllUsers().getOrNull() ?: emptyList()
        }
        return _users.asStateFlow()
    }

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: Flow<List<Message>> = _messages

    private val chatRepository = ChatRepository()
    private var chatId: String? = null
    var user: User? = null
    private val userId: String?
        get() = user?.uid

    fun getChatSummaries() = chatRepository.getChatSummaries()

    fun getOrCreateChat() = viewModelScope.launch {
        chatId = chatRepository.getOrCreateChat(userId ?: "")
        chatRepository.getMessages(chatId!!).collect { messages ->
            _messages.value = messages
        }
    }

    // Send a new message and add it to Firestore
    fun sendMessage(messageContent: String,username : String) {
        viewModelScope.launch {
            if (chatId != null)
                chatRepository.sendMessage(chatId!!, messageContent,username)
        }
    }

    fun markMessagesAsRead() = viewModelScope.launch{
        if (chatId != null)
            chatRepository.markMessagesAsRead(chatId!!)
    }
}
