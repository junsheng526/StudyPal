package com.example.studypal.bookroom.view.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.studypal.R
import com.example.studypal.bookroom.model.Payment
import com.example.studypal.bookroom.model.Venue
import com.example.studypal.bookroom.viewmodel.BookRoomViewModel
import com.example.studypal.databinding.FragmentPaymentBinding
import com.example.studypal.databinding.FragmentPaymentDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class PaymentDetailsFragment : Fragment() {

    private lateinit var binding: FragmentPaymentDetailsBinding
    private val nav by lazy { findNavController() }
    private lateinit var viewModel: BookRoomViewModel
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var paymentMethod: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPaymentDetailsBinding.inflate(inflater, container, false)
        viewModel = BookRoomViewModel()
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Retrieve data passed from PaymentFragment
        val venueName = arguments?.getString("venueName") ?: ""
        val outletName = arguments?.getString("outletName") ?: ""
        val bookingDate = arguments?.getString("bookingDate") ?: ""
        val duration = arguments?.getInt("duration", 0) ?: 0
        val numberOfPax = arguments?.getInt("numberOfPax", 0) ?: 0
        val price = arguments?.getInt("price", 0) ?: 0
        paymentMethod = arguments?.getString("paymentMethod") ?: ""

        binding.tvPrice.text = "RM $price"

        // Update UI based on payment method
        when (paymentMethod) {
            "Credit/Debit Card" -> showCardPaymentMethod()
            "TnG E-wallet" -> showEWalletPaymentMethod()
        }

        binding.submitBtn.setOnClickListener {
            if (validatePayment()) {
                // Payment validation successful, proceed to store payment details in Firestore
                val bookingId = UUID.randomUUID().toString() // Generate a unique booking ID
                val userId = auth.currentUser?.uid ?: ""
                val venue = Venue("", venueName, outletName)
                val payment = Payment(bookingId, venue, bookingDate, numberOfPax, duration, paymentMethod)

                // Store payment in Firestore
                firestore.collection("payments")
                    .document(bookingId)
                    .set(payment)
                    .addOnSuccessListener {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Payment Successful")
                            .setMessage("Your payment was processed successfully.")
                            .setPositiveButton("OK") { dialog, _ ->
                                // Handle the OK button click (if needed)
                                dialog.dismiss() // Close the dialog
                                // Navigate back to HomeFragment and clear back stack
                                findNavController().popBackStack(R.id.navigation_find_buddy, false)
                            }
                            .setCancelable(false) // Prevent dismissing the dialog on outside touch or back press
                            .show()
                        // Navigate to success screen or perform appropriate actions
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Payment failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        binding.cancelBtn.setOnClickListener {
            findNavController().popBackStack(R.id.paymentFragment, false)
        }

        return binding.root
    }

    private fun showCardPaymentMethod() {
        binding.cardPaymentMethod.visibility = View.VISIBLE
        binding.eWalletPaymentMethod.visibility = View.GONE
    }

    private fun showEWalletPaymentMethod() {
        binding.cardPaymentMethod.visibility = View.GONE
        binding.eWalletPaymentMethod.visibility = View.VISIBLE
    }

    private fun validatePayment(): Boolean {
        // Perform validation based on selected payment method
        when (paymentMethod) {
            "Credit/Debit Card" -> {
                val cardNumber = binding.editTextCardNumber.text.toString().trim()
                val nameOnCard = binding.editTextName.text.toString().trim()
                val expiryDate = binding.editTextExpiryDate.text.toString().trim()
                val securityCode = binding.editTextSecurityCode.text.toString().trim()

                // Validate Card Number
                if (!isValidCardNumber(cardNumber)) {
                    Toast.makeText(requireContext(), "Invalid card number. Please enter 16-digit card number.", Toast.LENGTH_SHORT).show()
                    return false
                }

                // Validate Name on Card (Only accept alphabetic characters)
                if (!isValidNameOnCard(nameOnCard)) {
                    Toast.makeText(requireContext(), "Invalid name on card. Only alphabetic characters are allowed.", Toast.LENGTH_SHORT).show()
                    return false
                }

                // Validate Expiry Date (Format: MM/YY)
                if (!isValidExpiryDate(expiryDate)) {
                    Toast.makeText(requireContext(), "Invalid expiry date. Please enter expiry date in MM/YY format.", Toast.LENGTH_SHORT).show()
                    return false
                }

                // Validate Security Code (3-digit number)
                if (!isValidSecurityCode(securityCode)) {
                    Toast.makeText(requireContext(), "Invalid security code. Please enter a 3-digit security code.", Toast.LENGTH_SHORT).show()
                    return false
                }
            }
            "TnG E-wallet" -> {
                val phoneNumber = binding.editTextPhoneNumber.text.toString().trim()
                val pinNumber = binding.editPinNumber.text.toString().trim()

                // Validate Phone Number (Malaysia phone number prefix + 9 digits)
                if (!isValidPhoneNumber(phoneNumber)) {
                    Toast.makeText(requireContext(), "Invalid phone number. Please enter a valid Malaysian phone number.", Toast.LENGTH_SHORT).show()
                    return false
                }

                // Validate PIN Number (6-digit number)
                if (!isValidPinNumber(pinNumber)) {
                    Toast.makeText(requireContext(), "Invalid PIN number. Please enter a 6-digit PIN.", Toast.LENGTH_SHORT).show()
                    return false
                }
            }
            else -> {
                Toast.makeText(requireContext(), "Please select a payment method", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

// Validation Functions

    private fun isValidCardNumber(cardNumber: String): Boolean {
        return cardNumber.length == 16 && cardNumber.matches(Regex("[0-9]+"))
    }

    private fun isValidNameOnCard(nameOnCard: String): Boolean {
        return nameOnCard.matches(Regex("[a-zA-Z ]+"))
    }

    private fun isValidExpiryDate(expiryDate: String): Boolean {
        return expiryDate.matches(Regex("^(0[1-9]|1[0-2])/[0-9]{2}\$"))
    }

    private fun isValidSecurityCode(securityCode: String): Boolean {
        return securityCode.length == 3 && securityCode.matches(Regex("[0-9]+"))
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.matches(Regex("^(01)[0-9]{8,9}\$"))
    }

    private fun isValidPinNumber(pinNumber: String): Boolean {
        return pinNumber.length == 6 && pinNumber.matches(Regex("[0-9]+"))
    }
}