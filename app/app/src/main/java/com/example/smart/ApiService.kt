package com.example.smart

import LoginResponse
import retrofit2.Call
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody


interface ApiService {
    @FormUrlEncoded
    @POST("login.php") // Ganti dengan nama file PHP backend kamu
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("register.php")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @FormUrlEncoded
    @POST("addtask.php")
    fun addTask(
        @Field("title") title: String,
        @Field("description") description: String,
        @Field("due_date") dueDate: String,
        @Field("due_time") dueTime: String,
        @Field("id_user") idUser: Int
    ): Call<DefaultResponse>

    @FormUrlEncoded
    @POST("deletetask.php")
    fun deleteTask(
        @Field("id_task") idTask: Int
    ): Call<DefaultResponse>

    @GET("searchtask.php")
    fun searchTask(
        @Query("query") query: String,
        @Query("user_id") userId: Int
    ): Call<SearchResponse>

    @FormUrlEncoded
    @POST("gettask.php")
    fun getTasks(@Field("id_user") idUser: Int): Call<GetTaskResponse>

    @GET("gettask_by_date.php")
    fun getTasksByDate(
        @Query("date") date: String,
        @Query("id_user") idUser: Int
    ): Call<GetTaskResponse>

    @GET("get_user.php")
    fun getUser(@Query("id_user") id: Int): Call<UserResponse>

    @FormUrlEncoded
    @POST("updatetask.php")
    fun updateTask(
        @Field("id_task") idTask: Int,
        @Field("title") title: String,
        @Field("description") description: String,
        @Field("due_date") dueDate: String,
        @Field("due_time") dueTime: String
    ): Call<DefaultResponse>

    @GET("get_logs.php")
    fun getLogs(@Query("id_user") idUser: Int): Call<LogResponse>

    @FormUrlEncoded
    @POST("log_activity.php")
    fun logActivity(
        @Field("id_user") idUser: Int,
        @Field("activity") activity: String
    ): Call<DefaultResponse>

    @Multipart
    @POST("upload_profile.php")
    fun uploadProfile(
        @Part image: MultipartBody.Part,
        @Part("user_id") userId: RequestBody,
        @Part("name") name: RequestBody
    ): Call<DefaultResponse>

}
