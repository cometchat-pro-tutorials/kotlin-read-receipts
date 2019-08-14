package com.vucko.cometchatdemo

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.MessagesRequest
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.*
import com.vucko.cometchatdemo.fragments.MessageInfoDialogFragment
import com.vucko.cometchatdemo.interfaces.OnMessageClickListener

class GroupActivity : AppCompatActivity() {

    val TAG = "GroupActivity"
    lateinit var messagesRecyclerView: RecyclerView
    lateinit var messageEditText: EditText
    lateinit var sendButton: ImageButton
    lateinit var noMessagesGroup: androidx.constraintlayout.widget.Group
    var group: Group? = null

    lateinit var messagesAdapter: MessagesAdapter


    // Some random ID for the listener for now
    private val incomingMessageListenerId: String = "1234"
    private val messageReadReceiptListenerId: String = "999"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        sendButton.setOnClickListener {
            attemptSendMessage()
        }

        getGroupDetailsAndMessages()

        val listener = object : OnMessageClickListener {
            override fun onMessageStatusLongClick(item: TextMessage?) {
                val fragmentManager = supportFragmentManager.beginTransaction()
                CometChat.getMessageReceipts(item!!.id, object : CometChat.CallbackListener<List<MessageReceipt>>() {
                    override fun onSuccess(messageReceipts: List<MessageReceipt>) {
                        val names = ArrayList<String>()
                        messageReceipts.forEach {
                            names.add(it.sender.name)
                        }
                        Log.d(TAG, "onSuccess: ${messageReceipts.size}")
                        val newFragment = MessageInfoDialogFragment.newInstance(names)
                        newFragment.show(fragmentManager, "dialog")
                    }

                    override fun onError(e: CometChatException) {
                        Log.d(TAG, "onError: ${e.message} ")
                    }
                })

            }
        }
        messagesAdapter = MessagesAdapter(ArrayList(), this, listener)
        messagesRecyclerView.adapter = messagesAdapter
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        noMessagesGroup = findViewById(R.id.noMessagesGroup)
        messageEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                attemptSendMessage()
                true
            } else {
                false
            }
        }
        registerMessageCallbacks()
    }


    private fun getGroupDetailsAndMessages() {
        // Get the details of the group, such as name, members and other data that may be used in the app later
        // For now only the group name is used

        val groupId = intent.getStringExtra("group_id")
        CometChat.getGroup(groupId, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(p0: Group?) {
                group = p0
                updateUI()
            }

            override fun onError(p0: CometChatException?) {

            }
        })
        val messagesRequest = MessagesRequest.MessagesRequestBuilder().setGUID(groupId).build()
        messagesRequest.fetchPrevious(object : CometChat.CallbackListener<List<BaseMessage>>() {
            override fun onSuccess(p0: List<BaseMessage>?) {
                if (!p0.isNullOrEmpty()) {
                    for (baseMessage in p0) {
                        if (baseMessage is TextMessage) {
                            addMessage(baseMessage)
                            markMessageAsReadIfNeeded(baseMessage)
                        }
                    }
                }

            }

            override fun onError(p0: CometChatException?) {
                Toast.makeText(this@GroupActivity, p0?.message, Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun registerMessageCallbacks() {
        CometChat.addMessageListener(messageReadReceiptListenerId, object : CometChat.MessageListener() {
            override fun onMessageDelivered(messageReceipt: MessageReceipt?) {
                notifyMessageInfoChanged(messageReceipt, MessageInfo.DELIVERED)
            }

            override fun onMessageRead(messageReceipt: MessageReceipt?) {
                notifyMessageInfoChanged(messageReceipt, MessageInfo.READ)
            }
        })
    }

    private fun notifyMessageInfoChanged(messageReceipt: MessageReceipt?, status: MessageInfo) {
        messagesAdapter.notifyMessageChanged(messageReceipt, status)
    }

    private fun markMessageAsReadIfNeeded(message: TextMessage?) {
        if (message?.readAt == 0L && !message.sender.uid.equals(CometChat.getLoggedInUser().uid, ignoreCase = true)) {
            CometChat.markMessageAsRead(message)
        }
    }

    private fun updateUI() {
        supportActionBar?.title = group?.name
    }

    private fun attemptSendMessage() {
        // Attempts to send the message to the group by current user
        val text = messageEditText.text.toString()
        if (!TextUtils.isEmpty(text)) {
            messageEditText.text.clear()
            val receiverID: String = group!!.guid
            val messageType: String = CometChatConstants.MESSAGE_TYPE_TEXT
            val receiverType: String = CometChatConstants.RECEIVER_TYPE_GROUP

            val textMessage = TextMessage(receiverID, text, messageType, receiverType)

            CometChat.sendMessage(textMessage, object : CometChat.CallbackListener<TextMessage>() {
                override fun onSuccess(p0: TextMessage?) {
                    addMessage(p0)
                }

                override fun onError(p0: CometChatException?) {

                }

            })
        }

    }

    private fun addMessage(message: TextMessage?) {
        noMessagesGroup.visibility = View.GONE
        messagesAdapter.addMessage(message)
        scrollToBottom()
    }

    private fun scrollToBottom() {
        messagesRecyclerView.smoothScrollToPosition(messagesAdapter.itemCount - 1)
    }

    override fun onResume() {
        super.onResume()
        // Add the listener to listen for incoming messages in this screen
        CometChat.addMessageListener(incomingMessageListenerId, object : CometChat.MessageListener() {

            override fun onTextMessageReceived(message: TextMessage?) {
                addMessage(message)
                markMessageAsReadIfNeeded(message)
            }

            override fun onMediaMessageReceived(message: MediaMessage?) {

            }

        })
    }

    override fun onPause() {
        super.onPause()
        CometChat.removeMessageListener(incomingMessageListenerId)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
