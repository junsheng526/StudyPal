package com.example.studypal.profile.view.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.studypal.R
import com.example.studypal.authentication.model.User
import com.example.studypal.databinding.ActivityMainBinding
import com.example.studypal.databinding.ActivityProfileDetailsBinding
import com.example.studypal.profile.viewmodel.ProfileViewModel
import com.example.studypal.utility.cropToBlob
import com.example.studypal.utility.toBitmap
import kotlinx.coroutines.launch

class ProfileDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileDetailsBinding
    private val vm: ProfileViewModel by viewModels()

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            binding.imgProfile.setImageURI(it.data?.data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cancelBtn.setOnClickListener {
            onBackPressed()
        }

        binding.imgProfileBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            launcher.launch(intent)
        }

        binding.saveBtn.setOnClickListener {
            submit()
        }

        val userId = intent.getStringExtra("userId")
        if (!userId.isNullOrBlank()) {
            lifecycleScope.launch {
                val user = vm.get(userId)
                user?.let { populateUserData(it) }
            }
        }
    }

    private fun submit() {
        val user = User(
            name = binding.editTextName.text.toString().trim(),
            studyField = binding.editTextStudyField.text.toString().trim(),
            learningStyle = binding.editTextLearningStyle.text.toString().trim(),
            interest = binding.editTextInterest.text.toString().trim(),
            photo = binding.imgProfile.cropToBlob(300,300),
        )

        lifecycleScope.launch {
            val err = vm.validate(user)
            if (err != "") {
                AlertDialog.Builder(this@ProfileDetailsActivity)
                    .setIcon(R.drawable.ic_error)
                    .setTitle("Error")
                    .setMessage(err)
                    .setPositiveButton("Dismiss", null)
                    .show()
                return@launch
            }
            vm.set(user)
            onBackPressed()
        }
    }

    private fun populateUserData(user: User) {
        with(binding) {
            editTextName.setText(user.name)
            editTextStudyField.setText(user.studyField)
            editTextLearningStyle.setText(user.learningStyle)
            editTextInterest.setText(user.interest)
            binding.imgProfile.setImageBitmap(user.photo?.toBitmap())
        }
    }
}