package com.ihubin.webrtc.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ihubin.webrtc.R
import com.ihubin.webrtc.databinding.RvItemOnlineUserBinding
import com.ihubin.webrtc.util.SPUtils

class OnlineUserAdapter(onlineUserList: List<String>, onContact: View.OnClickListener) : RecyclerView.Adapter<OnlineUserAdapter.ViewHolder>() {

    private val userList = onlineUserList
    private val onContactClick = onContact

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = RvItemOnlineUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
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

    class ViewHolder(binding: RvItemOnlineUserBinding) : RecyclerView.ViewHolder(binding.root) {
        var userName: TextView = binding.userName
        var contact: Button = binding.contact
    }

}