package com.csergio.myapp1.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.csergio.myapp1.R
import com.csergio.myapp1.chat.ChatRoomActivity
import com.csergio.myapp1.model.User
import com.csergio.myapp1.util.PostService
import com.csergio.myapp1.util.RetrofitBuilder
import com.csergio.myapp1.util.SQLiteHelper
import kotlinx.android.synthetic.main.fragment_friends.view.*
import kotlinx.android.synthetic.main.item_friend.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 친구 목록 프래그먼트
 * */
class FriendsFragment:Fragment() {

    private var friendsList = mutableListOf<User>()
    private lateinit var myId:String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val preferences = context?.getSharedPreferences("UserCookie", Context.MODE_PRIVATE)
        myId = preferences?.getString("user_id", "").toString()

        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // 서버에서 사용자 목록 가져오기
        RetrofitBuilder.retrofit.create(PostService::class.java)
            .getFriendsList().enqueue(object : Callback<MutableList<User>>{

                override fun onFailure(call: Call<MutableList<User>>, t: Throwable) {
                    Toast.makeText(context, "친구 목록 불러오기 실패", Toast.LENGTH_SHORT)
                }

                override fun onResponse(call: Call<MutableList<User>>, response: Response<MutableList<User>>) {
                    if (response.isSuccessful){
                        response.body()?.let {
                            for (item in it){
                                // 친구 목록에서 본인 제외
                                if (item.user_id != myId){
                                    friendsList.add(item)
                                }
                            }
                            view.fragment_friends_recyclerView.layoutManager = LinearLayoutManager(context)
                            view.fragment_friends_recyclerView.adapter = FriendsFragmentAdapter()
                        }
                    }
                }

            })

    }

    inner class FriendsFragmentViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val profileImageView = itemView.item_friend_profileImage
        val nameTextView = itemView.item_friend_name
        val stateMessageTextView = itemView.item_friend_stateMessage
    }

    inner class FriendsFragmentAdapter:RecyclerView.Adapter<FriendsFragmentViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsFragmentViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
            return FriendsFragmentViewHolder(view)
        }

        override fun getItemCount(): Int {
            return friendsList.size
        }

        override fun onBindViewHolder(holder: FriendsFragmentViewHolder, position: Int) {

            val friend = friendsList[position]
            holder.nameTextView.text = friend.user_name

            holder.itemView.setOnClickListener {

                val userIdList = mutableListOf<String>()
                userIdList.add(myId)
                userIdList.add(friend.user_id)

                val sqliteHelper = SQLiteHelper(context!!)
                // 1:1 채팅방 존재 여부 확인 및 생성
                val chatRoomId = sqliteHelper.makeChatRoom(userIdList, null,"private")
                Log.d("친구 목록 대화방 아이디", "친구 목록 대화방 아이디 : $chatRoomId")

                if (chatRoomId.isNotEmpty()){
                    val intent = Intent(context, ChatRoomActivity::class.java)
                    intent.putExtra("chatRoomId", chatRoomId)
                    startActivity(intent)
                } else {
                    Toast.makeText(context, "대화방 생성 실패", Toast.LENGTH_SHORT).show()
                }
            }

        }

    }

}