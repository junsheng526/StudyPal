package com.example.studypal.bookroom.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Venue(
    val venueId: String,
    val venueName: String,
    val outletName: String
) : Parcelable