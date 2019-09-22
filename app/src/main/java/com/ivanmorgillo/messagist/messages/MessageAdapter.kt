package com.ivanmorgillo.messagist.messages

import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.ivanmorgillo.messagist.R
import com.ivanmorgillo.messagist.helpers.exhaustive
import com.ivanmorgillo.messagist.messages.ChatMessage.FriendMessage
import com.ivanmorgillo.messagist.messages.ChatMessage.MyChatMessage
import com.ivanmorgillo.messagist.messages.MessageAdapter.ChatMessageViewHolder.*
import kotlinx.android.synthetic.main.friend_message_attachment_item.view.*
import kotlinx.android.synthetic.main.friend_message_item.view.*
import kotlinx.android.synthetic.main.my_message_attachment_item.view.*
import kotlinx.android.synthetic.main.my_message_item.view.*

class MessageAdapter(
    private val bottomReached: () -> Unit,
    private val onClick: (ChatMessage) -> Unit
) : RecyclerView.Adapter<MessageAdapter.ChatMessageViewHolder>() {

    private val items: MutableList<ChatMessage> = mutableListOf()

    fun setItems(items: List<ChatMessage>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long = items[position].id

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is FriendMessage.TextMessage -> 2
            is FriendMessage.Attachment -> 3
            is MyChatMessage.TextMessage -> 5
            is MyChatMessage.Attachment -> 6
        }.exhaustive
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            2 -> {
                val view = layoutInflater.inflate(R.layout.friend_message_item, parent, false)
                FriendMessageTextMessageVH(view)
            }
            3 -> {
                val view = layoutInflater.inflate(R.layout.friend_message_attachment_item, parent, false)
                FriendMessageAttachmentVH(view)
            }
            5 -> {
                val view = layoutInflater.inflate(R.layout.my_message_item, parent, false)
                MyMessageTextMessageVH(view)
            }
            6 -> {
                val view = layoutInflater.inflate(R.layout.my_message_attachment_item, parent, false)
                MyMessageAttachmentVH(view)
            }
            else -> {
                UnknownTypeVH(View(parent.context))
            }
        }.exhaustive
    }

    override fun onBindViewHolder(holder: ChatMessageViewHolder, position: Int) {
        val item: ChatMessage = items[position]
        holder.bind(item, onClick)

        if (position == items.size - 1) bottomReached()
    }

    sealed class ChatMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: ChatMessage, onClick: (ChatMessage) -> Unit)

        class FriendMessageTextMessageVH(view: View) : ChatMessageViewHolder(view) {
            override fun bind(item: ChatMessage, onClick: (ChatMessage) -> Unit) {
                item as FriendMessage.TextMessage
                itemView.friend_message_text.text = item.content

                if (item.header) {
                    itemView.friend_avatar.visibility = VISIBLE
                    itemView.friend_avatar.load(item.avatarUrl)

                    itemView.friend_message_header_text.visibility = VISIBLE
                    itemView.friend_message_header_text.text = item.name
                }
            }
        }

        class FriendMessageAttachmentVH(view: View) : ChatMessageViewHolder(view) {
            override fun bind(item: ChatMessage, onClick: (ChatMessage) -> Unit) {
                item as FriendMessage.Attachment
                itemView.friend_message_attachment_label.text = item.title
                itemView.friend_message_attachment_image.load(item.thumbnail)
                itemView.friend_message_attachment_text.text = item.content

                itemView.setOnClickListener { onClick(item) }

                if (item.header) {
                    itemView.friend_message_attachment_avatar.visibility = VISIBLE
                    itemView.friend_message_attachment_avatar.load(item.avatarUrl)

                    itemView.friend_message_attachment_header_text.visibility = VISIBLE
                    itemView.friend_message_attachment_header_text.text = item.name
                }
            }
        }

        class MyMessageTextMessageVH(view: View) : ChatMessageViewHolder(view) {
            override fun bind(item: ChatMessage, onClick: (ChatMessage) -> Unit) {
                item as MyChatMessage.TextMessage
                itemView.my_message_text.text = item.content

                if (item.header) {
                    itemView.my_message_header_text.visibility = VISIBLE
                }
            }
        }

        class MyMessageAttachmentVH(view: View) : ChatMessageViewHolder(view) {
            override fun bind(item: ChatMessage, onClick: (ChatMessage) -> Unit) {
                item as MyChatMessage.Attachment
                itemView.my_message_attachment_label.text = item.title
                itemView.my_message_attachment_text.text = item.content
                itemView.setOnClickListener { onClick(item) }

                if (item.header) {
                    itemView.my_message_attachment_header_text.visibility = VISIBLE
                    itemView.my_message_attachment_image.load(item.thumbnail)
                }
            }
        }

        class UnknownTypeVH(view: View) : ChatMessageViewHolder(view) {
            override fun bind(item: ChatMessage, onClick: (ChatMessage) -> Unit) {}
        }
    }
}

private fun AppCompatImageView.load(url: String) {
    val circularProgressDrawable = CircularProgressDrawable(this.context)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.start()

    Glide.with(context)
        .load(url)
        .placeholder(circularProgressDrawable)
        .circleCrop()
        .into(this)
}
