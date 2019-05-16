package com.csergio.myapp1.util

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder {

     val interceptor = Interceptor {
        val request = it.request().newBuilder().addHeader("Connection", "close").build()
        it.proceed(request)
    }

     val httpClient = OkHttpClient.Builder()
         .addInterceptor(interceptor)
         .retryOnConnectionFailure(true)
         .build()

    val retrofit = Retrofit.Builder()
        .baseUrl("http://ec2-52-79-251-44.ap-northeast-2.compute.amazonaws.com:3000")
//        .baseUrl("http://192.168.0.13:8080")
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

}