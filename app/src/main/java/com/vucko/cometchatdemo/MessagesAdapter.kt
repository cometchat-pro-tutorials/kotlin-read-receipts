package com.vucko.cometchatdemo

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.MessageReceipt
import com.cometchat.pro.models.TextMessage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.my_message_layout.view.*


class MessagesAdapter(var messages: MutableList<TextMessage?>, val context: Context) :
    RecyclerView.Adapter<MessageViewHolder>() {
    val TAG = "MessagesAdapter"
    val currentUserId = CometChat.getLoggedInUser()?.uid
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        if (viewType == GeneralConstants.MY_MESSAGE) {
            return MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.my_message_layout, parent, false))
        }
        return MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.others_message_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        if (isCurrentUserMessage(messages[position])) {
            return GeneralConstants.MY_MESSAGE
        }
        return GeneralConstants.OTHERS_MESSAGE
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.messageTextView.text = messages[position]?.text
        // Check if the sender is the current user
        Glide.with(context).load(GeneralConstants.AVATARS_URL + messages[position]?.sender?.name)
            .into(holder.avatarImageView)
        if (isCurrentUserMessage(messages[position])) {
            messages[position]?.id?.let {
                CometChat.getMessageReceipts(it, object : CometChat.CallbackListener<List<MessageReceipt>>() {
                    override fun onSuccess(messageReceipts: List<MessageReceipt>) {
                        Log.d(TAG, "onSuccess: ${messageReceipts.size}")
                        holder.messageStatusImageView.isVisible
                        if (messageReceipts.isEmpty()) {
                            holder.messageStatusImageView.setImageResource(R.drawable.sent_tick)
                        } else if (messageReceipts[0]?.receiptType == MessageReceipt.RECEIPT_TYPE_DELIVERED) {
                            holder.messageStatusImageView.setImageResource(R.drawable.delivered_double_tick)
                        } else {
                            holder.messageStatusImageView.setImageResource(R.drawable.read_double_tick)
                        }
                    }

                    override fun onError(e: CometChatException) {
                        Log.d(TAG, "onError: ${e.message} ")
                    }
                })
            }
        }
    }

    private fun isCurrentUserMessage(message: TextMessage?): Boolean {
        return currentUserId!! == message?.sender?.uid
    }

    fun addMessage(message: TextMessage?) {
        messages.add(message)
        notifyDataSetChanged()
    }

    fun notifyMessageChanged(messageReceipt: MessageReceipt?, status: MessageInfo) {
        if (status == MessageInfo.DELIVERED) {
            messages.find { message -> messageReceipt?.messageId == message?.id }?.deliveredAt =
                messageReceipt?.deliveredAt!!
        } else if (status == MessageInfo.READ) {
            messages.find { message -> messageReceipt?.messageId == message?.id }?.readAt = messageReceipt?.readAt!!
        }
        notifyDataSetChanged()
    }
}


class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val messageTextView: TextView = itemView.messageTextView
    val avatarImageView: CircleImageView = itemView.avatarImageView
    val messageStatusImageView: ImageView = itemView.messageStatusImageView
}