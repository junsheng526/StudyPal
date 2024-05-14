package com.example.studypal.authentication.view

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.studypal.R
import com.example.studypal.authentication.model.User
import com.example.studypal.authentication.viewmodel.AuthViewModel
import com.example.studypal.databinding.FragmentRegisterBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlin.random.Random

class RegisterFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentRegisterBinding
    private val nav by lazy { findNavController() }
    private val vm: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        auth = Firebase.auth

        binding.toLoginBtn.setOnClickListener {
            nav.navigate(R.id.loginFragment)
        }

        binding.registerBtn.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val pwd = binding.editTextPassword.text.toString()
            if (validateInputFields()) {
                auth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener {
                    if (it.isSuccessful) {
                        saveUserToFireStoreDb()
                    } else {
                        Toast.makeText(context, "Failed to create user!", Toast.LENGTH_SHORT).show()
                        Log.e("Error during creating user >> ", it.exception.toString())
                    }
                }
            }
        }
        return binding.root
    }

    private val db = FirebaseFirestore.getInstance()

    private fun saveUserToFireStoreDb() {

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val name = binding.editTextName.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()

        val userObj = User(
            id = userId,
            name = name,
            email = email,
        )

        lifecycleScope.launch {
            val success: Boolean = vm.set(userObj)
            if (success) {
                val (latitude, longitude) = generateRandomCoordinates()
                val locationData = hashMapOf(
                    "latitude" to latitude,
                    "longitude" to longitude
                )
                db.collection("location").document(userId)
                    .set(locationData)
                    .addOnSuccessListener {
                        // Success handling
                        Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT)
                            .show()
                        binding.editTextName.text.clear()
                        binding.editTextEmail.text.clear()
                        binding.editTextPassword.text.clear()
                        auth.signOut()
                        nav.navigate(R.id.loginFragment)
                    }
                    .addOnFailureListener { e ->
                        // Failure handling for location data
                        Toast.makeText(
                            context,
                            "Failed to save location data: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(context, "Failed to create user!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateRandomCoordinates(): Pair<Double, Double> {
        // Define latitude and longitude bounds for Kuala Lumpur
        val klLatitudeMin = 3.0
        val klLatitudeMax = 3.3
        val klLongitudeMin = 101.5
        val klLongitudeMax = 101.8

        // Generate random latitude within the bounds of Kuala Lumpur
        val latitude = Random.nextDouble(klLatitudeMin, klLatitudeMax)

        // Generate random longitude within the bounds of Kuala Lumpur
        val longitude = Random.nextDouble(klLongitudeMin, klLongitudeMax)

        return Pair(latitude, longitude)
    }

    private fun validateInputFields(): Boolean {
        val name = binding.editTextName.text.toString()
        val email = binding.editTextEmail.text.toString()
        val pwd = binding.editTextPassword.text.toString()

        if (name == "") {
            binding.editTextName.error = "This field is required!"
            return false
        }
        if (email == "") {
            binding.editTextEmail.error = "This field is required!"
            return false
        }
        if (!isValidTarumtEmail(email)) {
            binding.editTextEmail.error = "Invalid email format/please use TARUMT email"
            return false
        }
        if (pwd == "") {
            binding.editTextPassword.error = "This field is required!"
            return false
        }
        if (binding.editTextPassword.length() < 8) {
            binding.editTextPassword.error = "Password should at least 8 characters long!"
            return false
        }
        return true
    }

    private fun isValidTarumtEmail(email: String): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false
        }
        if (!email.contains("@student.tarc.edu.my")) {
            return false
        }
        return true
    }
}