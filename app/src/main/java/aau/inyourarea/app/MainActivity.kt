package aau.inyourarea.app

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import aau.inyourarea.app.screens.LoginScreen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
/* AUDIO TEIL*/
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.runtime.*
import kotlinx.coroutines.*
import android.util.Log
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.ui.input.pointer.pointerInteropFilter




class MainActivity : ComponentActivity() {

    val networkServiceHolder = getNetworkService(this::onVoiceData)

    lateinit var locationSend: LocationSend

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        locationSend = LocationSend(this, networkServiceHolder)

        val intent = Intent(this, NetworkService::class.java)
        ContextCompat.startForegroundService(this, intent)

        setContent {
            AppNav(networkServiceHolder)
        }

        locationSend.startLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationSend.stopLocationUpdates()
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

    fun onVoiceData(data: ByteArray) {
    }
}

@Composable
fun AppNav(networkService: NetworkServiceHolder) {
    val navController = rememberNavController()


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

            MainPage(networkService)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(networkService: NetworkServiceHolder) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("InYourArea") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.DarkGray,
                    titleContentColor = Color.White
                )
            )
        }
    )

    { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            Text(
                text = "Verbindung: Online",        //Dann mit Socket machen
                fontSize = 16.sp,
                color = Color.Green,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AudioRecorderButton(networkService)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AudioRecorderButton(networkService: NetworkServiceHolder) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val permission = Manifest.permission.RECORD_AUDIO
    val hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    if (networkService.service == null) {
        Text("Netzwerkdienst nicht verfügbar")
        return
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), 100)
        }
    }

    if (!hasPermission) {
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
            .size(150.dp)
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
                true
            },
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isRecording.value) Color.Blue else Color.DarkGray,
            contentColor = Color.White
        )
    ) {
        Text(
            text = if (isRecording.value) "Aufnahme läuft..." else "Push-To-Talk",
            color = Color.Gray
        )
    }
}