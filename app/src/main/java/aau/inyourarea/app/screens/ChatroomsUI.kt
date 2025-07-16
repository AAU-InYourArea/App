
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.graphics.Color

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

import androidx.navigation.NavController

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController



@Composable
fun ChatroomsScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var currentChatroom by remember { mutableStateOf<Chatroom?>(null) }
    var chatroom:Chatroom=Chatroom(name = "asdas", users = listOf("User1", "User2"))
    val chatrooms:List<Chatroom> by remember { mutableStateOf(listOf(chatroom)) }

    Box(
        modifier = Modifier.fillMaxSize().background(color = Color.Black)

    ) {

        Box(contentAlignment = Alignment.TopEnd, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.End) {
                Button(
                    onClick = { showDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Add Chatroom",
                        fontStyle = FontStyle.Italic,
                        color = Color.DarkGray
                    )
                }

                currentChatroom?.let {
                    Button(
                        onClick = { navController.navigate("DisplayChatroom") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            "Show Chatroom",
                            fontStyle = FontStyle.Italic,
                            color = Color.DarkGray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
                Button(
                    onClick = { navController.navigate("main") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Back to Main Page",
                        fontStyle = FontStyle.Italic,
                        color = Color.DarkGray
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            //Liste der Chatrooms
            chatrooms.forEach{room->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .padding(8.dp)
                            .background(Color.DarkGray)
                            .clickable {
                                currentChatroom = room
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = room.name,
                            color = Color.White,
                            modifier = Modifier.padding(8.dp),
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))


        if (showDialog) {
            AddChatroom(
                onDismiss = { showDialog = false },
                onChatroomCreated = {
                    currentChatroom = it
                    showDialog = false
                }
            )
        }
    }


    currentChatroom?.let {

        ChatroomHolder.chatroom = it
    }
}



@Composable
fun AddChatroom(
    onDismiss: () -> Unit,
    onChatroomCreated: (Chatroom) -> Unit
) {
    var chatroom by remember { mutableStateOf(Chatroom(name = "", users = listOf())) }
    var password by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { onDismiss() }) {
        Box(
            modifier = Modifier
                .background(Color.Black)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column {
                OutlinedTextField(
                    value = chatroom.name,
                    onValueChange = { chatroom = chatroom.copy(name = it) },
                    label = { Text("Chatroom Name", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
                )

                Spacer(modifier = Modifier.height(16.dp))



                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Passwort", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)

                )



                Button(
                    onClick = {
                        if (chatroom.name.isNotBlank()) {
                            onChatroomCreated(chatroom)

                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.White)
                ) {
                    Text(
                        "Chatroom erstellen",
                        fontStyle = FontStyle.Italic,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}
object ChatroomHolder {
    var chatroom: Chatroom? = null
}
@Composable
fun DisplayChatroomDetail(navController: NavController) {
    var password by remember{ mutableStateOf("") }
    val chatroom = ChatroomHolder.chatroom

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Row(modifier= Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceEvenly){
            Column {
                if (chatroom != null) {
                    Text(text = "Chatroom: ${chatroom.name}", color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Users:", color = Color.White)
                    chatroom.users.forEach { user ->
                        Text(text = "- $user", color = Color.LightGray, fontStyle = FontStyle.Italic)
                    }
                } else {
                    Text("Kein Chatroom ausgewählt", color = Color.Red)
                }
            }
            Spacer(modifier= Modifier.width(16.dp))
            Column(modifier= Modifier.fillMaxHeight()){
                OutlinedTextField(value=password, onValueChange = { password = it },
                    label = { Text("Passwort", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
                )
                Spacer(modifier= Modifier.height(16.dp))
                Button(onClick={/*if(password=)*/}, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.DarkGray)) {
                    Text("Join", color = Color.DarkGray, fontStyle = FontStyle.Italic)
                }
            }
        }
    }
}



data class Chatroom(
    var name:String,
    val users: List<String>
)

