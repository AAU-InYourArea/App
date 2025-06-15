package aau.inyourarea.app.network.messages

data class LoginRequest(
    val username: String,
    val password: String,
    val session: Boolean = false,
    val register: Boolean = false
)