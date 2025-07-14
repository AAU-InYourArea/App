package aau.inyourarea.app.screens

import aau.inyourarea.app.MainActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

import androidx.navigation.NavController

data class Colors(
    val colors: List<Color> = listOf(
        Color(0xFFBB86FC),
        Color(0xFF6200EE),
        Color(0xFF3700B3),
        Color(0xFF03DAC5),
        Color(0xFFFF5722),
        Color(0xFFFF9800),
        Color(0xFFFFEB3B),
        Color(0xFF4CAF50)
    )
)

class ChatroomsUI{}
@Composable
fun ChatroomsScreen(
    navController: NavController,
    initialChatrooms: List<Chatroom>
) {
    var chatrooms by remember { mutableStateOf(initialChatrooms) }
    var showDialog by remember { mutableStateOf(false) }
    var roomName by remember { mutableStateOf("") }
    var userListInput by remember { mutableStateOf("") }

    val colorList = Colors().colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { showDialog = true }) {
            Text("Neuen Chatroom erstellen")
        }

        Spacer(modifier = Modifier.height(16.dp))

        chatrooms.forEachIndexed { index, chatroom ->
            val color = colorList[index % colorList.size]

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        navController.navigate("chatroomDetail/${chatroom.name}")
                    }
            ) {
                IconCircle(color = color)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = chatroom.name,
                    color = Color.DarkGray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }

    // Dialog für neuen Chatroom
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                Column {
                    Text("Chatroom erstellen", color = Color.Black)

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = roomName,
                        onValueChange = { roomName = it },
                        label = { Text("Chatroom-Name") },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = userListInput,
                        onValueChange = { userListInput = it },
                        label = { Text("User (mit Komma trennen)") },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        Button(onClick = {
                            if (roomName.isNotBlank()) {
                                val users = userListInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                val newRoom = Chatroom(name = roomName, users = users)
                                chatrooms = chatrooms + newRoom
                                roomName = ""
                                userListInput = ""
                                showDialog = false
                            }
                        }) {
                            Text("Erstellen")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(onClick = { showDialog = false }) {
                            Text("Abbrechen")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatroomDetailScreen(roomName: String, users: List<String>) {
    var mainActivity:MainActivity= MainActivity()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(text = "User im $roomName", color = Color.White)

        Spacer(modifier = Modifier.height(12.dp))

        users.forEach {
            Text(
                text = it,
                color = Color.DarkGray,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
        mainActivity
    }

}

@Composable
fun IconCircle(color: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color = color, shape = CircleShape)
    )
}
data class Chatroom(
    val name: String,
    val users: List<String>
)

@Preview
@Composable
fun PreviewChatroomsScreen() {

}