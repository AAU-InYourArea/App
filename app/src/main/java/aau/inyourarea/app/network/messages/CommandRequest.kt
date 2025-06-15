package aau.inyourarea.app.network.messages

import aau.inyourarea.app.network.CommandType
import aau.inyourarea.app.network.NetworkService

data class CommandRequest(val commandId: Long, val type: String, val payload: Any) {
    constructor(commandType: CommandType, payload: Any) : this(
        commandId = ++NetworkService.commandCounter,
        type = commandType.id,
        payload = payload
    )
}