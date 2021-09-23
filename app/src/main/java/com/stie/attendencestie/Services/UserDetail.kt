package com.stie.attendencestie.Services

import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface UserDetail {


    @Headers(
        "Content-Type:application/json"
    )
    @POST("attendance")
    open fun sendFormData(@Body jsonObject: JsonObject?): Call<ResponseBody?>?
    @Headers("Content-Type:application/json")
    @POST("user_nric/")
    open fun login(@Body jsonObject: JsonObject?): Call<ResponseBody?>?

    @GET("maps/api/geocode/json")
    fun getCityResults(
        @Query("address") types: String?,


        @Query("key") key: String?
    ): Call<ResponseBody?>?


}