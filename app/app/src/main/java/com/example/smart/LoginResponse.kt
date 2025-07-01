data class LoginResponse(
    val status: String,
    val message: String,
    val user: LoginUser?
)


data class LoginUser(
    val id_user: Int,
    val name: String,
    val email: String
)
