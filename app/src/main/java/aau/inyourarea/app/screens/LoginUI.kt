package aau.inyourarea.app.screens

import aau.inyourarea.app.network.NetworkServiceHolder
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking


@Composable
fun LoginScreen(navController: NavController,networkService: NetworkServiceHolder) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginStatus by remember { mutableStateOf<String?>(null) }
    var showLoginScreen by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = aau.inyourarea.app.R.drawable.key), // Hintergrundbild
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.50f), //Gradient für den Hintergrund
                        Color.White.copy(alpha = 0.500f),
                        Color.White.copy(alpha = 0.500f),
                        Color.Black.copy(alpha = 0.100f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val textColors = OutlinedTextFieldDefaults.colors(      // Textfeld Farben
                unfocusedBorderColor = Color.Black,
                unfocusedTextColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                unfocusedPlaceholderColor = Color.Black,
                focusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.DarkGray,
                focusedLabelColor = Color.DarkGray,
                focusedPlaceholderColor = Color.DarkGray,
                cursorColor = Color.DarkGray
            )
            val buttonColors = ButtonDefaults.buttonColors(   // Button Farben
                containerColor = Color.DarkGray,
                contentColor = Color.White,
                disabledContainerColor = Color.Gray.copy(alpha = 0.7f),
                disabledContentColor = Color.LightGray
            );

            OutlinedTextField(          // Eingabefeld für den Benutzernamen
                value = username,
                shape = RoundedCornerShape(16.dp),
                onValueChange = { username = it },
                colors = textColors,
                label = { Text("Username") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(     // Eingabefeld für das Passwort
                value = password,
                shape = RoundedCornerShape(16.dp),
                onValueChange = { password = it },
                colors = textColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrect = false),
                visualTransformation = PasswordVisualTransformation(),
                label = { Text("Password") }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) { // Buttons für Login und Registrierung
                Button(
                    onClick = {

                        networkService

                            .service.login(username, password, false)   // Login-Funktion
                            .thenAccept { success ->   // Callback für den Login-Versuch
                                if (success) {
                                    loginStatus = "Login erfolgreich"
                                    runBlocking(Dispatchers.Main) {        // Main-Thread für Navigation
                                        navController.navigate("main") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                } else {
                                    loginStatus = "Login fehlgeschlagen"
                                }
                            }
                    },
                    colors = buttonColors,
                    enabled = username.isNotBlank() && password.isNotBlank()
                ) {
                    Text("Login")
                }



                loginStatus?.let {      // Anzeige des Login-Status
                    Text(text = it)
                }

                Button(
                    onClick = {

                        networkService

                            .service.login(username, password, true) // Registrierung-Funktion
                            .thenAccept { success ->
                                if (success) {
                                    loginStatus = "Registrierung erfolgreich"
                                    showLoginScreen = true
                                    runBlocking(Dispatchers.Main) {     // Main-Thread für Navigation
                                        navController.navigate("main") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                } else {
                                    loginStatus = "Account existiert bereits"
                                }
                            }
                    },
                    colors = buttonColors,
                    enabled = username.isNotBlank() && password.isNotBlank()
                ) {
                    Text("Registrieren")
                }
            }
        }
    }
}





