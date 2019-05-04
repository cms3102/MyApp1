package com.csergio.myapp1.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.csergio.myapp1.R
import com.csergio.myapp1.chat.ChatRoomActivity
import com.csergio.myapp1.util.RetrofitBuilder
import com.csergio.myapp1.util.SQLiteHelper
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.item_chatroom.view.*

class ChatFragment:Fragment() {

    val chatRoomList = mutableListOf<com.csergio.myapp1.model.Message>()
    lateinit var sqliteHelper:SQLiteHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        sqliteHelper = SQLiteHelper(context!!)

        val cursor = sqliteHelper.loadChatRoomsFromDB()
        while (cursor.moveToNext()){
            val message = com.csergio.myapp1.model.Message()
            message.chatroom_id = cursor.getString(0)
            message.sender_name = cursor.getString(1)
            message.content = cursor.getString(2)
            chatRoomList.add(message)
        }

        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        chatFragment_recyclerView.layoutManager = LinearLayoutManager(context)
        chatFragment_recyclerView.adapter = ChatFragmentAdapter()
    }

    inner class ChatFragmentViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val profileImageView = itemView.item_chatroom_profileImageView
        val nameTextView = itemView.item_chatroom_nameTextView
        val contentTextView = itemView.item_chatroom_contentTextView
        val timestampTextView = itemView.item_chatroom_timestampTextView
    }

    inner class ChatFragmentAdapter:RecyclerView.Adapter<ChatFragmentViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatFragmentViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chatroom, parent, false)
            return ChatFragmentViewHolder(view)
        }

        override fun getItemCount(): Int {
            return chatRoomList.size
        }

        override fun onBindViewHolder(holder: ChatFragmentViewHolder, position: Int) {
            val room = chatRoomList[position]
            holder.nameTextView.text = room.sender_name
            holder.contentTextView.text = room.content

            holder.itemView.setOnClickListener {
                val intent = Intent(context, ChatRoomActivity::class.java)
                intent.putExtra("chatRoomId", room.chatroom_id)
                startActivity(intent)
            }
        }

    }

}