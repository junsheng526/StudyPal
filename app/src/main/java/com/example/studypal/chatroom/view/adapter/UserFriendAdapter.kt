package com.example.studypal.chatroom.view.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studypal.chatroom.model.Friend
import com.example.studypal.chatroom.view.ui.ChatActivity
import com.example.studypal.databinding.ItemPersonBinding
import com.example.studypal.utility.toBitmap
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UserFriendAdapter(
    private val context: Context,
    private val fn: (ViewHolder, Friend) -> Unit = { _, _ -> }
) : ListAdapter<Friend, UserFriendAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Friend>() {
        override fun areItemsTheSame(a: Friend, b: Friend) = a.id == b.id
        override fun areContentsTheSame(a: Friend, b: Friend) = a == b
    }

    class ViewHolder(val binding: ItemPersonBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPersonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = getItem(position)

        holder.binding.tvUsername.text = friend.name ?: ""
        holder.binding.imgProfile.setImageBitmap(friend.photo?.toBitmap())
        val formattedTime = formatTimestamp(friend.lastMessageTimestamp)
        holder.binding.tvLastMsgTime.text = formattedTime

        fn(holder, friend)

        // Handle item click to start ChatActivity
        holder.binding.root.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java).apply {
                putExtra("friendId", friend.id)
                putExtra("friendName", friend.name)
            }
            context.startActivity(intent)
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        if (timestamp == 0L) return ""

        val currentTime = System.currentTimeMillis()
        val date = Date(timestamp)
        val calendar = Calendar.getInstance()
        calendar.time = date

        val todayCalendar = Calendar.getInstance()
        todayCalendar.timeInMillis = currentTime

        return if (calendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)) {
            // If the timestamp is from today, return the time format
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            timeFormat.format(date)
        } else {
            // Otherwise, return the date format
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            dateFormat.format(date)
        }
    }
}