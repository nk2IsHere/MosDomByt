package ru.acurresearch.dombyta.App

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.acurresearch.dombyta.Constants

//code review: желательно интегрировать dependency injection в проект
object ApiProvider {
    fun provide(): Api = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterProvider.provide())
        .client(OkHttpClient.Builder().build())
        .build()
        .create(Api::class.java)
}
object GsonConverterProvider {
    fun provide(): GsonConverterFactory {
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .serializeNulls()
            .create()

        return GsonConverterFactory.create(gson)
    }
}
//"yyyy-MM-dd HH:mm"



