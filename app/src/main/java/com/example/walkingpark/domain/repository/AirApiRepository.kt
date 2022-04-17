package com.example.walkingpark.domain.repository

import com.example.walkingpark.data.source.api.dto.AirDTO
import com.example.walkingpark.data.source.api.dto.WeatherDTO
import retrofit2.Response


interface AirApiRepository {

    suspend fun startAirApi(query: Map<String, String>): Response<AirDTO>

    fun extractQuery(stationName: String) : Map<String, String>

    fun handleResponse(response: Response<AirDTO>) : List<AirDTO.Response.Body.Items>?
}