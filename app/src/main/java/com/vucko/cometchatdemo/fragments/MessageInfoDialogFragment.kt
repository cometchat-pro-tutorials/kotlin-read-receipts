package com.vucko.cometchatdemo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.vucko.cometchatdemo.R


class MessageInfoDialogFragment : DialogFragment() {

    private var names = ArrayList<String>()
    private val namesNull = ArrayList<String>()

    init {
        this.names = arguments?.getStringArrayList("names") ?: namesNull
        val style = DialogFragment.STYLE_NO_FRAME
        val theme = R.style.DialogTheme
        setStyle(style, theme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_message_info_dialog, container, false)
        val closeDialogButton = view.findViewById<View>(R.id.closeDialogButton) as Button
        val dialogBodyTextView = view.findViewById<View>(R.id.dialogBodyTextView) as TextView
        dialogBodyTextView.text = makeDialogBody()

        closeDialogButton.setOnClickListener {
            dismiss()
        }
        return view
    }

    private fun makeDialogBody(): String {
        val dialogBody :String
        if (names.size != 0) {
            dialogBody = getString(R.string.this_message_was_read_by)
            for (i in 0..(names.size - 1)) {
                when (i) {
                    names.size - 2 -> dialogBody.plus(names[i]).plus(" and ")
                    names.size - 1 -> dialogBody.plus(names[i]).plus(".")
                    else -> dialogBody.plus(names[i]).plus(", ")
                }
            }
        } else {
            dialogBody = getString(R.string.no_one_has_read_this_message)
        }
        return dialogBody
    }

    companion object {
        fun newInstance(names: ArrayList<String>): MessageInfoDialogFragment {
            val fragment = MessageInfoDialogFragment()
            val args = Bundle()
            args.putStringArrayList("names", names)
            fragment.arguments = args
            return fragment
        }
    }
}
