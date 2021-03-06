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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.csergio.myapp1.NotificationService
import com.csergio.myapp1.R
import com.csergio.myapp1.model.Message
import com.csergio.myapp1.util.SQLiteHelper
import kotlinx.android.synthetic.main.activity_chat_room.*
import kotlinx.android.synthetic.main.item_message.view.*

/**
 * 채팅 대화방 클래스
 * */
class ChatRoomActivity : AppCompatActivity() {

    private  val io = NotificationService.getIO()
    private var messages = mutableListOf<Message>()
    private lateinit var chatRoomID:String
    private lateinit var myId:String
    private lateinit var myName:String
    private var myPicAddress = ""
    private val chatRoomActivityAdapter = ChatRoomActivityAdapter()
    private lateinit var sqliteHelper:SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        state = true
        refreshHandler = handlerForRefreshingUI
        messageHandler = handlerForLoadingMessages

        sqliteHelper = SQLiteHelper(applicationContext)
        sqlite = sqliteHelper

        chatRoomID = intent.getStringExtra("chatRoomId")
        thisRoomId = chatRoomID
        Log.d("대화방 아이디", "대화방 아이디 : ${intent.getStringExtra("chatRoomId")}")
//        Toast.makeText(this, "chatRoomID : ${intent.getStringExtra("chatRoomId")}", Toast.LENGTH_LONG).show()
//        Log.d("사진 URL", "사진 URL : ${intent.getStringExtra("myPicAddress")}")

        val sharedPreferences = getSharedPreferences("UserCookie", Context.MODE_PRIVATE)
        myId = sharedPreferences.getString("user_id", "")
        myUserId = myId
        myName = sharedPreferences.getString("user_name", "")
        myPicAddress = sharedPreferences.getString("user_pic", "")

        loadMessages()

        messgeList = messages

        chatRoom_recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        chatRoom_recyclerView.adapter = chatRoomActivityAdapter
        chatRoom_recyclerView.scrollToPosition(messages.lastIndex)

        adapter = chatRoomActivityAdapter
        recyclerView = chatRoom_recyclerView

