package com.vucko.cometchatdemo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.cometchat.pro.models.MessageReceipt
import com.vucko.cometchatdemo.R


class MessageInfoDialogFragment : DialogFragment() {

    private var namesDelivered = ArrayList<String>()
    private var namesRead = ArrayList<String>()
    private val namesNull = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        namesDelivered = arguments?.getStringArrayList("namesDelivered") ?: namesNull
        namesRead = arguments?.getStringArrayList("namesRead") ?: namesNull

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_message_info_dialog, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val closeDialogButton = view.findViewById<View>(R.id.closeDialogButton) as Button
        val readByTextView = view.findViewById<View>(R.id.readByTextView) as TextView
        val deliveredTextView = view.findViewById<View>(R.id.deliveredTextView) as TextView
        readByTextView.text = makeDialogText(
            getString(R.string.this_message_was_read_by),
            getString(R.string.no_one_has_read_this_message), namesRead
        )

        val noOneString: String = if (namesRead.size == 0) {
            getString(R.string.no_one_has_received_this_message)
        } else {
            getString(R.string.no_one_else_has_received_this_message)
        }

        deliveredTextView.text = makeDialogText(
            getString(R.string.this_message_was_delivered_to),
            noOneString, namesDelivered
        )
        closeDialogButton.setOnClickListener {
            dismiss()
        }
    }

    private fun makeDialogText(startString: String, noOneString: String, names: ArrayList<String>): String {
        var dialogBody: String
        if (names.size != 0) {
            dialogBody = startString
            for (i in 0..(names.size - 1)) {
                dialogBody = when (i) {
                    names.size - 2 -> "$dialogBody ${names[i]} and"
                    names.size - 1 -> "$dialogBody ${names[i]}."
                    else -> "$dialogBody ${names[i]}, "
                }
            }
        } else {
            dialogBody = noOneString
        }
        return dialogBody
    }

    companion object {
        fun newInstance(messageReceipts: List<MessageReceipt>): MessageInfoDialogFragment {
            val fragment = MessageInfoDialogFragment()
            val args = Bundle()

            val namesDelivered = ArrayList<String>()
            val namesRead = ArrayList<String>()

            messageReceipts.forEach {
                if (it.receiptType == MessageReceipt.RECEIPT_TYPE_READ) {
                    namesRead.add(it.sender.name)
                }
                if (it.receiptType == MessageReceipt.RECEIPT_TYPE_DELIVERED) {
                    namesDelivered.add(it.sender.name)
                }
            }

            args.putStringArrayList("namesDelivered", namesDelivered)
            args.putStringArrayList("namesRead", namesRead)
            fragment.arguments = args
            return fragment
        }
    }
}
