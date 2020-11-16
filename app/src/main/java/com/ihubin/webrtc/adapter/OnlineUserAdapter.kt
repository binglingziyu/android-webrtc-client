package com.ihubin.webrtc.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ihubin.webrtc.R
import com.ihubin.webrtc.util.SPUtils

class OnlineUserAdapter(onlineUserList: List<String>, onContact: View.OnClickListener) : RecyclerView.Adapter<OnlineUserAdapter.ViewHolder>() {

    private val userList = onlineUserList
    private val onContactClick = onContact

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val root = LayoutInflater.from(parent.context).inflate(R.layout.rv_item_online_user, parent, false)
        return ViewHolder(root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val loginUserName = SPUtils.get(holder.itemView.context, "login", "") as String
        val userName = userList[position]
        holder.userName.text = userName
        if(loginUserName == userName) {
            holder.contact.visibility = View.GONE
        } else {
            holder.contact.visibility = View.VISIBLE
            holder.contact.tag = userName
            holder.contact.setOnClickListener(onContactClick)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userName: TextView = itemView.findViewById(R.id.user_name)
        var contact: Button = itemView.findViewById(R.id.contact)
    }

}