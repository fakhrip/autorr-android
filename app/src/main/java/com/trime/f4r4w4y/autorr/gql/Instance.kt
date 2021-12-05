package com.trime.f4r4w4y.autorr.gql

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object Instance {
    private const val BASE_URL: String = "https://gql.justak.id/"

    val graphQLService: Service by lazy {
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build().create(Service::class.java)
    }
}