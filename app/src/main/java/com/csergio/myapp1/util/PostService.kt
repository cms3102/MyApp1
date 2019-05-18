package com.csergio.myapp1.util

import com.csergio.myapp1.model.ChatRoom
import com.csergio.myapp1.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface PostService {

    @POST("/users/signup")
    fun requestSignUpPost(@Body user:User):Call<String>

    @POST("/users/login")
    fun requestLogin(@Body user:User):Call<String>

    @POST("/users/friends")
    fun getFriendsList():Call<MutableList<User>>

    @POST("/chat/checkroom")
    fun isExistRoom(@Body chatroom:ChatRoom):Call<MutableList<ChatRoom>>

    @POST("/chat/groupchat")
    fun makeGroupChat(@Body chatroom: ChatRoom):Call<String>

    @Multipart
    @POST("/upload/images")
    fun uploadProfileImage(@Part("user_id") userId:RequestBody, @Part image:MultipartBody.Part):Call<String>

    @POST("/users/statemessage")
    fun updateStateMessage(@Body user:User):Call<String>

}