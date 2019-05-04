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
        db?.execSQL(SQL_CREATE_MESSAGES)
        db?.execSQL(SQL_CREATE_READERS)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    fun makeChatRoom(userIdList: MutableList<String>?, participantList:MutableList<User>?, mode:String):String{

        try {
            var chatRoomId: String
            val uuid = UUID.randomUUID().toString().replace("-","")

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
                val result = NetworkCall().execute(call)
                Log.d("방 확인 결과", "방 확인 결과 : ${result.get()}")
                chatRoomId = result.get()

                Log.d("방 생성 결과", "방 생성 결과 : $chatRoomId")
                return chatRoomId

            } else if(mode == "group"){
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
            val sql = "insert into messages(chatroom_id, sender_id, sender_name, content) values('${message.chatroom_id}', '${message.sender_id}', '${message.sender_name}', '${message.content}')"
            Log.d("수신 메시지 DB 저장 SQL", "수신 메시지 DB 저장 SQL : $sql")
            this.writableDatabase.execSQL(sql)
            Log.d("수신 메시지 DB 저장됨", "수신 메시지 DB 저장됨")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadMessagesFromDB(chatRoomId:String):Cursor{
        val sql = "select * from messages where chatroom_id = '$chatRoomId'"
        Log.d("sql문", "sql문 : $sql")
        return readableDatabase.rawQuery(sql, null)
    }

    fun loadChatRoomsFromDB():Cursor{
        val sql = "select chatroom_id, sender_name, content, message_date from messages a INNER JOIN (SELECT max(message_idx) as max_message_idx FROM messages group by chatroom_id) b on a.message_idx = b.max_message_idx"
        return readableDatabase.rawQuery(sql, null)
    }

    private fun closeResources(){
        this.readableDatabase.close()
        this.writableDatabase.close()
        this.close()
    }

    private inner class NetworkCall:AsyncTask<Call<MutableList<ChatRoom>>,Int, String>(){
        override fun doInBackground(vararg params: Call<MutableList<ChatRoom>>?): String {
            val gson = Gson()
            val chatRoomJson = gson.toJson(params[0]?.execute()?.body()?.get(0))
            var result =
               gson.fromJson(chatRoomJson, ChatRoom::class.java)
            Log.d("네트워크 콜 결과", "네트워크 콜 결과 : ${result.chatroom_id}")
            return result.chatroom_id
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