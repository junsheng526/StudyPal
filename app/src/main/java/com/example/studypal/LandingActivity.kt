package com.example.studypal

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.studypal.authentication.model.User
import com.example.studypal.authentication.view.AuthenticationActivity
import com.example.studypal.databinding.ActivityLandingBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class LandingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandingBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("user")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandingBinding.inflate(layoutInflater)
        auth = Firebase.auth
        setContentView(binding.root)

        Handler().postDelayed({
            val currentUser = auth.currentUser
            if (currentUser != null) {
                getUserByUid(currentUser.uid) { user ->
                    if (user != null) {
                        // User found in Firestore
                        Log.d("LandingActivity", "User found: ${user.email}")
                        navigateToMainActivity()
                    } else {
                        // User document not found
                        Log.d("LandingActivity", "User document not found in Firestore, navigating to AuthenticationActivity")
                        navigateToAuthenticationActivity()
                    }
                }
            } else {
                // No current user
                Log.d("LandingActivity", "No current user, navigating to AuthenticationActivity")
                navigateToAuthenticationActivity()
            }
        }, 2000)
    }

    private fun navigateToMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToAuthenticationActivity(){
        val intent = Intent(this, AuthenticationActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun getUserByUid(uid: String, callback: (User?) -> Unit) {
        usersCollection.document(uid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    user?.id = documentSnapshot.id
                    callback(user)
                } else {
                    callback(null) // User document not found
                }
            }
            .addOnFailureListener { e ->
                Log.e("LandingActivity", "Error fetching user by UID", e)
                callback(null)
            }
    }
}