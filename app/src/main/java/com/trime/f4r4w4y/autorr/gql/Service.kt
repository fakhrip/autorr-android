package com.trime.f4r4w4y.autorr.gql

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface Service {

    @Headers("Content-Type: application/json")
    @POST("v1/graphql")
    suspend fun postGuestQuery(@Body body: String): Response<String>

    @Headers("Content-Type: application/json")
    @POST("v1/graphql")
    suspend fun postAuthenticatedQuery(
        @Header("Authorization") token: String,
        @Body body: String
    ): Response<String>
}