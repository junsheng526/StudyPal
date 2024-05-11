package com.example.studypal.authentication.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class UserRepository(private val auth: FirebaseAuth, private val firestore: FirebaseFirestore) {

    suspend fun loginUser(email: String, password: String): User? {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            getUserByEmail(email)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getUserByEmail(email: String): User? {
        val querySnapshot = firestore.collection("users").whereEqualTo("email", email).get().await()

        val user = querySnapshot.documents.firstOrNull()?.toObject<User>()
        user?.id = querySnapshot.documents.firstOrNull()?.id ?: ""
        return user
    }
}