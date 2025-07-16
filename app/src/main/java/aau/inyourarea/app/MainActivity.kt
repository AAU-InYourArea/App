package aau.inyourarea.app

import ChatroomsScreen
import DisplayChatroomDetail
import aau.inyourarea.app.network.CommandType
import aau.inyourarea.app.network.NetworkService
import aau.inyourarea.app.network.NetworkServiceHolder
import aau.inyourarea.app.network.getNetworkService
import aau.inyourarea.app.network.messages.LocationSend
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import aau.inyourarea.app.screens.LoginScreen
import aau.inyourarea.app.ui.theme.InYourAreaTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.runtime.*
import kotlinx.coroutines.*
import android.util.Log
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Handler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController


class MainActivity : ComponentActivity() {

    val networkServiceHolder = getNetworkService(this::onVoiceData)

    lateinit var locationSend: LocationSend
    lateinit var audioTrack: AudioTrack
    lateinit var handler: Handler

    var isRecording: Boolean = false
    val speakingTimes: MutableMap<String, Long> = mutableMapOf()
    val currentSpeaking: MutableState<List<String>> = mutableStateOf(listOf<String>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        locationSend = LocationSend(this, networkServiceHolder)

        handler = Handler(mainLooper)
        handler.postDelayed(this::updateSpeakingUsers, 500)

        val intent = Intent(this, NetworkService::class.java)
        ContextCompat.startForegroundService(this, intent)

        setContent {
            AppNav(networkServiceHolder, currentSpeaking) { isRecording ->
                this.isRecording = isRecording
            }
        }

        checkAndStartLocationUpdates()

        val audioBufferSize = AudioTrack.getMinBufferSize(
            Constants.AUDIO_SAMPLE_RATE,
            Constants.AUDIO_CHANNEL_OUT_CONFIG,
            Constants.AUDIO_ENCODING
        )
        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            Constants.AUDIO_SAMPLE_RATE,
            Constants.AUDIO_CHANNEL_OUT_CONFIG,
            Constants.AUDIO_ENCODING,
            audioBufferSize,
            AudioTrack.MODE_STREAM
        )
        audioTrack.play()
    }

    fun updateSpeakingUsers() {
        val time = System.nanoTime()
        val iter = speakingTimes.iterator()
        while (iter.hasNext()) {
            val entry = iter.next()
            if (time - entry.value > Constants.SPEAKING_TIMEOUT) {
                iter.remove()
            }
        }

        currentSpeaking.value = speakingTimes.keys
            .sorted()
            .take(Constants.MAX_SHOWN_SPEAKING_USERS)

        handler.postDelayed(this::updateSpeakingUsers, 500)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        locationSend.stopLocationUpdates()
        audioTrack.stop()
        audioTrack.release()
    }

    override fun onStart() {
        super.onStart()
        Intent(this, NetworkService::class.java).also {
            startService(it)
            bindService(it, networkServiceHolder.connection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(networkServiceHolder.connection)
    }

    fun onVoiceData(username: String, data: ByteArray) {
        speakingTimes.put(username, System.nanoTime())
        if (!isRecording) {
            audioTrack.write(data, 0, data.size)
        }
    }

    private fun checkAndStartLocationUpdates() {
        val hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (hasFine == PackageManager.PERMISSION_GRANTED || hasCoarse == PackageManager.PERMISSION_GRANTED) {
            locationSend.startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                2000
            )
        }
    }
}

