package com.example.studypal.bookroom.view.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.studypal.R
import com.example.studypal.bookroom.model.Booking
import com.example.studypal.bookroom.model.Venue
import com.example.studypal.databinding.FragmentBookingDetailsBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BookingDetailsFragment : Fragment() {

    private lateinit var binding: FragmentBookingDetailsBinding
    private val nav by lazy { findNavController() }
    private var startHour: Int = 0
    private var startMinute: Int = 0
    private var endHour: Int = 0
    private var endMinute: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBookingDetailsBinding.inflate(inflater, container, false)

        val args: BookingDetailsFragmentArgs by navArgs()
        val selectedVenue: Venue = args.selectedVenue

        binding.tvVenue.text = selectedVenue.venueName
        binding.tvOutlet.text = selectedVenue.outletName

        val seatOptions = arrayOf("1", "2", "3", "4")
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, seatOptions)
        binding.noOfPaxTv.setAdapter(adapter)

        binding.btnDatePicker.setOnClickListener {
            // Get current date
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

            // Create DatePickerDialog with minimum date set to tomorrow's date
            val datePicker = DatePickerDialog(requireContext())
            datePicker.datePicker.minDate = getNextDayMillis() // Set minimum date to tomorrow
            datePicker.setOnDateSetListener { view, year, month, dayOfMonth ->
                val selectedDate = "$dayOfMonth/${month + 1}/$year"
                binding.bookingDate.text = selectedDate
            }
            datePicker.show()
        }

        // Start Time Picker
        binding.btnStartTimePicker.setOnClickListener {
            showTimePicker(true)
        }

        // End Time Picker
        binding.btnEndTimePicker.setOnClickListener {
            showTimePicker(false)
        }

        binding.submitBtn.setOnClickListener {
            if (isFormValid()) {
                // Proceed with booking
                val bookingDetails = Booking(
                    selectedVenue,
                    binding.bookingDate.text.toString(),
                    "$startHour:$startMinute",
                    "$endHour:$endMinute",
                    binding.noOfPaxTv.text.toString().toInt()
                )

                val action = BookingDetailsFragmentDirections.actionBookingDetailsFragmentToPaymentFragment(bookingDetails)
                findNavController().navigate(action)
            }
        }

        binding.cancelBtn.setOnClickListener {
            findNavController().popBackStack(R.id.navigation_book_room, false)
        }

        return(binding.root)
    }

    private fun isFormValid(): Boolean {
        val context = requireContext()
        val binding = this.binding

        if (binding.bookingDate.text.isNullOrBlank() || binding.bookingDate.text == "dd MMM yyyy") {
            showToast(context, "Please select a booking date.")
            return false
        }

        if (binding.startBookingTime.text.isNullOrBlank() || binding.endBookingTime.text.isNullOrBlank()) {
            showToast(context, "Please select booking start and end times.")
            return false
        }

        if (binding.startBookingTime.text == "HH:mm" || binding.endBookingTime.text == "HH:mm") {
            showToast(context, "Please select booking start and end times.")
            return false
        }

        if (binding.noOfPaxTv.text.isNullOrBlank()) {
            showToast(context, "Please select the number of people (pax).")
            return false
        }

        val startTime = parseTime(binding.startBookingTime.text.toString())
        val endTime = parseTime(binding.endBookingTime.text.toString())

        if (startTime != null && endTime != null) {
            val durationInMinutes = calculateDurationInMinutes(startTime, endTime)
            if (durationInMinutes < 0) {
                showToast(context, "End time must after your start time!")
                return false
            }
            if (durationInMinutes < 60) {
                showToast(context, "Booking duration must be at least 60 minutes.")
                return false
            }
        }

        return true
    }

    private fun parseTime(timeStr: String): Date? {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return try {
            dateFormat.parse(timeStr)
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateDurationInMinutes(startTime: Date, endTime: Date): Long {
        val durationInMillis = endTime.time - startTime.time
        return durationInMillis / (60 * 1000) // Convert milliseconds to minutes
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                if (isStartTime) {
                    startHour = hourOfDay
                    startMinute = minute
                    binding.startBookingTime.text = selectedTime
                } else {
                    endHour = hourOfDay
                    endMinute = minute
                    binding.endBookingTime.text = selectedTime
                }
            },
            hour,
            minute,
            true // 24-hour format
        )

        timePickerDialog.show()
    }

    private fun getNextDayMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1) // Add one day
        calendar.set(Calendar.HOUR_OF_DAY, 0) // Set time to midnight
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}