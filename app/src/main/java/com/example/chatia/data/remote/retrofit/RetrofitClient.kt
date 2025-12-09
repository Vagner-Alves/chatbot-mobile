package com.example.chatia.data.remote.retrofit

import com.example.chatia.data.remote.OpenAIApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://api.openai.com/"
    private const val API_KEY = "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" // Substitua pela sua chave da API da OpenAI

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor {
        val request = it.request().newBuilder()
            .addHeader("Authorization", "Bearer $API_KEY")
            .build()
        it.proceed(request)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val openAIApi: OpenAIApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIApi::class.java)
    }
}