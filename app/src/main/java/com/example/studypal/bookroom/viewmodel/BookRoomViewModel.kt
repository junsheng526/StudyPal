package com.example.studypal.bookroom.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.studypal.bookroom.model.Venue
import com.google.firebase.firestore.FirebaseFirestore

class BookRoomViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val venues = MutableLiveData<List<Venue>>()

    init {
        loadVenues()
    }

    fun getVenues(): LiveData<List<Venue>> {
        return venues
    }

    private fun loadVenues() {
        firestore.collection("venue")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val venueList = mutableListOf<Venue>()
                for (document in querySnapshot) {
                    val venueId = document.id
                    val venueName = document.getString("venueName") ?: ""
                    val outletName = document.getString("outletName") ?: ""
                    val venue = Venue(venueId, venueName, outletName)
                    venueList.add(venue)
                }
                venues.value = venueList
            }
            .addOnFailureListener { e ->
                // Handle error
            }
    }
}