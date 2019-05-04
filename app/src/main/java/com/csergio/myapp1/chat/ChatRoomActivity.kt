package com.csergio.myapp1.chat

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.csergio.myapp1.R
import com.csergio.myapp1.util.SQLiteHelper
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import kotlinx.android.synthetic.main.activity_chat_room.*
import kotlinx.android.synthetic.main.item_message.view.*
import java.lang.RuntimeException
import java.net.URISyntaxException

class ChatRoomActivity : AppCompatActivity() {

    private val socketio = IO.socket("http://192.168.0.13:3000")
    private var messages = mutableListOf<com.csergio.myapp1.model.Message>()
    private lateinit var chatRoomID:String
    private lateinit var myId:String
    private lateinit var myName:String
    private val chatRoomActivityAdapter = ChatRoomActivityAdapter()
    private lateinit var sqliteHelper:SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        sqliteHelper = SQLiteHelper(applicationContext)

        val sharedPreferences = getSharedPreferences("UserCookie", Context.MODE_PRIVATE)
        myId = sharedPreferences.getString("user_id", "")
        chatRoomID = intent.getStringExtra("chatRoomId")
        Toast.makeText(this, "chatRoomID : ${intent.getStringExtra("chatRoomId")}", Toast.LENGTH_LONG).show()
        myName = sharedPreferences.getString("user_name", "")

        Log.d("마이 네임", "마이 네임 : $myName")

        val messageCursor = sqliteHelper.loadMessagesFromDB(chatRoomID)
        while (messageCursor.moveToNext()){
            val messageModel = com.csergio.myapp1.model.Message()
            messageModel.chatroom_id = messageCursor.getString(1)
            messageModel.sender_id = messageCursor.getString(2)
            messageModel.sender_name = messageCursor.getString(3)
            messageModel.content = messageCursor.getString(4)
            messages.add(messageModel)
        }

        chatRoom_recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        chatRoom_recyclerView.adapter = chatRoomActivityAdapter
        chatRoom_recyclerView.scrollToPosition(messages.lastIndex)

        try {
            socketio.on(Socket.EVENT_CONNECT, Emitter.Listener {
                Log.d("TAG", "소켓 접속 됨!")
                socketio.emit("joinRoom", chatRoomID)
            })
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            throw RuntimeException(e)
        }

        chatRoom_sendButton.setOnClickListener {
            val message = chatRoom_editText.text.toString()

            Log.d("메시지 전송", "메시지 전송 전")
            if (message.isEmpty()){
                Toast.makeText(this, "메시지를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            socketio.emit("sendMessage", message, chatRoomID, myId, myName)
            chatRoom_editText.text?.clear()
            Log.d("메시지 전송", "메시지 전송 후")
        }

        socketio.on("receiveMessage") {
            val message = com.csergio.myapp1.model.Message()
            message.content = it[0].toString()
            message.chatroom_id = it[1].toString()
            message.sender_id = it[2].toString()
            message.sender_name = it[3].toString()
            messages.add(message)

            sqliteHelper.saveMessageToDB(message)

            runOnUiThread {
                chatRoomActivityAdapter.notifyDataSetChanged()
                chatRoom_recyclerView.scrollToPosition(messages.lastIndex)
            }

        }

        socketio.connect()
    }

    inner class ChatRoomActivityViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val profileImageView = itemView.item_message_profileImage
        val nameTextView = itemView.item_message_name
        val contentTextView = itemView.item_message_content
        val timestampTextView = itemView.item_message_timestamp
        val readCountLeftTextView = itemView.item_message_readCount_L
        val readCountRightTextView = itemView.item_message_readCount_R
        val linearLayout = itemView.item_message_linearLayout
    }

    inner class ChatRoomActivityAdapter:RecyclerView.Adapter<ChatRoomActivityViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomActivityViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
            return ChatRoomActivityViewHolder(view)
        }

        override fun getItemCount(): Int {
            return messages.size
        }

        override fun onBindViewHolder(holder: ChatRoomActivityViewHolder, position: Int) {

            val item = messages[position]
            holder.contentTextView.text = item.content
            holder.nameTextView.text = item.sender_name

            if (item.sender_id != myId){
                holder.contentTextView.setBackgroundResource(R.drawable.leftbubble)
                holder.linearLayout.gravity = Gravity.LEFT
                holder.readCountLeftTextView.visibility = View.INVISIBLE
            } else {
                holder.contentTextView.setBackgroundResource(R.drawable.rightbubble)
                holder.linearLayout.gravity = Gravity.RIGHT
                holder.readCountRightTextView.visibility = View.INVISIBLE
            }

        }

    }

}
