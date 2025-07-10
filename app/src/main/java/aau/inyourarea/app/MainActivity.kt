package aau.inyourarea.app

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
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp







class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
                AppNav()
        }
        }
    }


@Composable
fun AppNav(){
    val navController = rememberNavController()


    NavHost(navController, startDestination = "splash") {

        composable("splash"){
            SplashScreen{
                navController.navigate("login"){
                    popUpTo("splash"){ inclusive = true }
                }
            }
        }

        composable("login") {
            LoginScreen(navController)
        }

        composable("main") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "MainPage Placeholder",
                    color = Color.White,
                    fontSize = 24.sp
                )
                Button(
                    onClick = { navController.navigate("login") },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = "Go to WalkieTalkie")
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onSplashFinished: () -> Unit){
    LaunchedEffect(true){
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

/*@Composable
fun MainPage() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("InYourArea – WalkieTalkie") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "Verbindung: Online",        //Dann mit Socket machen
                fontSize = 16.sp,
                color = Color.Green
            )

            Button(
                onClick = { /* Pushtotalk */ },
                shape = CircleShape,
                modifier = Modifier.size(200.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {

                Text(
                    text = "Zum Sprechen gedrückt halten",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}
*/