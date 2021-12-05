package com.trime.f4r4w4y.autorr.gql

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface Service {

    @Headers("Content-Type: application/json")
    @POST("v1/graphql")
    suspend fun postDynamicQuery(@Body body: String): Response<String>
}