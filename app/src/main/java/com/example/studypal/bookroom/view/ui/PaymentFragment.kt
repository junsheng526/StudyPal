package com.example.studypal.bookroom.view.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.studypal.R
import com.example.studypal.bookroom.model.Booking
import com.example.studypal.databinding.FragmentPaymentBinding

class PaymentFragment : Fragment() {

    private lateinit var binding: FragmentPaymentBinding
    private val nav by lazy { findNavController() }

    private var duration: Int = 0
    private var price: Int = 0
    private lateinit var bookingDetails: Booking

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPaymentBinding.inflate(inflater, container, false)
        bookingDetails = requireArguments().getParcelable("bookingDetails")!!

        binding.tvVenueName.text = bookingDetails.selectedVenue.venueName
        binding.tvOutletName.text = bookingDetails.selectedVenue.outletName
        binding.bookingDate.text = bookingDetails.bookingDate
        binding.tvNumberOfPax.text = bookingDetails.numberOfPax.toString()
        // Display duration based on start and end time
        val startTime = bookingDetails.startTime
        val endTime = bookingDetails.endTime
        // Calculate duration
        duration = calculateDuration(startTime, endTime)
        binding.duration.text = "$duration minutes"
        price = calculatePriceForDuration(duration)
        binding.tvPrice.text = "RM $price"
        // Handle "Continue" button click
        binding.submitBtn.setOnClickListener {
            navigateToPaymentDetails()
        }
        binding.cancelBtn.setOnClickListener {
            findNavController().popBackStack(R.id.bookingDetailsFragment, false)
        }
        return(binding.root)
    }

    private fun calculateDuration(startTime: String, endTime: String): Int {
        val start = startTime.split(":")
        val end = endTime.split(":")
        val startHour = start[0].toInt()
        val startMinute = start[1].toInt()
        val endHour = end[0].toInt()
        val endMinute = end[1].toInt()

        val startMinutes = startHour * 60 + startMinute
        val endMinutes = endHour * 60 + endMinute

        return endMinutes - startMinutes
    }

    private fun calculatePriceForDuration(durationInMinutes: Int): Int {
        val hours = durationInMinutes / 60
        // Calculate price: 5 ringgit per hour
        val price = hours * 5
        return price
    }

    private fun navigateToPaymentDetails() {
        // Check if any payment method is selected
        val selectedPaymentMethodId = binding.paymentMethodGroup.checkedRadioButtonId
        if (selectedPaymentMethodId == -1) {
            // No payment method selected, show an error message or handle accordingly
            // For example, display a Toast message indicating that a payment method must be selected
            Toast.makeText(requireContext(), "Please select a payment method", Toast.LENGTH_SHORT).show()
            return
        }

        // Get selected payment method based on the radio button ID
        val selectedPaymentMethod = when (selectedPaymentMethodId) {
            R.id.radioCardPayment -> "Credit/Debit Card"
            R.id.radioEwallet -> "TnG E-wallet"
            else -> "Unknown Payment Method"
        }

        // Create a bundle to pass data to PaymentDetailsFragment
        val bundle = Bundle().apply {
            // Pass necessary data to PaymentDetailsFragment
            putString("venueName", binding.tvVenueName.text.toString())
            putString("outletName", binding.tvOutletName.text.toString())
            putString("bookingDate", binding.bookingDate.text.toString())
            putInt("duration", duration)
            putInt("numberOfPax", bookingDetails.numberOfPax)
            putInt("price", price)
            putString("paymentMethod", selectedPaymentMethod)
        }

        // Navigate to PaymentDetailsFragment with data bundle
        findNavController().navigate(R.id.action_paymentFragment_to_paymentDetailsFragment, bundle)
    }
}