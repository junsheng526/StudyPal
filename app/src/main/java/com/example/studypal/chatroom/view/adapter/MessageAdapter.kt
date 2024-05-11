package com.example.studypal.chatroom.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studypal.R
import com.example.studypal.chatroom.model.Message

class MessageAdapter(private val currentUserId: String) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private var messagesList = mutableListOf<Message>()

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageContentSend: TextView = itemView.findViewById(R.id.tvMessageContentSend)
        val messageContentReceived: TextView = itemView.findViewById(R.id.tvMessageContentReceived)

        fun bind(message: Message) {
            if (message.senderId == currentUserId) {
                // Show sent message view
                messageContentSend.text = message.content
                messageContentSend.visibility = View.VISIBLE
                messageContentReceived.visibility = View.GONE
            } else {
                // Show received message view
                messageContentReceived.text = message.content
                messageContentReceived.visibility = View.VISIBLE
                messageContentSend.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messagesList[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

    fun setMessages(messages: List<Message>) {
        messagesList = messages.toMutableList()
        notifyDataSetChanged()
    }
}