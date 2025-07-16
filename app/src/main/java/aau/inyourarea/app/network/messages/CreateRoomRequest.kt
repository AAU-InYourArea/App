package aau.inyourarea.app.network.messages

data class CreateRoomRequest(
    val name: String,
    val password: String
)