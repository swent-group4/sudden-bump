package com.swent.suddenbump.model.meeting

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.swent.suddenbump.model.user.User

class MeetingRepositoryFirestore(private val db: FirebaseFirestore) : MeetingRepository {

    private val meetingsCollectionPath = "Meetings"

    override fun init(onSuccess: () -> Unit) {
        db.collection(meetingsCollectionPath).get().addOnCompleteListener { result ->
            if (result.isSuccessful) {
                onSuccess()
            } else {
                result.exception?.let {
                    Log.e("MeetingRepositoryFirestore", "Error initializing repository", it)
                }
            }
        }
    }

    override fun getMeetings(
        user: User,
        onSuccess: (List<Meeting>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(meetingsCollectionPath)
            .document(user.uid)
            .get()
            .addOnFailureListener { onFailure(it) }
            .addOnSuccessListener { result ->
                val meetingsList = result.data?.let { data ->
                    data.mapNotNull { entry ->
                        val meetingData = entry.value as? Map<*, *>
                        meetingData?.let {
                            Meeting(
                                userId = it["userId"] as? String ?: "",
                                location = it["location"] as? String ?: "",
                                date = it["date"] as? Timestamp ?: Timestamp.now(),
                                friendId = it["friendId"] as? String ?: ""
                            )
                        }
                    }
                } ?: emptyList()
                onSuccess(meetingsList)
            }
    }

    override fun addMeeting(
        meeting: Meeting,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(meetingsCollectionPath).document(meeting.userId).set(meeting.toMap()).addOnCompleteListener { result ->
            if (result.isSuccessful) {
                onSuccess()
            } else {
                result.exception?.let {
                    Log.e("MeetingRepositoryFirestore", "Error adding document", it)
                    onFailure(it)
                }
            }
        }
    }

    override fun updateMeeting(
        meeting: Meeting,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(meetingsCollectionPath).document(meeting.userId).set(meeting.toMap()).addOnCompleteListener { result ->
            if (result.isSuccessful) {
                onSuccess()
            } else {
                result.exception?.let {
                    Log.e("MeetingRepositoryFirestore", "Error updating document", it)
                    onFailure(it)
                }
            }
        }
    }

    override fun deleteMeetingById(
        id: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(meetingsCollectionPath).document(id).delete().addOnCompleteListener { result ->
            if (result.isSuccessful) {
                onSuccess()
            } else {
                result.exception?.let {
                    Log.e("MeetingRepositoryFirestore", "Error deleting document", it)
                    onFailure(it)
                }
            }
        }
    }

    private fun DocumentSnapshot.toMeeting(): Meeting? {
        return try {
            this.toObject(Meeting::class.java)
        } catch (e: Exception) {
            Log.e("MeetingRepositoryFirestore", "Error converting document to Meeting", e)
            null
        }
    }

    private fun Meeting.toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "location" to location,
            "date" to date,
            "friendId" to friendId
        )
    }
}