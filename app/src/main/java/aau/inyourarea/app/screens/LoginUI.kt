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
            painter = painterResource(id = aau.inyourarea.app.R.drawable.key),
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
                        Color.Black.copy(alpha = 0.50f), // oben dunkel
                        Color.White.copy(alpha = 0.500f), // Mitte dunkler als vorher
                        Color.White.copy(alpha = 0.500f),
                        Color.Black.copy(alpha = 0.100f)  // unten dunkel
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
            val textColors = OutlinedTextFieldDefaults.colors(
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
            val buttonColors = ButtonDefaults.buttonColors(
                containerColor = Color.DarkGray,
                contentColor = Color.White,
                disabledContainerColor = Color.Gray.copy(alpha = 0.7f),
                disabledContentColor = Color.LightGray
            );

            OutlinedTextField(
                value = username,
                shape = RoundedCornerShape(16.dp),
                onValueChange = { username = it },
                colors = textColors,
                label = { Text("Username") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                shape = RoundedCornerShape(16.dp),
                onValueChange = { password = it },
                colors = textColors,
                label = { Text("Password") }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                Button(
                    onClick = {

                        networkService

                            .service.login(username, password, false)
                            .thenAccept { success ->
                                if (success) {
                                    loginStatus = "Login erfolgreich"
                                    runBlocking(Dispatchers.Main) {
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



                loginStatus?.let {
                    Text(text = it)
                }

                Button(
                    onClick = {

                        networkService

                            .service.login(username, password, true)
                            .thenAccept { success ->
                                if (success) {
                                    loginStatus = "Registrierung erfolgreich"
                                    showLoginScreen = true
                                    runBlocking(Dispatchers.Main) {
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





