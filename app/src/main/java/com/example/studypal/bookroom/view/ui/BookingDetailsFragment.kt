package com.example.studypal.bookroom.view.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.studypal.R
import com.example.studypal.bookroom.model.Booking
import com.example.studypal.bookroom.model.Venue
import com.example.studypal.databinding.FragmentBookingDetailsBinding
import java.util.Calendar
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

        val seatOptions = arrayOf("1", "2", "3", "4", "5")
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

        binding.cancelBtn.setOnClickListener {
            findNavController().popBackStack(R.id.navigation_book_room, false)
        }

        return(binding.root)
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