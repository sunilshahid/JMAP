package com.example.network

import com.example.data.JmapRequest
import com.example.data.JmapResponse
import com.example.data.JmapSessionResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface JmapApiService {
    @GET
    suspend fun getSession(@Url url: String, @Header("Authorization") authHeader: String): JmapSessionResponse

    @POST
    suspend fun callApi(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body request: JmapRequest
    ): JmapResponse
}
