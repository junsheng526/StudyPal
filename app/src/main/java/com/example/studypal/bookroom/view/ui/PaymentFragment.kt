package com.example.studypal.bookroom.view.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.studypal.R
import com.example.studypal.bookroom.model.Booking
import com.example.studypal.databinding.FragmentPaymentBinding

class PaymentFragment : Fragment() {

    private lateinit var binding: FragmentPaymentBinding

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPaymentBinding.inflate(inflater, container, false)
        val bookingDetails: Booking = requireArguments().getParcelable("bookingDetails")!!

        binding.tvVenueName.text = bookingDetails.selectedVenue.venueName
        binding.tvOutletName.text = bookingDetails.selectedVenue.outletName
        binding.bookingDate.text = bookingDetails.bookingDate
        binding.tvNumberOfPax.text = bookingDetails.numberOfPax.toString()
        // Display duration based on start and end time
        val startTime = bookingDetails.startTime
        val endTime = bookingDetails.endTime
        // Calculate duration
        val duration = calculateDuration(startTime, endTime)
        binding.duration.text = "$duration minutes"
        val price = calculatePriceForDuration(duration)
        binding.tvPrice.text = "RM $price"
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
}