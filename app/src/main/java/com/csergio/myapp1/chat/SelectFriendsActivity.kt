package com.csergio.myapp1.chat

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.csergio.myapp1.R
import com.csergio.myapp1.model.User
import com.csergio.myapp1.util.PostService
import com.csergio.myapp1.util.RetrofitBuilder
import com.csergio.myapp1.util.SQLiteHelper
import kotlinx.android.synthetic.main.activity_select_friends.*
import kotlinx.android.synthetic.main.item_select_friend.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create

/**
 * 단체 대화방 생성을 위한 참가 인원 선택 액티비티
 * */
class SelectFriendsActivity : AppCompatActivity() {

    val userList = mutableListOf<User>()
    lateinit var myId:String
    val participantList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_friends)

        myId = getSharedPreferences("UserCookie", Context.MODE_PRIVATE).getString("user_id", "")

        // 서버에서 사용자 목록 가져오기
        RetrofitBuilder.retrofit.create(PostService::class.java)
            .getFriendsList().enqueue(object : Callback<MutableList<User>>{

                override fun onFailure(call: Call<MutableList<User>>, t: Throwable) {
                    Toast.makeText(applicationContext, "친구 목록 불러오기 실패", Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<MutableList<User>>, response: Response<MutableList<User>>) {
                    if (response.isSuccessful){
                        response.body()?.let {
                            for (item in it){
                                // 목록에서 본인 제외
                                if (item.user_id != myId){
                                    userList.add(item)
                                }
                            }
                        }
                        // 메인 스레드에서 UI 갱신하도록 처리
                        runOnUiThread {
                            selectFriendsActivity_recyclerView.layoutManager = LinearLayoutManager(applicationContext)
                            selectFriendsActivity_recyclerView.adapter = SelectFriendsActivityAdapter()
                        }
                    }
                }
            })

        selectFriendsActivity_createButton.setOnClickListener {

            val sqliteHelper = SQLiteHelper(applicationContext)
            val chatRoomId = sqliteHelper.makeChatRoom(null, participantList, "group")

            if (chatRoomId.isNotEmpty()){
                val intent = Intent(this, ChatRoomActivity::class.java)
                intent.putExtra("chatRoomId", chatRoomId)
                startActivity(intent)
                Toast.makeText(this, "단체 대화방 생성 버튼 클릭됨", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "단체 대화방 생성 실패", Toast.LENGTH_SHORT).show()
            }

        }

    }

    inner class SelectFriendsActivityViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val profileImageView = itemView.item_select_friend_profileImage
        val nameTextView = itemView.item_select_friend_name
        val checkBox = itemView.item_select_friend_checkBox
    }

    inner class SelectFriendsActivityAdapter:RecyclerView.Adapter<SelectFriendsActivityViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectFriendsActivityViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_select_friend, parent, false)
            return SelectFriendsActivityViewHolder(view)
        }

        override fun getItemCount(): Int {
            return userList.size
        }

        override fun onBindViewHolder(holder: SelectFriendsActivityViewHolder, position: Int) {

            val targetUser = userList[position]
            holder.nameTextView.text = targetUser.user_name

            holder.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked){
                    participantList.add(targetUser)
                } else {
                    participantList.remove(targetUser)
                }
            }

        }

    }
}
