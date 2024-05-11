package com.example.studypal.findbuddy.view.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.studypal.R
import com.example.studypal.databinding.FragmentFindBuddyBinding

class FindBuddyFragment : Fragment() {

    private lateinit var binding: FragmentFindBuddyBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFindBuddyBinding.inflate(inflater, container, false)

        return binding.root
    }
}