        chatRoomActivity_sendButton.setOnClickListener {

            val message = chatRoomActivity_editText.text.toString()

            if (message.isEmpty()){
                Toast.makeText(this, "메시지를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 서버로 메시지 전송
            io.emit("sendMessage", message, chatRoomID, myId, myName, myPicAddress)

            chatRoomActivity_editText.text?.clear()
//            Log.d("메시지 전송", "메시지 전송함")

        }

    }

    override fun onResume() {
        Log.d("ChatRoomActivity", "ChatRoomActivity에서 onResume 실행됨")
        refreshReadCountInCompanionObject(chatRoomID, myId)
        messgeList = messages
        io.emit("sendReaderInfo", myId, chatRoomID)
        super.onResume()
    }

    fun refreshReadCount(){
        for (message in messgeList){
            val count = message.readcount.toInt()
            if (count > 0){
                message.readcount = (count - 1).toString()
            }
        }
        refreshList()
    }

    fun loadMessages(){

        Log.d("ChatRoomActivity", "ChatRoomActivity에서 loadMessages 시작됨")
        messages.clear()

        // DB에서 저장된 메시지 불러오기
        val messageCursor = sqliteHelper.loadMessagesFromDB(chatRoomID)
        while (messageCursor.moveToNext()){

            val messageModel = Message()
            messageModel.message_idx = messageCursor.getInt(0)
            messageModel.chatroom_id = messageCursor.getString(1)
            messageModel.sender_id = messageCursor.getString(2)
            messageModel.sender_name = messageCursor.getString(3)
            messageModel.sender_pic = messageCursor.getString(4)
            messageModel.content = messageCursor.getString(5)
            messageModel.timestamp = messageCursor.getString(6)
            messageModel.readcount = messageCursor.getInt(7).toString()

            messages.add(messageModel)

        }
//        Log.d("ChatRoomActivity", "ChatRoomActivity에서 loadMessages 끝남")

    }

    // 서비스에서 채팅방 메시지 목록 갱신을 하도록 하기 위한 객체
    companion object{

        // 외부 클래스 요소 공유를 위한 변수
        lateinit var sqlite:SQLiteHelper
        var messgeList = mutableListOf<Message>()
        lateinit var adapter:ChatRoomActivityAdapter
        lateinit var recyclerView: RecyclerView
        lateinit var refreshHandler: Handler
        lateinit var messageHandler: Handler
        lateinit var thisRoomId:String
        lateinit var myUserId:String
        // 액티비티 실행 여부 공유를 위한 변수
        var state = false
        // 메시지와 대화방 매칭 결과 변수
        var targetMessageIdx = 0

        // 새로 받은 메시지 추가 및 UI 갱신
        fun addLastMessage(targetRoomID:String, targetUserId:String):Boolean{

            if (targetRoomID == thisRoomId){
                val messageCursor = sqlite.loadLastMessageFromDB(targetRoomID)
                while (messageCursor.moveToNext()){

                    val messageModel = Message()
                    targetMessageIdx = messageCursor.getInt(0)
                    messageModel.message_idx = messageCursor.getInt(0)
                    messageModel.chatroom_id = messageCursor.getString(1)
                    messageModel.sender_id = messageCursor.getString(2)
                    messageModel.sender_name = messageCursor.getString(3)
                    messageModel.sender_pic = messageCursor.getString(4)
                    messageModel.content = messageCursor.getString(5)
                    messageModel.timestamp = messageCursor.getString(6)
                    // 남이 읽은 건 신호 받아서 처리하므로 내가 읽은 것만 처리
                    messageModel.readcount = (messageCursor.getInt(7) - 1).toString()
//                    Log.d("readcount1", "content : ${messageCursor.getString(5)} / readcount1 : ${messageCursor.getInt(7)}")
//                    Log.d("readcount1", "content : ${messageCursor.getString(5)} / readcount1 : ${messageModel.readcount}")

                    messgeList.add(messageModel)

                }

                refreshUIByHandler()

                sqlite.inputReader(targetRoomID, targetMessageIdx, myUserId)
                Log.d("내가 메시지 읽은 거 DB 반영", "내가 메시지 읽은 거 DB 반영")

                targetMessageIdx = 0
                return true
            }

            targetMessageIdx = 0
            return false
        }

        // 메시지 읽음 처리 및 UI 갱신
        fun refreshReadCountInCompanionObject(targetRoomID:String, targetUserId:String):Boolean{
            if (targetRoomID == thisRoomId){
                Log.d("ChatRoomActivity", "ChatRoomActivity에서 refreshReadCount 시작됨")
                // 읽음 처리된 메시지 개수만큼 목록 뒤에서부터 readCount - 1 처리
                val result = sqlite.inputReaders(messgeList, targetUserId)
                if (result > 0){
                    var index = messgeList.lastIndex
                    for (i in 1..result){
                        messgeList[index].readcount = (messgeList[index].readcount.toInt() - 1).toString()
                        index--
                    }
                }
                // 목록 새로고침
                refreshUIByHandler()
//                Log.d("ChatRoomActivity", "ChatRoomActivity에서 refreshReadCount 끝남")
                return true
            }
            return false
        }

        // 메인스레드에서 DB에서 전체 메시지 불러오기
        fun refreshReadCountByHandler(){
            val msg = messageHandler.obtainMessage()
            messageHandler.handleMessage(msg)
        }

        // 메인 스레드에서 UI 갱신하도록 처리
        fun refreshUIByHandler(){
            val msg = refreshHandler.obtainMessage()
            refreshHandler.handleMessage(msg)
        }

    }

    // AsyncTask는 일회용이라 핸들러로 UI 업데이트 처리
    private val handlerForRefreshingUI = object : Handler(){
        override fun handleMessage(msg: android.os.Message?) {
            runOnUiThread {
                refreshList()
            }
        }
    }

    private val handlerForLoadingMessages = object : Handler(){
        override fun handleMessage(msg: android.os.Message?) {
            runOnUiThread {
                refreshReadCount()
            }
        }
    }

    fun refreshList(){
        Log.d("refreshList 실행됨", "refreshList 실행됨")
        chatRoomActivityAdapter.notifyDataSetChanged()
        chatRoom_recyclerView.scrollToPosition(messages.lastIndex)
    }

    inner class ChatRoomActivityViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val profileImageView = itemView.item_message_profileImage
        val nameTextView = itemView.item_message_name
        val contentTextView = itemView.item_message_content
        val timestampTextView = itemView.item_message_timestamp
        val leftReadCountTextView = itemView.item_message_leftReadCount
        val rightReadCountTextView = itemView.item_message_rightReadCount
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
            Glide.with(holder.itemView.context)
                .load(item.sender_pic)
                .apply(RequestOptions.circleCropTransform())
                .into(holder.profileImageView)
            holder.timestampTextView.text = item.timestamp

            when (item.sender_id){
                myId -> {
                    holder.contentTextView.setBackgroundResource(R.drawable.rightbubble)
                    holder.linearLayout.gravity = Gravity.RIGHT
                    holder.rightReadCountTextView.visibility = View.INVISIBLE
                    if (item.readcount.toInt() > 0){
                        holder.leftReadCountTextView.visibility = View.VISIBLE
                        holder.leftReadCountTextView.text = item.readcount
                    } else {
                        holder.leftReadCountTextView.visibility = View.INVISIBLE
                    }
                }
                else -> {
                    holder.contentTextView.setBackgroundResource(R.drawable.leftbubble)
                    holder.linearLayout.gravity = Gravity.LEFT
                    holder.leftReadCountTextView.visibility = View.INVISIBLE
                    if (item.readcount.toInt() > 0){
                        holder.rightReadCountTextView.visibility = View.VISIBLE
                        holder.rightReadCountTextView.text = item.readcount
                    } else {
                        holder.rightReadCountTextView.visibility = View.INVISIBLE
                    }
                }
            }

        }

    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        state = false
        super.onDestroy()
    }
}
