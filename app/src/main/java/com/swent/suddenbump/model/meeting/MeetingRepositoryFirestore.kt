package com.swent.suddenbump.model.meeting

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class MeetingRepositoryFirestore(private val db: FirebaseFirestore) : MeetingRepository {

  private val meetingsCollectionPath = "Meetings"

  override fun getNewMeetingId(): String {
    return db.collection(meetingsCollectionPath).document().id
  }

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

  override fun getMeetings(onSuccess: (List<Meeting>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(meetingsCollectionPath)
        .get()
        .addOnFailureListener { onFailure(it) }
        .addOnSuccessListener { result ->
          val meetings = result.documents.mapNotNull { it.toMeeting() }
          onSuccess(meetings)
        }
  }

  override fun addMeeting(meeting: Meeting, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(meetingsCollectionPath)
        .document(meeting.meetingId)
        .set(meeting.toMap())
        .addOnCompleteListener { result ->
          if (result.isSuccessful) {
            onSuccess()
          } else {
            result.exception?.let { onFailure(it) }
          }
        }
  }

  override fun updateMeeting(
      meeting: Meeting,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(meetingsCollectionPath)
        .document(meeting.meetingId)
        .set(meeting.toMap())
        .addOnCompleteListener { result ->
          if (result.isSuccessful) {
            onSuccess()
          } else {
            result.exception?.let { onFailure(it) }
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
        result.exception?.let { onFailure(it) }
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
        "meetingId" to meetingId,
        "location" to location,
        "date" to date,
        "friendId" to friendId,
        "creatorId" to creatorId,
        "accepted" to accepted
    )
  }
}