@Composable
fun AppNav(networkService: NetworkServiceHolder, currentSpeaking: MutableState<List<String>>, updateRecordingStatus: (Boolean) -> Unit) {
    val navController = rememberNavController()


    InYourAreaTheme {
        NavHost(navController, startDestination = "splash") {
            composable("splash") {
                SplashScreen {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }

            composable("login") {
                LoginScreen(navController, networkService)
            }

            composable("main") {
                MainPage(navController, networkService, currentSpeaking, updateRecordingStatus)
            }

            composable("chatrooms"){
                ChatroomsScreen(navController, networkService)
            }

            composable("DisplayChatroom") {
                DisplayChatroomDetail(navController, networkService)
            }
        }
    }
}

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    LaunchedEffect(true) {
        delay(2000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "InYourArea",
            color = Color.White,
            fontWeight = FontWeight.Light,
            fontSize = 32.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainPage(navController: NavController, networkService: NetworkServiceHolder, currentSpeaking: MutableState<List<String>>, updateRecordingStatus: (Boolean) -> Unit) {
    val isConnected by remember {
        derivedStateOf {
            networkService.service != null
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("InYourArea") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    ConnectionStatus(isConnected = isConnected)
                    ChatroomHolder.chatroom.value?.let {
                        Text(
                            text = "Chatroom: ${it.name}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            navController.navigate("chatrooms")
                        }
                    ) {
                        Text("Chatrooms")
                    }
                    if (ChatroomHolder.chatroom.value != null) {
                        Button(
                            onClick = {
                                networkService.service.sendCommand(CommandType.LEAVE_ROOM, "")
                                ChatroomHolder.chatroom.value = null
                            }
                        ) {
                            Text("Leave Chatroom")
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    maxLines = 2
                ) {
                    for (user in currentSpeaking.value) {
                        Text(
                            text = user,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .background(
                                    MaterialTheme.colorScheme.secondary,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(8.dp)
                        )
                    }
                }
            }

            AudioRecorderButton(networkService, updateRecordingStatus)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AudioRecorderButton(networkService: NetworkServiceHolder, updateRecordingStatus: (Boolean) -> Unit) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val permission = Manifest.permission.RECORD_AUDIO
    val lifecycleOwner = LocalLifecycleOwner.current
    var permissionGranted by remember { mutableStateOf(false) }

    if (networkService.service == null) {
        Text("Netzwerkdienst nicht verfügbar")
        return
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        permissionGranted = granted
        if (!granted) {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), 100)
        }
    }

    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionGranted = ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (!permissionGranted) {
        Text("Mikrofonberechtigung erlauben, um fortzufahren.")
        return
    }

    val bufferSize = AudioRecord.getMinBufferSize(
        Constants.AUDIO_SAMPLE_RATE,
        Constants.AUDIO_CHANNEL_IN_CONFIG,
        Constants.AUDIO_ENCODING
    )
    val audioBuffer = ByteArray(bufferSize)

    val audioRecord = remember {
        AudioRecord(
            MediaRecorder.AudioSource.MIC,
            Constants.AUDIO_SAMPLE_RATE,
            Constants.AUDIO_CHANNEL_IN_CONFIG,
            Constants.AUDIO_ENCODING,
            bufferSize
        )
    }

    val isRecording = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isRecording.value) {
        if (isRecording.value) {
            audioRecord.startRecording()

            scope.launch(Dispatchers.IO) {
                while (isRecording.value) {
                    val read = audioRecord.read(audioBuffer, 0, bufferSize)
                    if (read > 0) {
                        Log.d("Audio", "Gelesen: $read Bytes")

                        networkService.service.sendVoiceData(audioBuffer.copyOf(read))

                    }
                }
            }
        } else {
            if (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord.stop()
                Log.d("Audio", "Aufnahme gestoppt")
            }
        }
    }


    Button(
        onClick = {},
        modifier = Modifier
            .size(width = 200.dp, height = 200.dp)
            .pointerInteropFilter { event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        isRecording.value = true
                    }

                    android.view.MotionEvent.ACTION_UP,
                    android.view.MotionEvent.ACTION_CANCEL -> {
                        isRecording.value = false
                    }
                }
                updateRecordingStatus(isRecording.value)
                true
            },
        shape = RoundedCornerShape(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isRecording.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Mic,
                tint = if (isRecording.value) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                contentDescription = "Mic",
                modifier = Modifier.size(96.dp)
            )
            Text(
                text = if (isRecording.value) "Aufnahme läuft…" else "Push-To-Talk",
                fontSize = 18.sp,
                color = if (isRecording.value) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun ConnectionStatus(isConnected: Boolean) {
    val color = if (!isConnected) {
        Color.Red
    } else if (isSystemInDarkTheme()) {
        Color.Green
    } else {
        Color(0xFF00BF00)
    }
    val text = if (isConnected) "Verbindung: Online" else "Verbindung: Offline"

    Box(
        contentAlignment = Alignment.TopEnd
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

