package com.example.studypal.chatroom.model

import com.google.firebase.Timestamp

data class Message(
    val senderId: String,
    val content: String,
    val timestamp: Long
)