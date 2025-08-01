package aau.inyourarea.app.network

import aau.inyourarea.app.Constants
import aau.inyourarea.app.R
import aau.inyourarea.app.network.messages.ChatroomData
import aau.inyourarea.app.network.messages.CommandRequest
import aau.inyourarea.app.network.messages.LoginRequest
import aau.inyourarea.app.network.messages.LoginResponse
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
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
        var commandCounter: Long = 0L // each command gets a new id for potential data responses
    }

    private val gson: Gson = Gson()
    private val binder = LocalBinder()

    private lateinit var client: OkHttpClient
    private var connection: WebSocket? = null
    private var reconnect = true // whether to reconnect on websocket close

    // this map holds all the futures for command responses
    private val commandFutures: MutableMap<Long, CompletableFuture<String>> = mutableMapOf()

    // login-related variables
    private var loginFuture: CompletableFuture<Boolean>? = null
    var loggedIn: Boolean = false

    var username: String? = null
    private var sessionId: String? = null

    // this function is called for binary voice data
    var voiceListener: ((String, ByteArray) -> Unit)? = null

    inner class LocalBinder : Binder() {
        fun getService(): NetworkService {
            return this@NetworkService
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        client = OkHttpClient()
        startForeground(1, createNotification()) // start as foreground service
        connect() // open the connection
    }

    override fun onDestroy() {
        super.onDestroy()
        reconnect = false // don't try to reconnect
        connection?.close(1000, "Service destroyed") // close the connection
        client.dispatcher.executorService.shutdown() // shut down the http client

        exitProcess(0) // Our app can't be used without the network service, so we can exit here
    }

    fun login() {
        if (loggedIn) {
            throw IllegalStateException("Already logged in")
        }

        // send login request using previous data (from last login)
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

        loginFuture = CompletableFuture<Boolean>() // this future will receive whether the login was successful

        // send login/register request
        send(LoginRequest(
            username = username,
            password = password,
            register = register
        ))

        return loginFuture!!
    }

    private fun handleLoginResponse(response: String) {
        // parse response
        val loginResponse = gson.fromJson(response, LoginResponse::class.java)
        if (loginResponse.success) {
            // if successful save username and session token for future logins
            loggedIn = true
            username = loginResponse.username
            sessionId = loginResponse.session
            Log.i("WEBSOCKET", "Logged in as $username")
        } else {
            Log.e("WEBSOCKET", "Login failed")
        }

        // call the login future if set
        loginFuture?.complete(loginResponse.success)
        loginFuture = null
    }

    /**
     * Converts object to json and sends via the websocket connection
     */
    private fun send(message: Any) {
        val json = gson.toJson(message)
        connection?.send(json)
    }

    /**
     * send raw bytes via the websocket
     * byte messages are purely used for voice data
     */
    fun sendVoiceData(data: ByteArray) {
        connection?.send(data.toByteString())
    }

    /**
     * sends a chatrooms request
     * @return future with chatroom data
     */
    fun getChatrooms(): CompletableFuture<Array<ChatroomData>> {
        Log.i("WEBSOCKET", "Requesting chatrooms")
        return sendCommand(CommandType.GET_ROOMS, "").thenApply { json ->
            gson.fromJson<Array<ChatroomData>>(json, Array<ChatroomData>::class.java)
        }
    }

    /**
     * sends a text command message with the specified type and payload
     * @return future with command response (if type has a response), otherwise future contains empty string
     */
    fun sendCommand(commandType: CommandType, payload: Any): CompletableFuture<String> {
        if (!loggedIn) {
            throw IllegalStateException("Cannot send command before logging in")
        }

        // construct top-level command object
        val request = CommandRequest(commandType, payload)

        val future: CompletableFuture<String>
        if (commandType.returnsData) {
            // if this command type returns data we save the future
            future = CompletableFuture<String>()
            commandFutures.put(request.commandId, future)
        } else {
            // if this command type has no data response, we return an empty completed future
            future = CompletableFuture.completedFuture("")
        }

        // convert to json and send
        val json = gson.toJson(request)
        connection?.send(json)

        return future
    }

    /**
     * sets up and opens the websocket connection
     */
    private fun connect() {
        val request = Request.Builder()
            .url(Constants.WEBSOCKET_URL)
            .build()

        val listener = NetworkListener(
            open = { webSocket, response ->
                Log.i("WEBSOCKET", "Connected to server: ${response.message}")
            },
            voice = { webSocket, bytes ->
                // split username and voice bytes and pass to voice listener
                val bytes = bytes.toByteArray()
                val usernameLength = bytes[0]
                val usernameBytes = bytes.sliceArray(1 until 1 + usernameLength)
                val voiceData = bytes.sliceArray(1 + usernameLength until bytes.size)
                voiceListener?.invoke(String(usernameBytes), voiceData)
            },
            command = { webSocket, text ->
                if (loggedIn) {
                    Log.i("WEBSOCKET", "Received command: $text")
                    var split = text.split(" ", limit = 2)
                    // command responses are of format <commandId> <payload>
                    if (split.size == 2) {
                        val commandId = split[0].toLongOrNull()
                        val payload = split[1]

                        if (commandId != null) {
                            // pass payload to command future if we have one with this command id
                            val callback = commandFutures.remove(commandId)
                            callback?.complete(payload)
                        }
                    }
                } else {
                    // if not logged in, assume this to be the login response
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
                    stopSelf() // if we don't reconnect we stop the service
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
            .addAction( // add a disconnect button to the notification
                0,
                getString(R.string.network_service_action_disconnect),
                disconnectIntent
            )
            .setOngoing(true)
            .build()
    }
}

class NetworkServiceHolder {
    lateinit var connection: ServiceConnection
    lateinit var service: NetworkService
}

/**
 * helper function to bind to and retrieve the network service
 * the returned object might not immediately have the service
 * therefore the holder should be stored and the service accessed only when required
 *
 * @return NetworkServiceHolder with the connection and the service
 */
fun getNetworkService(voiceListener: ((String, ByteArray) -> Unit)? = null): NetworkServiceHolder {
    val holder = NetworkServiceHolder()
    holder.connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val binder = binder as NetworkService.LocalBinder
            val service = binder.getService()
            service.voiceListener = voiceListener
            holder.service = service
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            holder.service?.voiceListener = null
        }
    }
    return holder
}