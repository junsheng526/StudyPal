package com.example.studypal.findbuddy.view.ui

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.studypal.R
import com.example.studypal.databinding.FragmentFindBuddyBinding
import com.example.studypal.utility.toBitmap
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FindBuddyFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var binding: FragmentFindBuddyBinding
    private lateinit var mapFragment: SupportMapFragment
    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFindBuddyBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun addCustomMarker() {
        // Ensure googleMap is not null
        googleMap?.let { map ->
            // Get user's last known location
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request location permission
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
                return
            }
            // Add a marker for the user's current location
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val userLocation = LatLng(it.latitude, it.longitude)
                        // Add marker for current user's location
                        map.addMarker(
                            MarkerOptions()
                                .position(userLocation)
                                .title("Your Location")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        )
                        // Move camera to current user's location
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                    }
                }
        }
    }

    private fun enableMyLocation() {
        // Check and request location permission if not granted
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }

        // Enable showing user's location on the map
        googleMap?.isMyLocationEnabled = true
    }

    private fun addMarkersForUsers() {
        db.collection("location")
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Retrieve current user's UID
                val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

                // Iterate through each document (user location) in the collection
                for (document in querySnapshot.documents) {
                    // Get latitude and longitude from the document data
                    val latitude = document.getDouble("latitude") ?: 0.0
                    val longitude = document.getDouble("longitude") ?: 0.0
                    // Create LatLng object for the user's location
                    val userLatLng = LatLng(latitude, longitude)

                    // Get userId from the document
                    val userId = document.id

                    // Check if the userId is the same as the current user's UID
                    if (currentUserUid != userId) {
                        // Retrieve user data based on userId from the 'user' collection
                        db.collection("user").document(userId)
                            .get()
                            .addOnSuccessListener { userDocument ->
                                if (userDocument != null && userDocument.exists()) {
                                    // Get user's name from the 'user' document
                                    val userName = userDocument.getString("name") ?: "Unknown"

                                    // Add marker for the user's location on the map with the user's name
                                    googleMap?.addMarker(
                                        MarkerOptions()
                                            .position(userLatLng)
                                            .title(userDocument.id)
                                            .snippet("$userName's Location")
                                    )
                                } else {
                                    // User document does not exist or is null
                                    // Handle this case if needed
                                }
                            }
                            .addOnFailureListener { exception ->
                                // Handle failure to retrieve user data from 'user' collection
                                // You can log the error or display a message to the user
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure to retrieve user locations from Firestore
                // You can log the error or display a message to the user
            }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val userName = marker.title
        // Show an alert dialog with user details
        if (userName != null) {
            showUserDetailsDialog(userName)
        }
        return true
    }

    private fun showUserDetailsDialog(userId: String) {
        // Inflate the dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_user_details, null)

        // Initialize dialog components
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setTitle("Find your study buddy") // Set dialog title to userId
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                addFriend(userId)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }

        val userRef = db.collection("user").document(userId)
        userRef.get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.exists()) {
                    // User document exists, retrieve user details
                    val userName = userDocument.getString("name") ?: "Unknown"
                    val userInterests = userDocument.getString("interest") ?: ""
                    val userLearningStyle = userDocument.getString("learningStyle") ?: ""
                    val userStudyField = userDocument.getString("studyField") ?: ""
                    val photo = userDocument.getBlob("photo") ?: null

                    // Set user details in the dialog views
                    dialogView.findViewById<TextView>(R.id.tvName).text = "Name: $userName"
                    dialogView.findViewById<TextView>(R.id.tvInterests).text = "Interests: $userInterests"
                    dialogView.findViewById<TextView>(R.id.tvLearningStyle).text = "Learning Style: $userLearningStyle"
                    dialogView.findViewById<TextView>(R.id.tvStudyField).text = "Study Field: $userStudyField"

                    val ivPhoto = dialogView.findViewById<ImageView>(R.id.ivPhoto)
                    ivPhoto.setImageBitmap(photo?.toBitmap())

                    // Display the dialog
                    val dialog = dialogBuilder.create()
                    dialog.show()
                } else {
                    // User document doesn't exist
                    Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure to retrieve user document
                Toast.makeText(requireContext(), "Failed to retrieve user details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addFriend(userId: String) {
        // Get the current user's ID
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserUid != null) {
            // Reference to the current user's document
            val currentUserRef = db.collection("user").document(currentUserUid)

            // Check if the user is already a friend
            currentUserRef.get()
                .addOnSuccessListener { userDocument ->
                    val friends = userDocument.get("friends") as? List<String>

                    if (friends != null && friends.contains(userId)) {
                        // User is already a friend, show a message or perform an action
                        Toast.makeText(requireContext(), "User is already your friend", Toast.LENGTH_SHORT).show()
                    } else {
                        // Add the user to the friends list
                        db.runTransaction { transaction ->
                            val userSnapshot = transaction.get(currentUserRef)

                            val existingFriends = userSnapshot.get("friends") as? MutableList<String> ?: mutableListOf()
                            existingFriends.add(userId)

                            transaction.update(currentUserRef, "friends", existingFriends)
                        }
                            .addOnSuccessListener {
                                // Friend added successfully
                                Toast.makeText(requireContext(), "User added as friend", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                // Failed to add friend
                                Toast.makeText(requireContext(), "Failed to add friend", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    // Failed to retrieve current user's data
                    Toast.makeText(requireContext(), "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                }
        } else {
            // User not authenticated
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        // Add custom marker for user's location
        addCustomMarker()
        // Request location permission and show user's location
        enableMyLocation()
        // Add markers for all users
        addMarkersForUsers()
        // Set marker click listener
        googleMap?.setOnMarkerClickListener(this)
    }
}