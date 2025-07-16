package aau.inyourarea.app.network.messages

data class JoinRoomRequest(
    val room: Int,
    val password: String
)
