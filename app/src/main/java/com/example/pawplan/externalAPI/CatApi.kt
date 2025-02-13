package com.example.pawplan.externalAPI

import com.example.pawplan.models.CatBreed
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers

interface CatApi {
    @Headers("x-api-key: DEMO-API-KEY")
    @GET("breeds")
    fun getBreeds(): Call<List<CatBreed>>
}
