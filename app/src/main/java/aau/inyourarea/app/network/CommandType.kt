package aau.inyourarea.app.network

enum class CommandType(val id: String, val returnsData: Boolean) {
    GET_ACCOUNT_DATA("account", true),
    UPDATE_FREQUENCY("frequency", false),
    UPDATE_LOCATION("position", false),
    LOGOUT("logout", false)
}