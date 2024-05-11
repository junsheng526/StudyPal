package com.example.studypal.bookroom.model

data class Payment(
    val bookingId: String,
    val selectedVenue: Venue,
    val bookingDate: String,
    val numberOfPax: Int,
    val duration: Int,
    val paymentMethod: String,
)