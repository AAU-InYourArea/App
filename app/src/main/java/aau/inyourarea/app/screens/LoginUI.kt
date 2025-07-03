package aau.inyourarea.app.screens
import aau.inyourarea.app.network.NetworkService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController


class LoginUI : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            var navController = rememberNavController()
            LoginScreen(
                navController=NavController(this)



            )}
        }
    }


@Composable
fun LoginScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginStatus by remember { mutableStateOf<String?>(null) }
    var showLoginScreen by remember { mutableStateOf(false) }
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier=Modifier.fillMaxWidth(),Arrangement.SpaceEvenly) {
                Button(
                    onClick = {
                        // .getInstance().service
                        NetworkService()

                            .login(username, password, false)
                            .thenAccept { success ->
                                if (success) {
                                    loginStatus = "Login erfolgreich"
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                }} else {
                                    loginStatus = "Login fehlgeschlagen"
                                }
                            }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    ),modifier = Modifier.fillMaxWidth(),
                    enabled = username.isNotBlank() && password.isNotBlank()
                ) {
                    Text("Login")
                }



                loginStatus?.let {

                    Text(text = it)
                }
                Button(
                    onClick = {
                        // .getInstance().service
                        NetworkService()

                            .login(username, password, true)
                            .thenAccept { success ->
                                if (success) {
                                    loginStatus = "Registrierung erfolgreich"
                                    showLoginScreen= true
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }}

                                } else {
                                    loginStatus = "Account existiert bereits"
                                }
                            }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    ),modifier = Modifier.fillMaxWidth(),
                    enabled = username.isNotBlank() && password.isNotBlank()
                ) {
                    Text("Login")
                }
            }
        }


            }
        }






