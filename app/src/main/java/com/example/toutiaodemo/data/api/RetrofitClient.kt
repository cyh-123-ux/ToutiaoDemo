package com.example.toutiaodemo.data.api
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//String apiKey = "91160f2028b1a2528b85a0ba82333980";
//String apiUrl = "http://v.juhe.cn/toutiao/index";
object RetrofitClient {
    private const val BASE_URL = "http://v.juhe.cn/toutiao/"
    private val okHttpClient by lazy{
        val loggingInterceptor = HttpLoggingInterceptor().apply{
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }
    val instance: NewsApi by lazy{
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApi::class.java)
    }
}