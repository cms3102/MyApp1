package com.csergio.myapp1.util

import com.csergio.myapp1.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET

interface GetService {

    @GET("/chat/groupChat")
    fun makeGroupChat(@Body participantList:MutableList<User>):Call<String>
}