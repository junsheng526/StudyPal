package com.example.studypal.chatroom.view.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studypal.chatroom.model.Message
import com.example.studypal.chatroom.view.adapter.MessageAdapter
import com.example.studypal.chatroom.viewmodel.ChatViewModel
import com.example.studypal.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Set up RecyclerView with MessageAdapter
        messageAdapter = currentUser?.let { MessageAdapter(it.uid) }!!
        binding.rvMessages.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(this@ChatActivity)
        }

        // Get friendId from intent
        val friendId = intent.getStringExtra("friendId") ?: ""
        val friendName = intent.getStringExtra("friendName") ?: ""

        binding.tvUsername.text = friendName

        // Observe messages LiveData from ViewModel
        chatViewModel.getMessagesWithFriend(friendId).observe(this, Observer { messages ->
            messageAdapter.setMessages(messages)
            // Scroll to the bottom of the RecyclerView when new messages are added
            binding.rvMessages.scrollToPosition(messageAdapter.itemCount - 1)
        })

        // Send button click listener
        binding.btnSend.setOnClickListener {
            val messageContent = binding.etMessage.text.toString().trim()
            if (messageContent.isNotEmpty()) {
                // Send message using ViewModel
                chatViewModel.sendMessage(friendId, messageContent)
                // Clear message input field after sending
                binding.etMessage.text.clear()
            }
        }

        // Back button click listener
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }
}