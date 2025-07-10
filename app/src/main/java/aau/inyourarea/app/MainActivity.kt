package aau.inyourarea.app

import aau.inyourarea.app.network.NetworkService
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
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.runtime.*
import kotlinx.coroutines.*
import android.util.Log
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.ui.input.pointer.pointerInteropFilter




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val intent = Intent(this, NetworkService::class.java)
        ContextCompat.startForegroundService(this, intent)

            setContent {
                AppNav()
            }
        }
    }


    @Composable
    fun AppNav() {
        val navController = rememberNavController()


        NavHost(navController, startDestination = "splash") {

            composable("splash") {
                SplashScreen {
                    navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }

            /*composable("login") {
            LoginScreen(navController)
        }*/

            composable("main") {
                MainPage()
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
    fun MainPage() {
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
                    AudioRecorderButton()
                }
            }
        }
    }

@Composable
fun AudioRecorderButton() {
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

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), 100)
        }
    }

    if (!hasPermission) {
        Text("Mikrofonberechtigung erlauben, um fortzufahren.")
        return
    }

    val sampleRate = 44100                              //Standard Audio einstellungen
    val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    val audioBuffer = ByteArray(bufferSize)

    val audioRecord = remember {
        AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
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

                        NetworkService().sendVoiceData(audioBuffer.copyOf(read))        //Bereden ob laufende Instanz besesr wäre
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

