package com.example.smart

data class DefaultResponse(
    val status: String,
    val message: String,
    val id_task: Int? = null
)
