package aau.inyourarea.app.network.messages

data class LoginResponse(
    val success: Boolean,
    val username: String? = null,
    val session: String? = null
)
