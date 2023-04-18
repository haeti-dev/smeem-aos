package com.sopt.smeem.core.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object PreparedRetrofit {
    // 인증 전 혹은 비회원 api Retrofit
    val apiServerRetrofitForAnonymous = Retrofit.Builder().apply {
        // baseUrl(API_SERVER_URL) TODO : set API_SERVER_URL localproperties
        client(okHttpClientExcludeAuth)
    }.build()

    private val okHttpClientExcludeAuth = OkHttpClient.Builder().apply {
        connectTimeout(10, TimeUnit.SECONDS)
        writeTimeout(5, TimeUnit.SECONDS)
        readTimeout(5, TimeUnit.SECONDS)
    }.build()

    // 인증 후 api Retrofit
    val apiServerRetrofitForAuthentication = Retrofit.Builder().apply {
        // baseUrl(API_SERVER_URL)
        client(okHttpClientIncludeAuth)
    }.build()

    private val okHttpClientIncludeAuth = OkHttpClient.Builder().apply {
        connectTimeout(10, TimeUnit.SECONDS)
        writeTimeout(5, TimeUnit.SECONDS)
        readTimeout(5, TimeUnit.SECONDS)

        // TODO : local storage 로 부터 api server access token 을 가져오는로직 필요
        // if 분기 필요
        addInterceptor(
            RequestInterceptor(
                accessToken = "",
                refreshToken = ""
            )
        ) // TODO : local storage 에 인증토큰이 적재되어있다면, interceptor 에 담아준다.
    }.build()
}