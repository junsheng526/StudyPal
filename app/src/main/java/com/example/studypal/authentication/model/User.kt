package com.example.studypal.authentication.model

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var studyField: String = "",
    var learningStyle: String = "",
    var interest: String = "",
    var photo: Blob = Blob.fromBytes(ByteArray(0)),
)