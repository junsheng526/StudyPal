package com.example.studypal.profile.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.studypal.authentication.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileViewModel : ViewModel() {
    private val col = Firebase.firestore.collection("user")
    private val users = MutableLiveData<List<User>>()
    private lateinit var auth: FirebaseAuth

    init {
        col.addSnapshotListener { snap, _ -> users.value = snap?.toObjects() }
    }

    suspend fun get(id: String): User? {
        return col.document(id).get().await().toObject<User>()
    }

    suspend fun set(user: User): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                auth = Firebase.auth
                val currentUser = auth.currentUser
                if (currentUser != null && user != null) {
                    col.document(currentUser.uid).set(user)
                        .addOnSuccessListener {
                            Log.i("FireStore", "User saved successfully: $user")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FireStore", "Error saving user: $e")
                        }
                        .await()
                }
                true
            } catch (e: Exception) {
                Log.e("FireStore", "FireStore operation failed: $e")
                false
            }
        }
    }

    suspend fun update(user: User): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                auth = Firebase.auth
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val documentRef = col.document(currentUser.uid)
                    val updates = mutableMapOf<String, Any?>()

                    // Update specific fields of the user document
                    if (user.name.isNotEmpty()) {
                        updates["name"] = user.name
                    }
                    if (user.studyField.isNotEmpty()) {
                        updates["studyField"] = user.studyField
                    }
                    if (user.learningStyle.isNotEmpty()) {
                        updates["learningStyle"] = user.learningStyle
                    }
                    if (user.interest.isNotEmpty()) {
                        updates["interest"] = user.interest
                    }
                    // Update photo if available
                    if (user.photo != null) {
                        updates["photo"] = user.photo
                    }

                    // Perform the update with only specified fields
                    documentRef.update(updates)
                        .addOnSuccessListener {
                            Log.i("FireStore", "User fields updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FireStore", "Error updating user fields: $e")
                        }
                        .await()
                }
                true
            } catch (e: Exception) {
                Log.e("FireStore", "Firestore operation failed: $e")
                false
            }
        }
    }

    fun validate(user: User): String {
        var e = ""

        e += if (user.name == "") "- Name is required.\n"
        else if (user.name.length < 3) "- Name is too short (at least 3 letters).\n"
        else ""

        e += if (user.studyField == "") "- Study field is required.\n"
        else ""

        e += if (user.learningStyle == "") "- Learning style is required.\n"
        else ""

        e += if (user.interest == "") "- Interest field is required.\n"
        else ""

        return e
    }
}