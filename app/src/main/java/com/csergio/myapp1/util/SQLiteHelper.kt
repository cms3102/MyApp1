package com.csergio.myapp1.util

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.AsyncTask
import android.util.Log
import com.csergio.myapp1.model.ChatRoom
import com.csergio.myapp1.model.User
import com.google.gson.Gson
import retrofit2.Call
import java.util.*

class SQLiteHelper(context:Context):SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        // DB 생성
        db?.execSQL(SQL_CREATE_MESSAGES)
        db?.execSQL(SQL_CREATE_READERS)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    // 채팅방 존재 여부 확인 및 생성 메소드
    fun makeChatRoom(userIdList: MutableList<String>?, participantList:MutableList<User>?, mode:String):String{

        try {
            var chatRoomId: String
            val uuid = UUID.randomUUID().toString().replace("-","")

            // private - 1:1 채팅방, group - 단체방
            if (mode == "private"){
                Log.d("방 생성", "멤버 수 2임")

                val chatRoom = ChatRoom()
                chatRoom.chatroom_id = uuid
                chatRoom.member_count = 2
                userIdList?.let {
                    for (userId in userIdList){
                        val user = User()
                        user.user_id = userId
                        chatRoom.memberList.add(user)
                    }
                }

                val call = RetrofitBuilder.retrofit.create(PostService::class.java).isExistRoom(chatRoom)
                // AsyncTask를 써서 retrofit 동기 방식 호출을 해야 값을 받아오고 나서 return이 실행됨. 정확한 이유는 확인 필요.
                val result = PrivateChatCall().execute(call)
                Log.d("방 확인 결과", "방 확인 결과 : ${result.get()}")
                chatRoomId = result.get()

                Log.d("방 생성 결과", "방 생성 결과 : $chatRoomId")
                return chatRoomId

            } else if(mode == "group"){

                val chatRoom = ChatRoom()
                chatRoom.chatroom_id = uuid
                chatRoom.member_count = participantList!!.size
                chatRoom.memberList = participantList

                val call = RetrofitBuilder.retrofit.create(PostService::class.java).makeGroupChat(chatRoom)
                val result = GroupChatCall().execute(call)
                Log.d("단체 대화방 생성 결과", "단체 대화방 생성 결과 : ${result.get()}")

                return result.get()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            closeResources()
        }
        closeResources()
        return ""
    }

    fun saveMessageToDB(message: com.csergio.myapp1.model.Message){
        try {
            val sql = "insert into messages(chatroom_id, sender_id, sender_name, sender_pic, content) values('${message.chatroom_id}', '${message.sender_id}', '${message.sender_name}', '${message.sender_pic}', '${message.content}')"
            Log.d("수신 메시지 DB 저장 SQL", "수신 메시지 DB 저장 SQL : $sql")
            this.writableDatabase.execSQL(sql)
            Log.d("수신 메시지 DB 저장됨", "수신 메시지 DB 저장됨")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 메시지 전체 불러오기
    fun loadMessagesFromDB(chatRoomId:String):Cursor{
        val sql = "select * from messages where chatroom_id = '$chatRoomId'"
        return readableDatabase.rawQuery(sql, null)
    }

    // 최근 추가된 메시지 불러오기
    fun loadLastMessageFromDB(chatRoomId:String):Cursor{
        val sql = "select * from messages where chatroom_id = '$chatRoomId' order by message_idx desc limit 1"
        return readableDatabase.rawQuery(sql, null)
    }

    // 채팅방 목록 및 최신 메시지 불러오기
    fun loadChatRoomsFromDB():Cursor{
        val sql = "select chatroom_id, sender_name, sender_pic, content, message_date from messages a INNER JOIN (SELECT max(message_idx) as max_message_idx FROM messages group by chatroom_id) b on a.message_idx = b.max_message_idx"
        return readableDatabase.rawQuery(sql, null)
    }

    // 자원 닫기
    private fun closeResources(){
        this.readableDatabase.close()
        this.writableDatabase.close()
        this.close()
    }

    // retrofit 동기 방식 호출을 위한 클래스.
    private inner class PrivateChatCall:AsyncTask<Call<MutableList<ChatRoom>>,Int, String>(){

        override fun doInBackground(vararg params: Call<MutableList<ChatRoom>>?): String {

            // 결과 받아오고 json 변환/파싱을 해야 정상 처리됨. 정확한 이유는 확인 필요.
            val gson = Gson()
            val chatRoomJson = gson.toJson(params[0]?.execute()?.body()?.get(0))
            var result =
               gson.fromJson(chatRoomJson, ChatRoom::class.java)
            Log.d("네트워크 콜 결과", "네트워크 콜 결과 : ${result.chatroom_id}")

            return result.chatroom_id

        }

    }

    private inner class GroupChatCall:AsyncTask<Call<String>, Int, String>(){

        override fun doInBackground(vararg params: Call<String>?): String {

            val result = params[0]?.execute()?.body()
            Log.d("GroupCall 결과", "GroupCall 결과 : $result")

            return result.toString()
        }

    }

    companion object{

        val DATABASE_VERSION = 1
        val DATABASE_NAME = "chat.db"

        val SQL_CREATE_MESSAGES = "create table messages(" +
                "message_idx integer not null primary key autoincrement," +
                "chatroom_id text not null," +
                "sender_id text not null," +
                "sender_name text not null," +
                "sender_pic text," +
                "content text," +
                "message_date datetime" +
                ")"

        val SQL_CREATE_READERS = "create table readers(" +
                "reader_idx integer not null primary key autoincrement," +
                "message_idx integer not null," +
                "user_id text not null," +
                "foreign key (message_idx) references messages (message_idx)" +
                ")"

    }

}