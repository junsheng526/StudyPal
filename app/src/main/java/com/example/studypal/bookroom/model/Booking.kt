package com.example.studypal.bookroom.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Booking(
    val selectedVenue: Venue,
    val bookingDate: String,
    val startTime: String,
    val endTime: String,
    val numberOfPax: Int
) : Parcelable