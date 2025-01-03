package com.example.pawplan.externalAPI

import com.example.pawplan.models.BreedsResponse
import retrofit2.Call
import retrofit2.http.GET

interface DogApi {
    @GET("breeds/list/all")
    fun getBreeds(): Call<BreedsResponse>
}

