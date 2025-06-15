package aau.inyourarea.app.network

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class NetworkListener(
    private val open: (WebSocket, Response) -> Unit,
    private val voice: (WebSocket, ByteString) -> Unit,
    private val command: (WebSocket, String) -> Unit,
    private val closing: (WebSocket, Int, String) -> Unit
) : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        this.open(webSocket, response)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        this.voice(webSocket, bytes)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        this.command(webSocket, text)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        this.closing(webSocket, code, reason)
        webSocket.close(code, reason)
    }
}