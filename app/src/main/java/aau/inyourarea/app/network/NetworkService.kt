package aau.inyourarea.app.network

import aau.inyourarea.app.Constants
import aau.inyourarea.app.R
import aau.inyourarea.app.network.messages.CommandRequest
import aau.inyourarea.app.network.messages.LoginRequest
import aau.inyourarea.app.network.messages.LoginResponse
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okio.ByteString.Companion.toByteString
import java.util.concurrent.CompletableFuture
import kotlin.system.exitProcess

class NetworkService : Service() {
    companion object {
        var commandCounter: Long = 0L
    }

    private val gson: Gson = Gson()

    private lateinit var client: OkHttpClient
    private var connection: WebSocket? = null
    private var reconnect = true

    private val commandFutures: MutableMap<Long, CompletableFuture<String>> = mutableMapOf()

    private var loginFuture: CompletableFuture<Boolean>? = null
    private var loggedIn: Boolean = false

    var username: String? = null
    private var sessionId: String? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        client = OkHttpClient()
        startForeground(1, createNotification())
        connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        reconnect = false
        connection?.close(1000, "Service destroyed")
        client.dispatcher.executorService.shutdown()

        exitProcess(0) // Our app can't be used without the network service, so we can exit here
    }

    fun login() {
        if (loggedIn) {
            throw IllegalStateException("Already logged in")
        }

        send(LoginRequest(
            username = username ?: throw IllegalStateException("Username is not set"),
            password = sessionId ?: throw IllegalStateException("Session ID is not set"),
            session = true
        ))
    }

    fun login(username: String, password: String, register: Boolean = false): CompletableFuture<Boolean> {
        if (loggedIn) {
            throw IllegalStateException("Already logged in")
        }

        loginFuture = CompletableFuture<Boolean>()

        send(LoginRequest(
            username = username,
            password = password,
            register = register
        ))

        return loginFuture!!
    }

    private fun handleLoginResponse(response: String) {
        val loginResponse = gson.fromJson(response, LoginResponse::class.java)
        if (loginResponse.success) {
            loggedIn = true
            username = loginResponse.username
            sessionId = loginResponse.session
            Log.i("WEBSOCKET", "Logged in as $username")
        } else {
            Log.e("WEBSOCKET", "Login failed")
        }
        loginFuture?.complete(loginResponse.success)
        loginFuture = null
    }

    private fun send(message: Any) {
        val json = gson.toJson(message)
        connection?.send(json)
    }

    fun sendVoiceData(data: ByteArray) {
        connection?.send(data.toByteString())
    }

    fun sendCommand(commandType: CommandType, payload: Any): CompletableFuture<String> {
        if (!loggedIn) {
            throw IllegalStateException("Cannot send command before logging in")
        }

        val request = CommandRequest(commandType, payload)

        val future: CompletableFuture<String>
        if (commandType.returnsData) {
            future = CompletableFuture<String>()
            commandFutures.put(request.commandId, future)
        } else {
            future = CompletableFuture.completedFuture("")
        }

        val json = gson.toJson(request)
        connection?.send(json)

        return future
    }

    private fun connect() {
        val request = Request.Builder()
            .url(Constants.WEBSOCKET_URL)
            .build()

        val listener = NetworkListener(
            open = { webSocket, response ->
                Log.i("WEBSOCKET", "Connected to server: ${response.message}")
            },
            voice = { webSocket, bytes ->
                // TODO
            },
            command = { webSocket, text ->
                if (loggedIn) {
                    var split = text.split(" ", limit = 2)
                    if (split.size == 2) {
                        val commandId = split[0].toLongOrNull()
                        val payload = split[1]

                        if (commandId != null) {
                            val callback = commandFutures.remove(commandId)
                            callback?.complete(payload)
                        }
                    }
                } else {
                    handleLoginResponse(text)
                }
            },
            closing = { webSocket, code, reason ->
                Log.i("WEBSOCKET", "Closing connection: $code, $reason")
                loggedIn = false;

                if (reconnect) {
                    reconnect = false // Prevent multiple reconnections if one fails
                    Log.i("WEBSOCKET", "Reconnecting…")
                    connect()
                    login()
                } else {
                    stopSelf()
                }
            }
        )

        connection = client.newWebSocket(request, listener)
    }

    private fun createNotification(): Notification {
        // First create the channel
        val channel = NotificationChannel(
            "inya_netserv_chan",
            "InYourArea Network Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)


        // Then create the notification
        val disconnectIntent = PendingIntent.getBroadcast(this, 0, Intent(this, NotificationActionReceiver::class.java).apply {
            action = "network_disconnect"
        }, PendingIntent.FLAG_IMMUTABLE)

        return Notification.Builder(this, "inya_netserv_chan")
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.network_service_notification))
            .setSmallIcon(R.drawable.ic_app_icon)
            .addAction(
                0,
                getString(R.string.network_service_action_disconnect),
                disconnectIntent
            )
            .setOngoing(true)
            .build()
    }
}