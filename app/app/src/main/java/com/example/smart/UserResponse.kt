package com.example.smart

data class UserResponse(
    val status: String,
    val user: AccountUser
)

data class AccountUser(
    val id_user: Int,
    val name: String,
    val email: String,
    val password: String,
    val profile_picture: String?
)
