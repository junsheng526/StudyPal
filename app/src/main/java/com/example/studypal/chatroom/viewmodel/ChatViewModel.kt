package com.example.studypal.chatroom.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.studypal.chatroom.model.Friend
import com.example.studypal.chatroom.model.Message
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class ChatViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUser: FirebaseUser? = auth.currentUser

    private val friends = MutableLiveData<List<Friend>>()
    private val messages = MutableLiveData<List<Message>>()

    init {
        fetchFriends()
    }

    fun getFriends(): LiveData<List<Friend>> {
        return friends
    }

    fun getMessagesWithFriend(friendId: String): LiveData<List<Message>> {
        loadMessages(friendId)
        return messages
    }

    private fun fetchFriends() {
        currentUser?.uid?.let { uid ->
            getUserDocumentReference(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ChatViewModel", "Error fetching user data: $error")
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val friendsArray = snapshot["friends"] as? List<String> ?: emptyList()
                        fetchFriendDetailsWithLastMessage(friendsArray)
                    }
                }
        }
    }

    private fun fetchFriendDetailsWithLastMessage(friendUserIds: List<String>) {
        val friendList = mutableListOf<Friend>()

        for (friendUserId in friendUserIds) {
            getUserDocumentReference(friendUserId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val name = documentSnapshot["name"] as? String ?: ""
                        val photo = documentSnapshot["photo"] as? Blob ?: Blob.fromBytes(ByteArray(0))
                        fetchLastMessageTimestamp(friendUserId) { timestamp ->
                            val friend = Friend(friendUserId, name, photo, timestamp)
                            updateFriendList(friendList, friend)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ChatViewModel", "Error fetching friend details: $e")
                }
        }
    }

    private fun fetchLastMessageTimestamp(friendUserId: String, callback: (Long) -> Unit) {
        val chatRoomId = if (currentUser != null) {
            if (currentUser.uid < friendUserId) {
                "${currentUser.uid}_$friendUserId"
            } else {
                "${friendUserId}_${currentUser.uid}"
            }
        } else {
            return
        }

        firestore.collection("chats")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    Log.e("ChatViewModel", "Error fetching last message timestamp: $error")
                    callback(0L)
                    return@addSnapshotListener
                }

                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    val lastMessage = querySnapshot.documents[0]
                    val timestamp = lastMessage.getLong("timestamp") ?: 0L
                    callback(timestamp)
                } else {
                    callback(0L) // No messages, return 0
                }
            }
    }

    private fun updateFriendList(friendList: MutableList<Friend>, updatedFriend: Friend) {
        val index = friendList.indexOfFirst { it.id == updatedFriend.id }
        if (index != -1) {
            friendList[index] = updatedFriend
        } else {
            friendList.add(updatedFriend)
        }
        friends.value = friendList.sortedByDescending { it.lastMessageTimestamp }
    }

    private fun getUserDocumentReference(userDocumentId: String): DocumentReference {
        return firestore.collection("user").document(userDocumentId)
    }

    private fun loadMessages(friendId: String) {
        val chatRoomId = if (currentUser != null) {
            if (currentUser.uid < friendId) {
                "${currentUser.uid}_$friendId"
            } else {
                "${friendId}_${currentUser.uid}"
            }
        } else {
            return
        }

        firestore.collection("chats")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    Log.e("ChatViewModel", "Error fetching messages: $error")
                    return@addSnapshotListener
                }

                val messageList = mutableListOf<Message>()
                querySnapshot?.documents?.forEach { document ->
                    val senderId = document.getString("senderId") ?: ""
                    val content = document.getString("content") ?: ""
                    val timestamp = document.getLong("timestamp") ?: 0
                    val message = Message(senderId, content, timestamp)
                    messageList.add(message)
                }
                messages.value = messageList
            }
    }

    fun sendMessage(friendId: String, content: String) {
        // Construct the chat room ID based on both users' IDs
        val chatRoomId = if (currentUser != null) {
            if (currentUser.uid < friendId) {
                "${currentUser.uid}_$friendId"
            } else {
                "${friendId}_${currentUser.uid}"
            }
        } else {
            return
        }

        currentUser?.uid?.let { uid ->
            // Create a new message document within the specified chat room
            val messageMap = hashMapOf(
                "senderId" to uid,
                "content" to content,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("chats")
                .document(chatRoomId)
                .collection("messages")
                .add(messageMap)
                .addOnSuccessListener {
                    Log.d("ChatViewModel", "Message sent successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("ChatViewModel", "Error sending message: $e")
                }
        }
    }

    fun getTopTwoFriends(): Pair<Friend?, Friend?> {
        val friendList = friends.value ?: emptyList()

        // Check if there are at least two friends in the list
        return if (friendList.size >= 2) {
            Pair(friendList[0], friendList[1]) // Return the first two friends
        } else {
            // Return null for the second friend if there's only one friend or none
            Pair(friendList.getOrNull(0), null)
        }
    }
}