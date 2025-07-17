package aau.inyourarea.app.network

enum class CommandType(val id: String, val returnsData: Boolean) {
    GET_ACCOUNT_DATA("account", true),
    UPDATE_FREQUENCY("frequency", false),
    UPDATE_LOCATION("position", false),
    LOGOUT("logout", false),
    GET_ROOMS("rooms", true),
    CREATE_ROOM("room_create", true),
    JOIN_ROOM("room_join", true),
    LEAVE_ROOM("room_leave", false),
}