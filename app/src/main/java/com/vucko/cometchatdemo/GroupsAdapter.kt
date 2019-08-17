package com.vucko.cometchatdemo

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.Group
import kotlinx.android.synthetic.main.group_layout.view.*

class GroupsAdapter(private val groups: List<Group>?, val context: Context) : RecyclerView.Adapter<GroupViewHolder>() {
    val TAG = "GroupsAdapterø"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        return GroupViewHolder(LayoutInflater.from(context).inflate(R.layout.group_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return groups!!.size
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.groupNameTextView.text = groups!![position].name
        holder.descriptionTextView.text = groups[position].description

        val group = groups[position]
        // Depending on whether the group is joined or not, display "JOINED" text or not
        if (group.isJoined) {
            holder.joinedTextView.visibility = View.VISIBLE
        } else {
            holder.joinedTextView.visibility = View.INVISIBLE
        }
        // And if it's joined, go to that group details
        // Otherwise try to join the group. This should always succeed though, because we're using PUBLIC groups only at this point
        holder.container.setOnClickListener {
            if (group.isJoined) {
                goToGroupScreen(group)
            } else {
                attemptJoinGroup(group)
            }
        }
        CometChat.getUnreadMessageCountForGroup(
            group.guid,
            object : CometChat.CallbackListener<HashMap<String, Int>>() {
                override fun onSuccess(stringIntegerHashMap: HashMap<String, Int>) {
                    if (stringIntegerHashMap[group.guid] != null) {
                        holder.unreadMessagesTextView.text = stringIntegerHashMap[group.guid].toString()
                        holder.unreadMessagesTextView.visibility = View.VISIBLE
                    }
                    Log.d(TAG, "onSuccess: ${stringIntegerHashMap.size}")
                }

                override fun onError(e: CometChatException) {
                    Log.d(TAG, "onError: ${e.message}")
                }
            })
    }

    private fun attemptJoinGroup(group: Group) {
        // Try to join the group
        CometChat.joinGroup(group.guid, group.groupType, group.password, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(p0: Group?) {
                updateJoinedStatus(group)
            }

            override fun onError(p0: CometChatException?) {
                Toast.makeText(context, p0?.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateJoinedStatus(group: Group) {
        groups!!.forEach {
            // If successful, in order the show "JOINED" on that group, go through all of them, find the one we just joined,
            // And set Joined = true
            if (it.guid == group.guid) {
                it.setHasJoined(true)
                notifyDataSetChanged()
            }
        }
    }

    private fun goToGroupScreen(group: Group) {
        val intent = Intent(context, GroupActivity::class.java)
        intent.putExtra("group_id", group.guid)
        context.startActivity(intent)
    }

}

class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val groupNameTextView: TextView = itemView.groupNameTextView
    val descriptionTextView: TextView = itemView.descriptionTextView
    val container: ConstraintLayout = itemView.container
    val joinedTextView: TextView = itemView.joinedTextView
    val unreadMessagesTextView: TextView = itemView.unreadMessagesTextView
}