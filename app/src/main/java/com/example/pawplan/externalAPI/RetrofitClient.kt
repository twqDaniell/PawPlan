package com.example.pawplan.externalAPI

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val DOG_BASE_URL = "https://dog.ceo/api/"
    private const val CAT_BASE_URL = "https://api.thecatapi.com/v1/"

    val dogApi: DogApi by lazy {
        Retrofit.Builder()
            .baseUrl(DOG_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DogApi::class.java)
    }

    val catApi: CatApi by lazy {
        Retrofit.Builder()
            .baseUrl(CAT_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CatApi::class.java)
    }
}
