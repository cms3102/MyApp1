package com.csergio.myapp1.chat

import android.content.Context
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.csergio.myapp1.NotificationService
import com.csergio.myapp1.R
import com.csergio.myapp1.model.Message
import com.csergio.myapp1.util.SQLiteHelper
import kotlinx.android.synthetic.main.activity_chat_room.*
import kotlinx.android.synthetic.main.item_message.view.*

open class ChatRoomActivity : AppCompatActivity() {

    private  val io = NotificationService.getIO()
    private var messages = mutableListOf<Message>()
    private lateinit var chatRoomID:String
    private lateinit var myId:String
    private lateinit var myName:String
    private val chatRoomActivityAdapter = ChatRoomActivityAdapter()
    private lateinit var sqliteHelper:SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        state = true
        messageHandler = handler

        sqliteHelper = SQLiteHelper(applicationContext)
        sqlite = sqliteHelper

        chatRoomID = intent.getStringExtra("chatRoomId")
        Toast.makeText(this, "chatRoomID : ${intent.getStringExtra("chatRoomId")}", Toast.LENGTH_LONG).show()

        val sharedPreferences = getSharedPreferences("UserCookie", Context.MODE_PRIVATE)
        myId = sharedPreferences.getString("user_id", "")
        myName = sharedPreferences.getString("user_name", "")

        // DB에서 저장된 메시지 불러오기
        val messageCursor = sqliteHelper.loadMessagesFromDB(chatRoomID)
        while (messageCursor.moveToNext()){

            val messageModel = Message()
            messageModel.chatroom_id = messageCursor.getString(1)
            messageModel.sender_id = messageCursor.getString(2)
            messageModel.sender_name = messageCursor.getString(3)
            messageModel.content = messageCursor.getString(4)

            messages.add(messageModel)

        }

        messgeList = messages

        chatRoom_recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        chatRoom_recyclerView.adapter = chatRoomActivityAdapter
        chatRoom_recyclerView.scrollToPosition(messages.lastIndex)

        adapter = chatRoomActivityAdapter
        recyclerView = chatRoom_recyclerView

        chatRoom_sendButton.setOnClickListener {

            val message = chatRoom_editText.text.toString()

            if (message.isEmpty()){
                Toast.makeText(this, "메시지를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 서버로 메시지 전송
            io.emit("sendMessage", message, chatRoomID, myId, myName)

            chatRoom_editText.text?.clear()
            Log.d("메시지 전송", "메시지 전송함")

        }

    }

    override fun onResume() {
        refreshList()
        super.onResume()
    }

    // 서비스에서 채팅방 메시지 목록 갱신을 하도록 하기 위한 객체
    companion object{

        // 외부 클래스 요소 공유를 위한 변수
        lateinit var sqlite:SQLiteHelper
        lateinit var messgeList:MutableList<Message>
        lateinit var adapter:ChatRoomActivityAdapter
        lateinit var recyclerView: RecyclerView
        lateinit var messageHandler: Handler
        // 액티비티 실행 여부 공유를 위한 변수
        var state = false

        // 새로 받은 메시지 추가 및 UI 갱신
        fun addLastMessage(chatRoomID:String){

            val messageCursor = sqlite.loadLastMessageFromDB(chatRoomID)
            while (messageCursor.moveToNext()){

                val messageModel = Message()
                messageModel.chatroom_id = messageCursor.getString(1)
                messageModel.sender_id = messageCursor.getString(2)
                messageModel.sender_name = messageCursor.getString(3)
                messageModel.content = messageCursor.getString(4)

                messgeList.add(messageModel)

            }

            // 메인 스레드에서 UI 갱신하도록 처리
            val msg = messageHandler.obtainMessage()
            messageHandler.handleMessage(msg)

        }

    }

    // AsyncTask는 일회용이라 핸들러로 UI 업데이트 처리
    private val handler = object : Handler(){
        override fun handleMessage(msg: android.os.Message?) {
            runOnUiThread {
                refreshList()
            }
        }
    }

    fun refreshList(){
        chatRoomActivityAdapter.notifyDataSetChanged()
        chatRoom_recyclerView.scrollToPosition(messages.lastIndex)
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

    override fun onDestroy() {
        state = false
        super.onDestroy()
    }
}
