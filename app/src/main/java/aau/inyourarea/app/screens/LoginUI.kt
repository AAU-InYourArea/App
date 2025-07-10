package aau.inyourarea.app.screens
import aau.inyourarea.app.network.NetworkService
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController




@Composable
fun LoginScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginStatus by remember { mutableStateOf<String?>(null) }
    var showLoginScreen by remember { mutableStateOf(false) }
  Box(modifier=Modifier.fillMaxSize()){
      Image(painter=painterResource(id = aau.inyourarea.app.R.drawable.key),
          contentDescription = "Background Image",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop)
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
    )
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    ),
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
                    ),
                    enabled = username.isNotBlank() && password.isNotBlank()
                ) {
                    Text("Registrieren")
                }


                }
            }
        }


            

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController())
}





