package com.csergio.myapp1.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.csergio.myapp1.R
import com.csergio.myapp1.chat.ChatRoomActivity
import com.csergio.myapp1.util.SQLiteHelper
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.item_chatroom.view.*

/**
 * 채팅방 목록 프래그먼트
 * */
class ChatFragment:Fragment() {

    val chatRoomList = mutableListOf<com.csergio.myapp1.model.Message>()
    private lateinit var sqliteHelper:SQLiteHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        sqliteHelper = SQLiteHelper(context!!)

        companionHandler = handler
        state = true

        // DB에서 채팅방 목록 불러오기
        loadRooms()

        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        refreshList()
    }

    override fun onResume() {
        Log.d("ChatFragment","에서 onResume 실행됨")
        loadRooms()
        refreshList()
        super.onResume()
    }

    private fun loadRooms(){
        Log.d("ChatFragment","ChatFragment에서 loadRooms 실행됨")
        chatRoomList.clear()
        val cursor = sqliteHelper.loadChatRoomsFromDB()
        while (cursor.moveToNext()){
            val message = com.csergio.myapp1.model.Message()
            message.chatroom_id = cursor.getString(0)
            message.sender_name = cursor.getString(1)
            message.content = cursor.getString(2)
            chatRoomList.add(message)
        }
    }

    private fun refreshList(){
        Log.d("ChatFragment","ChatFragment에서 refreshList 실행됨")
        chatFragment_recyclerView.layoutManager = LinearLayoutManager(context)
        chatFragment_recyclerView.adapter = ChatFragmentAdapter()
    }

    // 서비스에서 대화방 목록 갱신이 가능하게 하기 위한 객체
    companion object {

        var state = false
        private lateinit var companionHandler:Handler

        fun refreshChatRoomList(){
            val msg = companionHandler.obtainMessage()
            companionHandler.handleMessage(msg)
        }

    }

    // UI 스레드에서 갱신 작업을 하기 위한 핸들러
    val handler = object : Handler(){
        override fun handleMessage(msg: Message?) {
            activity?.runOnUiThread {
                loadRooms()
                refreshList()
            }
        }
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

    override fun onDestroy() {
        state = false
        super.onDestroy()
    }

}