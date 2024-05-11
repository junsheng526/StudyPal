package com.example.studypal.authentication.view

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.studypal.R
import com.example.studypal.databinding.FragmentForgotPasswordBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class ForgotPasswordFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentForgotPasswordBinding
    private val nav by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)

        binding.toLoginBtn.setOnClickListener {
            nav.navigate(R.id.loginFragment)
        }

        binding.resetBtn.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            if (validateInputFields()) {
                auth.sendPasswordResetEmail(email).addOnSuccessListener { _ ->
                    Toast.makeText(
                        requireContext(),
                        "Please check your email for verification!",
                        Toast.LENGTH_SHORT
                    ).show()
                    nav.navigate(R.id.loginFragment)
                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Send verification email failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        return binding.root
    }

    private fun validateInputFields(): Boolean {
        val email = binding.editTextEmail.text.toString()
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.error = "Invalid email format!"
            return false
        }
        return true
    }
}