package com.example.studypal.chatroom.model

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId
data class Friend(
    @DocumentId
    var id   : String = "",
    var name : String = "",
    var photo: Blob = Blob.fromBytes(ByteArray(0)),
    val lastMessageTimestamp: Long = 0
)