package com.csergio.myapp1.util

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder {

    val retrofit = Retrofit.Builder()
//        .baseUrl("http://192.168.0.13:3000")
        .baseUrl("http://ec2-52-79-251-44.ap-northeast-2.compute.amazonaws.com:3000")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

}