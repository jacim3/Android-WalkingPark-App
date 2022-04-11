package com.example.walkingpark.retrofit2

import com.example.walkingpark.data.dto.AirDTO
import com.example.walkingpark.data.dto.StationDTO
import com.example.walkingpark.data.dto.WeatherDTO
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface PublicApiService {


    @GET("getMsrstnAcctoRltmMesureDnsty")
    suspend fun getAirDataByStationName(
        @Query(value= "serviceKey", encoded = true) serviceKey:String,
        @Query("returnType") type:String,
        @Query("stationName") stationName:String,
        @Query("dataTerm") dataTerm:String
    ):Response<AirDTO>

    @GET("getMsrstnList")
    suspend fun getStationDataByName(
        @Query(value = "serviceKey", encoded = true) serviceKey:String,
        @Query("returnType") type:String,
        @Query("addr") addr:String,

    ) : Response<StationDTO>

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("getUltraSrtFcst")
    suspend fun getWeatherByGridXY(
        @Query(value = "serviceKey", encoded = true) serviceKey: String,
        @Query("dataType") type:String,
        @Query("base_date") baseDate:String,       // yyyymmdd
        @Query("base_time") baseTime:String,       // hhmm : 30분 단위
        @Query("numOfRows") numOfRows:Int,
        @Query("nx") gridX:Int,
        @Query("ny") gridY:Int
    ) :  Response<WeatherDTO.Response.Body.Items>
}