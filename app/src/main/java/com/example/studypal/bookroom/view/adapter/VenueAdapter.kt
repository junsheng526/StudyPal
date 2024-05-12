package com.example.studypal.bookroom.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studypal.R
import com.example.studypal.bookroom.model.Venue

class VenueAdapter(
    private val onItemClick: (Venue) -> Unit,
    private val onBookingButtonClick: (Venue) -> Unit
) : RecyclerView.Adapter<VenueAdapter.VenueViewHolder>() {

    private var venuesList = mutableListOf<Venue>()

    inner class VenueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val venueName: TextView = itemView.findViewById(R.id.tvVenue)
        private val outletName: TextView = itemView.findViewById(R.id.tvOutlet)
        private val bookingButton: Button = itemView.findViewById(R.id.bookingBtn)

        fun bind(venue: Venue) {
            venueName.text = venue.venueName
            outletName.text = venue.outletName

            // Set OnClickListener for the booking button
            bookingButton.setOnClickListener {
                onBookingButtonClick.invoke(venue)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VenueViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_venue, parent, false)
        return VenueViewHolder(view)
    }

    override fun onBindViewHolder(holder: VenueViewHolder, position: Int) {
        val venue = venuesList[position]
        holder.bind(venue)

        holder.itemView.setOnClickListener {
            onItemClick.invoke(venue)
        }
    }

    override fun getItemCount(): Int {
        return venuesList.size
    }

    fun setVenues(venues: List<Venue>) {
        venuesList.clear()
        venuesList.addAll(venues)
        notifyDataSetChanged()
    }
}
