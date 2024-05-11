package com.example.studypal.bookroom.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.studypal.R
import com.example.studypal.bookroom.view.adapter.VenueAdapter
import com.example.studypal.bookroom.viewmodel.BookRoomViewModel
import com.example.studypal.databinding.FragmentBookRoomBinding

class BookRoomFragment : Fragment() {

    private lateinit var binding: FragmentBookRoomBinding
    private val bookRoomViewModel: BookRoomViewModel by activityViewModels()
    private val nav by lazy { findNavController() }
    private lateinit var venueAdapter: VenueAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBookRoomBinding.inflate(inflater, container, false)

        venueAdapter = VenueAdapter { venue ->
            val action = BookRoomFragmentDirections.actionNavigationBookRoomToBookingDetailsFragment(venue)
            nav.navigate(action)
        }

        binding.rvVenueList.adapter = venueAdapter
        binding.rvVenueList.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        bookRoomViewModel.getVenues().observe(viewLifecycleOwner, Observer { venues ->
            venueAdapter.setVenues(venues)
        })

        return(binding.root)
    }
}